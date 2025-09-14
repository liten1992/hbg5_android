package priv.liten.hbg5_widget.bin.button

import android.content.Context
import android.util.AttributeSet
import priv.liten.hbg.R

open class HBG5CheckButton : HBG5Button {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.HBG5_Theme_Base_CheckButton)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)
}