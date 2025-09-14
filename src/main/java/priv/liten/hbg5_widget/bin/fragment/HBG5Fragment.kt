package priv.liten.hbg5_widget.bin.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.annotation.DrawableRes
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import priv.liten.hbg.BuildConfig
import priv.liten.hbg.R
import priv.liten.hbg5_data.HBG5Data
import priv.liten.hbg5_extension.*
import priv.liten.hbg5_http.HBG5DownloadCall
import priv.liten.hbg5_widget.application.HBG5Application
import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import priv.liten.hbg5_widget.bin.alert.*
import priv.liten.hbg5_widget.bin.button.HBG5NavigationButton
import priv.liten.hbg5_widget.bin.button.HBG5Switch
import priv.liten.hbg5_widget.bin.layout.HBG5FrameLayout
import priv.liten.hbg5_widget.bin.layout.HBG5LinearLayout
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.group.HBG5RadioGroup
import priv.liten.hbg5_widget.impl.fragment.HBG5FragmentImpl
import priv.liten.hbg5_widget.impl.tab.HBG5FragmentTabImpl
import java.io.File
import java.lang.ref.WeakReference
import java.util.Calendar

/** 此頁面直接整合 導覽頁 */
@Suppress("NAME_SHADOWING", "UNUSED_ANONYMOUS_PARAMETER")
@SuppressLint("ViewConstructor")
open class HBG5Fragment: HBG5FrameLayout, HBG5FragmentImpl, LifecycleOwner {

    // MARK:- ====================== Define
    companion object {
        /** 彈出窗快取 */
        private val ALERT_CACHE: MutableMap<String, MutableList<HBG5BaseAlert>> = HashMap()
        /** 生物辨識等待彈窗 */
        private var BIOMETRIC_ALERT: MutableMap<Int, BiometricPrompt> = mutableMapOf()
        // todo hbg delete /** 隨機檔案名稱 */
//        val URI_GET_RANDOM_NAME: String
//            get() {
//                val randCode = mutableListOf<Char>()
//                for(index in 0 until 4) {
//                    randCode.add('a' + (0 until 26).random())
//                }
//                return "${Calendar.getInstance().toString(format = "yyyyMMddHHmmss")}${randCode.toLinkString(linkText = "")}"
//            }
        // todo hbg delete /** 從相機取得照片時的臨時相片儲存位置 */
//        val URI_GET_CAMERA_IMAGE: Uri?
//            get() {
//                val context = HBG5Application.instance ?: return null
//
//                return File(
//                    context.externalCacheDir,
//                    "${URI_GET_RANDOM_NAME}_camera.jpg")
//                    .toUri(context = context)
//            }
        // todo hbg delete /** 從相機取得照片時的臨時影片儲存位置 */
//        val URI_GET_CAMERA_VIDEO: Uri?
//            get() {
//                val context = HBG5Application.instance ?: return null
//
//                return File(
//                    context.externalCacheDir,
//                    "${URI_GET_RANDOM_NAME}_camera.mp4")
//                    .toUri(context = context)
//            }

        /** 自動隱藏全螢幕的子頁面(性能優化 但會有閃爍問題) */
        val AUTO_HIDE_FULL_SCREEN = false
        /** 紀錄畫面創建流水號 */
        var Count = 0
    }

    // MARK:- ====================== Constructor
    constructor(context: Context, dataRequest: DataRequest?): super(context) {
        v5Request = dataRequest
        v5OnUIRoot()
    }

    override fun v5OnUIRoot() { }

    override fun v5OnStatusCreated() {

        logLife("onStatusCreated")
        _lifecycle.currentState = Lifecycle.State.CREATED

        // View
        run {

        }
        // Data
        run {
            // 防止點擊穿透
            this.isFocusable = true
            this.isClickable = true
        }
        // Event
        run {
            // todo hbg
            when(val tabLayout = tabLayout) {
                is HBG5Switch -> {
                    tabLayout.registerSwitchClick { true }
                    tabLayout.registerSwitchChange { index ->

                        val dataResponse = data.response!!

                        v5ChildTab = dataResponse.childTabList.getOrNull(index)
                            ?: return@registerSwitchChange
                    }
                }
                else -> {
                    tabGroup.registerOnCheckedChange { _, checked ->
                        if (!checked) {
                            return@registerOnCheckedChange
                        }

                        val dataResponse = data.response!!

                        v5ChildTab = dataResponse.childTabList.getOrNull(tabGroup.checkedIndex)
                            ?: return@registerOnCheckedChange
                    }
                }
            }
        }
        // Init
        run {
            data.request = v5Request?.also { dataRequest ->
                v5ChildTabList = dataRequest.childTabList
                v5ChildTab = v5Request?.childTab
            }
        }
    }

    override fun v5OnStatusResume() {

        logLife("onStatusResume")
        _lifecycle.currentState = Lifecycle.State.STARTED
        _lifecycle.currentState = Lifecycle.State.RESUMED

        v5WaitResultCode
            ?.let { code ->

                logLife("onFragmentResult")

                v5WaitResultListener?.let {
                    v5WaitResultListener = null
                    it(code, v5WaitResultData)
                }
                v5WaitResultCode = null
                v5WaitResultData = null
                v5WaitFragment = null
            }
            ?:run {
                v5WaitFragment?.let { v5StartFragment(it, v5WaitResultListener) }
            }

        when(val waitChildTab = data.response!!.waitChildTab) {
            null -> { }
            else -> v5ChildTab = waitChildTab
        }

        hideKeyboard()

    }

