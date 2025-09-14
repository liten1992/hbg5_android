package priv.liten.hbg5_widget.impl.alert

interface HBG5IntentAlertImpl {
    fun v5RegisterYes(listener: (()->Unit)?)
    fun v5RegisterNo(listener: (()->Unit)?)
}