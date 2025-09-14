package priv.liten.hbg5_widget.bin.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import priv.liten.hbg.R
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl
import priv.liten.hbg5_widget.impl.image.HBG5ImageViewImpl
import kotlin.math.max

open class HBG5View: AppCompatImageView, HBG5ViewImpl, HBG5BackgroundImpl {
    // MARK:- ====================== Define


    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr) {
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5View,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5View_v5Touchable,
                        visibility = R.styleable.HBG5View_v5Visibility,
                        padding = R.styleable.HBG5View_v5Padding,
                        paddingStart = R.styleable.HBG5View_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5View_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5View_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5View_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5View_v5BackgroundImage,
                        radius = R.styleable.HBG5View_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5View_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5View_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5View_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5View_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5View_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5View_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5View_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5View_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5View_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5View_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5View_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5View_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5View_v5BackgroundBorderColorUnable
                    )
                },
                finish = {
                    refreshView()
                    refreshBackground()
                }
            )
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null)


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

    // MARK:- ====================== Method
}