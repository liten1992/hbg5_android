package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_widget.bin.list.HBG5ListView
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.impl.alert.HBG5OptionAlertImpl

open class HBG5OptionAlert: HBG5BaseAlert, HBG5OptionAlertImpl {

    // MARK:- ====================== Constructor
    constructor(
        context: Context,
        @LayoutRes layout: Int = R.layout.hbg5_widget_alert_option): super(context = context) {

        // Root
        run {
            inflate(context, layout, this)
        }

        // Data
        run {
            listView.v5Adapter = adapter
        }
    }


    // MARK:- ====================== View
    private val titleView: HBG5TextView by lazy { findViewById(R.id.text_title) }

    private val listView: HBG5ListView by lazy { findViewById(R.id.list_content) }


    // MARK:- ====================== Data
    private val adapter = HBG5ListView.Adapter<String> (
        v5CreateHolder = { adapter, listView, index -> return@Adapter ItemHolder(listView.context) },
        v5InitHolder = { adapter, holder ->
            (holder as ItemHolder).v5RegisterClick {
                selectedIndex = holder.bindingAdapterPosition
                v5Confirm()
            }
        }
    )
    private var selectedIndex: Int = -1


    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
        listView.v5ScrollStart() // todo hbg
    }
    override fun v5OnHide() {
        super.v5OnHide()
        adapter.v5Clear()
        onYesListener = null
        onNoListener = null
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

        val dataRequest = request as? DataRequest ?: return

        titleView.v5Text = dataRequest.title
        adapter.v5List = dataRequest.optionList
    }

    override fun v5Cancel() {
        onNoListener?.let { it() }
        super.v5Cancel()
    }

    override fun v5Confirm() {
        onYesListener?.let { it(selectedIndex) }
        super.v5Confirm()
    }


    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {

        var title = ""

        var optionList = listOf<String>()
    }

    open class ItemHolder: HBG5ListView.Holder {
        constructor(context: Context): super(View.inflate(context, R.layout.hbg5_widget_item_alert_enum, null))

        override fun v5LoadData(data: Any?) {
            (data as? String)?.let { _data ->
                (itemView as? HBG5Button)?.v5Text = _data
            }
        }
    }
}