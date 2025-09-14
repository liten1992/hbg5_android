package priv.liten.hbg5_widget.bin.text

import android.content.Context
import android.text.*
import android.text.TextUtils.TruncateAt
import android.text.method.ArrowKeyMovementMethod
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import androidx.appcompat.widget.AppCompatTextView
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.*
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.impl.text.HBG5TextInputImpl

open class HBG5TextInput : AppCompatTextView, HBG5TextInputImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr) {
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5TextInput,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5TextInput_v5Touchable,
                        visibility = R.styleable.HBG5TextInput_v5Visibility,
                        padding = R.styleable.HBG5TextInput_v5Padding,
                        paddingStart = R.styleable.HBG5TextInput_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5TextInput_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5TextInput_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5TextInput_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5TextInput_v5BackgroundImage,
                        radius = R.styleable.HBG5TextInput_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5TextInput_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5TextInput_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5TextInput_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5TextInput_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5TextInput_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5TextInput_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5TextInput_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5TextInput_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5TextInput_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5TextInput_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5TextInput_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5TextInput_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5TextInput_v5BackgroundBorderColorUnable
                    )
                    // 文字
                    buildTextByAttr(
                        typedArray = typedArray,
                        textNormal = R.styleable.HBG5TextInput_v5Text,
                        textChecked = null,
                        textSize = R.styleable.HBG5TextInput_v5TextSize,
                        textColorNormal = R.styleable.HBG5TextInput_v5TextColor,
                        textColorPressed = R.styleable.HBG5TextInput_v5TextColorPressed,
                        textColorChecked = R.styleable.HBG5TextInput_v5TextColorChecked,
                        textColorUnable = R.styleable.HBG5TextInput_v5TextColorUnable,
                        textAlignmentHorizontal = R.styleable.HBG5TextInput_v5TextAlignmentHorizontal,
                        textAlignmentVertical = R.styleable.HBG5TextInput_v5TextAlignmentVertical
                    )
                    // 提示
                    buildHintByAttr(
                        typedArray = typedArray,
                        hint = R.styleable.HBG5TextInput_v5Hint,
                        hintColor = R.styleable.HBG5TextInput_v5HintColor
                    )
                    // 輸入
                    buildInputByAttr(
                        typedArray = typedArray,
                        inputSecurityEnable = R.styleable.HBG5TextInput_v5TextInputSecurityEnable,
                        inputType = R.styleable.HBG5TextInput_v5TextInputType
                    )
                },
                finish = {
                    refreshBackground()
                    refreshTextState()
                    refreshTextColor()
                    refreshHint()
                    refreshInput()
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

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.HBG5_Theme_Base_TextInput)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.hbg5TextInputStyle)

    constructor(context: Context) : this(context, null)


    // MARK:- ====================== Data
    override fun getFreezesText(): Boolean {
        return true
    }

    override fun getDefaultEditable(): Boolean {
        return true
    }

    override fun getDefaultMovementMethod(): MovementMethod? {
        return ArrowKeyMovementMethod.getInstance()
    }

    override fun getAccessibilityClassName(): CharSequence? {
        return HBG5TextInput::class.java.name
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, BufferType.EDITABLE)
    }

    override fun setEllipsize(ellipsis: TruncateAt) {
        require(ellipsis != TextUtils.TruncateAt.MARQUEE) {
            ("HBG5TextInput cannot use the ellipsize mode "
                    + "TextUtils.TruncateAt.MARQUEE")
        }
        super.setEllipsize(ellipsis)
    }

    // MARK:- ====================== Event
    // MARK:- ====================== Event
    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        mergeDrawableStates(drawableState,
            if(isClickable) intArrayOf(android.R.attr.clickable)
            else intArrayOf(-android.R.attr.clickable)
        )
        return drawableState
    }

    // MARK:- ====================== Method
    fun select(start: Int, stop: Int) {
        val text = text as? Editable ?: return
        Selection.setSelection(text, start, stop)
    }

    fun select(index: Int) {
        val text = text as? Editable ?: return
        Selection.setSelection(text, index)
    }

    fun selectAll() {
        val text = text as? Editable ?: return
        Selection.selectAll(text)
    }
}