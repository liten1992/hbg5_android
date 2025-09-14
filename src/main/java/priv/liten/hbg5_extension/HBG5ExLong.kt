package priv.liten.hbg5_extension

import java.util.*

fun Long.toCalendar(utc: Int? = null): Calendar {
    return when(utc) {
        null -> Calendar.getInstance().also { calendar -> calendar.timeInMillis = this }
        else -> Calendar.getInstance(TimeZone.getTimeZone("GMT%+03d".format(utc))).also { calendar -> calendar.timeInMillis = this }
    }
}
/** format:"yyyy-MM-dd HH:mm:ss" */
fun Long.toCalendarString(format: String): String {
    return toCalendar().toString(format)
}