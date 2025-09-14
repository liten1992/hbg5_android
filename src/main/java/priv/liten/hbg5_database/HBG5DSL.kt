package priv.liten.hbg5_database

import android.database.Cursor
import android.util.Log
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_config.HBG5DbConfig
import priv.liten.hbg5_extension.toLinkString

import priv.liten.hbg5_database.HBG5DSL.Field
import priv.liten.hbg5_extension.getBooleanOrNull
import priv.liten.hbg5_extension.getJsonArrayOrNull
import priv.liten.hbg5_extension.getJsonOrNull
import priv.liten.hbg5_extension.throwIfFalse
import priv.liten.hbg5_extension.toJson

/**物件化語法生成*/
object HBG5DSL {
    /**新增語法產生器*/
    class Inserter {
        constructor(onlyInsert: Boolean) {
            this.action = if(onlyInsert) "INSERT INTO" else "INSERT OR REPLACE INTO"
        }

        private val action: String
        private var tableName: String = ""
        private var fieldList: MutableList<Field> = mutableListOf()

        fun table(table: Table): Inserter {
            tableName = table.name
            return this
        }

        fun field(fields: List<Field>): Inserter {
            fieldList.clear()
            fieldList.addAll(fields)
            return this
        }

        fun build(): String{
            val names = fieldList
                .map { field -> field.name }
                .toLinkString(linkText = ", ")

            val values = fieldList
                .map { field ->
                    val value = field.value ?: field.default
                    return@map if(value != null) sqlValue(value)
                    else "NULL"
                }
                .toLinkString(linkText = ", ")

            return "$action $tableName ($names) VALUES ($values)"
        }

        fun execute(database: HBG5Database): Boolean = database.execSql(this.build())
        @Throws
        fun executeOrThrow(database: HBG5Database, message: String = "Insert Row Failed") {
            if(!execute(database)) { throw Exception(message) }
        }
    }
    /**更新語法產生器*/
    class Updater {
        private var tableName: String = ""
        private var fieldList: MutableList<String> = mutableListOf()
        private var condition: String = ""

        fun table(table: Table): Updater {
            tableName = table.name
            return this
        }
        /**ADD*/
        fun field(fields: List<Field>): Updater {
            fieldList.addAll(fields.map { field ->
                val value = field.value ?: field.default

                return@map if(field.function != null) "${field.name} = ${field.function!!.buildField(field = field.name)}"
                    else "${field.name} = ${if(value != null) sqlValue(value) else "NULL"}"
            })
            return this
        }

        fun where(
            reset: Boolean = false,
            mode: Condition.MergeMode = Condition.MergeMode.AND,
            conditions: List<Condition>
        ): Updater {
            // 重置條件
            if(reset) { condition = "" }

            val nonEmptyConditions = conditions.map { it.build(tableVisible = false) }.filter { it.isNotEmpty() }

            if(nonEmptyConditions.isEmpty()) { return this }

            for(condition in nonEmptyConditions) {
                val formatSql =
                    if(condition.startsWith('(') && condition.endsWith(')')) condition
                    else "($condition)"

                this.condition = if(this.condition.isEmpty()) formatSql else "${this.condition} ${mode.sql} $formatSql"
            }

            return this
        }

        fun build(): String = listOf(
            "UPDATE $tableName SET ${fieldList.toLinkString(", ")}",
            if(condition.isEmpty()) "" else "WHERE $condition")
            .filter { it.isNotEmpty() }
            .toLinkString(linkText = " ")

        fun execute(database: HBG5Database): Boolean = database.execSql(this.build())
        @Throws
        fun executeOrThrow(database: HBG5Database, message: String = "Update Row Failed") = execute(database).throwIfFalse { Exception(message) }
    }
    /**查詢語法產生器*/
    class Selector {
        private var tableName: String = ""
        private var joinTable: String = ""
        private var unionTable: String = ""
        private var fieldList: MutableList<String> = mutableListOf()
        private var condition: String = ""
        private var orderBy: String = ""
        private var groupBy: String = ""
        private var limit: String = ""