    override fun v5OnStatusPause() {
        logLife("onStatusPause")
        _lifecycle.currentState = Lifecycle.State.STARTED
        _lifecycle.currentState = Lifecycle.State.CREATED
    }

    override fun v5OnStatusDestroy() {
        logLife("onStatusDestroy")
        _lifecycle.currentState = Lifecycle.State.DESTROYED
        removeFromSuperview()
    }

    // MARK:- ====================== View
    override val v5LaunchFragment: HBG5Fragment
        get() {
        return weakLaunchFragment?.get() ?: this
    }

    override val v5FinalFragment: HBG5Fragment
        get() {
        var result = this

        while (true) {
            result = result.v5NextFragment ?: return result.v5ChildFragment?.v5FinalFragment ?: result
        }
    }

    override var v5NavigationFragment: HBG5Fragment? = null

    override var v5RootFragment: HBG5Fragment? = null

    override var v5LastFragment: HBG5Fragment? = null

    override var v5NextFragment: HBG5Fragment? = null

    override var v5WaitFragment: HBG5Fragment? = null

    override val v5ParentLayout: ViewGroup by lazy {
        // 詭異優化 會被強制轉 HBG5Fragment導致錯誤
//        return@lazy findViewById(R.id.layout_parent) ?: this
        val view: ViewGroup? = findViewById(R.id.layout_parent)
        return@lazy view ?: this
    }

    override var v5ContentLayout: ViewGroup? = null

    override val v5AlertLayout: HBG5FrameLayout by lazy {
        HBG5FrameLayout(context).apply {
            v5BackgroundColor = 0x44000000
            isClickable = true
            isFocusable = true
            isFocusableInTouchMode = false
        }
    }

    override var v5Tab: Tab? = null

    override val v5ChildFragmentList: List<HBG5Fragment>
        get() {
            return childMap.values.toList()
        }

    override val v5ChildFragment: HBG5Fragment?
        get() {
            val dataResponse = data.response ?: return null
            val tab = dataResponse.childTab ?: return null
            return childMap[tab] ?: tab.childCreator(context, tab.dataRequest).also { child ->
                childMap[tab] = child
                child.weakLaunchFragment = WeakReference(this.v5LaunchFragment)
                child.v5LastFragment = this
                child.v5Tab = tab
                child.v5NavigationFragment = this
                child.v5RootFragment = child
            }
        }

    override val v5ChildLayout: ViewGroup? by lazy { findViewById(R.id.layout_child) }

    /** 請勿修改 */
    private var weakLaunchFragment: WeakReference<HBG5Fragment>? = null
    /** 讀取彈窗 */
    private var weakLoadingAlert: WeakReference<HBG5LoadingAlert> = WeakReference(null)
    /** 分頁按鈕容器 */
    private val tabLayout: HBG5LinearLayout? by lazy { findViewById(R.id.layout_tab) }
    /** 分頁歷史紀錄 <Key: Tab, Value: Fragment> */
    private val childMap = mutableMapOf<Tab, HBG5Fragment>()



