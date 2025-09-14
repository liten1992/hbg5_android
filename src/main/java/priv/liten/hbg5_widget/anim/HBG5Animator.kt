package priv.liten.hbg5_widget.anim

import android.os.Handler
import android.os.Looper
import android.view.animation.Interpolator

/** 動畫進度插值器 */
class HBG5Animator: Handler {

    constructor(): super(Looper.getMainLooper())


    /** 區間插值器  */
    var interpolator: Interpolator? = null

    /** 動畫是否執行中 */
    var isRunning: Boolean = false
        private set

    /** 上一次的呼叫時間  */
    private var startMillis: Long = 0

    /** 動畫時間  */
    var duration: Long = 3000

    /** 動畫啟動監聽器  */
    var onBeforeListener: Runnable? = null

    /** 動畫進度監聽器  */
    var onProgressListener: ((Float) -> Unit)? = null

    /** 動畫結束監聽器  */
    var onAfterListener: Runnable? = null

    private val runFunction: Runnable by lazy { Runnable {
        if (isRunning) {

            val nowDuration = System.currentTimeMillis() - startMillis

            val progress =
                if(nowDuration < 0 || nowDuration >= duration) 1f
                else (nowDuration.toDouble() / duration.toDouble()).toFloat()

            onProgressListener?.let { it(interpolator?.getInterpolation(progress) ?: progress) }

            if(progress >= 1f) {
                cancel()
                return@Runnable
            }

            postDelayed(runFunction, 15L)
        }
    } }

    fun start() {

        cancel()

        isRunning = true

        onBeforeListener?.run()

        onProgressListener?.let {
            it(interpolator?.getInterpolation(0f) ?: 0f)
        }

        startMillis = System.currentTimeMillis()

        runFunction.run()
    }

    fun cancel() {

        if(!isRunning) { return }

        isRunning = false

        onAfterListener?.run()

        removeCallbacks(runFunction)
    }
}