        enum class JoinType {
            INNER, LEFT, OUTER;

            val sql: String
                get() = when(this) {
                    INNER -> "INNER JOIN"
                    LEFT -> "LEFT JOIN"
                    OUTER -> "OUTER JOIN"
                }
        }

        enum class UnionType {
            /**結合非重複資料*/
            UNIQUE,
            /**結合全部資料*/
            ALL
        }

        enum class OrderType {
            ASC, DESC;

            val sql: String
                get() = when(this) {
                    ASC -> "ASC"
                    DESC -> "DESC"
                }
        }

        fun table(table: Table): Selector {
            tableName = if(table.nick.isNotEmpty()) "${table.name} AS ${table.nick}" else table.name
            return this
        }

        /** @param fields: <self,join>
         * 2025-01-25 調整可疊加多張資料表
         * */
        fun join(tables: List<JoinTable>): Selector {
            joinTable = tables
                .map { join ->
                    val fieldEquals = join.fields
                        .map { (selfField, joinField) ->
                            "${selfField.table}.${selfField.name} = ${joinField.table}.${joinField.name}"
                            //"${selfField.nickOrName()} = ${joinField.nickOrName()}"
                        }
                        .toLinkString(linkText = " AND ")

                    return@map "${join.type.sql} ${join.join.name} AS ${join.join.nick} ON $fieldEquals"
                }
                .toLinkString(linkText = " ")
            return this
        }

        fun union(
            type: UnionType,
            unionSelect: String): Selector {

            this.unionTable = when(type) {
                UnionType.UNIQUE -> "UNION $unionSelect"
                UnionType.ALL -> "UNION ALL $unionSelect"
            }

            return this
        }
        /**ADD*/
        fun field(fields: List<Field>): Selector {
            for(field in fields) {
                field(field = field)
            }
            return this
        }
        /**ADD*/
        fun field(field: Field): Selector {
            val fieldTableName = if(field.table.isNotEmpty()) "${field.table}.${field.name}" else field.name

            val fieldTableDefaultName =
                if(field.nick.isNotEmpty() && field.default != null) "COALESCE($fieldTableName, ${sqlValue(field.default!!)})"
                else fieldTableName

            val fieldTableDefaultFuncName =
                if(field.nick.isNotEmpty()) field.function?.buildField(fieldTableDefaultName) ?: fieldTableDefaultName
                else fieldTableDefaultName

            val fieldTableDefaultAsName =
                if(field.nick.isNotEmpty()) "$fieldTableDefaultFuncName AS ${field.nick}"
                else fieldTableDefaultName

            fieldList.add(fieldTableDefaultAsName)
            return this
        }
        /***/
        fun where(
            reset: Boolean = false,
            mode: Condition.MergeMode = Condition.MergeMode.AND,
            conditions: List<Condition>
        ): Selector {
            // 重置條件
            if(reset) { condition = "" }

            val nonEmptyConditions = conditions.map { it.build(tableVisible = true) }.filter { it.isNotEmpty() }

            if(nonEmptyConditions.isEmpty()) { return this }

            for(condition in nonEmptyConditions) {
                val formatSql =
                    if(condition.startsWith('(') && condition.endsWith(')')) condition
                    else "($condition)"

                this.condition = if(this.condition.isEmpty()) formatSql else "${this.condition} ${mode.sql} $formatSql"
            }

            return this
        }
        /**
         * @param fields 因為排序有優先順序 所以需要使用 LinkedMap 順序讀取
         * */
        fun orderBy(fields: LinkedHashMap<Field, OrderType>): Selector {
            orderBy = fields.map { (field, type) -> "${field.nickOrName()} ${type.sql}" }.toLinkString(linkText = ", ")
            return this
        }

        fun groupBy(fields: List<String>) : Selector {
            groupBy = fields.toList().toLinkString(", ")
            return this
        }

