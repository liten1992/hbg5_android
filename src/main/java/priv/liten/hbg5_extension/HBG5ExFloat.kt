package priv.liten.hbg5_extension

import android.content.res.Resources
import android.util.TypedValue
import kotlin.math.withSign

/** 文字單位 轉換 像素單位 */
fun Float.spToPx(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)
}
/** 布局單位 轉換 像素單位 */
fun Float.dpToPx(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
}

/** 範圍內的浮點數(包含 MIN MAX) */
fun Float.withIn(min: Float, max: Float): Float {
    return when {
        min > this -> min
        this > max -> max
        else -> this
    }
}

/** 範圍內的浮點數(包含 MIN MAX) */
fun Float.isWithIn(min: Float, max: Float): Boolean {
    return this in min..max
}