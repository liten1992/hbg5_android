package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.widget.DatePicker
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_extension.toCalendar
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_data.HBG5Date
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig

class HBG5DateAlert: HBG5BaseAlert {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {

        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_date, this)
        }

        // Data
        run {
            datePicker.minDate = "1900/01/01 00:00:00.000".toCalendar("yyyy/MM/dd HH:mm:ss.SSS")!!.timeInMillis
            datePicker.maxDate = "2100/12/31 23:59:59.999".toCalendar("yyyy/MM/dd HH:mm:ss.SSS")!!.timeInMillis
        }

        // Event
        run {
            // onNoListener
            cancelButton.v5RegisterClick { _ -> v5Cancel() }
            // onYesListener
            confirmButton.v5RegisterClick { _ -> v5Confirm() }
        }
    }


    // MARK:- ====================== View
    private val uiTitle: HBG5TextView by lazy { findViewById(R.id.text_title) }
    private val datePicker: DatePicker by lazy { findViewById(R.id.picker_date) }
    private val cancelButton: HBG5Button by lazy { findViewById(R.id.button_cancel) }
    private val confirmButton: HBG5Button by lazy { findViewById(R.id.button_confirm) }


    // MARK:- ====================== Data

    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
    }
    override fun v5OnHide() {
        super.v5OnHide()
        onYesListener  = null
        onNoListener = null
    }

    private var onYesListener: ((HBG5Date) -> Unit)? = null
    fun v5RegisterYes(closure: ((HBG5Date) -> Unit)?) {
        onYesListener = closure
    }

    private var onNoListener: (() -> Unit)? = null
    fun v5RegisterNo(closure: (() -> Unit)?) {
        onNoListener = closure
    }

    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {

        val dataRequest = request as? DataRequest ?: return

        val title = dataRequest.title ?: ""
        uiTitle.v5Text = title
        uiTitle.v5Visibility = if(title.isEmpty()) HBG5WidgetConfig.Attrs.Visibility.Gone else HBG5WidgetConfig.Attrs.Visibility.Visible

        datePicker.init(
            dataRequest.date.year,
            dataRequest.date.month,
            dataRequest.date.day,
            null
        )
    }

    override fun v5Cancel() {
        onNoListener?.let { it() }
        super.v5Cancel()
    }

    override fun v5Confirm() {
        onYesListener?.let {
            it(HBG5Date(
                year = datePicker.year,
                month = datePicker.month,
                day = datePicker.dayOfMonth
            ))
        }
        super.v5Confirm()
    }


    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest {

        constructor(): super()

        var title: String? = null

        var date: HBG5Date = HBG5Date()
    }
}