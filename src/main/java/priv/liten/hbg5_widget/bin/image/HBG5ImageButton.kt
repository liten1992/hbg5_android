package priv.liten.hbg5_widget.bin.image

import android.content.Context
import android.util.AttributeSet
import priv.liten.hbg.R
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.impl.button.HBG5ImageButtonImpl

open class HBG5ImageButton: HBG5ImageView, HBG5ImageButtonImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr,defStyleRes) {
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5ImageButton,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 狀態
                    buildCheckByAttr(
                        typedArray = typedArray,
                        checked = R.styleable.HBG5ImageButton_v5Checked,
                        checkable = R.styleable.HBG5ImageButton_v5Checkable
                    )
                },
                finish = {

                }
            )
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.HBG5_Theme_Base_ImageButton)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.hbg5ImageButtonStyle)

    constructor(context: Context) : this(context, null)


    // MARK:- ====================== Data


    // MARK:- ====================== Event
    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        mergeDrawableStates(drawableState,
            if(v5Checked) intArrayOf(android.R.attr.state_checked)
            else intArrayOf(-android.R.attr.state_checked)
        )
        return drawableState
    }

    // MARK:- ====================== Method
    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }
}