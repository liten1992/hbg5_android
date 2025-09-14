package priv.liten.hbg5_widget.bin.text

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatTextView
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_extension.readTypedArray

import priv.liten.hbg5_widget_impl.impl.text.HBG5TextViewImpl

/** V5文字顯示元件 */
open class HBG5TextView: AppCompatTextView, HBG5TextViewImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr) {
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5TextView,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5TextView_v5Touchable,
                        visibility = R.styleable.HBG5TextView_v5Visibility,
                        padding = R.styleable.HBG5TextView_v5Padding,
                        paddingStart = R.styleable.HBG5TextView_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5TextView_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5TextView_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5TextView_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5TextView_v5BackgroundImage,
                        radius = R.styleable.HBG5TextView_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5TextView_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5TextView_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5TextView_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5TextView_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5TextView_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5TextView_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5TextView_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5TextView_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5TextView_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5TextView_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5TextView_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5TextView_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5TextView_v5BackgroundBorderColorUnable
                    )
                    // 狀態
                    buildCheckByAttr(
                        typedArray = typedArray,
                        checked = R.styleable.HBG5TextView_v5Checked,
                        checkable = R.styleable.HBG5TextView_v5Checkable
                    )
                    // 文字
                    buildTextByAttr(
                        typedArray = typedArray,
                        textNormal = R.styleable.HBG5TextView_v5Text,
                        textChecked = R.styleable.HBG5TextView_v5TextChecked,
                        textSize = R.styleable.HBG5TextView_v5TextSize,
                        textColorNormal = R.styleable.HBG5TextView_v5TextColor,
                        textColorPressed = R.styleable.HBG5TextView_v5TextColorPressed,
                        textColorChecked = R.styleable.HBG5TextView_v5TextColorChecked,
                        textColorUnable = R.styleable.HBG5TextView_v5TextColorUnable,
                        textAlignmentHorizontal = R.styleable.HBG5TextView_v5TextAlignmentHorizontal,
                        textAlignmentVertical = R.styleable.HBG5TextView_v5TextAlignmentVertical
                    )
                    // 提示
                    buildHintByAttr(
                        typedArray = typedArray,
                        hint = R.styleable.HBG5TextView_v5Hint,
                        hintColor = R.styleable.HBG5TextView_v5HintColor
                    )
                },
                finish = {
                    refreshBackground()
                    refreshTextState()
                    refreshTextColor()
                    refreshHint()
                }
            )
        }

        // Event
        if(true) {
            // 2024-07-26 防止鍵盤按下確定後找不到下個焦點目標拋出例外
            setOnEditorActionListener { v, actionId, event ->
                when(actionId) {
                    EditorInfo.IME_ACTION_GO,
                    EditorInfo.IME_ACTION_DONE,
                    EditorInfo.IME_ACTION_NEXT,
                    EditorInfo.IME_ACTION_NONE,
                    EditorInfo.IME_ACTION_SEARCH,
                    EditorInfo.IME_ACTION_SEND,
                    EditorInfo.IME_ACTION_PREVIOUS,
                    EditorInfo.IME_ACTION_UNSPECIFIED -> hideKeyboard()
                }
                true
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.HBG5_Theme_Base_TextView)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.hbg5TextViewStyle)

    constructor(context: Context) : this(context, null)


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
}