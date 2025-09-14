package priv.liten.hbg5_widget.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer

/**設置需要套用的圖像並接受自訂最適大小的參數*/
class HBG5PanelDrawable : DrawableContainer {

    constructor(
        content: Drawable,
        intrinsicWidth: Int? = null,
        intrinsicHeight: Int? = null) : super() {
        this.content = content
        this.intrinsicWidth = intrinsicWidth
        this.intrinsicHeight = intrinsicHeight
    }

    val content: Drawable

    override fun draw(canvas: Canvas) {
        content.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
        content.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        content.colorFilter = colorFilter
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
        super.setBounds(left, top, right, bottom)
        content.setBounds(left, top, right, bottom)
    }

    override fun isStateful(): Boolean {
        return content.isStateful
    }

    override fun setState(stateSet: IntArray): Boolean {
        return content.setState(stateSet)
    }

    override fun getOpacity(): Int = content.opacity

    var intrinsicWidth: Int?
    override fun getIntrinsicWidth(): Int {
        return intrinsicWidth ?: content.intrinsicWidth
    }

    var intrinsicHeight: Int?
    override fun getIntrinsicHeight(): Int {
        return intrinsicHeight ?: content.intrinsicHeight
    }

    override fun mutate(): Drawable {
        content.mutate()
        return this
    }
}