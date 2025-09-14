package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_widget.bin.layout.HBG5ScrollLayout
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.TextAlignmentHorizontal
import priv.liten.hbg5_widget.impl.alert.HBG5TextAlertImpl

class HBG5TextAlert: HBG5BaseAlert, HBG5TextAlertImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {
        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_text, this)
        }

        // Event
        run {
            // onConfirm
            confirmButton.v5RegisterClick { v5Confirm() }
        }
    }

    // MARK:- ====================== View
    private val titleView: HBG5TextView by lazy { findViewById(R.id.text_title) }
    private val contentScroll: HBG5ScrollLayout by lazy { findViewById(R.id.scroll_content) }
    private val contentView: HBG5TextView by lazy { findViewById(R.id.text_content) }
    private val confirmButton: HBG5Button by lazy { findViewById(R.id.button_confirm) }



    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
    }
    override fun v5OnHide() {
        super.v5OnHide()
        onYesListener = null
    }

    private var onYesListener: (() -> Unit)? = null
    override fun v5RegisterYes(onYes: (() -> Unit)?) {
        onYesListener = onYes
    }


    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {

        super.v5LoadRequest(request)

        val dataRequest = request as? DataRequest ?: return

        titleView.v5Text = dataRequest.title
        contentView.v5Text = dataRequest.content
        contentView.v5TextAlignmentVertical = HBG5WidgetConfig.Attrs.TextAlignmentVertical.Center
        contentView.v5TextAlignmentHorizontal = dataRequest.contentAlignment
        contentScroll.v5Visibility =
            if(dataRequest.content.isEmpty()) HBG5WidgetConfig.Attrs.Visibility.Gone
            else HBG5WidgetConfig.Attrs.Visibility.Visible
    }

    override fun v5Cancel() { v5Confirm() }

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