package priv.liten.hbg5_data

import priv.liten.hbg5_extension.toCalendar
import java.util.*

class HBG5Time {

    // MARK: ====================== Constructor
    constructor(): this(Calendar.getInstance()) {

    }

    constructor(hour: Int, minute: Int, second: Int) {
        this.hour = hour
        this.minute = minute
        this.second = second
    }

    constructor(calendar: Calendar) {
        build(calendar = calendar)
    }
    // todo hbg
    constructor(timeInMillis: Long): this(calendar = timeInMillis.toCalendar())

    constructor(time: HBG5Time) {
        this.hour = time.hour
        this.minute = time.minute
        this.second = time.second
    }
    // todo hbg
    constructor(time: String) {
        val splitIndex = time.indexOf(':')
        if(splitIndex == -1) {
            build(calendar = Calendar.getInstance())
            return
        }

        val start = splitIndex - 2
        var end = splitIndex + 2
        if(end < time.length && time.getOrNull(end + 1) == ':') {
            end += 3
        }
        if(end >= time.length) {
            build(calendar = Calendar.getInstance())
            return
        }
        val newTime = time.substring(start, end)

        for((index, splitText) in newTime.split(":").withIndex()) {
            val value = splitText.toIntOrNull() ?: continue
            when(index) {
                // HOUR
                0 -> {
                    hour = value
                }
                // MINUTE
                1 -> {
                    minute = value
                }
                // SEC
                2 -> {
                    second = value
                }
            }
        }
    }

    private fun build(calendar: Calendar) {
        this.hour = calendar[Calendar.HOUR_OF_DAY]
        this.minute = calendar[Calendar.MINUTE]
        this.second = calendar[Calendar.SECOND]
    }


    // MARK: ====================== Data
    var hour: Int = 0
    var minute: Int = 0
    var second: Int = 0


    // MARK: ====================== Method
    override fun equals(other: Any?): Boolean {

        (other as? HBG5Time)?.let {
            return it.hour == this.hour && it.minute == this.hour && it.second == this.second
        }

        return super.equals(other)
    }

    fun clone(): HBG5Time {
        return HBG5Time(hour, minute, second)
    }

    fun set(hour: Int, minute: Int, second: Int) {
        this.hour = hour
        this.minute = minute
        this.second = second
    }
}