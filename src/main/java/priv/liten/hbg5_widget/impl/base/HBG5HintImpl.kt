package priv.liten.hbg5_widget.impl.base

import android.content.res.TypedArray
import android.graphics.Color
import android.widget.TextView
import androidx.annotation.StyleableRes
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetText

interface HBG5HintImpl {
    /** 提示 */
    var v5Hint:CharSequence?
        get() = (this as? TextView)?.hint
        set(value) {
            (this as? TextView)?.hint = value
        }
    /** 提示顏色 */
    var v5HintColor:Int
        get() = (this as? TextView)?.currentHintTextColor ?: Color.GRAY
        set(value) {
            (this as? TextView)?.setHintTextColor(value)
        }
    /** 更新XML */
    fun buildHintByAttr(
        typedArray: TypedArray,
        @StyleableRes hint: Int? = null,
        @StyleableRes hintColor: Int? = null,
    ) {
        v5Hint = hint?.let { typedArray.v5GetText(it) }
        v5HintColor = hintColor?.let { typedArray.v5GetColor(it) } ?: v5HintColor
    }

    fun refreshHint() { }
}