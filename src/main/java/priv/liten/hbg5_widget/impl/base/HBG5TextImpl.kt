package priv.liten.hbg5_widget.impl.base

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.Checkable
import android.widget.TextView
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetDimension
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_extension.hbg5.v5GetText
import priv.liten.hbg5_extension.toColorString
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.TextAlignmentHorizontal
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.TextAlignmentVertical

interface HBG5TextImpl {
    /** 文字 */
    var v5Text:CharSequence?
        get() = (this as? TextView)?.text
        set(value) {
            val view = this as? HBG5ViewImpl
            view?.v5AttrCompleted = false
            v5TextNormal = value
            v5TextChecked = value
            (this as? TextView)?.text = value
            view?.v5AttrCompleted = true
        }
    /** 文字(普通狀態) */
    var v5TextNormal:CharSequence?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_text_normal)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_text_normal, value)
            refreshTextState()
        }
    /** 文字(選取狀態) */
    var v5TextChecked:CharSequence?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_text_checked)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_text_checked, value)
            refreshTextState()
        }
    /** 文字尺寸 */
    var v5TextSize:Float
        get() = (this as? TextView)?.textSize ?: 0f
        set(value) {
            (this as? TextView)?.setTextSize(TypedValue.COMPLEX_UNIT_PX, value)
        }
    /** 文字顏色-預設 */
    var v5TextColor:Int
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_text_normal) ?: Color.BLACK
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_text_normal, value)
            refreshTextColor()
        }
    /** 文字顏色-按下 */
    var v5TextColorPressed:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_text_pressed)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_text_pressed, value)
            refreshTextColor()
        }
    /** 文字顏色-選取 */
    var v5TextColorChecked:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_text_checked)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_text_checked, value)
            refreshTextColor()
        }
    /** 文字顏色-禁用 */
    var v5TextColorUnable:Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_color_text_unable)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_color_text_unable, value)
            refreshTextColor()
        }
    /** 文字水平對齊 */
    var v5TextAlignmentHorizontal: TextAlignmentHorizontal
        get() {
            val default = TextAlignmentHorizontal.Start
            val view = this as? TextView ?: return default
            return TextAlignmentHorizontal.fromGravity(view.gravity) ?: default
        }
        set(value) {
            (this as? TextView)?.let { view ->
                view.gravity = view.gravity
                    .and(Gravity.VERTICAL_GRAVITY_MASK)
                    .or(value.gravity)
            }
        }
    /** 文字垂直對齊 */
    var v5TextAlignmentVertical: TextAlignmentVertical
        get() {
            val default = TextAlignmentVertical.Top
            val view = this as? TextView ?: return default
            return TextAlignmentVertical.fromGravity(view.gravity) ?: default
        }
        set(value) {
            (this as? TextView)?.let { view ->
                view.gravity = view.gravity
                    .and(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)
                    .or(value.gravity)
            }
        }

    /** 更新XML */
    fun buildTextByAttr(
        typedArray: TypedArray,
        @StyleableRes textNormal: Int? = null,
        @StyleableRes textChecked: Int? = null,
        @StyleableRes textSize: Int? = null,
        @StyleableRes textColorNormal: Int? = null,
        @StyleableRes textColorPressed: Int? = null,
        @StyleableRes textColorChecked: Int? = null,
        @StyleableRes textColorUnable: Int? = null,
        @StyleableRes textAlignmentHorizontal: Int? = null,
        @StyleableRes textAlignmentVertical: Int? = null
    ) {
        v5TextNormal = textNormal?.let { typedArray.v5GetText(it) }
        v5TextChecked = textChecked?.let { typedArray.v5GetText(it) ?: v5TextNormal }
        v5TextSize = textSize?.let { typedArray.v5GetDimension(it) } ?: v5TextSize
        v5TextColor = textColorNormal?.let { typedArray .v5GetColor(it) } ?: v5TextColor
        v5TextColorPressed = textColorPressed?.let { typedArray .v5GetColor(it) } ?: v5TextColorPressed
        v5TextColorChecked = textColorChecked?.let { typedArray .v5GetColor(it) } ?: v5TextColorChecked
        v5TextColorUnable = textColorUnable?.let { typedArray .v5GetColor(it) } ?: v5TextColorUnable
        v5TextAlignmentHorizontal = textAlignmentHorizontal?.let { TextAlignmentHorizontal.fromAttr(typedArray.v5GetInt(it)) } ?: v5TextAlignmentHorizontal
        v5TextAlignmentVertical = textAlignmentVertical?.let { TextAlignmentVertical.fromAttr(typedArray.v5GetInt(it)) } ?: v5TextAlignmentVertical
    }

    fun refreshTextState() {

        val view = this as? TextView ?: return

        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }

        val textNormal = v5TextNormal
        val textChecked = v5TextChecked

        when(this) {
            is Checkable -> {
                val checked = isChecked
                if(checked) {
                    textChecked?.let { view.text = it }
                }
                else {
                    view.text = textNormal
                }
            }
            else -> {
                view.text = textNormal
            }
        }
    }

    fun refreshTextColor() {
        val view = this as? TextView ?: return

        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }

        val stateList = mutableListOf<IntArray>()
        val colorList = mutableListOf<Int>()

        v5TextColorUnable?.let {
            stateList.add(intArrayOf(-android.R.attr.state_enabled))
            colorList.add(it)
        }
        v5TextColorChecked?.let {
            stateList.add(intArrayOf(android.R.attr.state_checked))
            colorList.add(it)
            stateList.add(intArrayOf(android.R.attr.state_focused))
            colorList.add(it)
        }
        v5TextColorPressed?.let {
            stateList.add(intArrayOf(android.R.attr.state_pressed))
            colorList.add(it)
        }
        v5TextColor.let {
            stateList.add(intArrayOf())
            colorList.add(it)
        }

        view.setTextColor(ColorStateList(stateList.toTypedArray(), colorList.toIntArray()))
    }
}