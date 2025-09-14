package priv.liten.hbg5_widget.impl.list

interface HBG5ListViewRadioAdapterImpl<TItem>: HBG5ListViewAdapterImpl<TItem> {
    /** 選取的項目 */
    var v5CheckedItem: TItem?

    /** 設置選取監聽 */
    fun v5RegisterCheckedChange(closure: ((TItem?)->Unit)?)
}