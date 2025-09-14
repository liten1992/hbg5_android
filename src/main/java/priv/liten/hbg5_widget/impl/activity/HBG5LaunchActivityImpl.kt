package priv.liten.hbg5_widget.impl.activity

import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import priv.liten.hbg5_widget.bin.fragment.HBG5Fragment


interface HBG5LaunchActivityImpl {
    /** 頁面生命週期 */
    var v5LifeStatus: HBG5Fragment.LifeStatus
    /** 套用頁面 */
    var v5ContentFragment: HBG5Fragment?
    /** 建立頁面 */
    fun v5CreateFragment(): HBG5Fragment
    /** 建立 */
    fun v5OnCreate()
    /** 銷毀 */
    fun v5OnDestroy()
    /** 返回 */
    fun v5OnBack()
    /** 重啟 */
    fun v5Restart()
}