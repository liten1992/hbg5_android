package priv.liten.hbg5_widget.drawable

import android.graphics.*
import android.graphics.drawable.Drawable

/**純色可設置最適尺寸的圖像*/
class HBG5ColorDrawable : Drawable {

    constructor() : super()

    constructor(color: Int, width: Int = 0, height: Int = 0) : this() {
        this.color = color
        this.intrinsicWidth = width
        this.intrinsicHeight = height
    }

    override fun draw(canvas: Canvas) {

        if (Color.alpha(color) <= 0) { return }

        canvas.drawRect(bounds, paint)
    }

    override fun setAlpha(alpha: Int) { }

    override fun setColorFilter(colorFilter: ColorFilter?) { }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }


    var color: Int
        get() { return paint.color }
        set(value) { paint.color = value }


    private val paint: Paint = run {
        val paint = Paint()
        paint.isAntiAlias = false
        paint.color = Color.TRANSPARENT
        paint
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