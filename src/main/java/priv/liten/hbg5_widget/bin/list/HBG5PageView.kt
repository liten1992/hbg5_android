package priv.liten.hbg5_widget.bin.list

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import priv.liten.hbg.R
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.impl.list.HBG5PageViewImpl

class HBG5PageView: FrameLayout, HBG5PageViewImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr) {
        // View
        run {
            this@HBG5PageView.uiList = ViewPager2(context)
            this.addView(this.uiList, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5PageView,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5PageView_v5Touchable,
                        visibility = R.styleable.HBG5PageView_v5Visibility,
                        padding = R.styleable.HBG5PageView_v5Padding,
                        paddingStart = R.styleable.HBG5PageView_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5PageView_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5PageView_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5PageView_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5PageView_v5BackgroundImage,
                        radius = R.styleable.HBG5PageView_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5PageView_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5PageView_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5PageView_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5PageView_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5PageView_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5PageView_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5PageView_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5PageView_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5PageView_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5PageView_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5PageView_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5PageView_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5PageView_v5BackgroundBorderColorUnable
                    )
                    // 頁面
                    buildPageViewByAttr(
                        typedArray = typedArray,
                        listOrientation = R.styleable.HBG5PageView_v5ListOrientation
                    )
                },
                finish = {
                    refreshBackground()
                    refreshPageView()
                }
            )
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.hbg5PageViewStyle)

    constructor(context: Context): this(context, null)


    // MARK:- ====================== Data
    override fun getAccessibilityClassName(): CharSequence? {
        return HBG5PageView::class.java.name
    }


    // MARK:- ====================== View
    val uiList: ViewPager2

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if(childCount >= 1) { return }
        super.addView(child, index, params)
    }


    // MARK:- ====================== Event
    private var onPageChangeCallback: ViewPager2.OnPageChangeCallback? = null
    override fun v5RegisterPageChange(closure: ((Int) -> Unit)?) {
        onPageChangeCallback?.let { callback ->
            uiList.unregisterOnPageChangeCallback(callback)
        }
        if(closure == null) { return }

        onPageChangeCallback = object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                closure(position)
            }
        }
        uiList.registerOnPageChangeCallback(onPageChangeCallback!!)
    }


    // MARK:- ====================== Class
    class Adapter<TItem>: HBG5ListView.Adapter<TItem> {
        // MARK:- ====================== Constructor
        constructor(
            v5GetHolderType: (HBG5ListView.Adapter<TItem>, Int) -> Int = { _, _ ->
                0 },
            v5CreateHolder: (HBG5ListView.Adapter<TItem>, RecyclerView, Int) -> HBG5ListView.Holder,

            v5InitHolder: (HBG5ListView.Adapter<TItem>, HBG5ListView.Holder) -> Unit = { _, _ -> },

            v5BindHolder: (HBG5ListView.Adapter<TItem>, HBG5ListView.Holder, Int) -> Unit = { adapter, holder, index ->
                holder.v5LoadData(adapter.v5Search(index))
            }) : super(v5GetHolderType, v5CreateHolder, v5InitHolder, v5BindHolder)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HBG5ListView.Holder {

            val holder = super.onCreateViewHolder(parent, viewType)

            holder.itemView.layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

            return holder
        }
    }
}