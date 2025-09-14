package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.view.ViewGroup
import android.view.animation.*
import android.widget.FrameLayout
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_extension.removeFromSuperview
import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import priv.liten.hbg5_widget.bin.fragment.HBG5Fragment
import priv.liten.hbg5_widget.bin.layout.HBG5FrameLayout
import priv.liten.hbg5_widget.impl.alert.HBG5BaseAlertImpl
import java.lang.ref.WeakReference
/**基礎彈窗*/
open class HBG5BaseAlert: HBG5FrameLayout, HBG5BaseAlertImpl {

    //===================== Constructor
    constructor(context: Context): super(context) {

        isClickable = true
        isFocusable = true
        isFocusableInTouchMode = false

        this.setOnClickListener {
            v5Cancel()
        }
    }


    //===================== View
    override var v5LaunchFragment = WeakReference<HBG5Fragment>(null)


    //===================== Data
    override val v5Visible: Boolean get() { return parent != null }


    //===================== Event
    private var onVisibleChangeListenerBySystem: ((HBG5BaseAlert) -> Unit)? = null
    override fun v5RegisterOnVisibleChangeBySystem(listener: ((HBG5BaseAlert) -> Unit)?) {
        onVisibleChangeListenerBySystem = listener
    }

    private var onVisibleChangeListener: ((HBG5BaseAlert) -> Unit)? = null
    override fun v5RegisterOnVisibleChange(listener: ((HBG5BaseAlert) -> Unit)?) {
        onVisibleChangeListener = listener
    }

    override fun v5OnShow() {}

    override fun v5OnHide() {}


    //===================== Method
    override fun v5LoadRequest(request: DataRequest) { }

    override fun v5Show(parent: FrameLayout, request: DataRequest) {

        if (v5Visible) { return }

        parent.addView(
            this,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT)

        if (requestFocus()) {
            this.hideKeyboard()
        }

        // Start animation
        run {
            this.animation = null
            createShowAnimation()
                ?.also { anim ->
                    this.startAnimation(anim)
                }
                ?:run {
                    this.clearAnimation()
                }
        }

        onVisibleChangeListenerBySystem?.let { it(this) }
        onVisibleChangeListener?.let { it(this) }

        v5LoadRequest(request)

        v5OnShow()
    }

    override fun v5Hide() {

        if (!v5Visible) { return }

        if (requestFocus()) {
            this.hideKeyboard()
        }

        this.clearAnimation()
        this.removeFromSuperview()

        // End animation
        run {
            this.animation = null
        }

        onVisibleChangeListenerBySystem?.let { it(this) }
        onVisibleChangeListenerBySystem = null
        onVisibleChangeListener?.let { it(this) }
        onVisibleChangeListener = null

        v5OnHide()
    }

    override fun v5Cancel() { v5Hide() }

    override fun v5Confirm() { v5Hide() }

    open fun createShowAnimation() : Animation? {

        val amSet = AnimationSet(false)
        amSet.duration = 150

        run {
            val am = AlphaAnimation(0.2f, 1f)
            am.interpolator = DecelerateInterpolator()

            amSet.addAnimation(am)
        }

        run {
            val am = ScaleAnimation(
                1.05f, 1.00f,
                1.05f, 1.00f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f)
            am.interpolator = DecelerateInterpolator()

            amSet.addAnimation(am)
        }

        return amSet
    }

    //===================== Class
    /** 請求內容 */
    open class DataRequest
    /** 請求結果 */
    open class DataResponse
}