package priv.liten.hbg5_database

import android.util.Log
import androidx.core.database.getStringOrNull
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_config.HBG5DbConfig
import priv.liten.hbg5_database.HBG5DSL.Field
import priv.liten.hbg5_extension.contains
import priv.liten.hbg5_extension.toLinkString


open class HBG5DbTable {

    companion object { }

    constructor()

    constructor(name:String) {
        this.name = name
    }

    var name : String = ""

    var fields : MutableMap<String, HBG5DbConfig.FieldType> = mutableMapOf()

    /** 生成表格 */
    fun create(database : HBG5Database) : Boolean {
        if(fields.isEmpty()) { return false }
        val fieldsSql = fields
            .map { (key, value) -> "$key ${value.value}" }
            .toLinkString(linkText = ", ")
        return database.execSql(sql = "CREATE TABLE IF NOT EXISTS $name (${fieldsSql})")
    }
    /** 清除表格資料 */
    fun clear(database : HBG5Database) : Boolean {
        return database.execSql(sql = "DELETE FROM $name")
                && database.execSql(sql = "DELETE FROM sqlite_sequence WHERE name = '$name'")
    }
    /** 移除整張表格 */
    fun drop(database: HBG5Database) : Boolean {
        return database.execSql(sql = "DROP TABLE IF EXISTS $name")
    }
    /** 判斷表格存在 */
    fun exist(database: HBG5Database) : Boolean {
        return database
            .querySqlList(sql = "SELECT tbl_name FROM sqlite_master WHERE tbl_name = '$name' LIMIT 1")
            .isNotEmpty()
    }
    /** 新增欄位 */
    fun addField(database: HBG5Database, fName: String, fType: HBG5DbConfig.FieldType, fDefault: String = "") : Boolean {
        // 如果欄位存在忽略操作
        if(hasField(database = database, fName = fName)) {
            if(BuildConfig.DEBUG) {
                Log.d(HBG5Database.LOG_TAG, "ignore ALTER TABLE {name} ADD COLUMN because column exist")
            }
            return true
        }
        var query = "ALTER TABLE $name ADD COLUMN $fName ${fType.value}"
        if(fDefault.isNotEmpty()) {
            query += " DEFAULT $fDefault"
        }

        return database.execSql(sql = query)
    }
    /** 存在欄位 */
    fun hasField(database: HBG5Database, fName: String) : Boolean {
        val cursor = database.querySqlCursor(sql = "PRAGMA table_info($name)") ?: return false

        val filedNameIndex = cursor.columnNames.indexOf("name")
        if(filedNameIndex == -1) {
            cursor.close()
            return false
        }
        while (cursor.moveToNext()) {
            if(cursor.getStringOrNull(filedNameIndex) == fName) {
                cursor.close()
                return true
            }
        }
        cursor.close()
        return false
    }
    /**[[name, type], [name, type] ...]*/ // todo hbg
    fun existedFields(database: HBG5Database): List<List<String>> {
        val cursor = database.querySqlCursor(sql = "PRAGMA table_info($name)") ?: return emptyList()

        database.db?.version
        val filedNameIndex = cursor.columnNames.indexOf("name")
        val fileTypeIndex = cursor.columnNames.indexOf("type")
        if(filedNameIndex == -1 || fileTypeIndex == -1) {
            cursor.close()
            return emptyList()
        }

        val result = mutableListOf<List<String>>()
        while (cursor.moveToNext()) {
            val fieldName = cursor.getStringOrNull(filedNameIndex) ?: continue
            val fieldType = cursor.getStringOrNull(fileTypeIndex) ?: continue
            result.add(listOf(fieldName, fieldType))
        }
        cursor.close()
        return result
    }
    /** 更名欄位 */ // todo hbg 3.25 以前無法使用該指令為表單修改欄位名稱
    fun renameField(database: HBG5Database, old: String, new: String): Boolean {
        val existedFields = existedFields(database = database)
        val oldFieldExist = existedFields.contains { info -> info.getOrNull(0) == old }
        val newFieldExist = existedFields.contains { info -> info.getOrNull(0) == new }
        // 舊欄位名稱存在
        if(oldFieldExist) {
            // 新欄位已存在無法覆蓋欄位名稱
            if(newFieldExist) { return false }
            try {
                return database.execSql(sql = "ALTER TABLE $name RENAME COLUMN $old TO $new")
            }
            catch (error: Exception) {
                return false
            }
        }
        // 舊欄位名稱不存在 無法更名
        else {
            return false
        }
    }
}