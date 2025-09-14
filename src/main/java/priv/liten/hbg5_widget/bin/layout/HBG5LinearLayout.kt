package priv.liten.hbg5_widget.bin.layout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import priv.liten.hbg.R
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.impl.layout.HBG5LinearLayoutImpl

/** 線性布局 */
open class HBG5LinearLayout : LinearLayout, HBG5LinearLayoutImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr) {
        // Data
        run {
            isMotionEventSplittingEnabled = false
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5LinearLayout,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5LinearLayout_v5Touchable,
                        visibility = R.styleable.HBG5LinearLayout_v5Visibility,
                        padding = R.styleable.HBG5LinearLayout_v5Padding,
                        paddingStart = R.styleable.HBG5LinearLayout_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5LinearLayout_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5LinearLayout_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5LinearLayout_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5LinearLayout_v5BackgroundImage,
                        radius = R.styleable.HBG5LinearLayout_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5LinearLayout_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5LinearLayout_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5LinearLayout_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5LinearLayout_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5LinearLayout_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5LinearLayout_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5LinearLayout_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5LinearLayout_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5LinearLayout_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5LinearLayout_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5LinearLayout_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5LinearLayout_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5LinearLayout_v5BackgroundBorderColorUnable
                    )
                    // 容器
                    buildLinearLayoutByAttr(
                        typedArray = typedArray,
                        dividerSize = R.styleable.HBG5LinearLayout_v5DividerSize,
                        dividerColor = R.styleable.HBG5LinearLayout_v5DividerColor
                    )
                },
                finish = {
                    refreshBackground()
                    refreshLinearLayout()
                }
            )
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.hbg5LinearLayoutStyle)

    constructor(context: Context) : this(context, null)


    // MARK:- ====================== Data


    // MARK:- ====================== Event
    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        mergeDrawableStates(drawableState,
            if(isClickable) intArrayOf(android.R.attr.clickable)
            else intArrayOf(-android.R.attr.clickable)
        )
        return drawableState
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (visibility != VISIBLE) {
            true
        } else {
            super.onInterceptTouchEvent(ev)
        }
    }
}