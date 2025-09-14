package priv.liten.hbg5_extension.hbg5

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.StyleableRes

// 參數 0 或者 "" 代表 未輸入內容
fun TypedArray.v5GetDimensionPixelSize(@StyleableRes id: Int, def: Int? = null) : Int? {
    def?.let { return getDimensionPixelSize(id, it) }
    return if(v5Exist(id)) getDimensionPixelSize(id, 0) else null
}

fun TypedArray.v5GetDimension(@StyleableRes id: Int, def: Float? = null) : Float? {
    def?.let { return getDimension(id, it) }

    return if (v5Exist(id)) getDimension(id, 0f) else null
}

fun TypedArray.v5GetLayoutDimension(@StyleableRes id: Int, def: Int? = null) : Int? {
    def?.let { return getLayoutDimension(id, it) }

    return if (v5Exist(id)) getLayoutDimension(id, 0) else null
}


fun TypedArray.v5GetText(@StyleableRes id: Int, def: CharSequence? = null) : CharSequence? {
    return getText(id) ?: def
}

fun TypedArray.v5GetString(@StyleableRes id: Int, def: String? = null) : String? {
    return v5GetText(id, def)?.toString()
}

fun TypedArray.v5GetColor(@StyleableRes id: Int, def: Int? = null) : Int? {
    def?.let { return getColor(id, def) }
    return if (v5Exist(id)) getColor(id, Color.BLACK) else null
}

fun TypedArray.v5GetDrawable(@StyleableRes id: Int, def: Drawable? = null) : Drawable? {
    return getDrawable(id) ?: def
}

fun TypedArray.v5GetBoolean(@StyleableRes id: Int, def: Boolean? = null) : Boolean? {
    def?.let { return getBoolean(id, def) }
    return if (v5Exist(id)) getBoolean(id, true) else null
}

fun TypedArray.v5GetInt(@StyleableRes id: Int, def: Int? = null) : Int? {
    def?.let { return getInt(id, def) }
    return if(v5Exist(id)) getInt(id, 0) else null
}

fun TypedArray.v5Exist(@StyleableRes id: Int) : Boolean {
    return getType(id) != TypedValue.TYPE_NULL
}

fun TypedArray.v5NotExist(@StyleableRes id: Int) : Boolean {
    return !v5Exist(id)
}