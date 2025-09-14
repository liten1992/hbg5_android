package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_widget.bin.layout.HBG5ScrollLayout
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.alert.HBG5IntentAlertImpl
import priv.liten.hbg5_widget.impl.alert.HBG5TextAlertImpl

// todo hbg

/** 意向彈出窗 自訂(選項 最多3個 相關功能) */
class HBG5IntentCustomAlert: HBG5BaseAlert {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {
        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_intent_custom, this)
        }

        // Event
        run {
            // OptionClick
            for(button in optionButtons) {
                button.v5RegisterClick { _ ->
                    val index = optionButtons.indexOf(button)
                    val option = alertRequest?.options?.get(index)
                    val callback = option?.callback

                    callback?.let { it(option.id) }
                    v5Hide()
                }
            }
        }
    }

    // MARK:- ====================== View
    private val titleView: HBG5TextView by lazy { findViewById(R.id.text_title) }
    private val contentLayout: HBG5ScrollLayout by lazy { findViewById(R.id.layout_content) }
    private val contentView: HBG5TextView by lazy { contentLayout.findViewById(R.id.text_content) }
    private val optionButtons: List<HBG5Button> by lazy { listOf(
        findViewById(R.id.button_1),
        findViewById(R.id.button_2),
        findViewById(R.id.button_3)
    ) }


    // MARK:- ====================== Data
    private var alertRequest: DataRequest? = null


    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
    }
    override fun v5OnHide() {
        super.v5OnHide()
        alertRequest = null
    }


    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {

        super.v5LoadRequest(request)

        val dataRequest = request as? DataRequest ?: return
        alertRequest = dataRequest

        titleView.v5Text = dataRequest.title
        contentLayout.v5Visibility = when(dataRequest.content.isEmpty()) {
            true -> HBG5WidgetConfig.Attrs.Visibility.Gone
            else -> HBG5WidgetConfig.Attrs.Visibility.Visible
        }
        contentView.v5Text = dataRequest.content
        contentView.v5TextAlignmentVertical = HBG5WidgetConfig.Attrs.TextAlignmentVertical.Center
        contentView.v5TextAlignmentHorizontal = dataRequest.contentAlignment

        val options = alertRequest?.options ?: emptyList()
        for((index, button) in optionButtons.withIndex()) {
            val option = options.getOrNull(index)
            button.v5Visibility =
                if(option == null) HBG5WidgetConfig.Attrs.Visibility.Gone
                else HBG5WidgetConfig.Attrs.Visibility.Visible
            button.v5Text = option?.name ?: "NULL"
        }
    }


    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {

        var title: String = ""

        var content: String = ""

        var contentAlignment: HBG5WidgetConfig.Attrs.TextAlignmentHorizontal = HBG5WidgetConfig.Attrs.TextAlignmentHorizontal.Start
        /**最多3個功能*/
        var options: List<Option> = emptyList()

        open class Option {
            constructor()

            constructor(id: Int? = null, name: String, callback: ((Int?) -> Unit)? = null) {
                this.id = id
                this.name = name
                this.callback = callback
            }

            var id: Int? = null

            var name: String? = "NULL"

            var callback: ((Int?) -> Unit)? = null
        }
    }
}