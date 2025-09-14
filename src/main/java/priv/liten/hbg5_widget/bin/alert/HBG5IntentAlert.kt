package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_widget.bin.layout.HBG5ScrollLayout
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.TextAlignmentHorizontal
import priv.liten.hbg5_widget.impl.alert.HBG5IntentAlertImpl

/** 意向彈出窗 (詢問用戶看完訊息後，進行(取消/確認)選擇) */
class HBG5IntentAlert: HBG5BaseAlert, HBG5IntentAlertImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {
        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_intent, this)
        }

        // Event
        run {
            // onConfirm
            confirmButton.v5RegisterClick { v5Confirm() }
            // onCancel
            cancelButton.v5RegisterClick { v5Cancel() }
        }
    }

    // MARK:- ====================== View
    private val titleView: HBG5TextView by lazy { findViewById(R.id.text_title) }
    private val contentLayout: HBG5ScrollLayout by lazy { findViewById(R.id.layout_content) }
    private val contentView: HBG5TextView by lazy { contentLayout.findViewById(R.id.text_content) }
    private val confirmButton: HBG5Button by lazy { findViewById(R.id.button_confirm) }
    private val cancelButton: HBG5Button by lazy { findViewById(R.id.button_cancel) }


    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
    }
    override fun v5OnHide() {
        super.v5OnHide()
        onYesListener = null
        onNoListener = null
    }

    private var onYesListener: (() -> Unit)? = null
    override fun v5RegisterYes(listener: (() -> Unit)?) {
        onYesListener = listener
    }
    private var onNoListener: (() -> Unit)? = null
    override fun v5RegisterNo(listener: (() -> Unit)?) {
        onNoListener = listener
    }


    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {

        super.v5LoadRequest(request)

        val dataRequest = request as? DataRequest ?: return

        titleView.v5Text = dataRequest.title
        contentLayout.v5Visibility = when(dataRequest.content.isEmpty()) {
            true -> HBG5WidgetConfig.Attrs.Visibility.Gone
            else -> HBG5WidgetConfig.Attrs.Visibility.Visible
        }
        contentView.v5Text = dataRequest.content
        contentView.v5TextAlignmentVertical = HBG5WidgetConfig.Attrs.TextAlignmentVertical.Center
        contentView.v5TextAlignmentHorizontal = dataRequest.contentAlignment
    }

    override fun v5Cancel() {
        onNoListener?.let { it() }
        super.v5Cancel()
    }

    override fun v5Confirm() {
        onYesListener?.let { it() }
        super.v5Confirm()
    }


    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {

        var title: String = ""

        var content: String = ""

        var contentAlignment: TextAlignmentHorizontal = TextAlignmentHorizontal.Start
    }
}