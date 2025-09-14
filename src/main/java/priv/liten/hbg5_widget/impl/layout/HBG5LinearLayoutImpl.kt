package priv.liten.hbg5_widget.impl.layout

import android.content.res.TypedArray
import android.graphics.Color
import android.widget.LinearLayout
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetDimensionPixelSize
import priv.liten.hbg5_widget.drawable.HBG5ColorDrawable
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5ClickImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl

interface HBG5LinearLayoutImpl:
    HBG5ViewImpl,
    HBG5ClickImpl,
    HBG5BackgroundImpl {

    /** 分隔線尺寸 null 不套用 */
    var v5DividerSize: Int?
        get() = v5GetTag(R.id.attr_div_size)
        set(value) {
            v5SetTag(R.id.attr_div_size, value)
            refreshLinearLayout()
        }

    /** 分隔線顏色 */
    var v5DividerColor: Int?
        get() = v5GetTag(R.id.attr_div_color)
        set(value) {
            v5SetTag(R.id.attr_div_color, value)
            refreshLinearLayout()
        }

    /** 更新XML */
    fun buildLinearLayoutByAttr(
        typedArray: TypedArray,
        @StyleableRes dividerSize: Int? = null,
        @StyleableRes dividerColor: Int? = null
    ) {
        v5DividerSize = dividerSize?.let { typedArray.v5GetDimensionPixelSize(it) }
        v5DividerColor = dividerColor?.let { typedArray.v5GetColor(it) }
    }

    /**刷新*/
    fun refreshLinearLayout() {
        if(this !is LinearLayout) { return }
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }

        val divSize = v5DividerSize ?: 0
        val divColor = v5DividerColor ?: Color.TRANSPARENT

        showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        dividerDrawable = if(divSize > 0) HBG5ColorDrawable(divColor, divSize, divSize) else null
    }
}