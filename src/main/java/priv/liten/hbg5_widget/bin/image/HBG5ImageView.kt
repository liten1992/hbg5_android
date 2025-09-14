package priv.liten.hbg5_widget.bin.image

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import priv.liten.hbg.R
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.image.HBG5ImageViewImpl
import kotlin.math.max

open class HBG5ImageView: AppCompatImageView, HBG5ImageViewImpl {
    // MARK:- ====================== Define
    companion object {
        val DRAWABLE_CACHE = mutableMapOf<String, Int>()
    }
    enum class Quality(val lv: Int) {
        HIGH(4), MEDIUM(2), LOW(1)
    }

    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr) {

        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5ImageView,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 元件
                    buildViewByAttr(
                        typedArray = typedArray,
                        touchable = R.styleable.HBG5ImageView_v5Touchable,
                        visibility = R.styleable.HBG5ImageView_v5Visibility,
                        padding = R.styleable.HBG5ImageView_v5Padding,
                        paddingStart = R.styleable.HBG5ImageView_v5PaddingStart,
                        paddingEnd = R.styleable.HBG5ImageView_v5PaddingEnd,
                        paddingTop = R.styleable.HBG5ImageView_v5PaddingTop,
                        paddingBottom = R.styleable.HBG5ImageView_v5PaddingBottom
                    )
                    // 背景
                    buildBackgroundByAttr(
                        typedArray = typedArray,
                        image = R.styleable.HBG5ImageView_v5BackgroundImage,
                        radius = R.styleable.HBG5ImageView_v5BackgroundRadius,
                        radiusLT = R.styleable.HBG5ImageView_v5BackgroundRadiusLT,
                        radiusRT = R.styleable.HBG5ImageView_v5BackgroundRadiusRT,
                        radiusRB = R.styleable.HBG5ImageView_v5BackgroundRadiusRB,
                        radiusLB = R.styleable.HBG5ImageView_v5BackgroundRadiusLB,
                        colorNormal = R.styleable.HBG5ImageView_v5BackgroundColor,
                        colorPressed = R.styleable.HBG5ImageView_v5BackgroundColorPressed,
                        colorChecked = R.styleable.HBG5ImageView_v5BackgroundColorChecked,
                        colorUnable = R.styleable.HBG5ImageView_v5BackgroundColorUnable,
                        borderSize = R.styleable.HBG5ImageView_v5BackgroundBorderSize,
                        borderColorNormal = R.styleable.HBG5ImageView_v5BackgroundBorderColor,
                        borderColorPressed = R.styleable.HBG5ImageView_v5BackgroundBorderColorPressed,
                        borderColorChecked = R.styleable.HBG5ImageView_v5BackgroundBorderColorChecked,
                        borderColorUnable = R.styleable.HBG5ImageView_v5BackgroundBorderColorUnable
                    )
                    // 圖像
                    buildImageByAttr(
                        typedArray = typedArray,
                        imageNormal = R.styleable.HBG5ImageView_v5Image,
                        imagePressed = R.styleable.HBG5ImageView_v5ImagePressed,
                        imageChecked = R.styleable.HBG5ImageView_v5ImageChecked,
                        imageUnable = R.styleable.HBG5ImageView_v5ImageUnable,
                        imageTintNormal = R.styleable.HBG5ImageView_v5ImageTint,
                        imageTintPressed = R.styleable.HBG5ImageView_v5ImageTintPressed,
                        imageTintChecked = R.styleable.HBG5ImageView_v5ImageTintChecked,
                        imageTintUnable = R.styleable.HBG5ImageView_v5ImageTintUnable,
                        imageScaleType = R.styleable.HBG5ImageView_v5ImageScaleType
                    )
                },
                finish = {
                    refreshView()
                    refreshBackground()
                    refreshImage()
                }
            )
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.hbg5ImageViewStyle)

    constructor(context: Context): this(context, null)

    // MARK:- ====================== Data

    // MARK:- ====================== Event
    override fun onCreateDrawableState(extraSpace: Int): IntArray? {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        mergeDrawableStates(
            drawableState,
            if (isClickable) intArrayOf(android.R.attr.clickable)
            else intArrayOf(-android.R.attr.clickable)
        )
        return drawableState
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        // MeasureSpec.EXACTLY
        // 1. layout给出了确定的值，比如：100dp
        // 2. layout使用的是match_parent，但父控件的size已经可以确定了，比如设置的是具体的值或者match_parent
        //
        // MeasureSpec.AT_MOST
        // 1. layout使用的是wrap_content
        // 2. layout使用的是match_parent,但父控件使用的是确定的值或者wrap_content
        //
        // UNSPECIFIED
        // 1. 没有任何限制，多半出现在自定义的父控件的情况下，期望由自控件自行决定大小
        // val widthMeasureSpec = widthMeasureSpec
        // val heightMeasureSpec = heightMeasureSpec
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        var widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        var heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        val image = drawable
        val imageW = image?.intrinsicWidth ?: 0
        val imageH = image?.intrinsicHeight ?: 0

        // 有一定比例
        if (imageW > 0 && imageH > 0 && scaleType == ScaleType.CENTER_CROP) {

            // 高度遷就寬度
            if (widthSpecMode == MeasureSpec.EXACTLY && (heightSpecMode == MeasureSpec.AT_MOST || heightSpecMode == MeasureSpec.UNSPECIFIED)) {
                val scale = imageH.toFloat() / imageW.toFloat()
                val contentWidth = widthSpecSize - paddingLeft - paddingRight
                heightSpecSize = max(0, paddingTop + paddingBottom)
                heightSpecSize += if (contentWidth > 0) (contentWidth * scale + 0.5f).toInt() else 0
                setMeasuredDimension(widthSpecSize, heightSpecSize)
                return
            }
            else if ((widthSpecMode == MeasureSpec.AT_MOST || widthSpecMode == MeasureSpec.UNSPECIFIED) && heightSpecMode == MeasureSpec.EXACTLY) {
                val scale = imageW.toFloat() / imageH.toFloat()
                val contentHeight = heightSpecSize - paddingTop - paddingBottom
                widthSpecSize = max(0, paddingLeft + paddingRight)
                widthSpecSize += if (contentHeight > 0) (contentHeight * scale + 0.5f).toInt() else 0
                setMeasuredDimension(widthSpecSize, heightSpecSize)
                return
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }


    // MARK:- ====================== Method
}