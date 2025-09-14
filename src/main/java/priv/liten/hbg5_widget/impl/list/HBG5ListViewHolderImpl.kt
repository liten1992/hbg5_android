package priv.liten.hbg5_widget.impl.list

import androidx.recyclerview.widget.RecyclerView
import priv.liten.hbg5_widget.bin.list.HBG5ListView
import priv.liten.hbg5_widget.impl.base.HBG5CheckImpl
import priv.liten.hbg5_widget.impl.base.HBG5TagImpl

/** 考慮項目關聯器 選取監聽 由配適器自動實作 故無閉包版本 */
interface HBG5ListViewHolderImpl: HBG5CheckImpl, HBG5TagImpl {
    /** 設置項目點擊監聽 */
    fun v5RegisterClick(closure: ((HBG5ListView.Holder?) -> Unit)?) {

        (this as? RecyclerView.ViewHolder)?.let { holder ->

            val view = holder.itemView

            closure
                ?.let {
                    view.setOnClickListener { it(this as? HBG5ListView.Holder) }
                }
                ?:run {
                    view.setOnClickListener(null)
                }
        }
    }

    /** 設置項目長按監聽 */
    fun v5RegisterLongClick(closure: ((HBG5ListView.Holder?) -> Unit)?) {

        (this as? RecyclerView.ViewHolder)?.let { holder ->

            val view = holder.itemView

            closure
                ?.let {
                    view.isLongClickable = true
                    view.setOnLongClickListener {
                        it(this as? HBG5ListView.Holder)
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        view.isPressed = false

                        return@setOnLongClickListener true
                    }
                }
                ?:run {
                    view.setOnLongClickListener(null)
                }
        }
    }

    /** 載入 */
    fun v5LoadData(data: Any?)
}