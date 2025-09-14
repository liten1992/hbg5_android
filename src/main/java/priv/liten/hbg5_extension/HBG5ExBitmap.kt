package priv.liten.hbg5_extension

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import priv.liten.base_extension.readFileType
import priv.liten.hbg5_http.HBG5DownloadCall
import priv.liten.hbg5_widget.application.HBG5Application
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 計算圖像縮小比例
 * @return width & height/{result}
 * */
fun BitmapFactory.Options.computeSampleSize(rotation: Int? = null, maxWidth: Int, maxHeight: Int) : Int {
    return listOf(outWidth, outHeight).computeSampleSize(rotation, maxWidth, maxHeight)
}
/**
 * 計算圖像縮小比例 X/1
 * this: [width, height]
 * @return width & height/{result}
 * */
fun List<Int>.computeSampleSize(rotation: Int? = null, maxWidth: Int, maxHeight: Int) : Int {
    var inSampleSize = 1

    if(this.size != 2) { return inSampleSize }

    var width = this[0]
    var height = this[1]
    when(rotation) {
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_ROTATE_270 -> {
            val temp = width
            width = height
            height = temp
        }
    }

    if (height > maxHeight || width > maxWidth) {
        // 無條件進位
        val widthRate = (width.toFloat() / maxWidth.toFloat() + 0.49999f).roundToInt()
        val heightRate = (height.toFloat() / maxHeight.toFloat() + 0.49999f).roundToInt()
        inSampleSize = heightRate.coerceAtLeast(widthRate).coerceAtLeast(1)
    }
    var size = 1
    while (size < inSampleSize) {
        size = size shl 1
        if (size >= inSampleSize) {
            return size
        }
    }

    return inSampleSize
}

/**圖片儲存為本機檔案
 * @param fileUri ex:content://xxx.jpg
 * */
fun Bitmap.save(fileUri: String, isPng: Boolean = true): Boolean {
    if(fileUri.isEmpty()) { return false }

    val app = HBG5Application.instance ?: return false

    // 執行寫入
    try {
        app.contentResolver.openOutputStream(Uri.parse(fileUri)).use { stream ->
            if(isPng) {
                this.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            else {
                this.compress(Bitmap.CompressFormat.JPEG, 95, stream)
            }
            return true
        }
    }
    catch (error: Throwable) {
        return false
    }
}
/**圖片儲存為本機檔案
 * @param fileUri ex:content://xxx.jpg
 * */
fun Bitmap.save(fileUri: Uri, isPng: Boolean = true): Boolean = save(fileUri = fileUri.toString(), isPng = isPng)

class BitmapBuilder {
    class BitmapConfig {
        class Format {
            companion object {
                val PNG = Format(type = "png")
                val JPG = Format(type = "jpg")
                val BMP = Format(type = "bmp")
                val HEIC = Format(type = "heic")
            }
            /**
             * @param type 檔案類型名稱 png jpg bmp ...
             * */
            private constructor(type: String) {
                this.type = type
            }
            val type: String
        }

        companion object {
            val NONE = BitmapConfig(width = 0, height = 0, format = null)
        }

        constructor(
            width: Int,
            height: Int,
            format: Format?
        ) {
            this.width = width
            this.height = height
            this.format = format
        }

        val width: Int
        val height: Int
        val format: Format?

        fun exist(): Boolean {
            return width > 0 && height > 0 && format != null
        }
    }

    companion object {
        /**取得圖檔資訊(尺寸、種類)*/
        fun buildConfig(context: Context? = HBG5Application.instance, uri: Uri?) : BitmapConfig {
            try {
                if(context == null) { throw NullPointerException("Not found application") }
                if(uri == null) { throw NullPointerException("Not found uri") }

                val options = BitmapFactory.Options()
                var width: Int = 0
                var height: Int = 0
                var format: BitmapConfig.Format? = null

                // 讀取圖片尺寸
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                    width = max(options.outWidth, 0)
                    height = max(options.outHeight, 0)
                }
                if(width <= 0 || height <= 0) { throw NullPointerException("Not found image width height")  }

                // 讀取圖片種類
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val type = stream.readFileType(
                        matchList = mapOf(
                            HBG5DownloadCall.FILE_TYPE_HEADER_JPG,
                            HBG5DownloadCall.FILE_TYPE_HEADER_PNG,
                            HBG5DownloadCall.FILE_TYPE_HEADER_BMP,
                            HBG5DownloadCall.FILE_TYPE_HEADER_HEIC
                        )
                    )
                    format = when(type) {
                        HBG5DownloadCall.FILE_TYPE_HEADER_JPG.first -> BitmapConfig.Format.JPG
                        HBG5DownloadCall.FILE_TYPE_HEADER_PNG.first -> BitmapConfig.Format.PNG
                        HBG5DownloadCall.FILE_TYPE_HEADER_BMP.first -> BitmapConfig.Format.BMP
                        HBG5DownloadCall.FILE_TYPE_HEADER_HEIC.first -> BitmapConfig.Format.HEIC
                        else -> null
                    }
                }
                if(format == null) { throw NullPointerException("Not found image format") }

                return BitmapConfig(
                    width = width,
                    height = height,
                    format = format
                )
            }
            catch (error: Exception) {
                return BitmapConfig.NONE
            }
        }
        /**建立*/
        fun build(
            context: Context? = HBG5Application.instance,
            uri: Uri?,
            bmpConfig: BitmapConfig? = null,
            maxSize: Int
        ) : Bitmap? = build(
            context = context,
            uri = uri,
            bmpConfig = bmpConfig,
            maxWidth = maxSize, maxHeight = maxSize,
            readRotation = false)
        /**建立*/
        fun build(
            context: Context? = HBG5Application.instance,
            uri: Uri?,
            bmpConfig: BitmapConfig? = null,
            maxWidth: Int, maxHeight: Int,
            readRotation: Boolean = false
        ) : Bitmap? {
            if(context == null) { return null }
            if(uri == null) { return null }
            // 照片轉向
            val rotation: Int? = run rotation@{

                if(!readRotation) { return@rotation null }

                try {
                    context.contentResolver.openInputStream(uri).use { stream ->
                        stream?.let { return@rotation ExifInterface(it).getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL) }
                    }
                }

                catch (error: Exception) {
                    error.printStackTrace()
                }

                return@rotation null
            }

            val options = BitmapFactory.Options()

            if(bmpConfig == null) {
                options.inJustDecodeBounds = true
                var sampleSize: Int
                try {
                    context.contentResolver.openInputStream(uri).use { stream ->
                        BitmapFactory.decodeStream(stream, null, options)
                        sampleSize = options.computeSampleSize(
                            rotation = rotation,
                            maxWidth =  maxWidth,
                            maxHeight = maxHeight)
                    }
                }
                catch (error: Exception) {
                    return null
                }

                options.inJustDecodeBounds = false
                options.inSampleSize = sampleSize
            }
            else {
                options.inJustDecodeBounds = false
                options.inSampleSize = listOf(bmpConfig.width, bmpConfig.height).computeSampleSize(
                    rotation = rotation,
                    maxWidth = maxWidth,
                    maxHeight = maxHeight)
            }

            try {
                context.contentResolver.openInputStream(uri).use { stream ->
                    return BitmapFactory.decodeStream(stream, null, options)
                }
            }
            catch (error: Exception) {
                return null
            }
        }
    }
}
