package priv.liten.hbg5_widget.impl.tab

import android.view.View
import priv.liten.hbg5_widget.bin.fragment.HBG5Fragment
import priv.liten.hbg5_widget.impl.base.HBG5CheckImpl
import priv.liten.hbg5_widget.impl.fragment.HBG5FragmentImpl

/** 標籤按鈕介面 */
interface HBG5FragmentTabImpl: HBG5CheckImpl {

    val view: View get() = this as View

    fun v5LoadData(tab: HBG5Fragment.Tab)
}