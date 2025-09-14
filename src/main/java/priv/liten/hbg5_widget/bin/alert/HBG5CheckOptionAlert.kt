package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.view.View
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.button.HBG5Button
import priv.liten.hbg5_widget.bin.image.HBG5ImageButton
import priv.liten.hbg5_widget.bin.list.HBG5ListView
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.impl.alert.HBG5CheckOptionAlertImpl

class HBG5CheckOptionAlert: HBG5BaseAlert, HBG5CheckOptionAlertImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {

        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_option_check, this)
        }

        // Data
        run {
            listView.v5Adapter = adapter
        }

        // Event
        run {
            doneButton.v5RegisterClick { v5Confirm() }

            cancelButton.v5RegisterClick { v5Cancel() }
        }
    }

    // MARK:- ====================== View
    private val titleView: HBG5TextView by lazy { findViewById(R.id.text_title) }

    private val listView: HBG5ListView by lazy { findViewById(R.id.list_content) }

    private val doneButton: HBG5Button by lazy { findViewById(R.id.button_confirm) }

    private val cancelButton: HBG5Button by lazy { findViewById(R.id.button_cancel) }


    // MARK:- ====================== Data
    private val adapter = HBG5ListView.CheckedAdapter<Item> (
        v5CreateHolder = { adapter, listView, index ->
            return@CheckedAdapter ItemHolder(listView.context)
        })


    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
    }
    override fun v5OnHide() {
        super.v5OnHide()
        adapter.v5Clear()
        onYesListener = null
        onNoListener = null
    }

    private var onYesListener: ((List<Int>) -> Unit)? = null
    override fun v5RegisterYes(listener: ((List<Int>) -> Unit)?) {
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
        adapter.v5CheckedItemList = dataRequest.optionList.filter { return@filter it.checked }.toMutableList()
    }

    override fun v5Cancel() {
        onNoListener?.let { it() }
        super.v5Cancel()
    }

    override fun v5Confirm() {
        onYesListener?.let {
            val checkedItems = adapter.v5CheckedItemList
            val indexList = checkedItems.map { return@map adapter.v5SearchIndex(it) }.sorted()
            it(indexList)
        }
        super.v5Confirm()
    }


    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {

        var title = ""

        var optionList = listOf<Item>()
    }

    class Item {

        constructor()

        constructor(checked: Boolean, name: String?) {
            this.checked = checked
            this.name = name ?: ""
        }

        var checked: Boolean = false
        var name: String = ""
    }

    class ItemHolder: HBG5ListView.Holder {
        constructor(context: Context): super(View.inflate(context, R.layout.hbg5_widget_item_alert_option_check, null)) {
            v5CheckBind = checkButton

            v5RegisterClick {
                checkButton.toggle()
            }
        }

        val textView: HBG5TextView by lazy { findViewById(R.id.text_name) }
        val checkButton: HBG5ImageButton by lazy { findViewById(R.id.button_check) }

        override fun v5LoadData(data: Any?) {
            (data as? Item)?.let { _data ->
                checkButton.v5Checked = _data.checked
                textView.v5Text = _data.name
            }
        }
    }
}