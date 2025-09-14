package priv.liten.hbg5_widget.impl.alert

interface HBG5TextInputAlertImpl {
    fun v5RegisterYes(listener: ((String)->Unit)?)
    fun v5RegisterNo(listener: (()->Unit)?)
}