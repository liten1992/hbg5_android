package priv.liten.hbg5_widget.impl.layout

import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_extension.hbg5.v5GetString
import priv.liten.hbg5_extension.toColor
import priv.liten.hbg5_widget.bin.layout.HBG5TabLayout
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl

interface HBG5TabLayoutImpl: HBG5ViewImpl {
    /**標籤選取索引*/
    var v5TabIndex: Int
        get() = v5GetTag(R.id.attr_tab_index) ?: -1
        set(value) {
            v5SetTag(R.id.attr_tab_index, value)
            if(this !is HBG5TabLayout) { return }
            this.uiTabGroup.check(value)
            // 更新標籤色彩
            val count = this.uiContentLayout.childCount
            this.uiTabView.setBackgroundColor(when {
                count > 0 && value >= 0 -> v5TabLineColors.getOrNull(value % count)
                else -> null
            } ?: Color.GRAY)
        }
    /**標籤背景色彩*/
    var v5TabLineBackgroundColor: Int
        get() = v5GetTag(R.id.attr_tab_line_background_color) ?: Color.TRANSPARENT
        set(value) {
            v5SetTag(R.id.attr_tab_line_background_color, value)
            if(this !is HBG5TabLayout) { return }
            this.uiTabLayout.setBackgroundColor(v5TabLineBackgroundColor)
        }
    /**標籤選取色彩*/
    var v5TabLineColors: List<Int>
        get() = v5GetTag(R.id.attr_tab_line_colors_checked) ?: emptyList()
        set(value) {
            v5SetTag(R.id.attr_tab_line_colors_checked, value)
            if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }
            if(this !is HBG5TabLayout) { return }
            v5TabIndex = v5TabIndex
        }

    /**更新XML*/
    fun buildTabLayoutByAttr(
        typedArray: TypedArray,
        @StyleableRes tabIndex: Int? = null,
        @StyleableRes tabLineBackgroundColor: Int? = null,
        @StyleableRes tabLineColors: Int? = null
    ) {
        v5TabIndex = tabIndex?.let { typedArray.v5GetInt(it) } ?: -1
        v5TabLineBackgroundColor = tabLineBackgroundColor?.let { typedArray.v5GetColor(it) } ?: Color.GRAY
        v5TabLineColors = (tabLineColors
            ?.let { typedArray.v5GetString(it) }
            ?.split(",")
            ?.map { it.toColor() }
            ?: listOf(Color.GRAY))
    }

    /**更新*/
    fun refreshTabLayout() { }
}