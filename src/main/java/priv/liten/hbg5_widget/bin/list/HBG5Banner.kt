package priv.liten.hbg5_widget.bin.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StyleableRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetDrawable
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.bin.image.HBG5ImageView
import priv.liten.hbg5_widget.bin.layout.HBG5FrameLayout
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.list.HBG5BannerImpl
import java.lang.ref.WeakReference

class HBG5Banner: HBG5FrameLayout, HBG5BannerImpl {

    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        // View
        run {
            this.addView(this.uiPager, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5Banner,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 頁面
                    buildBannerByAttr(
                        typedArray = typedArray,
                        listOrientation = R.styleable.HBG5Banner_v5ListOrientation,
                        imageScaleType = R.styleable.HBG5Banner_v5ImageScaleType,
                        imageLoading = R.styleable.HBG5Banner_v5ImageLoading,
                        imageFailed = R.styleable.HBG5Banner_v5ImageFailed
                    )
                },
                finish = {
                    refreshBanner()
                }
            )
        }

        // Event
        run {

        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.hbg5PageViewStyle)

    constructor(context: Context): this(context, null)


    // MARK:- ====================== Data
    override fun getAccessibilityClassName(): CharSequence? {
        return HBG5Banner::class.java.name
    }


    // MARK:- ====================== View
    /** 圖片列表 */
    val uiPager: ViewPager2 by lazy { ViewPager2(context).apply {
        isFocusable = false
        isClickable = false
    } }


    // MARK:- ====================== Event


    // MARK:- ====================== Method
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if(childCount >= 1) { return }
        super.addView(child, index, params)
    }


    // MARK:- ====================== Class
    open class Adapter: HBG5ListView.Adapter<String> {
        // MARK:- ====================== Constructor

        constructor(owner: HBG5Banner) : super(
            v5CreateHolder = { adapter, parent, type -> Holder(owner) },
            v5InitHolder = { adapter, holder ->

                holder.v5RegisterClick {
                    val pageHolder = (it as? Holder) ?: return@v5RegisterClick
                    val page = pageHolder.bindingAdapterPosition
                    pageHolder.weakOwner.get()?.v5GetTag<HBG5BannerImpl.ClickCallback>(R.id.attr_callback_page_click)?.run(page)
                }

                holder.v5RegisterLongClick {
                    val pageHolder = (it as? Holder) ?: return@v5RegisterLongClick
                    val page = pageHolder.bindingAdapterPosition
                    pageHolder.weakOwner.get()?.v5GetTag<HBG5BannerImpl.ClickCallback>(R.id.attr_callback_page_long_click)?.run(page)
                }
            })


        class Holder: HBG5ListView.Holder {

            constructor(parent: HBG5Banner): super(HBG5ImageView(parent.context)) {
                imageView.layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
                weakOwner = WeakReference(parent)
            }

            override fun v5LoadData(data: Any?) {
                super.v5LoadData(data)

                val owner = weakOwner.get() ?: return

                imageView.v5ImageScaleType = owner.v5ImageScaleType
                imageView.v5AsyncImage(
                    url = data as? String,
                    loading = owner.v5ImageLoading,
                    failed = owner.v5ImageFailed)
            }

            private val imageView: HBG5ImageView = itemView as HBG5ImageView
            val weakOwner: WeakReference<HBG5Banner>
        }
    }
}