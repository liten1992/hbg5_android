package priv.liten.hbg5_database

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.database.getLongOrNull
import com.google.gson.Gson
import priv.liten.base_extension.mapFirst
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_extension.getPrivatePath

import priv.liten.hbg5_extension.toLinkString
import priv.liten.hbg5_widget.application.HBG5Application
import priv.liten.hbg5_widget.config.HBG5WidgetConfig

open class HBG5Database {

    companion object {

        const val LOG_TAG = "////DB"

        val DEFAULT_GSON = Gson()

        /** 轉換輸入資料為資料庫格式 */
        fun toVal(value : Any?) : String {
            value
                ?.let {
                    if(it is Int || it is Long || it is Float || it is Double) {
                        return "$it"
                    }
                    if(it is String) {
                        return "'${it.replace("'", "''")}'"
                    }
                    if(it is Boolean) {
                        return if(it) "1" else "0"
                    }
                    return "'${DEFAULT_GSON.toJson(it)}'"
                }
                ?:return "NULL"
        }
        /** 判斷 模糊字串 */
        fun like(field: String, value: String?): String {
            if(value?.isNotEmpty() != true) { return "" }
            return "$field LIKE '%$value%'"
        }
        /** 判斷字串非空值 */
        fun notEmpty(field: String): String {
            return "$field NOT NULL AND $field != ''"
        }
        /** 判斷 == NULL */
        fun eqNull(field: String): String {
            return "$field IS NULL"
        }
        /** 判斷 != NULL & == 布林 */
        fun eq(field: String, value: Boolean?): String {
            if(value == null) { return "" }
            return "$field = ${toVal(value)}"
        }
        /** 判斷 != NULL & == 數值 */
        fun eq(field: String, value: Number?): String {
            if(value == null) { return "" }
            return "$field = ${toVal(value)}"
        }
        /** 判斷 != NULL & == 字串 */
        fun eq(field: String, value: String?): String {
            if(value.isNullOrEmpty()) { return "" }
            return "$field = ${toVal(value)}"
        }

        /** 判斷 each != NULL & == 參數  */
        fun eq(field: String, values: List<Any>?, link: String): String {
            if(values.isNullOrEmpty()) { return "" }
            return when(link) {
                "OR" -> "$field IN (${values.map { toVal(it) }.toLinkString(", ")})"
                else -> values.map { "$field = ${toVal(it)}" }.toLinkString(" $link ")
            }
        }
        /** 判斷 each != NULL & == 參數  */
        fun eqIn(field: String, values: List<Any>?): String {
            return eq(field, values, "OR")
        }
        fun eqIn(field: String, table: String): String {
            if(table.isEmpty()) { return "" }
            return "$field IN ($table)"
        }
        /**  */
        fun notEq(field: String, value: Any?): String {
            if(value == null) { return "" }
            return "($field != ${toVal(value)} OR ${eqNull(field)})"
        }
        /** 判斷 each != NULL & == 參數  */
        fun notEq(field: String, values: List<Any>?, link: String): String {
            if(values.isNullOrEmpty()) { return "" }
            val sql = when(link) {
                "OR" -> "$field NOT IN (${values.map { toVal(it) }.toLinkString(", ")})"
                else -> values.map { "$field != ${toVal(it)}" }.toLinkString(" $link ")
            }
            return "(${sql} OR ${eqNull(field)})"
        }
        /**  */
        fun notEqIn(field: String, values: List<Any>?): String {
            return notEq(field, values, "OR")
        }
        /** 判斷 != NULL & >= 數值 */
        fun moreEq(field: String, value: Number?): String {
            if(value == null) { return "" }
            return "$field NOT NULL AND $field >= ${toVal(value)}"
        }
        /** 判斷 each != NULL & >= 數值 */
        fun moreEq(fields: List<String>, value: Number?, link: String): String {
            if(value == null || fields.isEmpty()) { return "" }
            return fields.map { moreEq(it, value) }.toLinkString(" $link ")
        }
        /** 判斷 != NULL & > 數值 */
        fun more(field: String, value: Number?): String {
            if(value == null) { return "" }
            return "$field NOT NULL AND $field > ${toVal(value)}"
        }
        /** 判斷 each != NULL & > 數值 */
        fun more(fields: List<String>, value: Number?, link: String): String {
            if(value == null || fields.isEmpty()) { return "" }
            return fields.map { more(it, value) }.toLinkString(" $link ")
        }
        /** 判斷 != NULL & <= 數值 */
        fun lessEq(field: String, value: Number?): String {
            if(value == null) { return "" }
            return "$field NOT NULL AND $field <= ${toVal(value)}"
        }
        /** 判斷 each != NULL & <= 數值 */
        fun lessEq(fields: List<String>, value: Number?, link: String): String {
            if(value == null || fields.isEmpty()) { return "" }
            return fields.map { lessEq(it, value) }.toLinkString(" $link ")
        }
        /** 判斷 != NULL & < 數值 */
        fun less(field: String, value: Number?): String {
            if(value == null) { return "" }
            return "$field NOT NULL AND $field < ${toVal(value)}"
        }
        /** 判斷 each != NULL & <= 數值 */
        fun less(fields: List<String>, value: Number?, link: String): String {
            if(value == null || fields.isEmpty()) { return "" }
            return fields.map { less(it, value) }.toLinkString(" $link ")
        }
        /** 關聯句 OR */
        fun or(vararg whereList: String): String {
            return whereList
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toLinkString(" OR ")
                .let { whereSql ->
                    if(whereSql.isNotEmpty()) "($whereSql)" else ""
                }
        }
        /** 關聯句 AND */ // todo hbg
        fun and(vararg whereList: String): String {
            val trimWhereList = whereList.map { it.trim() }.filter { it.isNotEmpty() }
            if(trimWhereList.isEmpty()) { return "" }

            return trimWhereList
                .toLinkString(" AND ")
                .let { whereSql ->
                    if(whereSql.isNotEmpty()) "($whereSql)" else ""
                }
        }
    }

