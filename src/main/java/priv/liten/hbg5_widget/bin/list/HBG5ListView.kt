package priv.liten.hbg5_widget.bin.list

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.*
import priv.liten.hbg5_extension.isWithIn
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_extension.spToPx
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.ListGravity
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.ListOrientation
import priv.liten.hbg5_widget.impl.base.HBG5CheckImpl
import priv.liten.hbg5_widget.impl.list.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max
import kotlin.math.min


/** 列表元件 */
class HBG5ListView: RecyclerView, HBG5ListViewImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr) {
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5ListView,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5ListView_v5Touchable,
                        visibility = R.styleable.HBG5ListView_v5Visibility,
                        padding = R.styleable.HBG5ListView_v5Padding,
                        paddingStart = R.styleable.HBG5ListView_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5ListView_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5ListView_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5ListView_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5ListView_v5BackgroundImage,
                        radius = R.styleable.HBG5ListView_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5ListView_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5ListView_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5ListView_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5ListView_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5ListView_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5ListView_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5ListView_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5ListView_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5ListView_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5ListView_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5ListView_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5ListView_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5ListView_v5BackgroundBorderColorUnable
                    )
                    // 元件
                    buildListViewByAttr(
                        typedArray = typedArray,
                        listLayoutType = R.styleable.HBG5ListView_v5ListLayoutType,
                        listSpanCount = R.styleable.HBG5ListView_v5ListSpanCount,
                        listOrientation = R.styleable.HBG5ListView_v5ListOrientation,
                        listDividerStartSize = R.styleable.HBG5ListView_v5ListDividerStartSize,
                        listDividerMidSize = R.styleable.HBG5ListView_v5ListDividerMidSize,
                        listDividerEndSize = R.styleable.HBG5ListView_v5ListDividerEndSize,
                        listDividerGravity = R.styleable.HBG5ListView_v5ListDividerGravity,
                        listDividerColor = R.styleable.HBG5ListView_v5ListDividerColor,
                        listHint = R.styleable.HBG5ListView_v5ListHint,
                        listHintColor = R.styleable.HBG5ListView_v5ListHintColor,
                        listHintSize = R.styleable.HBG5ListView_v5ListHintSize,
                        listAnimationEnable = R.styleable.HBG5ListView_v5ListAnimationEnable,
                        listMaxHeight = R.styleable.HBG5ListView_android_maxHeight
                    )
                },
                finish = {
                    refreshBackground()
                    refreshListView()
                }
            )
            addItemDecoration(listDivider)
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null)


    // MARK:- ====================== Data
    val listDivider = ListDivider()
    val listHintPaint: Paint = run {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.GRAY
        paint.textSize = 12f.spToPx()
        paint
    }

    var maxHeight = -1
        set(value) {
            if(field == value) { return }
            field = value
            requestLayout()
        }

    private val moveTouchHelperCallback: ItemTouchHelper by lazy {
        val callback = MoveTouchHelperCallback(this)
        return@lazy ItemTouchHelper(callback)
    }
    var swapEnable = false
        set(value) {
            if(field == value) { return }
            field = value
            if(field) {
                moveTouchHelperCallback.attachToRecyclerView(this)
            }
            else {
                moveTouchHelperCallback.attachToRecyclerView(null)
            }
        }

    override fun getAccessibilityClassName(): CharSequence? {
        return HBG5ListView::class.java.name
    }

    // MARK:- ====================== Event
    // Add maxHeight
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = MeasureSpec.getSize(heightMeasureSpec)
        when (val heightMode = MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY, MeasureSpec.UNSPECIFIED -> {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
            MeasureSpec.AT_MOST -> {
                if (maxHeight > 0) {
                    val minHeight = min(maxHeight, height)
                    super.onMeasure(
                        widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(minHeight, heightMode)
                    )
                } else {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                }
            }
        }
    }
    // Call onScrollEndListener
    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        val callback = v5GetTag<Runnable>(R.id.attr_callback_scroll_end) ?: return
        if (SCROLL_STATE_IDLE == state && !canScrollVertically(1)) {
            callback.run()
        }
    }

    // MARK:- ====================== Method
    override fun dispatchDraw(canvas: Canvas) {

        super.dispatchDraw(canvas)

        // 無文字不繪製
        if (v5ListHint.trim().isEmpty()) { return }
        // 透明不繪製
        if (v5ListHintColor and -0x1000000 == 0x00000000) { return }
        // 有物件數量
        val adapter = adapter ?: return
        if (adapter.itemCount > 0) { return }

        // 置中繪圖
        val width = width
        val height = height
        canvas.drawText(
            v5ListHint,
            ((width - listHintPaint.measureText(v5ListHint)).toInt() shr 1).toFloat(),
            ((height - listHintPaint.ascent()).toInt() shr 1).toFloat(),
            listHintPaint)
    }


    // MARK:- ====================== Class
    /** 列表UI與資料關聯 */
    open class Holder : ViewHolder, HBG5ListViewHolderImpl {

        // MARK:- ====================== Constructor
        constructor(view:View) : super(view)

        constructor(context: Context, @LayoutRes id: Int) : this(View.inflate(context, id, null))

        // MARK:- ====================== Data
        val tag = mutableMapOf<Int, Any?>()

        val context: Context get() { return itemView.context }


        // MARK:- ====================== Event


        // MARK:- ====================== Method
        override fun v5LoadData(data: Any?) {

        }

        fun <T: View?> findViewById(@IdRes id: Int): T {
            return itemView.findViewById(id)
        }

        fun getColor(@ColorRes id: Int): Int {
            return itemView.context.getColor(id)
        }

        fun getDrawable(@DrawableRes id: Int): Drawable? {
            return ContextCompat.getDrawable(context, id)
        }

        fun getString(@StringRes id: Int): String {
            return itemView.context.getString(id)
        }

        fun getString(@ArrayRes id: Int, index: Int): String? {
            return itemView.context.resources.getStringArray(id).getOrNull(index)
        }
    }

    /** 基礎配適器預計預設自動 bind | layout 僅必須實作 create 方案 */
    open class Adapter<TItem> :
        RecyclerView.Adapter<Holder>,
        HBG5ListViewAdapterImpl<TItem> {

        // MARK:- ====================== Constructor
        constructor(
            v5GetHolderType: (Adapter<TItem>, Int) -> Int = { _, _ ->
                0 },

            v5CreateHolder: (Adapter<TItem>, RecyclerView, Int) -> Holder = { _, parent, _ -> Holder(View(parent.context)) },

            v5InitHolder: (Adapter<TItem>, Holder) -> Unit = { _, _ -> },

            v5BindHolder: (Adapter<TItem>, Holder, Int) -> Unit = { adapter, holder, index ->
                holder.v5LoadData(adapter.v5Search(index))
            }) {

            onGetHolderTypeAction = v5GetHolderType
            onCreateHolderAction = v5CreateHolder
            onInitHolderAction = v5InitHolder
            onBindHolderAction = v5BindHolder
        }

        override fun getItemCount(): Int {
            return v5Count
        }
        override fun getItemViewType(position: Int): Int {
            return v5GetHolderType(this, position)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

            val listView = parent as RecyclerView

            val holder = v5CreateHolder(this, listView, viewType)
            holder.itemView.setTag(R.id.holder, WeakReference(holder))
            v5InitHolder(this, holder)

            if (holder.itemView.layoutParams == null) {
                (listView as? HBG5ListView)?.let {
                    holder.itemView.layoutParams = when(it.v5ListOrientation) {
                        ListOrientation.VERTICAL -> LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT
                        )
                        ListOrientation.HORIZONTAL -> LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.MATCH_PARENT
                        )
                    }
                }
            }

            return holder
        }
        override fun onBindViewHolder(holder: Holder, position: Int) {
            v5BindHolder(this, holder, position)
        }

        // MARK:- ====================== Data
        override val v5Count: Int
            get() { return mV5List.size }

        private var mV5List:MutableList<TItem> = mutableListOf()
        override var v5List:List<TItem>
            get() {
                return mV5List.toList()
            }
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                mV5List.clear()
                mV5List.addAll(value)
                notifyDataSetChanged()
            }

        private var owner: WeakReference<HBG5ListView>? = null

        // MARK:- ====================== Event
        private val onGetHolderTypeAction: (Adapter<TItem>, Int) -> Int
        private val onCreateHolderAction: (Adapter<TItem>, RecyclerView, Int) -> Holder
        private val onInitHolderAction: (Adapter<TItem>, Holder) -> Unit
        private val onBindHolderAction: (Adapter<TItem>, Holder, Int) -> Unit

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            owner = WeakReference(recyclerView as? HBG5ListView)
        }

        // MARK:- ====================== Method
        override fun v5GetHolderType(adapter: Adapter<TItem>, index: Int): Int {
            return onGetHolderTypeAction(adapter, index)
        }
        override fun v5CreateHolder(adapter: Adapter<TItem>, parent: RecyclerView, type: Int) : Holder {
            return onCreateHolderAction(adapter, parent, type)
        }
        override fun v5InitHolder(adapter: Adapter<TItem>, holder: Holder) {
            onInitHolderAction(adapter, holder)
        }
        override fun v5BindHolder(adapter: Adapter<TItem>, holder: Holder, index: Int) {
            onBindHolderAction(adapter, holder, index)
        }

        override fun v5SearchIndex(item: TItem) : Int {
            return mV5List.indexOf(item)
        }

        override fun v5Search(index:Int) : TItem? {
            return mV5List.getOrNull(index)
        }
        override fun v5Add(index: Int?, value: TItem) {
            v5Add(index, listOf(value))
        }
        override fun v5Add(index:Int?, vararg values: TItem) {

            if (values.isEmpty()) { return }

            this.v5Add(index, values.toList())
        }
        override fun v5Add(index:Int?, list: List<TItem>) {

            if (list.isEmpty()) { return }

            index?.let {
                if (0 > it || it > mV5List.size) { throw IndexOutOfBoundsException("HBG5ListAdapter v5Add") }

                mV5List.addAll(it, list)
                notifyItemRangeInserted(it, list.size)
                owner?.get()?.invalidateItemDecorations()
                return
            }

            val start = mV5List.size

            mV5List.addAll(list)

            notifyItemRangeInserted(start, list.size)

            owner?.get()?.invalidateItemDecorations()
        }
        override fun v5AddOrUpdate(item: TItem) {
            val index = v5SearchIndex(item)

            if(index == -1) {
                v5Add(null, item)
            }
            else {
                v5Update(item)
            }
        }

        override fun v5Delete(vararg values: TItem) {
            if (values.isEmpty()) { return }
            this.v5Delete(values.toList())
        }
        override fun v5Delete(values: List<TItem>) {

            val delIndexList = mutableListOf<Int>()

            for (item in values) {
                val delIndex = mV5List.indexOf(item)
                if (delIndex == -1) { continue }
                delIndexList.add(delIndex)
            }

            if(delIndexList.isEmpty()) { return }

            mV5List.removeAll(values)

            delIndexList.sortDescending()
            // 連號
            var isLink = true
            run {
                var start = delIndexList[0]
                for (index in delIndexList) {
                    if(start != index) {
                        isLink = false
                        break
                    }
                    start -= 1
                }
            }

            if (isLink) {
                notifyItemRangeRemoved(delIndexList.last(), delIndexList.size)
            }
            else {
                for (index in delIndexList) {
                    notifyItemRemoved(index)
                }
            }

            owner?.get()?.postDelayed({owner?.get()?.invalidateItemDecorations()}, 100)
        }
        override fun v5DeleteWithIndex(index: Int, count: Int) {
            // todo hbg
            if(count <= 0) { return }

            for(i in (index until index + count).reversed()) {
                if(0 <= i && i < mV5List.size) {
                    mV5List.removeAt(i)
                }
            }
            if(count > 1) {
                notifyItemRangeRemoved(index, count)
            }
            else {
                notifyItemRemoved(index)
            }

            owner?.get()?.postDelayed({owner?.get()?.invalidateItemDecorations()}, 100)
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun v5UpdateAll() {
            notifyDataSetChanged()
        }
        override fun v5Update(vararg values: TItem) {
            v5Update(values.toList())
        }
        override fun v5Update(values: List<TItem>) {

            for (item in values) {
                val index = v5SearchIndex(item)
                if (index != -1) {
                    mV5List[index] = item
                    notifyItemChanged(index)
                }
            }
        }
        override fun v5UpdateWithIndex(index:Int) {
            notifyItemChanged(index)
        }
        // todo hbg
        fun v5UpdateWithRange(index: Int, count: Int) {
            notifyItemRangeChanged(index, count)
        }

        override fun v5Replace(index:Int, item:TItem) {
            v5ReplaceWithRange(index, 1, mutableListOf(item))
        }
        @SuppressLint("NotifyDataSetChanged")
        override fun v5ReplaceWithRange(index:Int, count:Int, values: List<TItem>) {

            when(count) {
                0 -> { return }
                1 -> {
                    values.firstOrNull()
                        ?.let { item ->
                            mV5List[index] = item
                        }
                        ?:return

                    notifyItemChanged(index)
                }
                else -> {
                    val end = index+count-1

                    for(i in end downTo index) {
                        mV5List.removeAt(i)
                    }

                    mV5List.addAll(index, values)

                    notifyDataSetChanged()
                }
            }
        }
        @SuppressLint("NotifyDataSetChanged")
        override fun v5Clear() {
            mV5List.clear()
            notifyDataSetChanged()
        }

        override fun v5Sort(sort: (TItem, TItem) -> Int) {
            mV5List.sortWith(Comparator(sort))
        }

        override fun v5Swap(from: Int, to: Int) {
            if(from.isWithIn(0, mV5List.size-1) && to.isWithIn(0, mV5List.size-1)) {
                // Update Data
                Collections.swap(mV5List, from, to)

                // Update UI
                notifyItemMoved(from, to)

                // Event
                owner?.get()?.v5GetTag<HBG5ListViewImpl.SwapCallback>(R.id.attr_callback_swap)?.run(from, to)
            }
        }
    }

    /** 多選配適器預計預設自動 bind | layout 僅必須實作 create 方案 */
    open class CheckedAdapter<TItem> : Adapter<TItem>, HBG5ListViewCheckedAdapterImpl<TItem> {
        // MARK:- ====================== Constructor
        constructor(
            v5GetHolderType: (Adapter<TItem>, Int) -> Int = { _, _ -> 0 },
            v5CreateHolder: (Adapter<TItem>, RecyclerView, Int) -> Holder,
            v5InitHolder: (Adapter<TItem>, Holder) -> Unit = { _, _ ->  },
            v5BindHolder: (Adapter<TItem>, Holder, Int) -> Unit =
                { adapter, holder, index -> holder.v5LoadData(adapter.v5Search(index)) })

                : super(
            v5GetHolderType,
            v5CreateHolder,
            v5InitHolder,
            v5BindHolder)

        // MARK:- ====================== Data
        private var checkedLock = false

        private var _v5CheckedItemList = mutableListOf<TItem>()
        override var v5CheckedItemList: List<TItem>
            get() {
                return _v5CheckedItemList.toList()
            }
            @SuppressLint("NotifyDataSetChanged")
            set(value) {
                _v5CheckedItemList.clear()
                _v5CheckedItemList.addAll(value)
                v5UpdateAll()

                onCheckedChangeListener?.let { it(v5CheckedItemList) }
            }


        // MARK:- ====================== Event
        private var onCheckedChangeListener: ((List<TItem>)->Unit)? = null
        override fun v5RegisterCheckedChange(closure: ((List<TItem>) -> Unit)?) {
            onCheckedChangeListener = closure
        }

        // MARK:- ====================== Method
        override fun v5CreateHolder(
            adapter: Adapter<TItem>,
            parent: RecyclerView,
            type: Int): Holder {

            val holder = super.v5CreateHolder(adapter, parent, type)
            holder.v5RegisterCheckedChange { checkable, checked ->
                // 選取鎖定狀態
                if (checkedLock) {
                    return@v5RegisterCheckedChange
                }

                val checkHolder = (checkable as? ViewHolder) ?: return@v5RegisterCheckedChange

                val index = checkHolder.bindingAdapterPosition

                val item = v5Search(index) ?: return@v5RegisterCheckedChange

                if (checked) {
                    _v5CheckedItemList.add(item)
                }
                else {
                    _v5CheckedItemList.remove(item)
                }

                onCheckedChangeListener?.let { it(v5CheckedItemList) }
            }

            return holder
        }

        override fun v5BindHolder(adapter: Adapter<TItem>, holder: Holder, index: Int) {

            checkedLock = true

            super.v5BindHolder(adapter, holder, index)

            (holder as? HBG5CheckImpl)?.let { checkedHolder ->

                val checkedItem = v5Search((checkedHolder as ViewHolder).bindingAdapterPosition)

                checkedHolder.v5Checked = _v5CheckedItemList.contains(checkedItem)
            }

            checkedLock = false
        }

        override fun v5Delete(values: List<TItem>) {
            super.v5Delete(values)
            refreshV5CheckedItemList()
        }

        override fun v5ReplaceWithRange(index: Int, count: Int, values: List<TItem>) {
            super.v5ReplaceWithRange(index, count, values)
            refreshV5CheckedItemList()
        }

        override fun v5Update(values: List<TItem>) {
            super.v5Update(values)
            refreshV5CheckedItemList()
        }

        override fun v5IsCheckedItem(item: TItem): Boolean {
            return _v5CheckedItemList.contains(item)
        }

        override fun v5IsCheckedItem(index: Int): Boolean {
            val item = v5Search(index = index) ?: return false
            return v5IsCheckedItem(item = item)
        }

        override fun v5Toggle(index: Int) {
            val item = v5Search(index = index) ?: return
            val checkedIndex = _v5CheckedItemList.indexOf(item)
            if(checkedIndex == -1) {
                _v5CheckedItemList.add(item)
            }
            else {
                _v5CheckedItemList.removeAt(checkedIndex)
            }
            v5UpdateWithIndex(index = index)
        }

        override fun v5Clear() {
            super.v5Clear()
            _v5CheckedItemList = mutableListOf()
        }

        /** 刷新選取項目的引用是否存在 */
        private fun refreshV5CheckedItemList() {

            val newCheckedItemList = mutableListOf<TItem>()

            for (nowCheckedItem in _v5CheckedItemList) {
                v5Search(v5SearchIndex(nowCheckedItem))?.let { newCheckedItem ->
                    newCheckedItemList.add(newCheckedItem)
                }
            }

            if (newCheckedItemList.size == _v5CheckedItemList.size) {
                // 引用更新
                _v5CheckedItemList = newCheckedItemList
            }
            else {
                // 異動更新
                v5CheckedItemList = newCheckedItemList
            }
        }
    }

    /** 單選配適器預計預設自動 bind | layout 僅必須實作 create 方案 */
    open class RadioAdapter<TItem> : Adapter<TItem>, HBG5ListViewRadioAdapterImpl<TItem> {
        // MARK:- ====================== Constructor
        constructor(
            v5GetHolderType: (Adapter<TItem>, Int) -> Int = { _, _ -> 0 },
            v5CreateHolder: (Adapter<TItem>, RecyclerView, Int) -> Holder,
            v5InitHolder: (Adapter<TItem>, Holder) -> Unit = { _, _ ->  },
            v5BindHolder: (Adapter<TItem>, Holder, Int) -> Unit =
                { adapter, holder, index -> holder.v5LoadData(adapter.v5Search(index)) })
                : super(
            v5GetHolderType,
            v5CreateHolder,
            v5InitHolder,
            v5BindHolder)

        // MARK:- ====================== Data

        override var v5CheckedItem: TItem? = null
            set(value) {

                val oldItem = field
                val newItem =
                    if (value != null) { if(v5SearchIndex(value) == -1) { null } else { value } }
                    else { null }

                if (oldItem == null && newItem == null) { return }

                field = newItem

                if (oldItem != null && oldItem == newItem) { return }

                if (oldItem != null) {
                    v5Update(oldItem)
                }

                if (newItem != null) {
                    v5Update(newItem)
                }

                onCheckedChangeListener?.let { it(v5CheckedItem) }
            }

        private var checkedLock = false

        // MARK:- ====================== Event
        private var onCheckedChangeListener: ((TItem?) -> Unit)? = null
        override fun v5RegisterCheckedChange(closure: ((TItem?) -> Unit)?) {
            onCheckedChangeListener = closure
        }

        // MARK:- ====================== Method
        override fun v5CreateHolder(
            adapter: Adapter<TItem>,
            parent: RecyclerView,
            type: Int): Holder {

            val holder = super.v5CreateHolder(adapter, parent, type)

            (holder as? HBG5CheckImpl)?.v5RegisterCheckedChange { checkable, checked ->
                // 選取鎖定狀態
                if(checkedLock) { return@v5RegisterCheckedChange }

                val checkHolder = (checkable as? ViewHolder) ?: return@v5RegisterCheckedChange

                val item = v5Search(checkHolder.bindingAdapterPosition) ?: return@v5RegisterCheckedChange

                if (checked) {
                    v5CheckedItem = item
                }
            }

            return holder
        }

        override fun v5BindHolder(adapter: Adapter<TItem>, holder: Holder, index: Int) {

            checkedLock = true

            super.v5BindHolder(adapter, holder, index)

            (holder as? HBG5CheckImpl)?.let { checkedHolder ->

                val checkedItem = v5Search((checkedHolder as ViewHolder).bindingAdapterPosition)

                checkedHolder.v5Checked = v5CheckedItem == checkedItem
            }

            checkedLock = false
        }

        override fun v5Delete(values: List<TItem>) {
            super.v5Delete(values)
            refreshV5CheckedItem()
        }

        override fun v5Update(values: List<TItem>) {
            super.v5Update(values)
            refreshV5CheckedItem()
        }

        override fun v5ReplaceWithRange(index: Int, count: Int, values: List<TItem>) {
            super.v5ReplaceWithRange(index, count, values)
            refreshV5CheckedItem()
        }

        /** 刷新選取項目的引用是否存在 */
        private fun refreshV5CheckedItem() {
            v5CheckedItem?.let { checkedItem ->
                v5CheckedItem = v5Search(v5SearchIndex(checkedItem))
            }
        }
    }

    /** 分隔線實作邏輯 */
    class ListDivider: ItemDecoration {

        constructor() {
        }

        var startSize = 0
        var midSize = 0
        var endSize = 0
        var gravity = ListGravity.Start
        var color: Int
            get() { return paint.color }
            set(value) { paint.color = value }

        private val enable: Boolean
            get() {
                return startSize > 0 || midSize > 0 || endSize > 0
            }

//        private val center: Boolean
//            get() {
//                return when(gravity) {
//                    ListGravity.Center -> { true }
//                    else -> { false }
//                }
//            }
        private val paint: Paint = run {
            val paint = Paint()
            paint.isAntiAlias = false
            paint.color = Color.TRANSPARENT
            paint
        }

        override fun onDraw(
            c: Canvas,
            parent: RecyclerView,
            state: State) {
            super.onDraw(c, parent, state)
            val color = paint.color
            val adapter = parent.adapter
            val adapterCount = adapter?.itemCount ?: 0

            // 繪製色彩透明，略過
            if (color and -0xf000000 == 0x00000000 || !enable) {
                return
            }

            // 沒有格線寬度，中止
            if (midSize <= 0) {
                return
            }

            // 沒有內容
            if (adapterCount <= 0) {
                return
            }
            val childCount = parent.childCount
            val pLeft = parent.paddingLeft
            val pTop = parent.paddingTop
            val pRight = parent.width - parent.paddingRight
            val pBottom = parent.height - parent.paddingBottom
            val offset = midSize - midSize shr 1
            val layoutManager = parent.layoutManager

            // Grid
            if (layoutManager is GridLayoutManager) {

                // 進行繪製
                val spanCount = layoutManager.spanCount
                val endPosition = spanCount - 1
                val endFirstPosition =
                    ((adapterCount shl 1) - 1) / (spanCount shl 1) * spanCount
                when (layoutManager.orientation) {
                    HORIZONTAL -> {
                        var i = 0
                        while (i < childCount) {
                            val child = parent.getChildAt(i)
                            val position =
                                (child.layoutParams as LayoutParams).viewAdapterPosition
                            val endRow = position >= endFirstPosition

                            // 水平線繪製
                            if (position % spanCount != endPosition) {
                                val boundBottom = parent.height - parent.paddingBottom
                                val drawTop = child.bottom + offset
                                var drawBottom = drawTop + midSize
                                if (drawBottom > boundBottom) {
                                    drawBottom = boundBottom
                                }
                                if (drawBottom > drawTop) {
                                    c.drawRect(
                                        child.left.toFloat(),
                                        drawTop.toFloat(),
                                        child.right.toFloat(),
                                        drawBottom.toFloat(),
                                        paint
                                    )
                                }
                            }

                            // 垂直線繪製
                            if (!endRow) {
                                val left = child.right + offset
                                c.drawRect(
                                    left.toFloat(),
                                    child.top.toFloat(), (
                                            left + midSize).toFloat(), (
                                            child.bottom + midSize).toFloat(),
                                    paint
                                )
                            }
                            i++
                        }
                        return
                    }
                    VERTICAL -> {
                        var i = 0
                        while (i < childCount) {
                            val child = parent.getChildAt(i)
                            val position =
                                (child.layoutParams as LayoutParams).viewAdapterPosition
                            val endRow = position >= endFirstPosition

                            // 垂直線繪製
                            if (position % spanCount != endPosition) {
                                val left = child.right + offset
                                c.drawRect(
                                    left.toFloat(),
                                    child.top.toFloat(), (
                                            left + midSize).toFloat(), (
                                            child.bottom + if (endRow) 0 else midSize).toFloat(),
                                    paint
                                )
                            }

                            // 水平線繪製
                            if (!endRow) {
                                val boundBottom = parent.height - parent.paddingBottom
                                val drawTop = child.bottom + offset
                                var drawBottom = drawTop + midSize
                                if (drawBottom > boundBottom) {
                                    drawBottom = boundBottom
                                }
                                if (drawBottom > drawTop) {
                                    c.drawRect(
                                        child.left.toFloat(),
                                        drawTop.toFloat(),
                                        child.right.toFloat(),
                                        drawBottom.toFloat(),
                                        paint
                                    )
                                }
                            }
                            i++
                        }
                        return
                    }
                }
                return
            }

            // Linear
            if (layoutManager is LinearLayoutManager) {
                when (layoutManager.orientation) {
                    HORIZONTAL -> {
                        var left: Int
                        var right: Int
                        var i = 0
                        while (i < childCount - 1) {
                            val child = parent.getChildAt(i)
                            left = child.right + offset
                            right = left + midSize
                            run {
                                if (left < pLeft) {
                                    left = pLeft
                                }
                                if (right > pRight) {
                                    right = pRight
                                }
                                if (left >= right) {
                                    return
                                }
                            }
                            c.drawRect(
                                left.toFloat(),
                                pTop.toFloat(),
                                right.toFloat(),
                                pBottom.toFloat(),
                                paint
                            )
                            i++
                        }
                    }
                    VERTICAL -> {
                        var top: Int
                        var bottom: Int
                        var i = 0
                        while (i < childCount - 1) {
                            val child = parent.getChildAt(i)
                            top = child.bottom + offset
                            bottom = top + midSize
                            run {
                                if (top < pTop) {
                                    top = pTop
                                }
                                if (bottom > pBottom) {
                                    bottom = pBottom
                                }
                                if (top >= bottom) {
                                    return
                                }
                            }
                            c.drawRect(
                                pLeft.toFloat(),
                                top.toFloat(),
                                pRight.toFloat(),
                                bottom.toFloat(),
                                paint
                            )
                            i++
                        }
                    }
                }
            }
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: State) {

            super.getItemOffsets(outRect, view, parent, state)

            if (!enable && gravity != ListGravity.Center) { return }

            val params = view.layoutParams as LayoutParams
            val nowPosition = params.bindingAdapterPosition
            val oldPosition = parent.getChildViewHolder(view).oldPosition
            val position = if(nowPosition == -1) oldPosition else nowPosition
            val layoutManager = parent.layoutManager
            val adapter = parent.adapter ?: return
            val childCount = adapter.itemCount

            var orientation = VERTICAL
            run {
                if (layoutManager is GridLayoutManager) {
                    orientation = layoutManager.orientation
                }
                else if (layoutManager is LinearLayoutManager) {
                    orientation = layoutManager.orientation
                }
            }
            if (position == 0) {

                var childAllSize = 0
                var parentSize = 0
                val parentContentWidth = parent.width - parent.paddingStart - parent.paddingEnd
                val parentContentHeight = parent.height - parent.paddingTop - parent.paddingBottom

                when(gravity) {
                    ListGravity.Start -> { }
                    ListGravity.Center -> {
                        when (orientation) {
                            HORIZONTAL -> {
                                // 使用第1個元素推斷總寬度
                                view.measure(
                                    MeasureSpec.makeMeasureSpec(parentContentWidth, MeasureSpec.AT_MOST),
                                    MeasureSpec.makeMeasureSpec(parentContentHeight, MeasureSpec.EXACTLY))

                                childAllSize = max(view.measuredWidth, 0) * childCount + (childCount - 1) * midSize
                                parentSize = parentContentWidth
                            }
                            VERTICAL -> {
                                // 使用第1個元素推斷總高度
                                view.measure(
                                    MeasureSpec.makeMeasureSpec(parentContentWidth, MeasureSpec.EXACTLY),
                                    MeasureSpec.makeMeasureSpec(parentContentHeight, MeasureSpec.AT_MOST))

                                childAllSize = max(view.measuredHeight, 0) * childCount + (childCount - 1) * midSize
                                parentSize = parentContentHeight
                            }
                        }
                        startSize = if (childAllSize > parentSize) 0 else parentSize - childAllSize shr 1
                        endSize = 0
                    }
                    ListGravity.End -> {
                        when (orientation) {
                            HORIZONTAL -> {
                                // 使用第1個元素推斷總寬度
                                view.measure(
                                    MeasureSpec.makeMeasureSpec(parentContentWidth, MeasureSpec.AT_MOST),
                                    MeasureSpec.makeMeasureSpec(parentContentHeight, MeasureSpec.EXACTLY))

                                childAllSize = max(view.measuredWidth, 0) * childCount + (childCount - 1) * midSize
                                parentSize = parentContentWidth
                            }
                            VERTICAL -> {
                                // 使用第1個元素推斷總高度
                                view.measure(
                                    MeasureSpec.makeMeasureSpec(parentContentWidth, MeasureSpec.EXACTLY),
                                    MeasureSpec.makeMeasureSpec(parentContentHeight, MeasureSpec.AT_MOST))

                                childAllSize = max(view.measuredHeight, 0) * childCount + (childCount - 1) * midSize
                                parentSize = parentContentHeight
                            }
                        }

                        startSize = if (childAllSize > parentSize) 0 else parentSize - childAllSize
                        startSize = max(startSize - endSize, 0)
                        //endSize = 0
                    }
                }
            }

            if (layoutManager is GridLayoutManager) {
                val spanCount = layoutManager.spanCount
                val endRowFrom = ((childCount shl 1) - 1) / (spanCount shl 1) * spanCount
                val lrPos = position % spanCount
                val radius = midSize shr 1
                var left = 0
                var top = 0
                var right = 0
                var bottom = 0
                when (orientation) {
                    HORIZONTAL -> {
                        left = if (position >= spanCount) radius else 0
                        top = if (lrPos > 0) radius else 0
                        right = if (position < endRowFrom) radius else 0
                        bottom = if (lrPos != spanCount - 1) radius else 0

                        // startSpace
                        if (startSize > 0 && position < spanCount) {
                            left += startSize
                        }

                        // endSpace
                        if (endSize > 0 && position >= endRowFrom) {
                            right += endSize
                        }
                    }
                    VERTICAL -> {
                        left = if (lrPos > 0) radius else 0
                        top = if (position >= spanCount) radius else 0
                        right = if (lrPos != spanCount - 1) radius else 0
                        bottom = if (position < endRowFrom) radius else 0

                        // startSpace
                        if (startSize > 0 && position < spanCount) {
                            top += startSize
                        }

                        // endSpace
                        if (endSize > 0 && position >= endRowFrom) {
                            bottom += endSize
                        }
                    }
                }
                outRect.set(left, top, right, bottom)
                return
            }
            if (layoutManager is LinearLayoutManager) {
                var left = 0
                var top = 0
                var right = 0
                var bottom = 0
                when (orientation) {
                    HORIZONTAL -> {
                        if (position > 0) {
                            left = midSize
                        }
                        else if (position == 0) {
                            left = startSize
                        }
                        if(nowPosition == -1 && oldPosition == childCount) {
                            right = endSize
                        }
                        else if (position == childCount - 1) {
                            right = endSize
                        }
                    }
                    VERTICAL -> {
                        if (position > 0) {
                            top = midSize
                        }
                        else if (position == 0) {
                            top = startSize
                        }
                        if(nowPosition == -1 && oldPosition == childCount) {
                            bottom = endSize
                        }
                        else if (position == childCount - 1) {
                            bottom = endSize
                        }
                    }
                }
                outRect.set(left, top, right, bottom)
            }
        }
    }

    class MoveTouchHelperCallback : ItemTouchHelper.Callback {

        constructor(parent: RecyclerView): super() {
            mParent = WeakReference(parent)
        }

        private val mParent: WeakReference<RecyclerView>

        private val adapter: Adapter<*>?
            get() { return mParent.get()?.adapter as? Adapter<*> }

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return false
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val swipeFlags = ItemTouchHelper.ACTION_STATE_IDLE
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder): Boolean {

            val adapter = this.adapter ?: return false

            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition

            adapter.v5Swap(from, to)

            return true
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {

        }
    }

    class DefaultItemAnimator : androidx.recyclerview.widget.DefaultItemAnimator() {
        init {
            supportsChangeAnimations = false
        }
    }
}