    // MARK:- ====================== Data
    override var v5LifeStatus: LifeStatus = LifeStatus.NONE
        set(value) {
            val activity = context as? HBG5LaunchActivity ?: return

            when (value) {
                // 未建立
                LifeStatus.NONE -> { }
                // 建立
                LifeStatus.CREATED -> {
                    // 需要狀態 '未建立'
                    if (!v5IsLifeStatus(LifeStatus.NONE)) { return }

                    field = value

                    v5OnStatusCreated()
                }
                // 活躍
                LifeStatus.RESUME -> {
                    // 需要狀態 '建立' '暫停'
                    if (!v5IsLifeStatus(LifeStatus.CREATED, LifeStatus.PAUSE)) { return }

                    field = value
                    // 導覽頁面同步狀態
                    v5NavigationFragment?.v5LifeStatus = LifeStatus.RESUME
                    // 喚醒區塊
                    v5OnStatusResume()
                    // 子頁面同步狀態
                    var v5ChildFragment = v5ChildFragment
                    while (true) {
                        v5ChildFragment = v5ChildFragment?.v5NextFragment ?: break
                    }
                    if(this.v5IsLifeStatus(LifeStatus.RESUME)) {
                        v5ChildFragment?.v5LifeStatus = LifeStatus.RESUME
                    }
                }
                // 暫停
                LifeStatus.PAUSE -> {
                    // 需要狀態 '活躍'
                    if (!v5IsLifeStatus(LifeStatus.RESUME)) { return }

                    field = value
                    // 子頁面暫停
                    v5ChildFragment?.let { child ->
                        if(child.v5WaitFinish) { return@let }
                        child.v5LifeStatus = LifeStatus.PAUSE
                    }
                    // 暫停區塊
                    v5OnStatusPause()
                    // 導覽頁暫停
                    v5NavigationFragment?.let navitaion@ { navigation ->
                        // 導覽頁與接續頁 結束中 返回
                        if(navigation.v5WaitFinish || v5WaitFinish) { return@navitaion }
                        // 子頁面的接續頁非全螢幕 返回
                        if(v5NextFragment != null && v5NextFragment?.v5Request?.isFullScreen != true ) { return@navitaion }
                        // 子頁面 非 套用中 返回 (避免導覽頁切換子頁面導致暫停)
                        if(navigation.v5ChildFragment != this.v5RootFragment) { return@navitaion }

                        navigation.v5LifeStatus = LifeStatus.PAUSE
                    }
                }
                // 銷毀
                LifeStatus.DESTROY -> {
                    // 需要狀態 '暫停'
                    if (!v5IsLifeStatus(LifeStatus.PAUSE)) { return; }

                    field = value
                    // 銷毀區塊
                    v5OnStatusDestroy()

                    v5LastFragment
                        // 有前頁面
                        ?.let { lastFragment ->
                            // 自身為導覽頁的子頁，向上關閉導覽頁
                            if(lastFragment == v5NavigationFragment) {
                                lastFragment.v5Finish()
                            }
                            // 自身非導覽頁的子頁，回拋執行結果
                            else {
                                lastFragment.v5WaitResultCode = v5ResultCode
                                lastFragment.v5WaitResultData = v5ResultData
                                lastFragment.v5NextFragment = null
                            }
                            // 傳遞當前頁面狀態至前頁面
                            if(!lastFragment.v5WaitFinish){
                                lastFragment.v5ContentLayout?.visibility = VISIBLE
                                lastFragment.v5LifeStatus = activity.v5LifeStatus
                            }
                        }

                    // 自身為全屏顯示 關閉時需顯示上一頁內容
                    if(AUTO_HIDE_FULL_SCREEN) {
                        if (v5Request?.isFullScreen == true) {
                            for (i in v5LaunchFragment.childCount - 1 downTo 0) {
                                val child = v5LaunchFragment.getChildAt(i)
                                if (child != v5LaunchFragment.v5AlertLayout) {
                                    child.visibility = View.VISIBLE
                                    break
                                }
                            }
                        }
                    }

                    // 移除導覽節點保存 、 根節點
                    v5WaitFragment = null
                    v5RootFragment = null
                    v5NavigationFragment = null
                    v5NextFragment = null
                    v5LastFragment = null
                    v5Tab = null
                }
            }
        }
    override var v5Request: DataRequest? = null

    override var v5ResultData: Any? = null

    override var v5ResultCode: Result = Result.CANCELED

    override var v5WaitResultCode: Result? = null

    override var v5WaitResultData: Any? = null

    override var v5WaitFinish = false

