package priv.liten.hbg5_widget.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

/**空白的圖像*/
class HBG5EmptyDrawable : Drawable {

    constructor() : super()

    override fun draw(canvas: Canvas) { }

    override fun setAlpha(alpha: Int) { }

    override fun setColorFilter(colorFilter: ColorFilter?) { }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }


    private var intrinsicWidth: Int = -1
    fun setIntrinsicWidth(width: Int) {
        intrinsicWidth = width
    }
    override fun getIntrinsicWidth(): Int {
        return intrinsicWidth
    }

    private var intrinsicHeight: Int = -1
    fun setIntrinsicHeight(height: Int) {
        intrinsicHeight = height
    }
    override fun getIntrinsicHeight(): Int {
        return intrinsicHeight
    }
}

