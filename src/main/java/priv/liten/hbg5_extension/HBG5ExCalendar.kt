package priv.liten.hbg5_extension

import priv.liten.hbg5_data.HBG5Date
import priv.liten.hbg5_data.HBG5Time
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun Calendar.betweenDays(to: Calendar): Int {

    val startMillis = this.timeInMillis
    val toMillis = to.timeInMillis

    return if(startMillis >= toMillis) {
        -TimeUnit.DAYS.convert(startMillis - toMillis, TimeUnit.MILLISECONDS).toInt()
    }
    else {
        TimeUnit.DAYS.convert(toMillis - startMillis, TimeUnit.MILLISECONDS).toInt()
    }
}

fun Calendar.toYMDIntArray(): IntArray {

    return intArrayOf(
        get(Calendar.YEAR),
        get(Calendar.MONTH),
        get(Calendar.DAY_OF_MONTH))
}

fun Calendar.toYMDInt(): Int {
    return get(Calendar.YEAR)*10000 + get(Calendar.MONTH)*100 + get(Calendar.DAY_OF_MONTH)
}

fun Calendar.toHMSInt(): Int {
    return get(Calendar.HOUR_OF_DAY)*10000 + get(Calendar.MINUTE)*100 + get(Calendar.SECOND)
}

fun Calendar.gmt(gmt: Int = TimeZone.getDefault().rawOffset / 3600000): Calendar {
    this.timeZone = TimeZone.getTimeZone("GMT+$gmt")
    return this
}

/** 設置日期為當周第一天 */
fun Calendar.toStartWeekCalendar(timeInMillis: Long? = null): Calendar {
    timeInMillis?.let { this.timeInMillis = it }
    val week = this.get(Calendar.DAY_OF_WEEK)
    this.add(Calendar.DAY_OF_MONTH, 1 - week)
    return this
}

fun Calendar.toStartWeekCalendar(date: IntArray): Calendar {
    this.set(date[0], date[1], date[2])
    return toStartWeekCalendar()
}

fun Calendar.toFirstDayOfMonthCalendar(): Calendar {
    this.set(this.get(Calendar.YEAR), this.get(Calendar.MONTH), 1)
    return this
}
fun Calendar.toLastDayOfMonthCalendar(): Calendar {
    this.set(this.get(Calendar.YEAR), this.get(Calendar.MONTH), this.getActualMaximum(Calendar.DAY_OF_MONTH))
    return this
}
/**@param format:yyyy-MM-dd*/
fun Calendar.toString(format: String) : String {

    val sdf = SimpleDateFormat(format, Locale.ENGLISH)
    sdf.timeZone = this.timeZone

    return sdf.format(this.time)
}

fun Calendar.set(date: HBG5Date, time: HBG5Time): Calendar {
    this.set(date.year, date.month, date.day, time.hour, time.minute, time.second)
    return this
}

fun Calendar.onAppend(field: Int, amount: Int): Calendar {
    val result = Calendar.getInstance()
    result.timeZone = this.timeZone
    result.timeInMillis = this.timeInMillis
    result.add(field, amount)
    return result
}