    override var v5ChildTab: Tab?
        get() { return data.response!!.childTab }
        set(value) {

            val dataResponse = data.response ?: return
            // 非活躍狀態無法開啟子頁面
            if(!v5IsLifeStatus(LifeStatus.RESUME)) {
                dataResponse.waitChildTab =
                    // 重複開啟註銷
                    if(dataResponse.childTab == value) null
                    // 更新等待啟動頁面
                    else value

                return
            }
            else {
                dataResponse.waitChildTab = null
            }
            // 重複開啟
            if(value == dataResponse.childTab) { return }

            // 目前套用頁保存後 設置 即將套用頁 隨即將目前套用頁設置"暫停" 即將套用頁設置"啟用" 完成過度
            val oldChild = v5ChildFragment
            var oldChildNextFinal = oldChild
            while (true) {
                oldChildNextFinal = oldChildNextFinal?.v5NextFragment ?: break
            }
            dataResponse.childTab = value
            val newChild = v5ChildFragment!!
            var newChildNextFinal: HBG5Fragment = newChild
            while(true) {
                newChildNextFinal = newChildNextFinal.v5NextFragment ?: break
            }
            // 發生頁面異動
            v5OnChildChanged(old = oldChild, new = newChild)
            // 暫停舊子頁面
            oldChildNextFinal?.v5LifeStatus = LifeStatus.PAUSE
            // 啟動新子頁面
            if(newChild.v5IsLifeStatus(LifeStatus.NONE)) {
                newChildNextFinal.v5LifeStatus = LifeStatus.CREATED
                v5OnChildCreate(child = newChild)
            }
            else {
                newChildNextFinal.v5LifeStatus = LifeStatus.CREATED
            }
            newChildNextFinal.v5LifeStatus = LifeStatus.RESUME
            v5ChildLayout?.removeAllViews()
            v5ChildLayout?.addView(newChild, ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            // 更新UI todo hbg
            when(val tabLayout = tabLayout) {
                is HBG5Switch -> {
                    tabLayout.checkedIndex = dataResponse.childTabList.indexOf(value)
                }
                else -> {
                    tabGroup.check(dataResponse.childTabList.indexOf(value))
                }
            }
        }

    override var v5ChildTabList: MutableList<Tab>
        get() {
            return data.response!!.childTabList.toMutableList()
        }
        set(value) {

            val dataResponse = data.response!!

            dataResponse.childTabList = value
            // todo hbg
            when(val tabLayout = tabLayout) {
                is HBG5Switch -> {
                    val tabNames = dataResponse.childTabList.map { tab -> tab.title ?: "" }
                    tabLayout.options = tabNames
                }
                else -> {
                    tabLayout?.removeAllViews()
                    tabGroup.clear()

                    for(tab in dataResponse.childTabList) {

                        val buttonImpl = tab.buttonCreator(context).also { impl ->
                            impl.v5LoadData(tab)
                            tab.viewImpl = impl
                        }

                        tabLayout?.let { layout ->
                            layout.addView(buttonImpl.view.also { view ->
                                view.layoutParams = buttonImpl.view.layoutParams
                                    ?: LinearLayout.LayoutParams(0,0,1f).also { layoutParams ->
                                        when(layout.orientation) {
                                            LinearLayout.HORIZONTAL -> {
                                                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                                            }
                                            else -> {
                                                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                                            }
                                        }
                                    }
                            })
                        }

                        tabGroup.add(buttonImpl)
                    }
                }
            }
        }
    /** 定義系統生命週期 */
    private val _lifecycle = LifecycleRegistry(this)
    override fun getLifecycle(): Lifecycle = _lifecycle
    /** 頁面流水編號 */
    private val pointID = ++Count
    /** IMPL 是否讀取狀態  */
    private var loadingCount = 0
    /** IMPL 目前活躍狀態的彈出視窗計數  */
    private var alertCount = 0
    private fun addAlertCount(add: Int) {

        val launch: HBG5Fragment = v5LaunchFragment
        if (this !== launch) {
            launch.addAlertCount(add)
            return
        }

        val oldIsShow = alertCount != 0

        alertCount += add

        val newIsShow = alertCount != 0

        if (oldIsShow != newIsShow) {

            if (newIsShow) {
                addView(
                    v5AlertLayout,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)
            }

            else {
                v5AlertLayout.removeFromSuperview()
            }
        }
    }
    /** IMPL 正在使用的彈出窗 */ private
    val visibleAlertMap: MutableMap<String, MutableList<HBG5BaseAlert>> = HashMap()
    /** 畫面資料 */ private
    val data = Data<DataRequest, DataResponse>().apply { response = DataResponse() }
    /** 分頁按鈕群組 */ private
    val tabGroup = HBG5RadioGroup()

    // MARK:- ====================== Event
    /** 啟動頁面返回結果的監聽 */
    override var v5WaitResultListener: ((Result, Any?) -> Unit)? = null


    // MARK:- ====================== Method
    override fun v5IsLifeStatus(vararg statuses: LifeStatus): Boolean {
        return statuses.contains(v5LifeStatus)
    }

    override fun v5LoadRequest(dataRequest: DataRequest?) {

    }

    override fun v5StartFragment(next: HBG5Fragment, listener: ((Result, Any?) -> Unit)?) {
        // 禁止同一畫面重複開啟畫面(無法透過快速點按鈕開同個畫面)
        if(v5NextFragment != null) { return }

        if (v5IsLifeStatus(LifeStatus.DESTROY)) { return }

        v5WaitResultListener = listener
        if (!v5IsLifeStatus(LifeStatus.RESUME)) {
            v5WaitFragment = next
            return
        }
        else {
            v5WaitFragment = null
        }

        val launch = v5LaunchFragment

        // 即將啟動頁為全屏顯示 隱藏前一個全屏顯示的畫面
        val isFullScreen = next.v5Request?.isFullScreen == true
        // 會造成閃爍問題
        if(AUTO_HIDE_FULL_SCREEN) {
            if (isFullScreen) {
                for (i in launch.childCount - 1 downTo 0) {
                    val launchChild = launch.getChildAt(i)
                    if (launchChild !== launch.v5AlertLayout) {
                        launchChild.visibility = GONE
                        break
                    }
                }
            }
        }

        // 決定承載即將啟動頁面的容器
        val parent: ViewGroup = if (isFullScreen) launch else v5ParentLayout

        this.hideKeyboard()

        val childParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        if (isFullScreen) {

            val index = parent.indexOfChild(launch.v5AlertLayout)

            if (index != -1) {
                parent.addView(next, index, childParams)
            }
            else {
                parent.addView(next, childParams)
            }
        }
        else {
            // 彈窗圖層前後層級
            var alertIndex = -1
            // 判斷當前頁面是否最頂層
            if(v5LaunchFragment == this) {
                alertIndex = parent.indexOfChild(v5AlertLayout)
            }
            // 當前頁面為最頂層且當前含有彈窗圖層，強制設定開啟頁面在彈窗圖層下方
            if(alertIndex >= 0) {
                parent.addView(next, alertIndex, childParams)
            }
            else {
                parent.addView(next, childParams)
            }
        }

        //v5ContentLayout?.visibility = GONE

        // 初始化接續頁面
        next.weakLaunchFragment = WeakReference(this.v5LaunchFragment)
        next.v5LastFragment = this
        next.v5Tab = v5Tab
        next.v5NavigationFragment = if(isFullScreen) null else v5NavigationFragment
        next.v5RootFragment = next.v5RootFragment ?: this.v5RootFragment ?: this

        // LifeStatus
        this.v5NextFragment = next
        this.v5LifeStatus = LifeStatus.PAUSE
        next.v5LifeStatus = LifeStatus.CREATED
        next.v5LifeStatus = LifeStatus.RESUME

        // 顯示頁面動畫
        if(next.v5ContentLayout?.visibility == VISIBLE) {
            next.clearAnimation()
            next.startAnimation(AlphaAnimation(0f, 1f).apply {
                duration = 200
            })
        }
    }

    override fun v5Finish() {
        if(v5WaitFinish) { return }
        v5WaitFinish = true
        // 底下物件關閉
        v5NextFragment?.v5Finish()
        // 優先關閉目前套用子頁面
        data.response?.let { dataResponse ->
            dataResponse.childTab?.let { tab ->
                childMap[tab]?.v5Finish()
            }
        }
        // 非活躍子頁面關閉
        v5ChildFragmentList.forEach { child -> child.v5Finish() }

        v5LifeStatus = LifeStatus.PAUSE
        v5LifeStatus = LifeStatus.DESTROY
    }

    override fun v5Back() {
        v5FinalFragment.v5OnBack()
    }

    override fun v5BackRoot() {
        v5RootFragment?.v5NextFragment?.v5Finish()
    }

    override fun <T: HBG5BaseAlert> v5ShowAlert(
        cls: Class<T>,
        creator: ()->T,
        request: HBG5BaseAlert.DataRequest,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)?): T {

        val launch = v5LaunchFragment
        if (this !== launch) {
            return launch.v5ShowAlert(cls, creator, request, onVisibleChange)
        }

        val key = cls.name

        val visibleListener: (HBG5BaseAlert) -> Unit = { alert: HBG5BaseAlert ->

            val cacheList = ALERT_CACHE[key] ?: mutableListOf()
            run { ALERT_CACHE[key] = cacheList }

            val visibleRecord = visibleAlertMap[key] ?: mutableListOf()
            run { visibleAlertMap[key] = visibleRecord }

            if(alert.v5Visible) {
                addAlertCount(1)
                visibleRecord.add(alert)
                cacheList.remove(alert)
            }
            else {
                // 確保被關閉視窗是可見的
                alert.v5Visibility = HBG5WidgetConfig.Attrs.Visibility.Visible
                addAlertCount(-1)
                visibleRecord.remove(alert)
                cacheList.add(alert)

                if(v5AlertLayout.childCount > 0) {
                    v5AlertLayout.getChildAt(v5AlertLayout.childCount - 1).visibility = VISIBLE
                }
            }
        }

        val alert = ALERT_CACHE[key]?.lastOrNull() as? T ?: creator()
        val alertCount = v5AlertLayout.childCount
        // 隱藏上一個alert如果是loadingAlert則繼續向前尋找
        if(alert !is HBG5LoadingAlert && alertCount > 0) {
            for(i in alertCount - 1 downTo 0) {
                val iAlert = v5AlertLayout.getChildAt(i)
                if(iAlert is HBG5LoadingAlert) { continue }
                iAlert.visibility = INVISIBLE
                break
            }
        }

        run {
            alert.v5RegisterOnVisibleChangeBySystem(visibleListener)
            alert.v5RegisterOnVisibleChange(onVisibleChange)
            alert.v5Show(this, request)
        }

        return alert
    }