        fun limit(count: Long?): Selector {
            count
                ?.let {
                    limit = "$it"
                }
                ?:run {
                    limit = ""
                }
            return this
        }

        fun limitFirst(): Selector = limit(count = 1)

        fun build(): String {

            val sqlList = mutableListOf<String>().apply {
                add("SELECT ${fieldList.toLinkString(", ")}")
                if(unionTable.isNotEmpty()) {
                    add("FROM ($tableName $unionTable)")
                }
                else {
                    add("FROM $tableName")
                }
                add(joinTable)
                if(condition.isNotEmpty()) { add("WHERE $condition") }
                if(groupBy.isNotEmpty()) { add("GROUP BY $groupBy") }
                if(orderBy.isNotEmpty()) { add("ORDER BY $orderBy") }
                if(limit.isNotEmpty()) { add("LIMIT $limit") }
            }

            return sqlList.filter { it.isNotEmpty() }.toLinkString(linkText = " ")
        }

        fun execute(database: HBG5Database): Cursor? = database.querySqlCursor(this.build())
        @Throws // todo hbg
        fun executeOrThrow(database: HBG5Database, message: String = "SQL語法錯誤"): Cursor {
            try {
                return database.querySqlCursor(this.build()) ?: throw NullPointerException(message.ifEmpty { "Not found database cursor" })
            }
            catch (error: Throwable) {
                if(BuildConfig.DEBUG) {
                    Log.e(HBG5Database.LOG_TAG, error.toString())
                }
                if(message.isNotEmpty()) { throw Exception(message) }
                throw error
            }
        }
    }
    /**刪除語法產生器*/
    class Deleter {
        private var tableName: String = ""
        private var condition: String = ""

        fun table(table: Table): Deleter {
            tableName = table.name
            return this
        }

        fun where(
            reset: Boolean = false,
            mode: Condition.MergeMode = Condition.MergeMode.AND,
            conditions: List<Condition>
        ): Deleter {
            // 重置條件
            if(reset) { condition = "" }

            val nonEmptyConditions = conditions.map { it.build(tableVisible = false) }.filter { it.isNotEmpty() }

            if(nonEmptyConditions.isEmpty()) { return this }

            for(condition in nonEmptyConditions) {
                val formatSql =
                    if(condition.startsWith('(') && condition.endsWith(')')) condition
                    else "($condition)"

                this.condition = if(this.condition.isEmpty()) formatSql else "${this.condition} ${mode.sql} $formatSql"
            }

            return this
        }

        fun build(): String = listOf(
            "DELETE FROM $tableName",
            if(condition.isEmpty()) "" else "WHERE $condition"
        )
            .filter { it.isNotEmpty() }
            .toLinkString(linkText = " ")

        fun execute(database: HBG5Database): Boolean = database.execSql(this.build())
        @Throws
        fun executeOrThrow(database: HBG5Database, message: String = "Delete Row Failed") = execute(database).throwIfFalse { Exception(message) }
    }
    /**條件語法產生器*/
    class Condition {
        constructor() { }

        /**合併模式*/
        enum class MergeMode {
            AND, OR;

            val sql: String
                get() = when(this) {
                    AND -> "AND"
                    OR -> "OR"
                }
        }
        /**比對模式*/
        enum class ThanMode {
            EQUALS, LIKE, MORE, MORE_EQUALS, LESS, LESS_EQUALS;
        }

        /**啟用條件*/
        private var enable: Boolean = true
        /**停用限制 如果 條件 NULL*/
        private var disableIfNull: Boolean = true
        /**反轉條件*/
        private var not: Boolean = false
        /**條件欄位*/
        private var field: Field? = null
        /**AND條件*/
        private var and: MutableList<Condition> = mutableListOf()
        /**OR條件*/
        private var or: MutableList<Condition> = mutableListOf()

        /**比對模式*/
        private var than: ThanMode? = null
        private fun than(than: ThanMode, field: Field): Condition {
            this.than = than
            this.field = field
            return this
        }

