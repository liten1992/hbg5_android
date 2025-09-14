package priv.liten.hbg5_widget.drawable

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

/**修正轉向繪製圖像*/
class HBG5ExifDrawable : Drawable {

    companion object {
        fun build(context: Context, drawable: BitmapDrawable, uri: Uri): Drawable {

            val rotation =
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        return@use ExifInterface(stream).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL)
                    }
                }
                catch (error: Exception) {
                    null
                }

            return when(rotation) {
                ExifInterface.ORIENTATION_ROTATE_90,
                ExifInterface.ORIENTATION_ROTATE_270,
                ExifInterface.ORIENTATION_ROTATE_180 -> HBG5ExifDrawable(drawable = drawable, rotation = rotation)
                else -> drawable
            }
        }
    }

    constructor(context: Context, bitmap: Bitmap, path: String): this(
        drawable = BitmapDrawable(context.resources, bitmap),
        rotation =
        try {
            ExifInterface(path).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL)
        }
        catch (error: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    )

    constructor(drawable: BitmapDrawable, rotation: Int) {
        this.main = drawable
        this.rotation = rotation
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
    }

    private val main: BitmapDrawable
    private val rotation: Int
    private val paint: Paint = Paint()

    override fun draw(canvas: Canvas) {

        val imageWidth = main.bounds.width().toFloat()
        val imageHeight = main.bounds.height().toFloat()

        when(rotation) {
            // 轉向繪製
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                canvas.save()
                canvas.rotate(90f, imageHeight*0.5f, imageWidth*0.5f)
                main.draw(canvas)
                canvas.restore()
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                canvas.save()
                canvas.rotate(270f, imageHeight*0.5f, imageWidth*0.5f)
                main.draw(canvas)
                canvas.restore()
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                canvas.save()
                canvas.rotate(180f, imageWidth*0.5f, imageHeight*0.5f)
                main.draw(canvas)
                canvas.restore()
            }
            else -> { main.draw(canvas) }
        }
    }

    override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {

        when(rotation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                val wRadius = (right - left).shr(1)
                val hRadius = (bottom - top).shr(1)
                val xCenter = (left + right).shr(1)
                val yCenter = (top + bottom).shr(1)
                main.setBounds(xCenter - hRadius, yCenter - wRadius, xCenter + hRadius, yCenter + wRadius)
            }
            else -> {
                main.setBounds(left, top, right, bottom)
            }
        }
    }

    override fun getAlpha(): Int {
        return main.alpha
    }
    override fun setAlpha(alpha: Int) {
        main.alpha = alpha
    }

    override fun getColorFilter(): ColorFilter? {
        return main.colorFilter
    }
    override fun setColorFilter(colorFilter: ColorFilter?) {
        main.colorFilter = colorFilter
    }

    override fun getMinimumWidth(): Int {
        return main.minimumWidth
    }
    override fun getMinimumHeight(): Int {
        return main.minimumHeight
    }

    override fun getIntrinsicWidth(): Int {

        return when(rotation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_ROTATE_270 -> { main.intrinsicHeight }
            else -> { main.intrinsicWidth }
        }
    }
    override fun getIntrinsicHeight(): Int {

        return when(rotation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_ROTATE_270 -> { main.intrinsicWidth }
            else -> { main.intrinsicHeight }
        }
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSPARENT
    }
}