    override fun <T: HBG5BaseAlert> v5HideAlert(cls: Class<T>) {

        val launch = v5LaunchFragment

        if (this != launch) {
            launch.v5HideAlert(cls)
            return
        }

        visibleAlertMap[cls.name]?.let { list ->
            for(alert in list.reversed()) {
                alert.v5Hide()
            }
        }
    }

    override fun v5ShowLoadingAlert(request: HBG5LoadingAlert.DataRequest) {

        val launch = v5LaunchFragment

        if (this !== launch) {
            launch.v5ShowLoadingAlert(request)
            return
        }

        if (loadingCount == 0) {
            v5ShowAlert(
                cls = HBG5LoadingAlert::class.java,
                creator = { HBG5LoadingAlert(context = context).also { alert -> weakLoadingAlert = WeakReference(alert) } },
                request = request,
                onVisibleChange = null)
        }
        else {
            weakLoadingAlert.get()?.v5LoadRequest(request = request)
        }

        loadingCount += 1
    }
    override fun v5HideLoadingAlert() {

        val launch = v5LaunchFragment

        if (this !== launch) {
            launch.v5HideLoadingAlert()
            return
        }

        if (loadingCount == 1) {
            v5HideAlert(HBG5LoadingAlert::class.java)
        }

        loadingCount -= 1
    }

    override fun v5AskPermission(
        permissionList: Array<String>,
        onResult: ((Boolean, String?) -> Unit)?) {
        val activity = context as? HBG5LaunchActivity
        if(activity != null) {
            activity.v5askPermission(
                permissionList = permissionList,
                onResult = onResult
            )
        }
        else {
            onResult?.let { it(false, "Not found activity context") }
        }
    }

    override fun v5SelectCreateFile(newName: String, listener: (Result, UriResponse) -> Unit) {
        val activity = context as? HBG5LaunchActivity
        activity?.v5SaveFile(
            saveFileName = newName,
            onResult = listener)
    }

