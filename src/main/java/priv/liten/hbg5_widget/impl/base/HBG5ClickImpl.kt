package priv.liten.hbg5_widget.impl.base

import android.view.View

interface HBG5ClickImpl {

    /** 設置點擊監聽 */
    fun v5RegisterClick(closure: ((HBG5ViewImpl?) -> Unit)?) {
        (this as? View)?.let { view ->
            closure
                ?.let {
                    view.setOnClickListener { it(this as? HBG5ViewImpl) }
                    view.isClickable = true
                }
                ?:run {
                    view.setOnClickListener(null)
                    view.isClickable = false
                }
        }
    }
}