        /**啟用條件*/
        fun enable(value: Boolean): Condition {
            this.enable = value
            return this
        }
        /**停用限制 如果 條件 NULL || ""*/
        fun disableIfNull(value: Boolean): Condition {
            this.disableIfNull = value
            return this
        }

        fun not(): Condition {
            not != not
            return this
        }

        fun and(conditions: List<Condition>): Condition {
            and.addAll(conditions)
            return this
        }
        fun or(conditions: List<Condition>): Condition {
            or.addAll(conditions)
            return this
        }

        fun eq(field: Field): Condition = than(ThanMode.EQUALS, field)
        fun notEq(field: Field): Condition = eq(field).not()
        fun like(field: Field): Condition = than(ThanMode.LIKE, field)
        fun more(field: Field): Condition = than(ThanMode.MORE, field)
        fun moreEq(field: Field): Condition = than(ThanMode.MORE_EQUALS, field)
        fun less(field: Field): Condition = than(ThanMode.LESS, field)
        fun lessEq(field: Field): Condition = than(ThanMode.LESS_EQUALS, field)

        fun build(tableVisible: Boolean): String {
            if(!this.enable) { return "" }

            var commandQuerySelf = ""
            // 反轉無效 = 有效判定 ， 無效判定 = (空值不啟用 && 條件空值)
            val enable = !(disableIfNull && (field?.value == null || field?.value == ""))
            // 欄位、比對存在
            if(field != null && than != null && enable) {
                val field = this.field!!
                // 欄位名稱
                val fieldName = if(tableVisible && field.table.isNotEmpty()) "${field.table}.${field.name}" else field.name
                val fieldFullName = when(val fieldDefault = field.default) {
                    is Boolean, is Number, is CharSequence -> "COALESCE($fieldName, ${sqlValue(fieldDefault)})"
                    else -> fieldName
                }
                // 欄位資料
                val fieldValue = when(val fieldValue = field.value) {
                    // 布林、數值、字串型別
                    is Boolean, is Number, is String -> sqlValue(fieldValue)
                    // 區間
                    is List<*> -> fieldValue.filterNotNull().map { sqlValue(it) }.toLinkString(linkText = ", ")
                    // 表單
                    is Selector -> fieldValue.build()
                    // 欄位
                    is Field -> if(fieldValue.table.isNotEmpty()) "${fieldValue.table}.${fieldValue.name}" else fieldValue.name
                    // 不套用
                    else -> ""
                }
                // 條件指令 ex: "FIELD_NAME = 0"
                commandQuerySelf =
                    when(than) {
                        ThanMode.EQUALS -> when(field.value) {
                            // 布林、數值、字串型別、欄位
                            is Boolean, is Number, is String, is Field ->
                                if(!not) "$fieldFullName = $fieldValue"
                                else "$fieldFullName != $fieldValue"
                            // 區間、表單型別
                            is List<*>, is Selector ->
                                if(fieldValue.isNotEmpty())
                                    if(!not) "$fieldFullName IN ($fieldValue)"
                                    else "$fieldFullName NOT IN ($fieldValue)"
                                else ""
                            // 空值
                            null ->
                                if(!not) "$fieldFullName IS NULL"
                                else "$fieldFullName IS NOT NULL"
                            // 錯誤型別
                            else -> ""
                        }
                        ThanMode.LIKE -> when(field.value) {
                            // 字串型別
                            is String ->
                                if(!not) "$fieldFullName LIKE '%${fieldValue.substring(1, fieldValue.length - 1)}%'"
                                else "$fieldFullName NOT LIKE '%${fieldValue.substring(1, fieldValue.length - 1)}%'"
                            // 錯誤型別
                            else -> ""
                        }
                        ThanMode.MORE -> when(field.value) {
                            // 數值型別
                            is Number ->
                                if(!not) "$fieldFullName > $fieldValue"
                                else "$fieldFullName <= $fieldValue"
                            // 錯誤型別
                            else -> ""
                        }
                        ThanMode.MORE_EQUALS -> when(field.value) {
                            // 數值型別
                            is Number ->
                                if(!not) "$fieldFullName >= $fieldValue"
                                else "$fieldFullName < $fieldValue"
                            // 錯誤型別
                            else -> ""
                        }
                        ThanMode.LESS -> when(field.value) {
                            // 數值型別
                            is Number ->
                                if(!not) "$fieldFullName < $fieldValue"
                                else "$fieldFullName >= $fieldValue"
                            // 錯誤型別
                            else -> ""
                        }
                        ThanMode.LESS_EQUALS -> when(field.value) {
                            // 數值型別
                            is Number ->
                                if(!not) "$fieldFullName <= $fieldValue"
                                else "$fieldFullName > $fieldValue"
                            // 錯誤型別
                            else -> ""
                        }
                        else -> ""
                    }
            }
            // 交集條件
            val commandQueriesAnd = and.map { condition -> condition.build(tableVisible) }.filter { query -> query.isNotEmpty() }
            // 聯集條件
            val commandQueriesOr = or.map { condition -> condition.build(tableVisible) }.filter { query -> query.isNotEmpty() }
            // 條件總數量
            val conditionCount = if(commandQuerySelf.isNotEmpty()) 1 else 0 +
                    commandQueriesAnd.count() +
                    commandQueriesOr.count()
            // 完整比對請求命令
            val commandQueryFull = StringBuilder()
            if(true) {
                // 當條件 > 1 需要使用括號包裹多重條件
                if(conditionCount > 1) {
                    commandQueryFull.append("(")
                }

                if(commandQuerySelf.isNotEmpty()) {
                    commandQueryFull.append(commandQuerySelf)
                }

                if(commandQueriesAnd.isNotEmpty()) {
                    if(commandQuerySelf.isNotEmpty()) {
                        commandQueryFull.append(" AND ")
                    }
                    commandQueryFull.append(commandQueriesAnd.toLinkString(linkText = " AND "))
                }

                if(commandQueriesOr.isNotEmpty()) {
                    if(commandQuerySelf.isNotEmpty() || commandQueriesAnd.isNotEmpty()) {
                        commandQueryFull.append(" OR ")
                    }
                    commandQueryFull.append(commandQueriesOr.toLinkString(linkText = " OR "))
                }

                if(conditionCount > 1) {
                    commandQueryFull.append(")")
                }
            }

            return commandQueryFull.toString()
        }
    }
    /**表格欄位資訊*/
    class Field {
        constructor(
            table: String = "",
            name: String,
            nick: String = "",
            default: Any? = null,
            value: Any? = null,
            type: HBG5DbConfig.FieldType
        ) {
            this.table = table
            this.name = name
            this.nick = nick
            this.default = default
            this.value = value
            this.type = type
        }

