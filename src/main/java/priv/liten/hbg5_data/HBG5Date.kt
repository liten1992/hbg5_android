package priv.liten.hbg5_data

import priv.liten.hbg5_extension.toCalendar
import java.text.SimpleDateFormat
import java.util.*

/** Year 1900-2100, Month 0-11, Day 1-31 */
class HBG5Date {

    // MARK: ====================== Constructor
    constructor(calendar: Calendar) { build(calendar = calendar) }

    constructor(): this(Calendar.getInstance()) { }

    constructor(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
    }

    constructor(timeInMillis: Long): this(calendar = timeInMillis.toCalendar())
    /**初始化失敗 將會套用系統當前日期*/ // todo hbg
    constructor(date: String) {
        val args = date
            .replace("/", "")
            .replace("-", "")
            .replace("T", " ")
            .split(" ")
        val dashDate = args.firstOrNull()
        if(dashDate == null) {
            build(calendar = Calendar.getInstance())
            return
        }
        if(dashDate.length != 8) {
            build(calendar = Calendar.getInstance())
            return
        }
        try {
            val calendar = dashDate.toCalendar(format = "yyyyMMdd") ?: throw NullPointerException()
            build(calendar = calendar)
        }
        catch (error: Throwable) {
            build(calendar = Calendar.getInstance())
        }
    }

    private fun build(calendar: Calendar) {
        this.year = calendar[Calendar.YEAR]
        this.month = calendar[Calendar.MONTH]
        this.day = calendar[Calendar.DAY_OF_MONTH]
    }


    // MARK: ====================== Data
    var year: Int = 0
    var month: Int = 0
    var day: Int = 0


    // MARK: ====================== Method
    override fun equals(other: Any?): Boolean {

        (other as? HBG5Date)?.let {
            return it.year == this.year && it.month == this.year && it.day == this.day
        }

        return super.equals(other)
    }

    fun clone(): HBG5Date {
        return HBG5Date(year, month, day)
    }

    fun set(date: HBG5Date) {
        this.year = date.year
        this.month = date.month
        this.day = date.day
    }

    fun set(year: Int, month: Int, day: Int) {
        this.year = year
        this.month = month
        this.day = day
    }

    val longDate: Long
        get() {
            return year.toLong() * 10000L + month.toLong() * 100L + day.toLong()
        }

    /** 2022-12-31 : 20221131 */
    fun toYMDInt(): Int {
        return year*10000+month*100+day
    }

    fun toYMDArray(): IntArray {
        return intArrayOf(year, month, day)
    }
    /** @param format: yyyy-MM-dd */
    fun toString(format: String): String {

        val calendar = Calendar.getInstance().apply { set(year, month, day) }

        val sdf = SimpleDateFormat(format, Locale.ENGLISH)

        return sdf.format(calendar.time)
    }

    fun toCalendar(): Calendar {
        return Calendar.getInstance().apply {
            set(year, month, day, 0, 0,0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}