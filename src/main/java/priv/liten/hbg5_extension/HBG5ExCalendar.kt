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
fun Calendar.toString(format: String, utc: Int? = null) : String {
    val sdf = SimpleDateFormat(format, Locale.getDefault())
    sdf.timeZone = when (utc) {
        null -> this.timeZone ?: TimeZone.getDefault()
        else -> TimeZone.getTimeZone("GMT%+03d".format(utc))
    }

    return sdf.format(this.time)
}

fun Calendar.set(date: HBG5Date, time: HBG5Time): Calendar {
    this.set(date.year, date.month, date.day, time.hour, time.minute, time.second)
    this.set(Calendar.MILLISECOND, 0)
    return this
}

fun Calendar.onAppend(field: Int, amount: Int): Calendar {
    val result = Calendar.getInstance()
    result.timeZone = this.timeZone
    result.timeInMillis = this.timeInMillis
    result.add(field, amount)
    return result
}
/** todo hbg */
fun Calendar.initUtc(utc: Int): Calendar {
    val result = Calendar.getInstance(TimeZone.getTimeZone("GMT%+03d".format(utc)))
    result.timeInMillis = this.timeInMillis
    return result
}
/** todo hbg year * 12 + month */
fun Calendar.months(): Int {
    return this.get(Calendar.YEAR) * 12 + this.get(Calendar.MONTH)
}

// todo hbg delete
//fun Calendar.toYMDIntArray(): IntArray

// todo hbg delete
//fun Calendar.toYMDInt(): Int

// todo hbg delete
//fun Calendar.toHMSInt(): Int

// todo hbg delete
//fun Calendar.gmt(gmt: Int): Calendar