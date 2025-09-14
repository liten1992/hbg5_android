package priv.liten.hbg5_widget.impl.alert

import priv.liten.hbg5_data.HBG5Date
import priv.liten.hbg5_data.HBG5Time

interface HBG5DateTimeAlertImpl: HBG5BaseAlertImpl {

    val v5Date: HBG5Date
    val v5Time: HBG5Time

    fun v5RegisterYes(closure: ((HBG5Date, HBG5Time) -> Unit)?)
    fun v5RegisterNo(closure: (() -> Unit)?)
}