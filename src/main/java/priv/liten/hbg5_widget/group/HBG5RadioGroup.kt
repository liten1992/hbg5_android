package priv.liten.hbg5_widget.group

import android.util.Log
import android.view.ViewGroup
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.impl.base.HBG5CheckImpl

class HBG5RadioGroup {

    // MARK:- ====================== Constructor
    constructor(list: MutableList<HBG5CheckImpl>) {
        if(list.isEmpty()) { return  }
        itemList = list
    }

    constructor(vararg args: HBG5CheckImpl): this(args.toMutableList())

    constructor(group: ViewGroup?): this() {
        set(group)
    }


    // MARK:- ====================== Data
    /** 已選取項目  */
    var checkedItem: HBG5CheckImpl? = null
        set(value) {
            value
                ?.let {
                    if (itemList.contains(value)) {
                        field = value
                        field?.v5Checked = true
                    }
                }
                ?:run {
                    field = null
                }
        }
    /** 已選取項目的索引 -1:找不到 */
    val checkedIndex: Int
        get() {
            return _itemList.indexOf(checkedItem)
        }

    /** (type > List<HBG5CheckImpl>) 選取項目列表 */
    var itemList: MutableList<HBG5CheckImpl>
        get() {
            return _itemList.toMutableList()
        }
        set(value) {

            checkedItem = null

            // 註銷舊列表邏輯
            for(item in _itemList){
                item.v5RegisterCheckedChange(null)
            }
            _itemList.clear()
            _itemList.addAll(value)

            // 註冊新列表邏輯
            for(item in value) {
                if(item.v5Checked) {
                    if(checkedItem == null) {
                        checkedItem = item
                    }
                    else {
                        item.v5Checked = false
                    }
                }
                item.v5RegisterCheckedChange(onBindCheckedChangeCallback)
            }
        }
    private
    val _itemList = mutableListOf<HBG5CheckImpl>()


    // MARK:- ====================== Event
    private
    val onBindCheckedChangeCallback: ((HBG5CheckImpl, Boolean) -> Unit)? = { checkable, checked ->
        // 如為單選型態，將取消其他物件核取狀態
        if (checked) {
            if(checkedItem != checkable) {
                val oldCheckedItem = checkedItem
                checkedItem = checkable
                if(oldCheckedItem != null) {
                    oldCheckedItem.v5Checked = false
                }

                // 觸發選取物件更動
                onCheckedChangeCallback?.let { it(checkable, checked) }
            }
        }
        else if(checkedItem == checkable) {
            checkedItem = null
            onCheckedChangeCallback?.let { it(checkable, false) }
        }
    }

    private
    var onCheckedChangeCallback: ((HBG5CheckImpl, Boolean) -> Unit)? = null
    fun registerOnCheckedChange(callback: ((HBG5CheckImpl, Boolean) -> Unit)?) {
        onCheckedChangeCallback = callback
    }


    // MARK:- ====================== Method
    /** 查詢索引 -1:未找到 */
    fun indexOf(check: HBG5CheckImpl): Int {
        return itemList.indexOf(check)
    }

    /**新增勾選功能元件*/
    fun add(item: HBG5CheckImpl) {
        if (item.v5Checked) {
            if (checkedItem == null) {
                checkedItem = item
            }
        }

        item.v5RegisterCheckedChange(onBindCheckedChangeCallback)
        _itemList.add(item)
    }
    /**讀取容器中具勾選功能元件*/
    fun set(group: ViewGroup?) {

        val parent = group ?: return

        val result = mutableListOf<HBG5CheckImpl>()
        run {
            for(i in 0 until parent.childCount) {
                val child = parent.getChildAt(i) as? HBG5CheckImpl ?: continue
                result.add(child)
            }
        }
        itemList = result
    }

    /** 選取 */
    fun check(index: Int) {
        if(index >= 0 && index < itemList.size) {
            itemList[index].v5Checked = true
        }
        else {
            checkedItem?.v5Checked = false
        }
    }
    /** 取消選取 */
    fun uncheck() {
        checkedItem?.v5Checked = false
    }

    /** 清除 */
    fun clear() {
        check(-1)
        _itemList.forEach { item -> item.v5RegisterCheckedChange(null) }
        itemList = mutableListOf()
    }
}