    var db : SQLiteDatabase? = null

    var transactionCount = 0

    fun dbName(): String {
        return "app.db"
    }
    fun dbPath(): String {

        val context = HBG5Application.instance!!

        return context.getPrivatePath(
            dirName = HBG5WidgetConfig.PRIVATE_DIR_DOCUMENTS,
            fileName = dbName()
        ) ?: ""
    }

    fun openDB(version: Long? = null) : Boolean {

        if(db != null) {
            version?.let { this.version = it }
            return true
        }

        db = SQLiteDatabase.openOrCreateDatabase(dbPath(), null)

        version?.let { this.version = it }

        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "openDB")
        }

        return true
    }

    fun closeDB() {

        db?.let {

            val db = it

            db.close()

            this.db = null

            if(BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "closeDB")
            }
        }
    }

    fun selectMaxIdx(tableName: String): Long {

        return querySqlCursor(SelectBuilder()
            .setTable(name = "sqlite_sequence")
            .addField(field = "seq")
            .setWhere(sql = "name = '$tableName'")
            .build())
            ?.mapFirst { cursor -> cursor.getLongOrNull(0)!! } ?: 0
    }
    // todo hbg5
    fun nextIdx(tableName: String): Long {
        return selectMaxIdx(tableName = tableName) + 1
    }

    fun updateMaxIdx(tableName: String, id: Long) {

        execSql(sql = UpdateBuilder()
            .setTable(name = "sqlite_sequence")
            .addField(key = "seq", value = id)
            .setWhere(sql = "name = '$tableName'")
            .build())
    }

    /** 開始交易 */
    fun beginTransaction() {
        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "beginTransaction")
        }
        db?.beginTransaction()
    }

    /** 結束交易 @param success = true 才會真正被執行 */
    fun endTransaction(success: Boolean = false) {
        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "endTransaction ${if(success) "SUCCESS" else "FAILED"}")
        }
        if(success) {
            db?.setTransactionSuccessful()
        }
        db?.endTransaction()
    }

    /** 建立交易打包運行邏輯 會有個計數器 數量為0才會真正執行 */
    fun transaction(closure: (() -> Boolean)): Boolean {
        if(transactionCount == 0) {
            transactionResult = true
            beginTransaction()
        }
        if(transactionResult) {
            transactionCount += 1
            try {
                transactionResult = closure()
            }
            catch (error: Exception) {
                if(BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "TransactionError $error")
                }
                transactionResult = false
            }
            transactionCount -= 1
        }
        if(transactionCount == 0) {
            endTransaction(success = transactionResult)
        }
        return transactionResult
    }
    private
    var transactionResult = false

    @Synchronized
    fun execSql(sql : String) : Boolean {
        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, sql)
        }
        db?.let {
            val db = it
            return try {
                db.execSQL(sql)
                true
            }
            catch (error : Exception) {
                if(BuildConfig.DEBUG) {
                    Log.e(LOG_TAG, error.message ?: "execSql unknown error")
                }
                false
            }
        }
        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "execSql db is null cause failed")
        }
        return false
    }

    @Synchronized
    fun querySqlCursor(sql:String): Cursor? {

        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, sql)
        }

        return db?.let { return@let it.rawQuery(sql, arrayOfNulls(0)) }
    }

    /** 撈出來的資料只會有 Double Long 最高經度等級的資料 */
    @Synchronized
    fun querySqlList(sql:String) : List<List<Any?>> {

        if(BuildConfig.DEBUG) {
            Log.d(LOG_TAG, sql)
        }

        val dbItemList = mutableListOf<MutableList<Any?>>()

        db?.let {
            val db = it

            val cursor = db.rawQuery(sql, arrayOfNulls(0))

            val fCount = cursor.columnCount

            while (cursor.moveToNext()) {

                val dbItem = mutableListOf<Any?>()

                for (i in 0 until fCount) {
                    when (cursor.getType(i)) {
                        Cursor.FIELD_TYPE_INTEGER -> {
                            dbItem.add(cursor.getInt(i))
                        }

                        Cursor.FIELD_TYPE_FLOAT -> {
                            dbItem.add(cursor.getDouble(i))
                        }

                        Cursor.FIELD_TYPE_STRING -> {
                            dbItem.add(cursor.getString(i))
                        }

                        Cursor.FIELD_TYPE_BLOB -> {
                            dbItem.add(cursor.getBlob(i))
                        }

                        Cursor.FIELD_TYPE_NULL -> {
                            dbItem.add(null)
                        }
                    }
                }

                dbItemList.add(dbItem)
            }

            cursor.close()
        }

        return dbItemList
    }

    var version: Long
        get() {
            var result: Long? = null
            querySqlCursor(sql = "PRAGMA user_version")?.let { cursor ->
                cursor.mapFirst { result = it.getLongOrNull(0)!! }
            }
            return result ?: 0
        }
        set(value) {

            val oldVersion = this.version

            if(value <= oldVersion) { return }

            onVersion(oldVer = oldVersion, newVer = value)

            execSql(sql = "PRAGMA user_version = $value")
        }

    open fun onVersion(oldVer : Long, newVer : Long) {

    }

    open class InsertBuilder {

        constructor(action: String) {
            this.action = action
        }
        // todo hbg
        constructor(): this("INSERT INTO")

        private val action: String
        private var tableName: String = ""
        private var fieldList: MutableList<Field> = mutableListOf()

        fun setTable(name: String): InsertBuilder {
            tableName = name
            return this
        }

        fun setFields(vararg fields: Field): InsertBuilder {
            fieldList.clear()
            fieldList.addAll(fields)
            return this
        }

        fun addField(key: String, value: Any?, enable: Boolean = true): InsertBuilder {
            if(!enable) { return this }
            // todo hbg
            if(key == "_id") {
                when(value) {
                    is Long -> {
                        fieldList.add(Field(
                            key = key,
                            value = toVal(value = if(value == 0L) null else value)))
                    }
                    is Int -> {
                        fieldList.add(Field(
                            key = key,
                            value = toVal(value = if(value == 0) null else value)))
                    }
                    else -> {
                        fieldList.add(Field(
                            key = key,
                            value = toVal(value = value)))
                    }
                }
            }
            else {
                fieldList.add(Field(
                    key = key,
                    value = toVal(value = value)))
            }

            return this
        }
        // todo hbg
        fun addFields(fields: Map<String, Any?>): InsertBuilder {
            for((key,value) in fields) {
                addField(key = key, value = value)
            }
            return this
        }

        fun build(): String{

            val strBuilder = StringBuilder()

            strBuilder.append("$action $tableName ")
            strBuilder.append("(")

            for((i, field) in fieldList.withIndex()) {
                if(i == 0) { strBuilder.append(field.key) }
                else { strBuilder.append(", ${field.key}") }
            }

            strBuilder.append(") ")
            strBuilder.append("VALUES ")
            strBuilder.append("(")
            for((i, field) in fieldList.withIndex()) {
                if(i == 0) { strBuilder.append(field.value) }
                else { strBuilder.append(", ${field.value}") }
            }
            strBuilder.append(")")

            return strBuilder.toString()
        }

        fun execute(database: HBG5Database): Boolean = database.execSql(this.build())

        class Field {

            constructor()

            constructor(key: String, value: String) {
                this.key = key
                this.value = value
            }

            var key: String = ""
            var value: String = ""
        }
    }

    open class InsertOrReplaceBuilder : InsertBuilder(action = "INSERT OR REPLACE INTO")

    open class UpdateBuilder {

        private var tableName: String = ""
        private var fieldList: MutableList<String> = mutableListOf()
        private var where: String = ""

        fun setTable(name: String): UpdateBuilder {
            tableName = name
            return this
        }

        fun addField(key: String, value: Any?): UpdateBuilder {
            fieldList.add("$key = ${toVal(value)}")
            return this
        }
        // todo hbg
        fun addFields(fields: Map<String, Any?>): UpdateBuilder {
            for((key, value) in fields) {
                addField(key = key, value = value)
            }
            return this
        }

        // todo hbg
        fun setWhere(sql: String): UpdateBuilder {
            if(sql.isEmpty()) { return this }
            var formatSql = sql
                .replace("= NULL", "IS NULL")
                .replace("!= NULL", "IS NOT NULL")

            formatSql =
                if(formatSql.startsWith('(') && formatSql.endsWith(')')) formatSql
                else "($formatSql)"

            where = formatSql

            return this
        }

        fun andWhere(sql: String): UpdateBuilder {
            val formatSql = sql.replace("= NULL", "IS NULL").replace("!= NULL", "IS NOT NULL")
            if(formatSql.isEmpty()) { return this }
            if(where.isEmpty()) { setWhere(formatSql) }
            else { where = "$where AND ($formatSql)" }
            return this
        }

        fun orWhere(sql: String): UpdateBuilder {
            val formatSql = sql.replace("= NULL", "IS NULL").replace("!= NULL", "IS NOT NULL")
            if(formatSql.isEmpty()) { return this }
            if(where.isEmpty()) { setWhere(formatSql) }
            else { where = "$where OR ($formatSql)" }
            return this
        }


        fun build(): String {

            val strBuilder = StringBuilder("UPDATE $tableName SET ${fieldList.toLinkString(", ")} ")

            if(where.isNotEmpty()) {
                strBuilder.append("WHERE $where ")
            }

            return strBuilder.toString()
        }

        fun execute(database: HBG5Database): Boolean = database.execSql(this.build())
    }

    open class SelectBuilder {

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

        fun setTable(name: String, tableNick: String? = null): SelectBuilder {
            tableNick
                ?.let {
                    tableName = "$name AS $it"
                }
                ?:run {
                    tableName = name
                }

            return this
        }

        /** @param fields: <self,join>
         * 2025-01-25 調整可疊加多張資料表
         * */
        fun joinTable(
            type: JoinType,
            selfTable: String,
            joinTable: String, joinTableAs: String,
            fields: Map<String, String>): SelectBuilder {

            val sqlCondition = fields
                .map { (selfField, joinField) -> "$selfTable.$selfField = $joinTableAs.$joinField" }
                .toLinkString(" AND ")

            val sql = "${type.sql} $joinTable AS $joinTableAs ON $sqlCondition"

            if(this.joinTable.isNotEmpty()) {
                this.joinTable = "${this.joinTable} ${sql}"
            }
            else {
                this.joinTable = sql
            }

            return this
        }

        fun unionTable(
            type: UnionType,
            unionSelect: String): SelectBuilder {

            this.unionTable = when(type) {
                UnionType.UNIQUE -> "UNION $unionSelect"
                UnionType.ALL -> "UNION ALL $unionSelect"
            }

            return this
        }

        fun addField(field: String, tableNick: String? = null, fieldNick: String? = null): SelectBuilder {
            tableNick
                ?.let {
                    fieldNick
                        ?.let {
                            fieldList.add("$tableNick.$field AS $fieldNick")
                        }
                        ?:run {
                            fieldList.add("$tableNick.$field")
                        }
                }
                ?:run {
                    fieldNick
                        ?.let {
                            fieldList.add("$field AS $fieldNick")
                        }
                        ?:run {
                            fieldList.add(field)
                        }
                }

            return this
        }
        // todo hbg
        fun addFields(vararg fields: String): SelectBuilder {
            for(field in fields) {
                addField(field = field)
            }
            return this
        }
        /** 初始條件 */
        fun setWhere(sql: String?): SelectBuilder {
            if(sql.isNullOrEmpty()) { return this }
            var formatSql = sql
                .replace("= NULL", "IS NULL")
                .replace("!= NULL", "IS NOT NULL")

            formatSql =
                if(formatSql.startsWith('(') && formatSql.endsWith(')')) formatSql
                else "($formatSql)"

            condition = formatSql

            return this
        }
        /** AND 條件 */
        fun andWhere(sql: String?): SelectBuilder {
            if(sql.isNullOrEmpty()) { return this }
            var formatSql = sql
                .replace("= NULL", "IS NULL")
                .replace("!= NULL", "NOT NULL")

            formatSql =
                if(formatSql.startsWith('(') && formatSql.endsWith(')')) formatSql
                else "($formatSql)"

            if(condition.isEmpty()) { setWhere(formatSql) }
            else { condition = "$condition AND $formatSql" }

            return this
        }
        /** OR 條件 */
        fun orWhere(sql: String?): SelectBuilder {
            if(sql.isNullOrEmpty()) { return this }
            var formatSql = sql
                .replace("= NULL", "IS NULL")
                .replace("!= NULL", "IS NOT NULL")

            formatSql =
                if(formatSql.startsWith('(') && formatSql.endsWith(')')) formatSql
                else "($formatSql)"

            if(condition.isEmpty()) { setWhere(formatSql) }
            else { condition = "$condition OR $formatSql" }

            return this
        }


        fun setOrderBy(sql: String): SelectBuilder {
            orderBy = sql
            return this
        }

        fun setOrderBy(sqlList: List<String>?): SelectBuilder {
            return setOrderBy(sqlList?.toLinkString(", ") ?: "")
        }

        fun setGroupBy(vararg fields: String) : SelectBuilder {
            groupBy = fields.toList().toLinkString(", ")
            return this
        }

        fun limit(count: Long?): SelectBuilder {
            count
                ?.let {
                    limit = "$it"
                }
                ?:run {
                    limit = ""
                }
            return this
        }

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

            return sqlList.filter { it.isNotEmpty() }.toLinkString(" ")
        }

        fun execute(database: HBG5Database): Cursor? = database.querySqlCursor(this.build())
    }

    open class DeleteBuilder {

        private var tableName: String = ""
        private var where: String = ""

        fun setTable(name: String): DeleteBuilder {
            tableName = name
            return this
        }

        fun setWhere(sql: String): DeleteBuilder {
            where = sql
            return this
        }
        fun setWhere(
            field: String,
            condition: String,
            result: String): DeleteBuilder {

            where = "$field $condition $result"
            return this
        }

        fun andWhere(sql: String): DeleteBuilder {
            if(sql.isEmpty()) { return this }
            if(where.isEmpty()) { setWhere(sql) }
            else { where = "$where AND ($sql)" }
            return this
        }

        fun orWhere(sql: String): DeleteBuilder {
            if(sql.isEmpty()) { return this }
            if(where.isEmpty()) { setWhere(sql) }
            else { where = "$where OR ($sql)" }
            return this
        }


        fun build(): String {

            val strBuilder = StringBuilder()

            strBuilder.append("DELETE FROM $tableName ")
            if(where.isNotEmpty()) {
                strBuilder.append("WHERE $where ")
            }

            return strBuilder.toString()
        }

        fun execute(database: HBG5Database): Boolean = database.execSql(this.build())
    }
}
