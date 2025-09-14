package priv.liten.hbg5_extension

import java.util.*

/** 範圍內的整數(包含 MIN MAX) */
fun Int.withIn(minInclude: Int, maxInclude: Int): Int {
    return when {
        minInclude > this -> minInclude
        this > maxInclude -> maxInclude
        else -> this
    }
}

/** 範圍內的浮點數(包含 MIN MAX) */
fun Int.isWithIn(minInclude: Int, maxInclude: Int): Boolean {
    return this in minInclude..maxInclude
}

/** #FFFFFFFF */
fun Int.toColorString(): String {
    return "#%08x".format(this).uppercase()
}

/** 依據指定格式轉換整數至日期 */
fun Int.toCalendarWithYYYYMMDD(): Calendar {
    return "%04d-%02d-%02d 00:00:00.000"
        .format(this/10000, ((this/100)%100) + 1, (this%100))
        .toCalendar("yyyy-MM-dd HH:mm:ss.SSS")!!
}

fun Int.toYMDArray(): IntArray {
    return intArrayOf(this / 10000, this / 100 % 100, this % 100)
}

fun Int.toHMSArray(): IntArray {
    return intArrayOf(this / 10000, this / 100 % 100, this % 100)
}

fun <T> Int.getOrNull(list: List<T>?): T? {
    return list?.getOrNull(this)
}