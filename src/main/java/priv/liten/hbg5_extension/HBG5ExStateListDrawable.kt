package priv.liten.hbg5_extension.android

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import priv.liten.hbg5_widget.drawable.HBG5RectDrawable

/** 定義 normal pressed checked 狀態的繪製圖件 */
fun StateListDrawable.build(
    normal: Drawable?,
    pressed: Drawable?,
    checked: Drawable?,
    unable: Drawable? = null): StateListDrawable {

    if (checked != null) {
        this.addState(intArrayOf(android.R.attr.state_checked), checked)
        this.addState(intArrayOf(android.R.attr.state_focused), checked)
    }

    if (unable != null) {
        this.addState(intArrayOf(-android.R.attr.state_enabled), unable)
    }

    if (pressed != null) {
        this.addState(intArrayOf(android.R.attr.state_pressed), pressed)
    }

    if (normal != null) {
        this.addState(intArrayOf(), normal)
    }

    return this
}

fun StateListDrawable.build(
    corners: FloatArray?, borderSize: Float?,
    normal_bg:Int?, normal_border:Int?,
    pressed_bg: Int?, pressed_border:Int?,
    checked_bg: Int?, checked_border:Int?,
    unable_bg: Int? = null, unable_border: Int? = null): StateListDrawable {

    val newCorners = corners ?: floatArrayOf(0f,0f,0f,0f)
    val newBorderSize = borderSize ?: 0f

    var normalDrawable: Drawable? = null
    var pressedDrawable: Drawable? = null
    var unableDrawable: Drawable? = null
    var checkedDrawable: Drawable? = null


    if (checked_bg != null || checked_border != null) {
        checkedDrawable = HBG5RectDrawable(
            newCorners,
            checked_bg ?: Color.TRANSPARENT,
            checked_border ?: Color.TRANSPARENT,
            newBorderSize)
    }

    if (unable_bg != null || unable_border != null) {
        unableDrawable = HBG5RectDrawable(
            newCorners,
            unable_bg ?: Color.TRANSPARENT,
            unable_border ?: Color.TRANSPARENT,
            newBorderSize)
    }

    if (pressed_bg != null || pressed_border != null) {
        pressedDrawable = HBG5RectDrawable(
            newCorners,
            pressed_bg ?: Color.TRANSPARENT,
            pressed_border ?: Color.TRANSPARENT,
            newBorderSize)
    }

    if (normal_bg != null || normal_border != null) {
        normalDrawable = HBG5RectDrawable(
            newCorners,
            normal_bg ?: Color.TRANSPARENT,
            normal_border ?: Color.TRANSPARENT,
            newBorderSize)
    }

    return StateListDrawable().build(
        normal = normalDrawable,
        pressed = pressedDrawable,
        checked = checkedDrawable,
        unable = unableDrawable)
}