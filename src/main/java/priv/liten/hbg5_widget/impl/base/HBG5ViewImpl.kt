package priv.liten.hbg5_widget.impl.base

import android.content.res.TypedArray
import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetBoolean
import priv.liten.hbg5_extension.hbg5.v5GetDimensionPixelSize
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_widget.bin.list.HBG5ListView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig

interface HBG5ViewImpl: HBG5TagImpl {
    /** 啟用觸控 */
    var v5Touchable: Boolean
        get() {
            if(this !is View) { return false }
            return this.isFocusable && this.isClickable
        }
        set(value) {
            if(this !is View) { return }
            this.isFocusable = value
            this.isClickable = value
        }
    /** 可見性 */
    var v5Visibility: HBG5WidgetConfig.Attrs.Visibility
        get() {
            if(this !is View) { return HBG5WidgetConfig.Attrs.Visibility.Visible }
            return HBG5WidgetConfig.Attrs.Visibility.fromView(this)
        }
        set(value) {
            if(this !is View) { return }
            value.setView(this)
        }
    /** 啟用性 */
    var v5Enabled: Boolean
        get() {
            if(this !is View) { return false }
            return this.isEnabled
        }
        set(value) {
            if(this !is View) { return }
            this.isEnabled = value
        }

    /** 內間距 [左、上、右、下] */
    var v5Padding: IntArray
        get() = (this as? View)?.let { intArrayOf(paddingStart, paddingTop, paddingEnd, paddingBottom) } ?: intArrayOf(0, 0, 0, 0)
        set(value) { (this as? View)?.setPaddingRelative(value[0], value[1], value[2], value[3]) }

    /** 內邊界-左 */
    var v5PaddingStart:Int
        get() = (this as? View)?.paddingStart ?: 0
        set(value) { (this as? View)?.let { setPaddingRelative(value, paddingTop, paddingEnd, paddingBottom) } }
    /** 內邊界-右 */
    var v5PaddingEnd:Int
        get() = (this as? View)?.paddingEnd ?: 0
        set(value) { (this as? View)?.let { setPaddingRelative(paddingStart, paddingTop, value, paddingBottom) } }
    /** 內邊界-上 */
    var v5PaddingTop:Int
        get() = (this as? View)?.paddingTop ?: 0
        set(value) { (this as? View)?.let { setPaddingRelative(paddingStart, value, paddingEnd, paddingBottom) } }
    /** 內邊界-下 */
    var v5PaddingBottom:Int
        get() = (this as? View)?.paddingTop ?: 0
        set(value) { (this as? View)?.let { setPaddingRelative(paddingStart, paddingTop, paddingEnd, value) } }

    /** 參數設定是否完成 */
    var v5AttrCompleted: Boolean
        get() = v5GetTag(R.id.attr_completed) ?: true
        set(value) {
            v5SetTag(R.id.attr_completed, value)
        }

    /** 更新XML */
    fun buildViewByAttr(
        typedArray: TypedArray,
        @StyleableRes touchable: Int? = null,
        @StyleableRes visibility: Int? = null,
        @StyleableRes padding: Int? = null,
        @StyleableRes paddingStart: Int? = null,
        @StyleableRes paddingEnd: Int? = null,
        @StyleableRes paddingTop: Int? = null,
        @StyleableRes paddingBottom: Int? = null
    ) {
        val defPadding = padding?.let { typedArray.v5GetDimensionPixelSize(padding) }
        val defPaddings = v5Padding

        v5Touchable = touchable?.let { typedArray.v5GetBoolean(it) } ?: false
        v5Visibility = visibility
            ?.let { HBG5WidgetConfig.Attrs.Visibility.fromAttr(typedArray.v5GetInt(it)) }
            ?: HBG5WidgetConfig.Attrs.Visibility.Visible
        v5Padding = intArrayOf(
            paddingStart?.let { typedArray.v5GetDimensionPixelSize(it) } ?: defPadding ?: defPaddings[0],
            paddingTop?.let { typedArray.v5GetDimensionPixelSize(it) } ?: defPadding ?: defPaddings[1],
            paddingEnd?.let { typedArray.v5GetDimensionPixelSize(it) } ?: defPadding ?: defPaddings[2],
            paddingBottom?.let { typedArray.v5GetDimensionPixelSize(it) } ?: defPadding ?: defPaddings[3]
        )
    }

    fun refreshView() {

    }
}