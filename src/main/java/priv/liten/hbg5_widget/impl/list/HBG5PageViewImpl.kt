package priv.liten.hbg5_widget.impl.list

import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import androidx.viewpager2.widget.ViewPager2
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_widget.bin.list.HBG5PageView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5TagImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl

interface HBG5PageViewImpl:
    HBG5ViewImpl,
    HBG5BackgroundImpl {

    /** 列表滾動方向 垂直(vertical) 水平(horizontal) */
    var v5ListOrientation: HBG5WidgetConfig.Attrs.ListOrientation
        get() = v5GetTag(R.id.attr_list_orientation) ?: HBG5WidgetConfig.Attrs.ListOrientation.HORIZONTAL
        set(value) {
            v5SetTag(R.id.attr_list_orientation, value)
            if(this is HBG5PageView) {
                this.uiList.orientation = when(value) {
                    HBG5WidgetConfig.Attrs.ListOrientation.VERTICAL -> ViewPager2.ORIENTATION_VERTICAL
                    else -> ViewPager2.ORIENTATION_HORIZONTAL
                }
            }
        }

    /** 配適器 */
    var v5Adapter: HBG5PageView.Adapter<*>?
        get() = (this as? HBG5PageView)?.uiList?.adapter as? HBG5PageView.Adapter<*>
        set(value) { (this as? HBG5PageView)?.uiList?.adapter = value }

    /** 設置頁面異動監聽 */
    fun v5RegisterPageChange(closure: ((Int)-> Unit)?) {
        (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_callback_page_change, when(closure) {
            null -> null
            else -> object : Callback {
                override fun onChange(page: Int) {
                    closure(page)
                }
            }
        })
    }

    fun buildPageViewByAttr(
        typedArray: TypedArray,
        @StyleableRes listOrientation: Int? = null) {
        v5ListOrientation = HBG5WidgetConfig.Attrs.ListOrientation.fromAttr(listOrientation
            ?.let { typedArray.v5GetInt(it) })
            ?: HBG5WidgetConfig.Attrs.ListOrientation.HORIZONTAL
    }

    /** 更新 */
    fun refreshPageView() {
        if(this is HBG5PageView) {
            this.uiList.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    v5GetTag<Callback>(R.id.attr_callback_page_change)?.onChange(position)
                }
            })
        }
    }

    private interface Callback {
        fun onChange(page: Int) {

        }
    }
}