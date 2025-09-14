package priv.liten.hbg5_widget.bin.button

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import androidx.core.graphics.alpha
import androidx.core.view.children
import priv.liten.hbg.R
import priv.liten.hbg5_extension.dpToPx
import priv.liten.hbg5_extension.getDimension
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetDimension
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_extension.hbg5.v5GetString
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.bin.layout.HBG5LinearLayout
import priv.liten.hbg5_widget.group.HBG5RadioGroup
import priv.liten.hbg5_widget.impl.base.HBG5CheckImpl
import kotlin.math.roundToInt

/**切換單選標籤*/
class HBG5Switch: HBG5LinearLayout {
    // MARK:- ====================== Constructor
    constructor(context:Context, attrs:AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {

        readTypedArray(
            attrs = attrs,
            styleable = R.styleable.HBG5Switch,
            defStyleAttr = defStyleAttr,
            defStyleRes = defStyleRes,
            read = { typedArray ->
                // todo hbg
                buttonStyle = typedArray.getResourceId(R.styleable.HBG5Switch_v5ButtonStyle, buttonStyle)
                textSize = typedArray.v5GetDimension(R.styleable.HBG5Switch_v5TextSize) ?: getDimension(priv.liten.hbg.R.dimen.hbg5_text_size_normal)
                options = typedArray.v5GetString(R.styleable.HBG5Switch_options)?.split("|")?.toList() ?: emptyList()
                checkedIndex = typedArray.v5GetInt(R.styleable.HBG5Switch_checkedIndex) ?: -1
                baselinePaint = typedArray.v5GetColor(R.styleable.HBG5Switch_v5BaseLineColor)?.let { lineColor ->
                    if(lineColor.alpha < 4) null
                    else Paint().also {
                        it.color = lineColor
                    }
                }
                baselineGravity = typedArray.v5GetInt(R.styleable.HBG5Switch_v5BaseLineGravity)
                when(typedArray.getBoolean(R.styleable.HBG5Switch_android_enabled, false)) {
                    true -> {
                        registerSwitchClick { true }
                    }
                    else -> { }
                }
            },
            finish = { })

        setWillNotDraw(false)

        // Event
        if(true) {
            optionRadio.registerOnCheckedChange { checkable, checked ->
                if(!checked) { return@registerOnCheckedChange }
                val index = optionRadio.checkedIndex
                if(index == -1) { return@registerOnCheckedChange }
                if(baselinePaint != null) { invalidate() } // todo hbg
                onSwitchChangeCallback?.let { it(index) }
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, R.style.HBG5_Theme_Base_Parent_Switch)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)



    // MARK:- ====================== Data
    /**允取編輯*/
    var editable: Boolean = true
        set(value) {
            field = value
            for(child in children) {
                child.isEnabled = field
            }
        }

    /**選取的項目索引*/
    var checkedIndex: Int
        get() { return optionRadio.checkedIndex }
        set(value) { optionRadio.check(value) }

    /**設置選項*/
    var options: List<String> = emptyList()
        set(value) {
            // 替換
            field = value

            this.orientation = HORIZONTAL
            // 新增選項
            val insertChildSize = field.size - this.childCount
            if(insertChildSize > 0) {
                for(index in 0 until insertChildSize) {
                    this.addView(HBG5RadioButton(context, null, 0, buttonStyle).also { uiChild ->
                        uiChild.v5Checkable = false
                        uiChild.v5TextSize = textSize
                        uiChild.layoutParams = LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f)
                        uiChild.v5RegisterClick { uiView ->
                            val checkable = uiView as? HBG5CheckImpl ?: return@v5RegisterClick
                            val checkedIndex = optionRadio.indexOf(checkable)
                            if(checkedIndex == -1) { return@v5RegisterClick }
                            // 允許狀態變動 自動調整
                            if(onSwitchClickCallback?.let { it(checkedIndex) } == true) {
                                hideKeyboard() // todo hbg
                                optionRadio.check(checkedIndex)
                            }
                        }
                    })
                }
            }
            // 移除選項
            else if(insertChildSize < 0) {
                val childSize = this.childCount
                for(index in 0 until -insertChildSize) {
                    this.removeViewAt(childSize - index - 1)
                }
            }
            // 初始化選項初始化
            optionRadio.clear()
            for((index, uiChild) in this.children.withIndex()) {
                if(uiChild !is HBG5RadioButton) { continue }
                uiChild.v5Checked = false
                uiChild.v5Text = field[index]
                uiChild.isEnabled = editable
                optionRadio.add(uiChild)
            }
            // 重繪選擇
            if(baselinePaint != null) { invalidate() } // todo hbg
        }

    /**文字大小*/
    var textSize: Float = 28f
        set(value) {
            field = value

            for(child in this.children) {
                val button = child as? HBG5RadioButton ?: continue
                button.v5TextSize = field
            }
        }

    /**按鈕樣式 todo hbg*/
    private var buttonStyle: Int = R.style.HBG5_Theme_Base_Switch

    /**單選*/
    private var optionRadio = HBG5RadioGroup()

    /**選取特效樣式*/
    private var baselinePaint: Paint? = null

    /**選取特效位置 0:Top 1:Bottom todo hbg*/
    private var baselineGravity: Int? = null


    // MARK:- ====================== Event
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(baselinePaint != null) { invalidate() } // todo hbg
    }

    override fun onDrawForeground(canvas: Canvas?) {
        super.onDrawForeground(canvas)
        canvas ?: return

        val width = width
        val height = height
        if(width <= 0 || height <= 0) { return }
        if(childCount <= 0) { return }
        val checkedIndex = checkedIndex
        if(0 > checkedIndex || checkedIndex >= childCount) { return }

        val baselinePaint = baselinePaint ?: return
        val baseLineHeight = 4f.dpToPx().roundToInt()
        val baseLineWidth = this.width / childCount

        val rectLeft = checkedIndex * baseLineWidth
        val rectRight = rectLeft + baseLineWidth
        val rectTop = when(baselineGravity) {
            // Top
            0 -> 0
            // Bottom
            else -> height - baseLineHeight
        }
        val rectBottom = rectTop + baseLineHeight

        canvas.drawRect(Rect(rectLeft, rectTop, rectRight, rectBottom), baselinePaint)
    }

    /**點擊選項
     * @param callback arg0(Int): 點擊的索引, return true 允許選取
     * */
    fun registerSwitchClick(callback: ((Int) -> Boolean)?) {
        onSwitchClickCallback = callback
    }
    private var onSwitchClickCallback: ((Int) -> Boolean)? = null

    /** 異動選項 */
    fun registerSwitchChange(callback: ((Int) -> Unit)?) {
        onSwitchChangeCallback = callback
    }
    private var onSwitchChangeCallback: ((Int) -> Unit)? = null
}