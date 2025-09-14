package priv.liten.hbg5_widget.impl.list

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import androidx.annotation.StyleableRes
import androidx.viewpager2.widget.ViewPager2
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetDrawable
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_widget.bin.list.HBG5Banner
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl

interface HBG5BannerImpl:
    HBG5ViewImpl,
    HBG5BackgroundImpl {

    // ================ Data
    /** 列表滾動方向 垂直(vertical) 水平(horizontal) */
    var v5ListOrientation: HBG5WidgetConfig.Attrs.ListOrientation
        get() = v5GetTag(R.id.attr_list_orientation) ?: HBG5WidgetConfig.Attrs.ListOrientation.HORIZONTAL
        set(value) {
            v5SetTag(R.id.attr_list_orientation, value)
            if(this is HBG5Banner) {
                this.uiPager.orientation = when(value) {
                    HBG5WidgetConfig.Attrs.ListOrientation.VERTICAL -> ViewPager2.ORIENTATION_VERTICAL
                    else -> ViewPager2.ORIENTATION_HORIZONTAL
                }
            }
        }

    /** 圖片填充方式 */
    var v5ImageScaleType: HBG5WidgetConfig.Attrs.ImageScaleType
        get() = v5GetTag(R.id.attr_image_scale_type) ?: HBG5WidgetConfig.Attrs.ImageScaleType.Crop
        set(value) {
            v5SetTag(R.id.attr_image_scale_type, value)
            v5Adapter?.v5UpdateAll()
        }

    /** 圖片 讀取 */
    var v5ImageLoading: Drawable?
        get() = v5GetTag(R.id.attr_image_loading)
        set(value) { v5SetTag(R.id.attr_image_loading, value) }

    /** 圖片 失敗 */
    var v5ImageFailed: Drawable?
        get() = v5GetTag(R.id.attr_image_failed)
        set(value) { v5SetTag(R.id.attr_image_failed, value) }

    /** 圖片列表 */
    var v5ImageList: List<String>
        get() = v5Adapter?.v5List ?: emptyList()
        set(value) { v5Adapter?.v5List = value }

    /** 列表適配器 */
    val v5Adapter: HBG5Banner.Adapter?
        get() {
            if(this !is HBG5Banner) { return null }
            val adapter: HBG5Banner.Adapter = this.v5GetTag(R.id.attr_list_adapter) ?: HBG5Banner.Adapter(this).also {
                this.v5SetTag(R.id.attr_list_adapter, it)
                this.uiPager.adapter = it
            }
            return adapter
        }


    // ================ Event
    /** 設置頁面異動監聽 */
    fun v5RegisterPageChange(listener: ((Int)-> Unit)?) {
        if(this !is HBG5Banner) { return }
        this.v5GetTag<ViewPager2.OnPageChangeCallback>(R.id.attr_callback_page_change)?.let { callback ->
            this.v5SetTag(R.id.attr_callback_page_change, null)
            this.uiPager.unregisterOnPageChangeCallback(callback)
        }
        if(listener != null) {
            val callback = object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) { listener(position) }
            }
            this.v5SetTag(R.id.attr_callback_page_change, callback)
            this.uiPager.registerOnPageChangeCallback(callback)
        }
    }
    /** 設置頁面點擊監聽 */
    fun v5RegisterPageClick(listener: ((Int)-> Unit)?) {
        if(this !is HBG5Banner) { return }

        this.v5SetTag(R.id.attr_callback_page_click, when(listener) {
            null -> null
            else -> object : ClickCallback {
                override fun run(page: Int) { listener(page) }
            }
        })
    }
    /** 設置頁面長點擊監聽 */
    fun v5RegisterPageLongClick(listener: ((Int)-> Unit)?) {
        if(this !is HBG5Banner) { return }

        this.v5SetTag(R.id.attr_callback_page_long_click, when(listener) {
            null -> null
            else -> object : ClickCallback {
                override fun run(page: Int) { listener(page) }
            }
        })
    }

    interface ClickCallback {
        fun run(page: Int)
    }

    // ================ Method
    /** 新增圖片 */
    fun v5AddImage(url: String) { v5Adapter?.v5Add(null, url) }
    /** 移除指定索引圖片 */
    fun v5DeleteImage(index: Int) { v5Adapter?.v5DeleteWithIndex(index) }

    /** 建立XML */
    fun buildBannerByAttr(
        typedArray: TypedArray,
        @StyleableRes listOrientation: Int? = null,
        @StyleableRes imageScaleType: Int? = null,
        @StyleableRes imageLoading: Int? = null,
        @StyleableRes imageFailed: Int? = null
    ) {
        v5ListOrientation = listOrientation
            ?.let { HBG5WidgetConfig.Attrs.ListOrientation.fromAttr(typedArray.v5GetInt(it)) }
            ?: HBG5WidgetConfig.Attrs.ListOrientation.HORIZONTAL
        v5ImageScaleType = imageScaleType
            ?.let { HBG5WidgetConfig.Attrs.ImageScaleType.fromAttr(typedArray.v5GetInt(it)) }
            ?: HBG5WidgetConfig.Attrs.ImageScaleType.Crop
        v5ImageLoading = imageLoading?.let { typedArray.v5GetDrawable(it) }
        v5ImageFailed = imageFailed?.let { typedArray.v5GetDrawable(it) }
    }

    /** 更新 */
    fun refreshBanner() {

    }
}