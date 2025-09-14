package priv.liten.hbg5_widget.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import priv.liten.hbg5_extension.contains
import priv.liten.hbg5_extension.dpToPx
import priv.liten.hbg5_extension.equalsEach
import kotlin.math.max
import kotlin.math.min

class HBG5RectDrawable : Drawable {
    enum class CornerType {
        CIRCLE, RECT, ROUND
    }
    enum class Radius(val value: Float) {
        FILL(-1f)
    }

    constructor(corner: Float, fillColor: Int, strokeColor: Int, strokeSize: Float) : this(
        corners = floatArrayOf(corner, corner, corner, corner),
        fillColor = fillColor,
        strokeColor = strokeColor,
        strokeSize = strokeSize)
    /**
     * @param corners [4] { LT, RT, RB, LB }
     * */
    constructor(corners: FloatArray, fillColor: Int, strokeColor: Int, strokeSize: Float) : super() {

        this.needRedraw = true
        this.corners = corners

        this.fillPaint.style = Paint.Style.FILL
        this.fillPaint.isAntiAlias = true
        this.fillPaint.color = fillColor

        this.strokePaint.style = Paint.Style.STROKE
        this.strokePaint.isAntiAlias = true
        this.strokePaint.color = strokeColor
        this.strokePaint.strokeCap = Paint.Cap.ROUND
        this.strokePaint.strokeWidth = max(strokeSize, 0f)
    }

    override fun draw(canvas: Canvas) {

        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        if(width <= 0 || height <= 0) { return }

        val strokeSize = max(strokePaint.strokeWidth, 0f)
        val strokeRadius = strokeSize * 0.5f

        if(recWidth != width || recHeight != height || needRedraw) {
            needRedraw = false
            recWidth = width
            recHeight = height
            fillPath.reset()
            strokePath.reset()
            // init fillPath & strokePath
            when(cornerType) {
                CornerType.ROUND -> {
                    val leftTop = if(corners[0] == Radius.FILL.value) min(width, height) * 0.5f else corners[0]
                    val rightTop = if(corners[1] == Radius.FILL.value) min(width, height) * 0.5f else corners[1]
                    val leftBottom = if(corners[2] == Radius.FILL.value) min(width, height) * 0.5f else corners[2]
                    val rightBottom = if(corners[3] == Radius.FILL.value) min(width, height) * 0.5f else corners[3]

                    val fillLeftTop = max(leftTop - strokeSize, 0f)
                    val fillRightTop = max(rightTop - strokeSize, 0f)
                    val fillLeftBottom = max(leftBottom - strokeSize, 0f)
                    val fillRightBottom = max(rightBottom - strokeSize, 0f)

                    val fillOffset = if(strokeSize > 0.0f) 0.5f else 0.0f

                    val offsetX = bounds.left.toFloat()
                    val offsetY = bounds.top.toFloat()
                    // 背景著色
                    fillPath.addRoundRect(
                        RectF(
                            offsetX + strokeSize - fillOffset,
                            offsetY + strokeSize - fillOffset,
                            offsetX + width - strokeSize + fillOffset,
                            offsetY + height - strokeSize + fillOffset),
                        floatArrayOf(
                            fillLeftTop, fillLeftTop,
                            fillRightTop, fillRightTop,
                            fillLeftBottom, fillLeftBottom,
                            fillRightBottom, fillRightBottom),
                        Path.Direction.CW)
                    // 邊線著色
                    strokePath.addRoundRect(
                        RectF(
                            offsetX + strokeRadius,
                            offsetY + strokeRadius,
                            offsetX + width - strokeRadius + fillOffset,
                            offsetY +  + height - strokeRadius),
                        floatArrayOf(
                            fillLeftTop, fillLeftTop,
                            fillRightTop, fillRightTop,
                            fillLeftBottom, fillLeftBottom,
                            fillRightBottom, fillRightBottom),
                        Path.Direction.CW)
                }
                else -> { }
            }
        }

        val cx = bounds.centerX().toFloat()
        val cy = bounds.centerY().toFloat()

        // Fill
        if (Color.alpha(fillPaint.color) > 0) {
            when(cornerType) {
                CornerType.CIRCLE -> {
                    val offset = 0.25f.dpToPx()
                    val radius = (min(width, height) * 0.5f) - when {
                        strokeSize > 2 -> strokeSize - 1
                        strokeSize > 1 -> max(offset, 0.5f)
                        else -> max(strokeSize, 0.5f)
                    }
                    if(radius > 0) {
                        canvas.drawCircle(cx+offset, cy+offset, radius, fillPaint)
                    }
                }
                CornerType.RECT -> {
                    rectF.set(
                        bounds.left + strokeSize,
                        bounds.top + strokeSize,
                        bounds.right - strokeSize,
                        bounds.bottom - strokeSize)

                    if(rectF.width() > 0 && rectF.height() > 0) {
                        canvas.drawRect(rectF, fillPaint)
                    }
                }
                CornerType.ROUND -> {
                    canvas.drawPath(fillPath, fillPaint)
                }
            }
        }

        // Stroke
        if (Color.alpha(strokePaint.color) > 0 && strokeSize > 0) {

            when(cornerType) {
                CornerType.CIRCLE -> {
                    val offset = 0.25f.dpToPx()
                    val radius = (min(width, height) - strokeSize - 1) * 0.5f
                    if(radius > 0) {
                        canvas.drawCircle(cx+offset, cy+offset, radius, strokePaint)
                    }
                }
                CornerType.RECT -> {
                    rectF.set(
                        bounds.left + strokeRadius,
                        bounds.top + strokeRadius,
                        bounds.right - strokeRadius,
                        bounds.bottom - strokeRadius)

                    canvas.drawRect(rectF, strokePaint)
                }
                CornerType.ROUND -> {
                    canvas.drawPath(strokePath, strokePaint)
                }
            }
        }
    }

    fun update(corners: FloatArray, fillColor: Int, strokeColor: Int, strokeSize: Float) {
        this.corners.forEachIndexed { index, corner -> this.corners[index] = corners.getOrNull(index) ?: corner }
        this.fillPaint.color = fillColor
        this.strokePaint.color = strokeColor
        this.strokePaint.strokeWidth = max(strokeSize, 0f)
    }

    private var needRedraw = true
    private var fillPath = Path()
    private var strokePath = Path()
    private var recWidth = 0f
    private var recHeight = 0f

    private val fillPaint = Paint()
    private val strokePaint = Paint()
    /** { LT, RT, RB, LB } */
    private var corners = floatArrayOf(0f, 0f, 0f, 0f)
        set(value) {
            field = value
            val a = listOf<Any>()
            a.contains {  }
            cornerType =
                if(value.equalsEach(equal = { it == Radius.FILL.value }, emptyResult = false)) CornerType.CIRCLE
                else if(value.contains { it == Radius.FILL.value || it > 0f }) CornerType.ROUND
                else CornerType.RECT
        }
    private var cornerType = CornerType.RECT
    private val rectF = RectF()

    override fun setAlpha(alpha: Int) { }

    override fun setColorFilter(colorFilter: ColorFilter?) { }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }
}