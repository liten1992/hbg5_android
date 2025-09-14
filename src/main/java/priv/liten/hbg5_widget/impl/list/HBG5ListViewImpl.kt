package priv.liten.hbg5_widget.impl.list

import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.StyleableRes
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetBoolean
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetDimension
import priv.liten.hbg5_extension.hbg5.v5GetDimensionPixelSize
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_extension.hbg5.v5GetString
import priv.liten.hbg5_extension.spToPx
import priv.liten.hbg5_widget.bin.list.HBG5ListView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.ListLayoutType
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.ListOrientation
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.ListGravity
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl

interface HBG5ListViewImpl:
    HBG5ViewImpl,
    HBG5BackgroundImpl {

    // ================ 屬性設置
    /** 列表排列方式 線性(linear)、網格(grid) */
    var v5ListLayoutType: ListLayoutType
        get() = v5GetTag(R.id.attr_list_layout_type) ?: ListLayoutType.Linear
        set(value) {
            v5SetTag(R.id.attr_list_layout_type, value)
            refreshLayoutManager()
        }
    /** 列表單行最大數量 僅網格(grid)作用 */
    var v5ListSpanCount: Int
        get() = v5GetTag(R.id.attr_list_span_count) ?: 3
        set(value) {
            v5SetTag(R.id.attr_list_span_count, value)
            refreshLayoutManager()
        }
    /** 列表滾動方向 垂直(vertical) 水平(horizontal) */
    var v5ListOrientation: ListOrientation
        get() = v5GetTag(R.id.attr_list_orientation) ?: ListOrientation.VERTICAL
        set(value) {
            v5SetTag(R.id.attr_list_orientation, value)
            refreshLayoutManager()
        }

    /** 開頭分隔線寬度 */
    var v5ListDividerStartSize: Int
        get() = v5GetTag(R.id.attr_div_size_start) ?: 0
        set(value) {
            v5SetTag(R.id.attr_div_size_start, value)
            refreshDivider()
        }
    /** 內容分隔線寬度 */
    var v5ListDividerMidSize: Int
        get() = v5GetTag(R.id.attr_div_size_mid) ?: 0
        set(value) {
            v5SetTag(R.id.attr_div_size_mid, value)
            refreshDivider()
        }
    /** 結尾分隔線寬度 */
    var v5ListDividerEndSize: Int
        get() = v5GetTag(R.id.attr_div_size_end) ?: 0
        set(value) {
            v5SetTag(R.id.attr_div_size_end, value)
            refreshDivider()
        }
    /** 列表對齊位置 起始、置中、結尾 */
    var v5ListDividerGravity: ListGravity
        get() = v5GetTag(R.id.attr_div_gravity) ?: ListGravity.Start
        set(value) {
            v5SetTag(R.id.attr_div_gravity, value)
            refreshDivider()
        }
    /** 分隔線顏色 */
    var v5ListDividerColor: Int
        get() = v5GetTag(R.id.attr_div_color) ?: Color.TRANSPARENT
        set(value) {
            v5SetTag(R.id.attr_div_color, value)
            refreshDivider()
        }

    /** 無資料提示字 */
    var v5ListHint: String
        get() = v5GetTag(R.id.attr_hint) ?: ""
        set(value) {
            v5SetTag(R.id.attr_hint, value)
            refreshHint()
        }
    /** 無資料提示字顏色 */
    var v5ListHintColor: Int
        get() = v5GetTag(R.id.attr_hint_color) ?: Color.GRAY
        set(value) {
            v5SetTag(R.id.attr_hint_color, value)
            refreshHint()
        }
    /** 無資料提示字尺寸 */
    var v5ListHintSize: Float
        get() = v5GetTag(R.id.attr_hint_size) ?: 12f.spToPx()
        set(value) {
            v5SetTag(R.id.attr_hint_size, value)
            refreshHint()
        }

    /** 項目更新動畫啟用 */
    var v5ListAnimationEnable: Boolean
        get() = (this as? HBG5ListView)?.itemAnimator != null
        set(value) { (this as? HBG5ListView)?.itemAnimator = if(value) DefaultItemAnimator() else null }
    /** 配適器 */
    var v5Adapter: HBG5ListView.Adapter<*>?
        get() = (this as? HBG5ListView)?.adapter as? HBG5ListView.Adapter<*>
        set(value) { (this as? HBG5ListView)?.adapter = value }

    // ================ Event
    /** 設置 滾動列表至底部監聽 */
    fun v5RegisterScrollEnd(closure: (() -> Unit)?) {
        v5SetTag(R.id.attr_callback_scroll_end, when(closure) {
            null -> null
            else -> Runnable { closure() }
        })
    }
    /** 設置 刷新列表監聽(需要外層嵌套SwipeRefreshLayout才會生效) */
    fun v5RegisterRefresh(closure: (() -> Unit)?) {
        if(this !is HBG5ListView) { return }
        (this.parent as? SwipeRefreshLayout)?.let { refreshLayout ->
            refreshLayout.setOnRefreshListener(closure)
            refreshLayout.isRefreshing = false
        }
    }
    /** 設置 資料交換監聽 @param closure(From, To) */
    fun v5RegisterSwap(closure: ((Int, Int) -> Unit)?) {
        v5SetTag(R.id.attr_callback_swap, when(closure) {
            null -> null
            else -> object: SwapCallback { override fun run(from: Int, to: Int) { closure(from, to) } }
        })
    }

    interface SwapCallback {
        fun run(from: Int, to: Int)
    }

    // ================ Method
    /** 滾動列表至頂部 */
    fun v5ScrollStart() {
        if(this is HBG5ListView) {
            val adapter = adapter ?: return
            val size = adapter.itemCount
            if (size == 0) {
                return
            }

            scrollToPosition(0)
        }
    }
    /** 滾動列表至底部 */
    fun v5ScrollEnd() {
        if(this is HBG5ListView) {
            val adapter = adapter ?: return
            val size = adapter.itemCount
            if (size == 0) {
                return
            }
            scrollToPosition(size - 1)
        }
    }
    /** 更新XML */
    fun buildListViewByAttr(
        typedArray: TypedArray,
        @StyleableRes listLayoutType: Int? = null,
        @StyleableRes listSpanCount: Int? = null,
        @StyleableRes listOrientation: Int? = null,
        @StyleableRes listDividerStartSize: Int? = null,
        @StyleableRes listDividerMidSize: Int? = null,
        @StyleableRes listDividerEndSize: Int? = null,
        @StyleableRes listDividerGravity: Int? = null,
        @StyleableRes listDividerColor: Int? = null,
        @StyleableRes listHint: Int? = null,
        @StyleableRes listHintColor: Int? = null,
        @StyleableRes listHintSize: Int? = null,
        @StyleableRes listAnimationEnable: Int? = null,
        @StyleableRes listMaxHeight: Int? = null
    ) {
        val mid = listDividerMidSize?.let { typedArray.v5GetDimensionPixelSize(it) } ?: 0
        val start = listDividerStartSize?.let { typedArray.v5GetDimensionPixelSize(it) } ?: 0
        val end = listDividerEndSize?.let { typedArray.v5GetDimensionPixelSize(it) } ?: 0

        v5ListLayoutType = listLayoutType?.let { ListLayoutType.fromAttr(typedArray.v5GetInt(it)) } ?: ListLayoutType.Linear
        v5ListSpanCount = listSpanCount?.let { typedArray.v5GetInt(it) } ?: 3
        v5ListOrientation = listOrientation?.let { ListOrientation.fromAttr(typedArray.v5GetInt(it)) } ?: ListOrientation.VERTICAL
        v5ListDividerStartSize = listDividerStartSize?.let { typedArray.v5GetDimensionPixelSize(it) } ?: 0
        v5ListDividerMidSize = listDividerMidSize?.let { typedArray.v5GetDimensionPixelSize(it) } ?: 0
        v5ListDividerEndSize = listDividerEndSize?.let { typedArray.v5GetDimensionPixelSize(it) } ?: 0
        v5ListDividerGravity = listDividerGravity?.let { ListGravity.fromAttr(typedArray.v5GetInt(it)) } ?: ListGravity.Start
        v5ListDividerColor = listDividerColor?.let { typedArray.v5GetColor(it) } ?: Color.TRANSPARENT
        v5ListHint = listHint?.let { typedArray.v5GetString(it) } ?: ""
        v5ListHintColor = listHintColor?.let { typedArray.v5GetColor(it) } ?: Color.GRAY
        v5ListHintSize = listHintSize?.let { typedArray.v5GetDimension(it) } ?: 12f.spToPx()
        v5ListAnimationEnable = listAnimationEnable?.let { typedArray.v5GetBoolean(it) } ?: true
        (this as? HBG5ListView)?.let { view ->
            view.maxHeight = listMaxHeight?.let { typedArray.v5GetDimensionPixelSize(it) } ?: view.maxHeight
        }
    }

    /** 更新 */
    fun refreshListView() {
        refreshLayoutManager()
        refreshDivider()
        refreshHint()
    }

    private fun refreshLayoutManager() {
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }
        if(this is HBG5ListView) {
            // listLayoutType
            // listSpanCount
            // listOrientation
            layoutManager = when(v5ListLayoutType) {
                ListLayoutType.Grid -> GridLayoutManager(
                    context,
                    v5ListSpanCount,
                    v5ListOrientation.value,
                    false)
                else -> LinearLayoutManager(
                    context,
                    v5ListOrientation.value,
                    false)
            }.also { manager -> manager.orientation = v5ListOrientation.value }
        }
    }
    private fun refreshDivider() {
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }
        if(this is HBG5ListView) {
            // listDividerStartSize
            // listDividerMidSize
            // listDividerEndSize
            // listDividerGravity
            // listDividerColor
            listDivider.startSize = v5ListDividerStartSize
            listDivider.midSize = v5ListDividerMidSize
            listDivider.endSize = v5ListDividerEndSize
            listDivider.gravity = v5ListDividerGravity
            listDivider.color = v5ListDividerColor
            invalidateItemDecorations()
        }
    }
    private fun refreshHint() {
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }
        if(this is HBG5ListView) {
            // listHint
            // listHintColor
            // listHintSize
            listHintPaint.color = v5ListHintColor
            listHintPaint.textSize = v5ListHintSize
            invalidate()
        }
    }
}