package priv.liten.hbg5_widget.impl.alert

interface HBG5CheckOptionAlertImpl {
    fun v5RegisterYes(listener: ((List<Int>) -> Unit)?)
    fun v5RegisterNo(listener: (() -> Unit)?)
}