package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.widget.ProgressBar
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.alert.HBG5LoadingAlertImpl
/**讀取效果彈窗*/
class HBG5LoadingAlert: HBG5BaseAlert, HBG5LoadingAlertImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {
        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_loading, this)
        }

        // Event
        run {

        }
    }

    // MARK:- ====================== View
    private val uiMessage: HBG5TextView by lazy { findViewById(R.id.text_message) }
    private val uiProgress: ProgressBar by lazy { findViewById(R.id.progressBar_Spinner) }

    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
        // 開始讀取動畫
        uiProgress.visibility = VISIBLE
    }

    override fun v5OnHide() {
        super.v5OnHide()
        // 結束讀取動畫
        uiProgress.visibility = GONE
    }


    // MARK:- ====================== Method
    override fun v5Cancel() { }

    override fun v5Confirm() { }

    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {
        (request as? DataRequest).also { dataRequest ->
            uiMessage.v5Text = dataRequest?.message
            uiMessage.v5Visibility = when(dataRequest?.message?.isNotEmpty() ?: false) {
                true -> HBG5WidgetConfig.Attrs.Visibility.Visible
                false -> HBG5WidgetConfig.Attrs.Visibility.Gone
            }
        }
    }

    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {
        var message: String = ""
    }
}