        var table: String
        var name: String
        var nick: String
        var default: Any?
        /** Boolean, Number, String, Field, List<*>, Selector, */
        var value: Any?
        var type: HBG5DbConfig.FieldType
        var cursor: Int? = null
        var function: Function? = null

        fun copy(): Field = Field(
            table, name, nick, default, value, type
        )

        open class Function {
            open fun buildField(field: String): String = field
        }
        class Distinct: Function() {
            override fun buildField(field: String): String = "DISTINCT($field)"
        }
        class Count: Function() {
            override fun buildField(field: String): String = "COUNT($field)"
        }
        class Sum: Function() {
            override fun buildField(field: String): String = "SUM($field)"
        }
        class Max: Function() {
            override fun buildField(field: String): String = "MAX($field)"
        }
        /** CASE WHEN THEN WHEN THEN ELSE END */
        class IfElse(private val raw: CharSequence): Function() {
            override fun buildField(field: String): String = raw.toString()
        }
    }

    class Table(val name: String, val nick: String)
    /**關聯表單資訊*/
    class JoinTable(
        val type: Selector.JoinType,
        val join: Table,
        val fields: Map<Field, Field>
    )
    /**事務處理紀錄*/
    class TransactionRecord {
        var count = 0
        var success = true
        var error: Exception? = null

