package priv.liten.hbg5_widget.impl.list

import androidx.recyclerview.widget.RecyclerView
import priv.liten.hbg5_widget.bin.list.HBG5ListView

interface HBG5ListViewAdapterImpl<TItem> {


    /** 依據資料判斷使用的 Holder 類型  */
    fun v5GetHolderType(adapter: HBG5ListView.Adapter<TItem>, index: Int) : Int
    /** 建　立 Holder */
    fun v5CreateHolder(adapter: HBG5ListView.Adapter<TItem>, parent: RecyclerView, type: Int) : HBG5ListView.Holder
    /** 初始化 Holder */
    fun v5InitHolder(adapter: HBG5ListView.Adapter<TItem>, holder: HBG5ListView.Holder)
    /** 載　入 Holder */
    fun v5BindHolder(adapter: HBG5ListView.Adapter<TItem>, holder: HBG5ListView.Holder, index: Int)

    /** 資料長度 */
    val v5Count: Int
    /** 資料源 get:複本 */
    var v5List:List<TItem>
    /** 查詢索引 @return no found: -1 */
    fun v5SearchIndex(item:TItem) : Int
    /** 查詢資料項目 */
    fun v5Search(index:Int) : TItem?
    /** 新增資料 */
    fun v5Add(index:Int? = null, value:TItem)
    /** 新增資料 @param index:null start */
    fun v5Add(index:Int? = null, vararg values:TItem)
    /** 新增資料 @param index:null start */
    fun v5Add(index:Int? = null, list:List<TItem>)
    /** 新增或取代資料 */
    fun v5AddOrUpdate(item:TItem)
    /** 移除資料 */
    fun v5Delete(vararg values: TItem)
    /** 移除資料 */
    fun v5Delete(values:List<TItem>)
    /** 移除資料 */
    fun v5DeleteWithIndex(index:Int, count:Int = 1)
    /** 更新資料 */
    fun v5UpdateAll()
    /** 更新資料 */
    fun v5Update(vararg values:TItem)
    /** 更新資料 */
    fun v5Update(values:List<TItem>)
    /** 更新資料 */
    fun v5UpdateWithIndex(index:Int)
    /** 取代資料 */
    fun v5Replace(index:Int, item:TItem)
    /** 取代資料 */
    fun v5ReplaceWithRange(index:Int, count:Int, values: List<TItem>)
    /** 全部清除 */
    fun v5Clear()
    /** 排序 */
    fun v5Sort(sort: (TItem,TItem) -> Int)
    /** 交換 */
    fun v5Swap(from: Int, to: Int)
    /** 是否為空 todo hbg */
    fun v5IsEmpty(): Boolean = v5Count <= 0
}