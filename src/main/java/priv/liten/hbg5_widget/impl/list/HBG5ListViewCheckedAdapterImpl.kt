package priv.liten.hbg5_widget.impl.list

interface HBG5ListViewCheckedAdapterImpl<TItem>: HBG5ListViewAdapterImpl<TItem> {
    /** 選取的項目 強制觸發選取異動監聽 */
    var v5CheckedItemList: List<TItem>
    /** 設置選取監聽 */
    fun v5RegisterCheckedChange(closure: ((List<TItem>)->Unit)?)
    /** 是否為選取項目 */
    fun v5IsCheckedItem(item: TItem): Boolean
    /** 是否為選取項目 */
    fun v5IsCheckedItem(index: Int): Boolean
    /** 變換選取狀態 */
    fun v5Toggle(index: Int)
}