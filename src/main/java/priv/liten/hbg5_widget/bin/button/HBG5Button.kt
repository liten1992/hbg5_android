package priv.liten.hbg5_widget.bin.button

import android.content.Context
import android.util.AttributeSet
import priv.liten.hbg.R
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.impl.button.HBG5ButtonImpl
import priv.liten.hbg5_widget.bin.text.HBG5TextView

/** 按鈕元件 */
open class HBG5Button: HBG5TextView, HBG5ButtonImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5Button,
                defStyleAttr = defStyleAttr,
                defStyleRes = defStyleRes,
                read = { typedArray ->
                    // 圖像
                    buildButtonByAttr(
                        typedArray = typedArray,
                        images = R.styleable.HBG5Button_v5Images,
                        imageNormal = R.styleable.HBG5Button_v5Image,
                        imagePressed = R.styleable.HBG5Button_v5ImagePressed,
                        imageChecked = R.styleable.HBG5Button_v5ImageChecked,
                        imageAlignment = R.styleable.HBG5Button_v5ImageAlignment,
                        imageWidth = R.styleable.HBG5Button_v5ImageWidth,
                        imageHeight = R.styleable.HBG5Button_v5ImageHeight,
                        imagePadding = R.styleable.HBG5Button_v5ImagePadding,
                        imageTintNormal = null,
                        imageTintPressed = null,
                        imageTintChecked = null)
                },
                finish = {
                    refreshButton()
                }
            )
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.HBG5_Theme_Base_Button)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)



    // MARK:- ====================== Method
    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }
}