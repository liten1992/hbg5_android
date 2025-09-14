package priv.liten.hbg5_extension

import java.util.*

fun Long.toCalendar(): Calendar {
    return Calendar.getInstance().also { calendar -> calendar.timeInMillis = this }
}
/** format:"yyyy-MM-dd HH:mm:ss" */
fun Long.toCalendarString(format: String): String {
    return toCalendar().toString(format)
}