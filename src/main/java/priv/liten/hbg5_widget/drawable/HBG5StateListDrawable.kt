package priv.liten.hbg5_widget.drawable

import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import java.lang.ref.WeakReference
/**特製的狀態圖像 限制了放入圖像的類別，從而通過類別控制繪製*/
class HBG5StateListDrawable: StateListDrawable {

    constructor(): super() {

    }

    constructor(
        corners: FloatArray, borderSize: Float,
        colorNormal:Int?,     borderNormal:Int?,
        colorPressed: Int?,   borderPressed:Int?,
        colorChecked: Int?,   borderChecked:Int?,
        colorUnable: Int?, borderUnable: Int?): super() {

        contents.normal =
            if(colorNormal != null || (borderSize > 0f && borderNormal != null)) HBG5RectDrawable(
                corners,
                colorNormal ?: Color.TRANSPARENT,
                borderNormal ?: Color.TRANSPARENT,
                borderSize)
            else null

        contents.pressed =
            if(colorPressed != null || (borderSize > 0f && borderPressed != null)) HBG5RectDrawable(
                corners,
                colorPressed ?: Color.TRANSPARENT,
                borderPressed ?: Color.TRANSPARENT,
                borderSize)
            else null

        contents.unable =
            if(colorUnable != null || (borderSize > 0f && borderUnable != null)) HBG5RectDrawable(
                corners,
                colorUnable ?: Color.TRANSPARENT,
                borderUnable ?: Color.TRANSPARENT,
                borderSize)
            else null

        contents.checked =
            if(colorChecked != null || (borderSize > 0f && borderChecked != null)) HBG5RectDrawable(
                corners,
                colorChecked ?: Color.TRANSPARENT,
                borderChecked ?: Color.TRANSPARENT,
                borderSize)
            else null

        contents.checked?.let { drawable ->
            this.addState(intArrayOf(android.R.attr.state_checked), drawable)
            this.addState(intArrayOf(android.R.attr.state_focused), drawable)
        }

        contents.unable?.let { drawable ->
            this.addState(intArrayOf(-android.R.attr.state_enabled), drawable)
        }

        contents.pressed?.let { drawable ->
            this.addState(intArrayOf(android.R.attr.state_pressed, android.R.attr.clickable), drawable)
        }

        contents.normal?.let { drawable ->
            this.addState(intArrayOf(), drawable)
        }
    }

    private var contents = Contents()

    /**
     * @return false:更新失敗 不匹配
     * */
    fun update(
        corners: FloatArray, borderSize: Float,
        colorNormal:Int?,     borderNormal:Int?,
        colorPressed: Int?,   borderPressed:Int?,
        colorChecked: Int?,   borderChecked:Int?,
        colorUnable: Int?, borderUnable: Int?
    ): Boolean {

        val normalExist = colorNormal != null || (borderSize > 0f && borderNormal != null)
        val pressedExist = colorPressed != null || (borderSize > 0f && borderPressed != null)
        val checkedExist = colorChecked != null || (borderSize > 0f && borderChecked != null)
        val unableExist = colorUnable != null || (borderSize > 0f && borderUnable != null)

        val normalPass = (normalExist && contents.normal != null) || (!normalExist && contents.normal == null)
        val pressedPass = (pressedExist && contents.pressed != null) || (!pressedExist && contents.pressed == null)
        val checkedPass = (checkedExist && contents.checked != null) || (!checkedExist && contents.checked == null)
        val unablePass = (unableExist && contents.unable != null) || (!unableExist && contents.unable == null)

        // 資料可以匹配動態更新
        if(normalPass && pressedPass && checkedPass && unablePass) {

            contents.normal?.update(
                corners = corners,
                fillColor = colorNormal ?: Color.TRANSPARENT,
                strokeColor = borderNormal ?: Color.TRANSPARENT,
                strokeSize = borderSize)

            contents.pressed?.update(
                corners = corners,
                fillColor = colorPressed ?: Color.TRANSPARENT,
                strokeColor = borderPressed ?: Color.TRANSPARENT,
                strokeSize = borderSize)

            contents.checked?.update(
                corners = corners,
                fillColor = colorChecked ?: Color.TRANSPARENT,
                strokeColor = borderChecked ?: Color.TRANSPARENT,
                strokeSize = borderSize)

            contents.unable?.update(
                corners = corners,
                fillColor = colorUnable ?: Color.TRANSPARENT,
                strokeColor = borderUnable ?: Color.TRANSPARENT,
                strokeSize = borderSize)

            this.invalidateSelf()

            return true
        }
        return false
    }

    class Contents {
        private var _normal: WeakReference<HBG5RectDrawable>? = null
        var normal: HBG5RectDrawable?
            get() = _normal?.get()
            set(value) {
                _normal = WeakReference(value)
            }

        private var _pressed: WeakReference<HBG5RectDrawable>? = null
        var pressed: HBG5RectDrawable?
            get() = _pressed?.get()
            set(value) {
                _pressed = WeakReference(value)
            }

        private var _checked: WeakReference<HBG5RectDrawable>? = null
        var checked: HBG5RectDrawable?
            get() = _checked?.get()
            set(value) {
                _checked = WeakReference(value)
            }

        private var _unable: WeakReference<HBG5RectDrawable>? = null
        var unable: HBG5RectDrawable?
            get() = _unable?.get()
            set(value) {
                _unable = WeakReference(value)
            }
    }
}