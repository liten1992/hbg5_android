package priv.liten.hbg5_widget.impl.layout

import android.content.res.TypedArray
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetDimension
import priv.liten.hbg5_widget.bin.layout.HBG5FrameLayout
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5ClickImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl
import priv.liten.hbg5_widget.impl.base.HBG5CheckImpl

interface HBG5FrameLayoutImpl :
    HBG5ViewImpl,
    HBG5ClickImpl,
    HBG5CheckImpl,
    HBG5BackgroundImpl {

    /** 特定調整下固定元件縮放比例 */
    var v5MeasureScale: Float?
        get() = v5GetTag(R.id.attr_measure_scale)
        set(value) {
            v5SetTag(R.id.attr_measure_scale, if(value != null && value > 0) value else null)
            if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }
            if(this is HBG5FrameLayout) {
                this.requestLayout()
            }
        }

    /** XML */
    fun buildFrameLayoutByAttr(
        typedArray: TypedArray,
        @StyleableRes measureScale: Int? = null) {
        v5MeasureScale = measureScale?.let { typedArray.getFloat(it, -1f) }
    }

    /** 更新 */
    fun refreshFrameLayout() {

    }
}