    override fun v5StartBiometric(
        success: () -> Unit,
        failed: () -> Unit,
        cancel: () -> Unit,
        unsupported: () -> Unit) {

        val activity = context as? HBG5LaunchActivity ?: return
        // 生物辨識進行中
        if(BIOMETRIC_ALERT.isNotEmpty()) { return }
        // 支援性判斷
        if(!canBiometric()) {
            unsupported()
            return
        }

        val biometricAlert = BiometricPrompt(
            activity,
            object: BiometricPrompt.AuthenticationCallback() {
                // 關閉辨識視窗
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    v5StopBiometric()
                    cancel()
                }
                // 辨識指紋成功 (自動關閉視窗)
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    v5StopBiometric()
                    success()
                }
                // 辨識指紋失敗
                override fun onAuthenticationFailed() {
                    failed()
                }
            }
        )
        BIOMETRIC_ALERT[pointID] = biometricAlert

        biometricAlert.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.hbg5_widget_n_biometrics)) //標題
                .setSubtitle(getString(R.string.hbg5_widget_n_running)) // 標題底下的提示
                .setNegativeButtonText(getString(R.string.hbg5_widget_n_cancel)) //取消按鈕
                .build())
    }

    override fun v5StopBiometric() {
        // 生物辨識非進行中，退出
        val biometricAlert = BIOMETRIC_ALERT[pointID] ?: return
        BIOMETRIC_ALERT.clear()
        biometricAlert.cancelAuthentication()
    }
    /**分享訊息*/ // todo hbg
    fun v5Share(message: String) {
        val options = HBG5WidgetConfig.MessageShareType.VALUES
        v5ShowOptionAlert(
            request = HBG5OptionAlert.DataRequest().also { alertRequest ->
                alertRequest.title = "分享訊息"
                alertRequest.optionList = options.map { it.name }
            },
            onYes = { index ->
                val option = options[index]

                when(option) {
                    HBG5WidgetConfig.MessageShareType.TEXT -> {
                        val intent = Intent().apply {
                            this.type = "text/plain"
                            this.action = Intent.ACTION_SEND
                            this.putExtra(Intent.EXTRA_TEXT, message)
                        }
                        // 檢查是否有應用程序可以處理此意圖
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                        else {
                            // 處理沒有應用程序可以處理這個意圖的情況
                            v5ShowTextAlert(
                                request = HBG5TextAlert.DataRequest().also { alertRequest ->
                                    alertRequest.title = "錯誤"
                                    alertRequest.content = "沒有應用程序可以分享此訊息"
                                }
                            )
                        }
                    }
                    HBG5WidgetConfig.MessageShareType.MAIL -> {
                        val intent = Intent().apply {
                            this.type = "message/rfc822"
                            this.action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("")) // 收件人的電子郵件地址
                            putExtra(Intent.EXTRA_SUBJECT, "請輸入你的電子郵件標題")
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        // 檢查是否有應用程序可以處理此意圖
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            // 處理沒有應用程序可以處理這個意圖的情況
                            v5ShowTextAlert(
                                request = HBG5TextAlert.DataRequest().also { alertRequest ->
                                    alertRequest.title = "錯誤"
                                    alertRequest.content = "沒有應用程序可以發送此電子郵件"
                                }
                            )
                        }
                    }
                    HBG5WidgetConfig.MessageShareType.CLIPBOARD -> {
                        v5WriteClipboard(message = message)
                    }
                }
            }
        )
    }
    /**訊息寫入剪貼簿*/
    fun v5WriteClipboard(message: String) {
        // 取得剪貼簿管理器
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if(clipboard == null) {
            v5ShowTextAlert(
                request = HBG5TextAlert.DataRequest().also { alertRequest ->
                    alertRequest.title = "錯誤"
                    alertRequest.content = "剪貼簿管理器錯誤 "
                }
            )
            return
        }
        // 建立一個ClipData物件，其中包含要複製的文字
        val clip = ClipData.newPlainText("label", message)

        // 將ClipData設置到剪貼簿
        clipboard.setPrimaryClip(clip)

        v5ShowTextAlert(
            request = HBG5TextAlert.DataRequest().also { alertRequest ->
                alertRequest.title = "成功"
                alertRequest.content = "已複製訊息至剪貼簿"
            }
        )
    }

    /** 打印生命週期訊息 */
    fun logLife(message: String) {
        if(BuildConfig.DEBUG) {
            when(false) {
                true -> Log.d("//LifeStatus:", "${javaClass.simpleName} $pointID $message NAVI: ${v5NavigationFragment?.pointID} LAST: ${v5LastFragment?.pointID}")
                else -> Log.d("//LifeStatus:", "${javaClass.simpleName} $pointID $message")
            }
        }
    }

    // MARK:- ====================== Class
    /** 頁面資料 */
    open class Data<TRequest : DataRequest?, TResponse : DataResponse?> : HBG5Data<TRequest, TResponse>()
    /** 頁面請求 */
    open class DataRequest {

        var isFullScreen = false

        var backText = ""

        var titleText = ""

        var childTab: Tab? = null

        var childTabList = mutableListOf<Tab>()
    }
    /** 頁面結果 */
    open class DataResponse {

        var lock = false
        /** 鎖定區塊 只有未鎖定狀態下才會執行closure行為 */
        fun lock(closure: (() -> Unit)) {

            if(lock) { return }

            lock = true

            closure()

            lock = false
        }
        /** 等待開啟的分頁標籤 */
        var waitChildTab: Tab? = null
        /** 套用中的分頁標籤 */
        var childTab: Tab? = null
        /** 分頁標籤列表 */
        var childTabList = mutableListOf<Tab>()
    }

    /** 頁面生命週期 */
    enum class LifeStatus {
        /** 未使用 */
        NONE,
        /** 建立 */
        CREATED,
        /** 活躍 */
        RESUME,
        /** 暫停 */
        PAUSE,
        /** 銷毀 */
        DESTROY
    }

    /** 執行結果 */
    enum class Result {
        /** 取消 */
        CANCELED,
        /** 成功 */
        OK;

        val text:String
            get() = when(this) {
                CANCELED -> "Canceled"
                OK -> "Ok"
            }

        companion object {
            fun parse(result: ActivityResult): Result {
                return when(result.resultCode) {
                    Activity.RESULT_OK -> OK
                    else -> CANCELED
                }
            }
        }
    }

    /** 導覽標籤 */
    open class Tab  {

        constructor(
            id: Int?,
            title: String?,
            icon: Int?,
            dataRequest: DataRequest,
            childCreator: ((Context, DataRequest) -> HBG5Fragment),
            buttonCreator: ((Context) -> HBG5FragmentTabImpl) = { context -> HBG5NavigationButton(context) }
        ) {
            this.id = id
            this.title = title
            this.icon = icon
            this.dataRequest = dataRequest
            this.childCreator = childCreator
            this.buttonCreator = buttonCreator
        }

        /** 資料連結介面 */
        private var weakViewImpl = WeakReference<HBG5FragmentTabImpl>(null)
        /** 識別碼 */
        val id: Int?
        /** 名稱 */
        val title: String?
        /** 圖示 */ @DrawableRes
        val icon: Int?
        /** 請求 */
        val dataRequest: DataRequest
        /** 頁面建立邏輯 */
        val childCreator: ((Context, DataRequest) -> HBG5Fragment)
        /** 按鈕建立邏輯 */
        val buttonCreator: ((Context) -> HBG5FragmentTabImpl)

        /** 資料連結介面 */
        var viewImpl: HBG5FragmentTabImpl?
            get() {
                return weakViewImpl.get()
            }
            set(value) {
                weakViewImpl = WeakReference(value)
            }

        /** 由資料刷新關聯元件 */
        fun refresh() {
            viewImpl?.v5LoadData(this)
        }
    }

    /** 路徑型態結果 */
    class UriResponse {

        constructor()

        constructor(data: ActivityResult, error: String? = null) {
            this.error = error
            val intent = data.data ?: return

            // Single Uri
            intent.data?.let { uri -> uriList.add(uri) }
            // Multiple Uri
            intent.clipData?.let { clipData ->
                for (i in 0 until clipData.itemCount) {
                    uriList.add(clipData.getItemAt(i).uri)
                }
            }
        }

        constructor(data: Uri, error: String? = null) {
            this.error = error
            uriList.add(data)
        }

        constructor(dataList: List<Uri>, error: String? = null) {
            this.error = error
            uriList.addAll(dataList)
        }

        constructor(error: String?) {
            this.error = error
        }

        var uriList = mutableListOf<Uri>()

        var error:String? = null
    }

    /** 檔案種類 */
    enum class FileType(val mime: String) {
        /**所有*/
        ALL(mime = "*/*"),
        /**PDF*/
        PDF(mime = "application/pdf"),
        /**圖像*/
        IMAGE(mime = "image/*"),
        /**聲音*/
        AUDIO(mime = "audio/*"),
        /**影片*/
        VIDEO(mime = "video/*"),
        /**文字*/
        TEXT(mime = "text/plain"),
        /**安卓安裝檔*/
        APK(mime = "application/vnd.android.package-archive"),
        /**Sqlite資料庫*/ // todo hbg
        SQLITE(mime = "*/*"),
        //SQLITE(mime = "application/x-sqlite3"),
        /**Csv*/ // todo hbg
        CSV(mime = "text/comma-separated-values"),
        /**壓縮檔*/
        ZIP(mime = "application/zip");
        // todo hbg
        companion object {
            /**
             * @param fileType
             * */
            fun parse(fileType: String?): FileType? {
                if(fileType == null) { return null }
                val lowerFiletype = fileType.lowercase()

                val isContains: ((List<Pair<String, Array<ByteArray>>>) -> Boolean) = { list ->
                    list.contains { it.first.replace(".", "").lowercase().endsWith(lowerFiletype) }
                }

                var whites: List<Pair<String, Array<ByteArray>>> = emptyList()

                // PDF
                whites = listOf(HBG5DownloadCall.FILE_TYPE_HEADER_PDF)
                if(isContains(whites)) { return PDF }
                // IMAGE
                whites = listOf(
                    HBG5DownloadCall.FILE_TYPE_HEADER_HEIC,
                    HBG5DownloadCall.FILE_TYPE_HEADER_BMP,
                    HBG5DownloadCall.FILE_TYPE_HEADER_PNG,
                    HBG5DownloadCall.FILE_TYPE_HEADER_JPG
                )
                if(isContains(whites)) { return IMAGE }
                // AUDIO
                whites = listOf(HBG5DownloadCall.FILE_TYPE_HEADER_MP3)
                if(isContains(whites)) { return AUDIO }
                // VIDEO
                whites = listOf(
                    HBG5DownloadCall.FILE_TYPE_HEADER_MP4,
                    HBG5DownloadCall.FILE_TYPE_HEADER_MOV
                )
                if(isContains(whites)) { return VIDEO }
                // TEXT
                if(false) { return TEXT }
                // APK
                if(false) { return APK }
                // SQLITE
                if(false) { return SQLITE }
                // Csv
                if(false) { return CSV }
                // ZIP
                if(false) { return ZIP }

                return null
            }
        }


//        _png("image/png"),
//        _jpeg("image/jpeg"),
//        _jpg("image/jpeg"),
//        _webp("image/webp"),
//        _gif("image/gif"),
//        _bmp("image/bmp"),
//        _3gp("video/3gpp"),
//        _apk("application/vnd.android.package-archive"),
//        _asf("video/x-ms-asf"),
//        _avi("video/x-msvideo"),
//        _bin("application/octet-stream"),
//        _c("text/plain"),
//        _class("application/octet-stream"),
//        _conf("text/plain"),
//        _cpp("text/plain"),
//        _doc("application/msword"),
//        _docx("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
//        _xls("application/vnd.ms-excel"),
//        _xlsx("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
//        _exe("application/octet-stream"),
//        _gtar("application/x-gtar"),
//        _gz("application/x-gzip"),
//        _h("text/plain"),
//        _htm("text/html"),
//        _html("text/html"),
//        _jar("application/java-archive"),
//        _java("text/plain"),
//        _js("application/x-javascript"),
//        _log("text/plain"),
//        _m3u("audio/x-mpegurl"),
//        _m4a("audio/mp4a-latm"),
//        _m4b("audio/mp4a-latm"),
//        _m4p("audio/mp4a-latm"),
//        _m4u("video/vnd.mpegurl"),
//        _m4v("video/x-m4v"),
//        _mov("video/quicktime"),
//        _mp2("audio/x-mpeg"),
//        _mp3("audio/x-mpeg"),
//        _mp4("video/mp4"),
//        _mpc("application/vnd.mpohun.certificate"),
//        _mpe("video/mpeg"),
//        _mpeg("video/mpeg"),
//        _mpg("video/mpeg"),
//        _mpg4("video/mp4"),
//        _mpga("audio/mpeg"),
//        _msg("application/vnd.ms-outlook"),
//        _ogg("audio/ogg"),
//        _pdf("application/pdf"),
//        _pps("application/vnd.ms-powerpoint"),
//        _ppt("application/vnd.ms-powerpoint"),
//        _pptx("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
//        _prop("text/plain"),
//        _rc("text/plain"),
//        _rmvb("audio/x-pn-realaudio"),
//        _rtf("application/rtf"),
//        _sh("text/plain"),
//        _tar("application/x-tar"),
//        _tgz("application/x-compressed"),
//        _txt("text/plain"),
//        _wav("audio/x-wav"),
//        _wma("audio/x-ms-wma"),
//        _wmv("audio/x-ms-wmv"),
//        _wps("application/vnd.ms-works"),
//        _xml("text/plain"),
//        _z("application/x-compress"),
//        _zip("application/x-zip-compressed"),
//        _0("*/*"),
//        ————————————————
//        版权声明：本文为CSDN博主「林深人不知」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/smallbabylong/article/details/105574848
    }

    // todo hbg
    fun v5InsertFile(
        request: HBG5WidgetConfig.Request.InsertFile,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }
        activity.v5InsertFile(
            request = request,
            onResult = onResult
        )
    }

    override
    fun v5SelectImageByCamera(
        request: HBG5WidgetConfig.Request.SelectImage,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }
        activity.v5SelectImageByCamera(
            request = request,
            onResult = onResult
        )
    }
    override
    fun v5SelectImageByFile(
        request: HBG5WidgetConfig.Request.SelectImage,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }

        activity.v5SelectImageByFile(
            request = request,
            onResult = onResult
        )
    }
    override
    fun v5SelectVideoByCamera(
        request: HBG5WidgetConfig.Request.SelectVideo,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }

        activity.v5SelectVideoByCamera(
            request = request,
            onResult = onResult
        )
    }
    override
    fun v5SelectVideoByFile(
        request: HBG5WidgetConfig.Request.SelectVideo,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }

        activity.v5SelectVideoByFile(
            request = request,
            onResult = onResult
        )
    }
    override
    fun v5SelectPdf(
        request: HBG5WidgetConfig.Request.SelectFile,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }

        activity.v5SelectPdf(
            request = request,
            onResult = onResult
        )
    }
    /**查詢資料庫檔案*/ // todo hbg
    fun v5SelectSqlite(
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }
        activity.v5SelectSqlite(
            onResult = onResult
        )
    }
    /**查詢壓縮檔案*/ // todo hbg
    fun v5SelectZip(
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }
        activity.v5SelectZip(
            onResult = onResult
        )
    }
    /**查詢CSV檔案*/ // todo hbg
    fun v5SelectCsv(
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        val activity = context as? HBG5LaunchActivity
        if(activity == null) {
            onResult(Result.CANCELED, UriResponse(error = "Activity is null"))
            return
        }
        activity.v5SelectCsv(
            onResult = onResult
        )
    }
}