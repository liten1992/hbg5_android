package priv.liten.hbg5_widget.bin.image

import android.content.Context
import android.util.AttributeSet
import priv.liten.hbg.R
/**圖像勾選按鈕*/
class HBG5ImageCheckButton: HBG5ImageButton {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr,defStyleRes)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.HBG5_Theme_Base_ImageButton)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.hbg5ImageButtonStyle)
    constructor(context: Context) : this(context, null)

    // MARK:- ====================== Method
    override fun v5Toggle() {
        if (v5Checkable) {
            isChecked = !isChecked
        }
    }
}