        fun errorMessage(): String {
            return when(success) {
                true -> ""
                else -> (error?.message?.trim() ?: "").ifEmpty { "Run sqlite query failed, but not found error information" }
            }
        }
    }

    /**
     * 1. 字串資料轉換
     * 2. 布林轉換數據庫結構
     * 3. 數值轉換數據庫結構
     * 4. JSON物件轉換數據庫結構
     * */ private
    fun sqlValue(value: Any): String = when(value) {
        is CharSequence -> "'${value.toString().replace("'", "''")}'"
        is Number -> value.toString()
        is Boolean -> if(value) "1" else "0"
        else -> "'${value.toJson().replace("'", "''")}'"
    }
}

/** 欄位暱稱或名稱(暱稱優先) */
fun Field.nickOrName(): String = nick.ifEmpty { name }
/** 明確指定欄位歸屬表格 NEW MEM */
fun Field.table(
    table: HBG5DSL.Table,
    default: Any? = this.default
): Field {
    val newField = copy()
    newField.table = table.nick.ifEmpty { table.name }
    newField.nick = "${newField.table}$name".replace(".", "")
    newField.default = default
    return newField
}
/** 設置欄位處理方法 NEW MEM */
fun Field.function(function: Field.Function?): Field {
    val newField = copy()
    newField.function = function
    return newField
}

/** 設置欄位的數值 NEW MEM */
fun Field.value(default: Any? = this.default, value: Any?): Field {
    val newField = copy()
    newField.value = value
    newField.default = when(default) {
        is Boolean,
        is Number,
        is CharSequence -> default
        else -> null
    }
    return newField
}
/** 欄位讀取資料庫結果指標 並且記錄結果對照的索引 */
fun Field.cursor(cursor: Cursor): Cursor? {
    if(this.cursor == null) {
        val names = cursor.columnNames
        this.cursor = names.indexOf(nickOrName())
    }
    return if(this.cursor == -1) null else cursor
}

inline fun<reified T> Field.getOrNull(cursor: Cursor): T? {
    val fieldCursor = this.cursor(cursor) ?: return null

    return when (T::class) {
        Long::class    -> fieldCursor.getLongOrNull(this.cursor!!) as? T
        Int::class     -> fieldCursor.getIntOrNull(this.cursor!!) as? T
        Double::class  -> fieldCursor.getDoubleOrNull(this.cursor!!) as? T
        Float::class   -> fieldCursor.getFloatOrNull(this.cursor!!) as? T
        String::class  -> fieldCursor.getStringOrNull(this.cursor!!) as? T
        Boolean::class -> fieldCursor.getBooleanOrNull(this.cursor!!) as? T
        else -> fieldCursor.getJsonOrNull(this.cursor!!) as? T
    }
}
inline fun<reified T> Field.getJsonArrayOrNull(cursor: Cursor): List<T>? = cursor(cursor)?.getJsonArrayOrNull(this.cursor!!)




/**
 * 執行失敗會扔出例外
 * @param closure 如果執行失敗則拋出例外
 * */
@Throws
fun HBG5Database.transaction(record: HBG5DSL.TransactionRecord, closure: (HBG5DSL.TransactionRecord) -> Unit) {
    if(record.count == 0) {
        beginTransaction()
    }
    if(record.success) {
        record.count += 1
        try { closure(record) }
        // 提前中斷 撤回操作
        catch (error: Exception) {
            // 紀錄底層錯誤
            if(record.success) {
                record.success = false
                record.error = error
                endTransaction(success = false)
                throw error
            }
            // 暴露底層錯誤 否則暴露目前錯誤
            else {
                throw record.error ?: error
            }
        }
        record.count -= 1
    }
    if(record.count == 0) {
        endTransaction(success = true)
    }
}