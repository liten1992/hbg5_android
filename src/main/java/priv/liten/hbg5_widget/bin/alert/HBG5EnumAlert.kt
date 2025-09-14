package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.graphics.Rect
import android.view.Gravity
import android.view.View
import priv.liten.hbg.R
import priv.liten.hbg5_extension.getLocationInFrame
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_extension.withIn
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_widget.bin.layout.HBG5LinearLayout
import priv.liten.hbg5_widget.bin.list.HBG5ListView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.alert.HBG5OptionAlertImpl

open class HBG5EnumAlert: HBG5BaseAlert, HBG5OptionAlertImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {

        // Root
        run {
            inflate(context, getRootLayout(), this)
        }

        // Data
        run {
            listView.v5Adapter = adapter
        }
    }

    // MARK:- ====================== View
    private val parentView: HBG5LinearLayout by lazy { findViewById(R.id.alert_parent) }

    private val listView: HBG5ListView by lazy { findViewById(R.id.list_content) }


    // MARK:- ====================== Data
    private var dataRequest: DataRequest? = null
    private var selectedIndex: Int = -1
    private val adapter = HBG5ListView.Adapter<String> (
        v5CreateHolder = { adapter, listView, index -> return@Adapter ItemHolder(listView.context, getItemLayout()) },
        v5InitHolder = { adapter, holder -> (holder as ItemHolder).v5RegisterClick {
            selectedIndex = holder.bindingAdapterPosition
            v5Confirm()
        } },
        v5BindHolder = { adapter, holder, index ->
            holder.v5LoadData(adapter.v5Search(index))
            when(holder) {
                is ItemHolder -> {
                    holder.uiText.v5TextAlignmentHorizontal = dataRequest?.alignment ?: HBG5WidgetConfig.Attrs.TextAlignmentHorizontal.Center
                }
            }
        }
    )

    var isFillHorizontal = false
    open fun getRootLayout(): Int { return R.layout.hbg5_widget_alert_enum }
    open fun getItemLayout(): Int { return R.layout.hbg5_widget_item_alert_enum }


    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
    }
    override fun v5OnHide() {
        super.v5OnHide()
        dataRequest = null
        adapter.v5Clear()
        onYesListener = null
        onNoListener = null
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refreshAlertLocation()
    }

    private var onYesListener: ((Int) -> Unit)? = null
    override fun v5RegisterYes(listener: ((Int) -> Unit)?) {
        onYesListener = listener
    }

    private var onNoListener: (() -> Unit)? = null
    override fun v5RegisterNo(listener: (() -> Unit)?) {
        onNoListener = listener
    }


    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {
        super.v5LoadRequest(request)

        dataRequest = request as? DataRequest
        dataRequest?.also { dataRequest ->
            adapter.v5List = dataRequest.optionList
            if(adapter.itemCount > 0) {
                listView.smoothScrollToPosition(0)
            }
            refreshAlertLocation()
        }
    }

    override fun v5Cancel() {
        onNoListener?.let { it() }
        super.v5Cancel()
    }

    override fun v5Confirm() {
        onYesListener?.let { it(selectedIndex) }
        super.v5Confirm()
    }

    private fun refreshAlertLocation() {

        // 設置出現位置
        dataRequest?.fromBounds
            ?.let { bounds ->

                val screenWidth = width
                val screenHeight = height
                if(screenWidth <= 0 || screenHeight <= 0) { return }

                val parentWidth = parentView.layoutParams.width

                val left =
                    if(!isFillHorizontal) (bounds.centerX() - parentWidth.shr(1)).withIn(
                        minInclude = 0,
                        maxInclude = screenWidth - parentWidth)
                    else 0

                // 呼叫原件位於下方 需要向上展開
                if(bounds.centerY() > screenHeight.shr(1)) {
                    parentView.layoutParams = (parentView.layoutParams as LayoutParams).apply {
                        gravity = Gravity.START.or(Gravity.BOTTOM)
                        setMargins(left, 0, 0, screenHeight - bounds.top)
                    }
                }
                // 呼叫原件位於上方 需要向下展開
                else {
                    parentView.layoutParams = (parentView.layoutParams as LayoutParams).apply {
                        gravity = Gravity.START.or(Gravity.TOP)
                        setMargins(left, bounds.bottom - 1, 0, 0)
                    }
                }
            }
            ?:run {
                // 未設置讀取原件位置 置中
                parentView.layoutParams = (parentView.layoutParams as LayoutParams).apply {
                    setMargins(0, 0, 0, 0)
                    gravity = Gravity.CENTER
                }
            }
    }

    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {

        var optionList = listOf<String>()
        /**呼叫視窗的元件位置*/
        var fromBounds : Rect? = null
        /**呼叫視窗的元件位置*/
        var fromView : View?
            get() = null
            set(value) {
                fromBounds =
                    if(value == null) null
                    else Rect().apply {
                        val location = intArrayOf(0, 0).apply { value.getLocationInFrame(this) }
                        set(location[0], location[1], location[0] + value.width, location[1] + value.height)
                    }
            }
        /**文字對齊*/
        var alignment : HBG5WidgetConfig.Attrs.TextAlignmentHorizontal = HBG5WidgetConfig.Attrs.TextAlignmentHorizontal.Center
    }

    open class ItemHolder: HBG5ListView.Holder {
        constructor(context: Context, layout: Int): super(View.inflate(context, layout, null))

        val uiText: HBG5Button by lazy { itemView as HBG5Button }

        override fun v5LoadData(data: Any?) {
            when(data) {
                is String -> uiText.v5Text = data
            }
        }
    }
}