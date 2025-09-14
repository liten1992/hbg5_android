package priv.liten.base_extension

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * @param quality: 0 ~ 100, 100 -> png, 0~99 -> jpg
 * @param flag: 填充方式 default: NO_WRAP
 * */
fun Bitmap.toBase64(quality: Int, flag: Int = Base64.NO_WRAP): String? {

    var base64: String?

    ByteArrayOutputStream().use { stream ->
        val newQuality = min(max(80, quality), 100)
        this.compress(
            if(newQuality == 100) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
            newQuality,
            stream)

        base64 = Base64.encodeToString(stream.toByteArray(), flag)
    }

    return base64
}