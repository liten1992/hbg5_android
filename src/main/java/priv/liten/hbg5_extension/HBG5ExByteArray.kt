package priv.liten.hbg5_extension

import android.util.Base64

/**轉換BASE64*/
fun ByteArray.toBase64(): String? {
    return try {
        Base64.encodeToString(this, Base64.NO_WRAP)
    }
    catch (e: Exception) {
        null
    }
}