package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import androidx.core.view.isVisible
import priv.liten.hbg.R
import priv.liten.hbg5_extension.showKeyboard
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_widget.bin.text.HBG5TextEditor
import priv.liten.hbg5_widget.bin.text.HBG5TextInput
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.TextAlignmentHorizontal
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.TextInputType
import priv.liten.hbg5_widget.impl.alert.HBG5TextInputAlertImpl
import kotlin.math.max

/**文字輸入彈窗*/
class HBG5TextInputAlert: HBG5BaseAlert, HBG5TextInputAlertImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {
        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_text_input, this)
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
    private val textView: HBG5TextView by lazy { findViewById(R.id.text) }
    private val textInput: HBG5TextInput by lazy { findViewById(R.id.input_text) }
    // todo hbg5
    private val textEditor: HBG5TextEditor by lazy { findViewById(R.id.editor_text) }
    private val confirmButton: HBG5Button by lazy { findViewById(R.id.button_confirm) }
    private val cancelButton: HBG5Button by lazy { findViewById(R.id.button_cancel) }

    // MARK:- ====================== Event
    private var onYesListener: ((String) -> Unit)? = null
    override fun v5RegisterYes(listener: ((String) -> Unit)?) { onYesListener = listener }
    private var onNoListener: (() -> Unit)? = null
    override fun v5RegisterNo(listener: (() -> Unit)?) { onNoListener = listener }

    override fun v5OnShow() {
        super.v5OnShow()
        if(this.isVisible) {
            textInput.postDelayed({
                textInput.showKeyboard()
                val len = textInput.v5Text?.length ?: 0
                if(len > 0) {
                    textInput.select(len)
                }
            }, 100)
        }
    }

    override fun v5OnHide() {
        super.v5OnHide()
        onYesListener = null
        onNoListener = null
    }

    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {
        super.v5LoadRequest(request)

        val dataRequest = request as? DataRequest ?: return

        titleView.v5Text = dataRequest.title

        textView.v5Text = dataRequest.text
        textView.v5TextAlignmentHorizontal = dataRequest.textAlignment
        textView.v5Visibility =
            if(dataRequest.text.isEmpty()) HBG5WidgetConfig.Attrs.Visibility.Gone
            else HBG5WidgetConfig.Attrs.Visibility.Visible

        when {
            dataRequest.lines > 1 -> {
                val lines = dataRequest.lines
                textInput.v5Visibility = HBG5WidgetConfig.Attrs.Visibility.Gone
                textEditor.v5Visibility = HBG5WidgetConfig.Attrs.Visibility.Visible
                textEditor.v5Text = dataRequest.input
                textEditor.v5Hint = dataRequest.hint
                textEditor.minLines = lines
                textEditor.maxLines = lines
            }
            else -> {
                textEditor.v5Visibility = HBG5WidgetConfig.Attrs.Visibility.Gone
                textInput.v5Visibility = HBG5WidgetConfig.Attrs.Visibility.Visible
                textInput.v5Text = dataRequest.input
                textInput.v5TextAlignmentHorizontal = dataRequest.inputAlignment
                textInput.v5TextInputType = dataRequest.inputType
                textInput.v5TextInputSecurityEnable = dataRequest.inputSecurityEnable
                textInput.v5Hint = dataRequest.hint // todo hbg
            }
        }
    }

    override fun v5Cancel() {
        onNoListener?.let { it() }
        super.v5Cancel()
    }

    override fun v5Confirm() {
        onYesListener?.let {
            // todo hbg5
            if(textEditor.v5Visibility == HBG5WidgetConfig.Attrs.Visibility.Visible) {
                it(textEditor.v5Text?.toString() ?: "")
                return@let
            }
            if(textInput.v5Visibility == HBG5WidgetConfig.Attrs.Visibility.Visible) {
                it(textInput.v5Text?.toString() ?: "")
                return@let
            }
        }
        super.v5Confirm()
    }



    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {
        var title: String = ""

        var text: String = ""

        var textAlignment = TextAlignmentHorizontal.Start

        var input: String = ""

        var inputAlignment = TextAlignmentHorizontal.Start

        var hint: String = "" // todo hbg

        var inputType = TextInputType.Text

        var inputSecurityEnable: Boolean = false
        // 行數 1 才會判斷 是否為密碼以及數字輸入 // todo hbg
        var lines = 1
    }
}