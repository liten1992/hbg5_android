package priv.liten.hbg5_widget.bin.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import priv.liten.hbg.R
import priv.liten.hbg5_extension.dpToPx
import priv.liten.hbg5_extension.getColor
import priv.liten.hbg5_extension.readTypedArray
import priv.liten.hbg5_widget.group.HBG5RadioGroup
import priv.liten.hbg5_widget.impl.layout.HBG5TabLayoutImpl

/**整合了指示條布局元件*/
class HBG5TabLayout: HBG5LinearLayout, HBG5TabLayoutImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {
        // Data
        run {
            readTypedArray(
                attrs = attrs,
                styleable = R.styleable.HBG5TabLayout,
                defStyleAttr = defStyleAttr,
                defStyleRes = 0,
                read = { typedArray ->
                    // 標籤
                    buildTabLayoutByAttr(
                        typedArray = typedArray,
                        tabIndex = R.styleable.HBG5TabLayout_v5TabIndex,
                        tabLineBackgroundColor = R.styleable.HBG5TabLayout_v5TabLineBackgroundColor,
                        tabLineColors = R.styleable.HBG5TabLayout_v5TabLineColors
                    )
                },
                finish = {
                    refreshTabLayout()
                }
            )
        }

        // Event
        run {
            uiTabGroup.registerOnCheckedChange { checkable, checked ->
                if(checked) {
                    val checkedIndex = uiTabGroup.checkedIndex
                    v5SetTag(R.id.attr_tab_index, checkedIndex)
                    onTabChange(checkedIndex)
                    startTabAnimation()
                    onTabChangeListener?.let { it(checkedIndex) }
                }
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, R.attr.hbg5TabLayoutStyle)

    constructor(context: Context) : this(context, null)


    // MARK:- ====================== View
    /**標籤單選約束*/
    val uiTabGroup = HBG5RadioGroup()
    /**標籤選取指示布局*/
    val uiTabLayout: FrameLayout by lazy {
        FrameLayout(context).also {
            it.addView(uiTabView, 0, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
    /**標籤指示*/
    val uiTabView: View by lazy { View(context) }
    /**內容頁面*/
    val uiContentLayout: FrameLayout by lazy {
        FrameLayout(context).also {
            it.layoutParams = LayoutParams(
                LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ))
        }
    }


    // MARK:- ====================== Data


    // MARK:- ====================== Event
    // MARK:- ====================== Event
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startTabAnimation()
    }
    /**頁面標籤變換時*/
    fun onTabChange(checkedIndex: Int) {
        // 基礎為TabLayout & MarkLayout
        if(this.childCount <= 2) { return }
        val childCount = uiContentLayout.childCount
        if(childCount <= 0) { return }
        for(i in 0 until childCount) {
            uiContentLayout.getChildAt(i).visibility = if(i != checkedIndex) GONE else VISIBLE
        }
    }
    /**註冊標籤索引變更*/
    private var onTabChangeListener: ((Int) -> Unit)? = null
    fun registerTabChange(listener: ((Int) -> Unit)?) {
        onTabChangeListener = listener
    }
    /**播放標籤異動效果*/
    private val onTabAnimationStart = Runnable {
        val checkIndex = uiTabGroup.checkedIndex
        val checkView = uiTabGroup.checkedItem as? View ?: return@Runnable
        val oldX = uiTabView.translationX
        uiTabView.setBackgroundColor(v5TabLineColors.getOrNull(checkIndex) ?: getColor(R.color.hbg5_dark))
        uiTabView.layoutParams = uiTabView.layoutParams.also { it.width = checkView.width }
        uiTabView.translationX = checkView.x
        uiTabView.startAnimation(TranslateAnimation(
            Animation.ABSOLUTE,
            oldX - checkView.x,
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f,
            Animation.ABSOLUTE,
            0f,
        ).apply { duration = 200 })
    }

    // MARK:- ====================== Method
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if(childCount == 0) {
            if(child !is ViewGroup) { return }
            super.addView(child, index, params)
            uiTabGroup.set(child)
            if(v5TabIndex != -1) {
                uiTabGroup.check(v5TabIndex)
            }
            // 第一個元件建立指示器 編輯模式下無法正確獲取元件尺寸，使用線性布局做視覺演算
            if(isInEditMode) {
                val uiEditMarkLayout = HBG5LinearLayout(context)
                uiEditMarkLayout.orientation = HORIZONTAL
                uiEditMarkLayout.background = this.background

                val markCount = child.childCount
                if(markCount > 0) {
                    val checkedIndex = uiTabGroup.checkedIndex
                    for(i in 0 until markCount) {
                        val uiMark = View(context)
                        if(i == checkedIndex) {
                            uiMark.visibility = VISIBLE
                            uiMark.setBackgroundColor(v5TabLineColors.getOrNull(uiTabGroup.checkedIndex) ?: getColor(R.color.hbg5_dark))
                        }
                        else {
                            uiMark.visibility = INVISIBLE
                        }
                        uiEditMarkLayout.addView(uiMark, LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
                    }
                }
                uiEditMarkLayout.setBackgroundColor(v5TabLineBackgroundColor)
                super.addView(uiEditMarkLayout, -1, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4f.dpToPx().toInt(), 0f))
            }
            else {
                uiTabLayout.setBackgroundColor(v5TabLineBackgroundColor)
                super.addView(uiTabLayout, -1, LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4f.dpToPx().toInt(), 0f))
            }
            startTabAnimation()
        }
        // 後面新增的元件視為
        else {
            if(child == null) { return }
            if(super.indexOfChild(uiContentLayout) == -1) {
                super.addView(uiContentLayout, -1, uiContentLayout.layoutParams)
            }
            child.visibility = if(uiContentLayout.childCount != uiTabGroup.checkedIndex) GONE else VISIBLE
            uiContentLayout.addView(
                child,
                ViewGroup.LayoutParams.MATCH_PARENT,
                params?.height ?: ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }
    /**播放標籤位移動畫*/
    fun startTabAnimation() {
        if(isInEditMode) { return }

        val checkView = uiTabGroup.checkedItem as? View

        if(checkView == null) {
            uiTabView.visibility = INVISIBLE
        }
        else {
            uiTabView.visibility = VISIBLE
            uiTabView.removeCallbacks(onTabAnimationStart)
            uiTabView.clearAnimation()
            uiTabView.postDelayed(onTabAnimationStart, 16)
        }
    }
}