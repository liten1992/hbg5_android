package priv.liten.hbg5_widget.impl.alert

interface HBG5OptionAlertImpl {

    fun v5RegisterYes(listener: ((Int)->Unit)?)
    fun v5RegisterNo(listener: (()->Unit)?)
}