package priv.liten.base_extension

import android.database.Cursor
/**轉換資料*/
fun <T> Cursor.map(convert: ((Cursor) -> T)): List<T> {
    val result = mutableListOf<T>()
    while (this.moveToNext()) {
        result.add(convert(this))
    }
    this.close()
    return result
}
/**僅轉換首筆資料*/
fun <T> Cursor.mapFirst(convert: ((Cursor) -> T)): T? {
    var result: T? = null
    if(this.moveToNext()) {
        result = convert(this)
    }
    this.close()
    return result
}
/**遍歷資料*/
fun Cursor.each(action: ((Cursor) -> Unit)): Unit {
    while(this.moveToNext()) {
        action(this)
    }
    this.close()
}
/**資料是否存在*/
fun Cursor.exists(): Boolean {
    val result = this.moveToNext()
    this.close()
    return result
}