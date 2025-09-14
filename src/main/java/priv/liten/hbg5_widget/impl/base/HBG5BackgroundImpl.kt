package priv.liten.hbg5_widget.impl.base

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetDimension
import priv.liten.hbg5_extension.hbg5.v5GetDrawable
import priv.liten.hbg5_extension.hbg5.v5GetLayoutDimension
import priv.liten.hbg5_widget.bin.image.HBG5ImageView
import priv.liten.hbg5_widget.drawable.HBG5StateListDrawable

interface HBG5BackgroundImpl {
    /** 背景圖片 */
    var v5BackgroundImage: Drawable?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image, value)
            refreshBackground()
        }

    /** 背景圓角(預設) */
    var v5BackgroundRadius:Float
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_size_radius) ?: 0f
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_size_radius, value)
            refreshBackground()
        }

    /** 背景圓角(獨立設置) */
    var v5BackgroundRadiusRect:FloatArray
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_size_radius_rect) ?: v5BackgroundRadius.let { floatArrayOf(it, it, it, it) }
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_size_radius_rect, value)
            refreshBackground()
        }

    /** 背景顏色-預設 */
    var v5BackgroundColor:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_normal)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_normal, value)
            refreshBackground()
        }
    /** 背景顏色-按下 */
    var v5BackgroundColorPressed:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_pressed)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_pressed, value)
            refreshBackground()
        }
    /** 背景顏色-選取 */
    var v5BackgroundColorChecked:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_checked)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_checked, value)
            refreshBackground()
        }
    /** 背景顏色-禁用 */
    var v5BackgroundColorUnable: Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_unable)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_unable, value)
            refreshBackground()
        }

    /** 背景框線尺寸 */
    var v5BackgroundBorderSize:Float?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_size_border)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_size_border, value)
            refreshBackground()
        }

    /** 背景框線顏色-預設 */
    var v5BackgroundBorderColor:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_border_normal)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_border_normal, value)
            refreshBackground()
        }
    /** 背景框線顏色-按下 */
    var v5BackgroundBorderColorPressed:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_border_pressed)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_border_pressed, value)
            refreshBackground()
        }
    /** 背景框線顏色-選取 */
    var v5BackgroundBorderColorChecked:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_border_checked)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_border_checked, value)
            refreshBackground()
        }
    /** 背景框線顏色-禁用 */
    var v5BackgroundBorderColorUnable:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_border_unable)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_border_unable, value)
            refreshBackground()
        }

    /** 更新XML */
    fun buildBackgroundByAttr(
        typedArray: TypedArray,
        @StyleableRes image: Int? = null,
        @StyleableRes radius: Int? = null,
        @StyleableRes radiusLT: Int? = null,
        @StyleableRes radiusRT: Int? = null,
        @StyleableRes radiusRB: Int? = null,
        @StyleableRes radiusLB: Int? = null,
        @StyleableRes colorNormal: Int? = null,
        @StyleableRes colorPressed: Int? = null,
        @StyleableRes colorChecked: Int? = null,
        @StyleableRes colorUnable: Int? = null,
        @StyleableRes borderSize: Int? = null,
        @StyleableRes borderColorNormal: Int? = null,
        @StyleableRes borderColorPressed: Int? = null,
        @StyleableRes borderColorChecked: Int? = null,
        @StyleableRes borderColorUnable: Int? = null
    ) {
        val defRadius = radius?.let{ typedArray.v5GetLayoutDimension(radius)?.toFloat() } ?: 0f

        v5BackgroundImage = image?.let { typedArray.v5GetDrawable(it) }
        v5BackgroundRadius = defRadius
        v5BackgroundRadiusRect = floatArrayOf(
            radiusLT?.let { typedArray.v5GetLayoutDimension(it)?.toFloat() } ?: defRadius,
            radiusRT?.let { typedArray.v5GetLayoutDimension(it)?.toFloat() } ?: defRadius,
            radiusRB?.let { typedArray.v5GetLayoutDimension(it)?.toFloat() } ?: defRadius,
            radiusLB?.let { typedArray.v5GetLayoutDimension(it)?.toFloat() } ?: defRadius
        )
        v5BackgroundColor = colorNormal?.let { typedArray.v5GetColor(it) }
        v5BackgroundColorPressed = colorPressed?.let { typedArray.v5GetColor(it) }
        v5BackgroundColorChecked = colorChecked?.let { typedArray.v5GetColor(it) }
        v5BackgroundColorUnable = colorUnable?.let { typedArray.v5GetColor(it) }
        v5BackgroundBorderSize = borderSize?.let { typedArray.v5GetDimension(it) }
        v5BackgroundBorderColor = borderColorNormal?.let { typedArray.v5GetColor(it) }
        v5BackgroundBorderColorPressed = borderColorPressed?.let { typedArray.v5GetColor(it) }
        v5BackgroundBorderColorChecked = borderColorChecked?.let { typedArray.v5GetColor(it) }
        v5BackgroundBorderColorUnable = borderColorUnable?.let { typedArray.v5GetColor(it) }
    }

    /**刷新背景色彩*/
    fun refreshBackground() {
        if(this !is View) { return }
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }

        // 背景影像存在，忽略(背景影像優先度更高)
        val image = v5BackgroundImage
        if(image != null) {
            if(this.background === image) {
                image.invalidateSelf()
            }
            else {
                this.background = image
            }
            return
        }

        val corners = v5BackgroundRadiusRect
        val borderSize = v5BackgroundBorderSize ?: 0f
        val colorNormal = v5BackgroundColor
        val colorPressed =  v5BackgroundColorPressed
        val colorChecked = v5BackgroundColorChecked
        val colorUnable = v5BackgroundColorUnable
        val borderColorNormal = v5BackgroundBorderColor
        val borderColorPressed = v5BackgroundBorderColorPressed
        val borderColorChecked = v5BackgroundBorderColorChecked
        val borderColorUnable = v5BackgroundBorderColorUnable
        // 更新背景失敗，重新建立
        if((this.background as? HBG5StateListDrawable)?.update(
                corners = corners, borderSize = borderSize,
                colorNormal = colorNormal, borderNormal = borderColorNormal,
                colorPressed = colorPressed, borderPressed = borderColorPressed,
                colorChecked = colorChecked, borderChecked = borderColorChecked,
                colorUnable = colorUnable, borderUnable = borderColorUnable) != true) {
            this.background = HBG5StateListDrawable(
                corners = corners, borderSize = borderSize,
                colorNormal = colorNormal, borderNormal = borderColorNormal,
                colorPressed = colorPressed, borderPressed = borderColorPressed,
                colorChecked = colorChecked, borderChecked = borderColorChecked,
                colorUnable = colorUnable, borderUnable = borderColorUnable
            )
        }
    }


}