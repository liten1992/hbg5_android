package priv.liten.hbg5_widget.impl.layout

import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl

interface HBG5ScrollLayoutImpl:
    HBG5ViewImpl,
    HBG5BackgroundImpl {

    /** 更新 */
    fun refreshScrollLayout() { }
}