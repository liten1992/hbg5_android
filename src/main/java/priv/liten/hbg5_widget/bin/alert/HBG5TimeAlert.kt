package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.widget.TimePicker
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_data.HBG5Time

class HBG5TimeAlert: HBG5BaseAlert {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {

        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_time, this)
        }

        // Data
        run {

        }

        // Event
        run {
            // onNoListener
            cancelButton.v5RegisterClick { v5Cancel() }
            // onYesListener
            confirmButton.v5RegisterClick { v5Confirm() }
        }
    }


    // MARK:- ====================== View
    private val timePicker: TimePicker by lazy { findViewById(R.id.picker_time) }
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

    private var onYesListener: ((HBG5Time) -> Unit)? = null
    fun v5RegisterYes(closure: ((HBG5Time) -> Unit)?) {
        onYesListener = closure
    }

    private var onNoListener: (() -> Unit)? = null
    fun v5RegisterNo(closure: (() -> Unit)?) {
        onNoListener = closure
    }

    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {

        val dataRequest = request as? DataRequest ?: return

        timePicker.hour = dataRequest.time.hour
        timePicker.minute = dataRequest.time.minute
    }

    override fun v5Cancel() {
        onNoListener?.let { it() }
        super.v5Cancel()
    }

    override fun v5Confirm() {
        onYesListener?.let {
            it(HBG5Time(
                hour = timePicker.hour,
                minute = timePicker.minute,
                second = 0
            ))
        }
        super.v5Confirm()
    }


    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest {

        constructor(): super()

        var time: HBG5Time = HBG5Time()
    }
}