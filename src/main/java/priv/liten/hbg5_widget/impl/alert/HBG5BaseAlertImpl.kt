package priv.liten.hbg5_widget.impl.alert

import android.widget.FrameLayout
import priv.liten.hbg5_widget.bin.alert.HBG5BaseAlert
import priv.liten.hbg5_widget.bin.fragment.HBG5Fragment
import java.lang.ref.WeakReference

interface HBG5BaseAlertImpl {

    /** 可見性 */
    val v5Visible: Boolean
    /** 啟動視窗的所屬頁面 */
    var v5LaunchFragment: WeakReference<HBG5Fragment>

    /** 設置當可見性異動時監聽(系統用) */
    fun v5RegisterOnVisibleChangeBySystem(listener: ((HBG5BaseAlert)->Unit)?)
    /** 設置當可見性異動時監聽 @param listener(Alert) */
    fun v5RegisterOnVisibleChange(listener: ((HBG5BaseAlert)->Unit)?)

    /** 當顯示時觸發 */
    fun v5OnShow()
    /** 顯示 */
    fun v5Show(parent: FrameLayout, request: HBG5BaseAlert.DataRequest)
    /** 顯示 */
    fun v5Show(fragment: HBG5Fragment, request: HBG5BaseAlert.DataRequest) {
        v5LaunchFragment = WeakReference(fragment)
        v5Show(
            parent = fragment.v5AlertLayout,
            request = request)
    }
    /** 當隱藏時觸發 */
    fun v5OnHide()
    /** 隱藏 */
    fun v5Hide()

    /** 取消(隱藏) */
    fun v5Cancel()
    /** 確認(隱藏) */
    fun v5Confirm()

    /** 讀取設定 */
    fun v5LoadRequest(request: HBG5BaseAlert.DataRequest)
}