package priv.liten.hbg5_widget.bin.layout

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import priv.liten.hbg.R
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import priv.liten.hbg5_widget.impl.layout.HBG5FrameLayoutImpl
import kotlin.math.max
import kotlin.math.roundToInt

/** 絕對位置布局 */
open class HBG5FrameLayout : FrameLayout, HBG5FrameLayoutImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs:AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr) {
        // Data
        run {
            isMotionEventSplittingEnabled = false
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5FrameLayout,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5FrameLayout_v5Touchable,
                        visibility = R.styleable.HBG5FrameLayout_v5Visibility,
                        padding = R.styleable.HBG5FrameLayout_v5Padding,
                        paddingStart = R.styleable.HBG5FrameLayout_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5FrameLayout_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5FrameLayout_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5FrameLayout_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5FrameLayout_v5BackgroundImage,
                        radius = R.styleable.HBG5FrameLayout_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5FrameLayout_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5FrameLayout_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5FrameLayout_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5FrameLayout_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5FrameLayout_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5FrameLayout_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5FrameLayout_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5FrameLayout_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5FrameLayout_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5FrameLayout_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5FrameLayout_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5FrameLayout_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5FrameLayout_v5BackgroundBorderColorUnable
                    )
                    // 狀態
                    buildCheckByAttr(
                        typedArray = typedArray,
                        checked = R.styleable.HBG5FrameLayout_v5Checked,
                        checkable = R.styleable.HBG5FrameLayout_v5Checkable
                    )
                    // 元件
                    buildFrameLayoutByAttr(
                        typedArray = typedArray,
                        measureScale = R.styleable.HBG5FrameLayout_v5Scale
                    )
                },
                finish = {
                    refreshBackground()
                    refreshFrameLayout()
                }
            )
        }
    }

    constructor(context: Context, attrs:AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.hbg5FrameLayoutStyle)

    constructor(context: Context): this(context, null)


    // MARK:- ====================== Data


    // MARK:- ====================== Event
    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 2)
        mergeDrawableStates(drawableState,
            if(isClickable) intArrayOf(android.R.attr.clickable)
            else intArrayOf(-android.R.attr.clickable)
        )
        mergeDrawableStates(drawableState,
            if(v5Checked) intArrayOf(android.R.attr.state_checked)
            else intArrayOf(-android.R.attr.state_checked)
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // MeasureSpec.EXACTLY
        // 1. layout给出了确定的值，比如：100dp
        // 2. layout使用的是match_parent，但父控件的size已经可以确定了，比如设置的是具体的值或者match_parent
        //
        // MeasureSpec.AT_MOST
        // 1. layout使用的是wrap_content
        // 2. layout使用的是match_parent,但父控件使用的是确定的值或者wrap_content

        v5MeasureScale
            ?.let { scale ->

                val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
                var widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
                val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
                var heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

                // 高度遷就寬度
                if (widthSpecMode == MeasureSpec.EXACTLY && (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED)) {
                    val contentWidth = widthSpecSize - paddingLeft - paddingRight
                    heightSpecSize = max(0, paddingTop + paddingBottom)
                    heightSpecSize += if (contentWidth > 0) (contentWidth * scale).roundToInt() else 0

                    setMeasuredDimension(widthSpecSize, heightSpecSize)
                    super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY))
                    return
                }
                // 寬度遷就高度
                else if (heightSpecMode == MeasureSpec.EXACTLY && (widthSpecMode == MeasureSpec.AT_MOST || widthSpecMode == MeasureSpec.UNSPECIFIED)) {
                    val contentHeight = heightSpecSize - paddingTop - paddingBottom
                    widthSpecSize = max(0, paddingLeft + paddingRight)
                    widthSpecSize += if (contentHeight > 0) (contentHeight * scale).roundToInt() else 0

                    setMeasuredDimension(widthSpecSize, heightSpecSize)
                    super.onMeasure(MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY), heightMeasureSpec)
                    return
                }
            }
            ?:run { }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    // MARK:- ====================== Method
}
