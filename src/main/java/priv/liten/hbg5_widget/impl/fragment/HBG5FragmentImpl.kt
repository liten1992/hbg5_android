package priv.liten.hbg5_widget.impl.fragment

import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import priv.liten.base_extension.readFileType
import priv.liten.hbg5_extension.md5
import priv.liten.hbg5_widget.application.HBG5Application
import priv.liten.hbg5_widget.bin.alert.*
import priv.liten.hbg5_widget.bin.layout.HBG5FrameLayout
import priv.liten.hbg5_data.HBG5Date
import priv.liten.hbg5_data.HBG5Time
import priv.liten.hbg5_extension.exists
import priv.liten.hbg5_extension.getPrivateUri
import priv.liten.hbg5_widget.bin.fragment.HBG5Fragment
import priv.liten.hbg5_widget.config.HBG5WidgetConfig

interface HBG5FragmentImpl {

    // MARK:- ====================== Constructor
    /** 畫面初始化  */
    fun v5OnUIRoot()

    /** 運行周期 - 建立  */
    fun v5OnStatusCreated()

    /** 運行周期 - 活躍  */
    fun v5OnStatusResume()

    /** 運行周期 - 暫停  */
    fun v5OnStatusPause()

    /** 運行周期 - 關閉  */
    fun v5OnStatusDestroy()


    // MARK:- ====================== View
    /** 始點頁 */
    val v5LaunchFragment: HBG5Fragment
    /** 終點頁 */
    val v5FinalFragment: HBG5Fragment
    /** 導覽頁 */
    var v5NavigationFragment: HBG5Fragment?
    /** 根結頁 */
    var v5RootFragment: HBG5Fragment?
    /** 上一頁 */
    var v5LastFragment: HBG5Fragment?
    /** 下一頁 */
    var v5NextFragment: HBG5Fragment?
    /** 等待啟動頁 */
    var v5WaitFragment: HBG5Fragment?

    /** 頁面容器 */
    val v5ParentLayout: ViewGroup
    /** 內容容器 */
    var v5ContentLayout: ViewGroup?
    /** 彈窗容器 */
    val v5AlertLayout: HBG5FrameLayout

    /** 所屬分頁索引 */
    var v5Tab: HBG5Fragment.Tab?
    /** 子頁面 */
    val v5ChildFragmentList: List<HBG5Fragment>
    /** 套用中子頁面 */
    val v5ChildFragment: HBG5Fragment?
    /** 子頁面容器 */
    val v5ChildLayout: ViewGroup?


    // MARK:- ====================== Data
    /** 運行狀態(禁止用戶自行設定) */
    var v5LifeStatus: HBG5Fragment.LifeStatus

    /** 請求資料 */
    var v5Request: HBG5Fragment.DataRequest?
    /** 運行資料 */
    var v5ResultData: Any?
    /** 運行結果 */
    var v5ResultCode: HBG5Fragment.Result

    /** 啟動頁面返回結果 */
    var v5WaitResultCode: HBG5Fragment.Result?
    /** 啟動頁面返回結果資料 */
    var v5WaitResultData: Any?
    /** 等待關閉 */
    var v5WaitFinish: Boolean

    /** 套用分頁 */
    var v5ChildTab: HBG5Fragment.Tab?
    /** 分頁清單 */
    var v5ChildTabList: MutableList<HBG5Fragment.Tab>


    // MARK:- ====================== Event
    /** 當進行返回 */
    fun v5OnBack() {
        when(this) {
            // 如為導覽分頁跟結點則執行導覽返回指令
            is HBG5Fragment -> {
                val navigation = this.v5NavigationFragment
                if(navigation?.v5ChildFragmentList?.contains(this) == true) {
                    navigation.v5OnBack()
                }
                else {
                    v5Finish()
                }
            }
            else -> v5Finish()
        }
    }
    /** 當子頁面被初始化 */
    fun v5OnChildCreate(child: HBG5Fragment) { }
    /** 當子頁面發生異動 */
    fun v5OnChildChanged(old: HBG5Fragment?, new: HBG5Fragment?) { }

    /** 啟動頁面返回結果的監聽 */
    var v5WaitResultListener: ((HBG5Fragment.Result, Any?) -> Unit)?


    // MARK:- ====================== Method
    /** 判斷運行狀態是否符合輸入狀態其一 */
    fun v5IsLifeStatus(vararg statuses: HBG5Fragment.LifeStatus): Boolean

    /** 讀取請求 */
    fun v5LoadRequest(dataRequest: HBG5Fragment.DataRequest?)

    /** 啟動接續頁面 */
    fun v5StartFragment(next: HBG5Fragment, listener: ((HBG5Fragment.Result, Any?) -> Unit)? = null)

    /** 結束頁面 */
    fun v5Finish()

    /** 返回頁面 */
    fun v5Back()

    /** 返回根節頁  */
    fun v5BackRoot()

    /** 權限確認 */
    fun v5AskPermission(permissionList: Array<String>, onResult: ((Boolean, String?) -> Unit)?)

    /**拍照取得圖片*/
    fun v5SelectImageByCamera(
        request: HBG5WidgetConfig.Request.SelectImage,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    )
    /**檔案取得圖片*/
    fun v5SelectImageByFile(
        request: HBG5WidgetConfig.Request.SelectImage,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    )
    /**錄影取得影像*/
    fun v5SelectVideoByCamera(
        request: HBG5WidgetConfig.Request.SelectVideo,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    )
    /**檔案取得影像*/
    fun v5SelectVideoByFile(
        request: HBG5WidgetConfig.Request.SelectVideo,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    )
    /**取得PDF*/
    fun v5SelectPdf(
        request: HBG5WidgetConfig.Request.SelectFile,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    )

    /**
     * 取得授權建立檔案路徑
     * @param newName 建立檔案的名稱
     * @param listener 授權結果回調
     * */
    fun v5SelectCreateFile(newName: String, listener: (HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)

    /**啟動生物辨識
     * @param success 辨識成功 (關閉)
     * @param failed 辨識失敗 (僅拋出訊息通知)
     * @param cancel 辨識取消 (關閉)
     * @param unsupported 辨識不支援
     * */
    fun v5StartBiometric(success:(()->Unit), failed:(()->Unit), cancel:(()->Unit), unsupported: (()->Unit))

    /**關閉生物辨識*/
    fun v5StopBiometric()

    /**使用指定方法開啟路徑*/ @Throws
    fun v5Open(uri: String, type: HBG5Fragment.FileType?) {
        val context = HBG5Application.instance ?: throw Exception("初始化資源失敗")

        try {
            // 嘗試轉出資料夾
            if(uri.startsWith("raw://")) {
                val rawName = uri.substring(6)
                val id = context.resources.getIdentifier(
                    rawName,
                    "raw",
                    context.packageName
                )
                if(id == 0) { throw Exception("找不到指定資源") }
                val fileType = context.resources.openRawResource(id).use { inputStream -> inputStream.readFileType() }
                if(fileType.isEmpty()) {
                    throw Exception("未知的檔案類型")
                }
                val fileName = "${uri.md5()}${fileType}"
                // todo hbg5
                val fileUriString = context.getPrivateUri(dirName = HBG5WidgetConfig.PRIVATE_DIR_DOWNLOAD, fileName = fileName).toString()
                if(fileUriString.isEmpty()) {
                    throw Exception("找不到可提取資料夾")
                }
                val fileUri = Uri.parse(fileUriString)
                val fileExist: Boolean = fileUri.exists()
                if(!fileExist) {
                    context.resources.openRawResource(id).use { inputStream ->
                        val fileReader = ByteArray(4096)
                        context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                            var read = inputStream.read(fileReader)
                            while (read != -1) {
                                outputStream.write(fileReader, 0, read)
                                read = inputStream.read(fileReader)
                            }
                            outputStream.flush()
                        } ?: throw Exception("建立寫入檔案串流失敗")
                    }
                }

                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(fileUri, type?.mime)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            }
            else {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(Uri.parse(uri), type?.mime)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                )
            }
        }
        catch (error: Exception) {
            throw Exception("找不到合適的應用程式")
        }
    }

    /** 顯示彈出窗 */
    fun <T: HBG5BaseAlert> v5ShowAlert(
        cls: Class<T>, creator: ()->T,
        request: HBG5BaseAlert.DataRequest,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)?): T
    /** 關閉彈出窗 */
    fun <T: HBG5BaseAlert> v5HideAlert(cls: Class<T>)

    /** 顯示讀取窗  */
    fun v5ShowLoadingAlert(request: HBG5LoadingAlert.DataRequest = HBG5LoadingAlert.DataRequest())
    /** 關閉讀取窗  */
    fun v5HideLoadingAlert()

    /** 顯示提示窗 */
    fun v5ShowTextAlert(
        request: HBG5TextAlert.DataRequest,
        onYes: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5TextAlert::class.java,
            creator = { HBG5TextAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
        }
    }
    /** 關閉提示窗 */
    fun v5HideTextAlert() = v5HideAlert(HBG5TextAlert::class.java)

    /** 顯示確認窗 */
    fun v5ShowIntentAlert(
        request: HBG5IntentAlert.DataRequest,
        onYes: (() -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5IntentAlert::class.java,
            creator = { HBG5IntentAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
            v5RegisterNo(onNo)
        }
    }
    /** 關閉確認窗 */
    fun v5HideIntentAlert() = v5HideAlert(HBG5IntentAlert::class.java)

    /** 顯示自訂確認窗 */ // todo hbg
    fun v5ShowIntentCustomAlert(
        request: HBG5IntentCustomAlert.DataRequest,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5IntentCustomAlert::class.java,
            creator = { HBG5IntentCustomAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange)
    }
    /** 關閉自訂確認窗 */ // todo hbg
    fun v5HideIntentCustomAlert() = v5HideAlert(HBG5IntentCustomAlert::class.java)

    /** 顯示文字輸入窗 */
    fun v5ShowTextInputAlert(
        request: HBG5TextInputAlert.DataRequest,
        onYes: ((String) -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5TextInputAlert::class.java,
            creator = { HBG5TextInputAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
            v5RegisterNo(onNo)
        }
    }
    /** 關閉文字輸入窗 */
    fun v5HideTextInputAlert() = v5HideAlert(HBG5TextInputAlert::class.java)

    /** 顯示浮動選項窗 */
    fun v5ShowEnumAlert(
        request: HBG5EnumAlert.DataRequest,
        onYes: ((Int) -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5EnumAlert::class.java,
            creator = { HBG5EnumAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
            v5RegisterNo(onNo)
        }
    }
    /** 關閉浮動選項窗 */
    fun v5HideEnumAlert() = v5HideAlert(HBG5OptionAlert::class.java)

    /** 顯示選項窗 */
    fun v5ShowOptionAlert(
        request: HBG5OptionAlert.DataRequest,
        onYes: ((Int) -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5OptionAlert::class.java,
            creator = { HBG5OptionAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
            v5RegisterNo(onNo)
        }
    }
    /** 關閉選項窗 */
    fun v5HideOptionAlert() = v5HideAlert(HBG5OptionAlert::class.java)

    /** 顯示複選窗 */
    fun v5ShowCheckOptionAlert(
        request: HBG5CheckOptionAlert.DataRequest,
        onYes: ((List<Int>) -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5CheckOptionAlert::class.java,
            creator = { HBG5CheckOptionAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
            v5RegisterNo(onNo)
        }
    }
    /** 關閉複選窗 */
    fun v5HideCheckOptionAlert() = v5HideAlert(HBG5CheckOptionAlert::class.java)

    /** 顯示日期選擇窗 */
    fun v5ShowDateAlert(
        request: HBG5DateAlert.DataRequest,
        onYes: ((HBG5Date) -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5DateAlert::class.java,
            creator = { HBG5DateAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
            v5RegisterNo(onNo)
        }
    }
    /** 關閉日期選擇窗 */
    fun v5HideDateAlert() = v5HideAlert(HBG5DateAlert::class.java)

    /** 顯示時間選擇窗 */
    fun v5ShowTimeAlert(
        request: HBG5TimeAlert.DataRequest,
        onYes: ((HBG5Time) -> Unit)? = null,
        onNo: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5TimeAlert::class.java,
            creator = { HBG5TimeAlert(context = context) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterYes(onYes)
            v5RegisterNo(onNo)
        }
    }
    /** 關閉時間選擇窗 */
    fun v5HideTimeAlert() = v5HideAlert(HBG5TimeAlert::class.java)

    /** 顯示日期&時間選擇窗 */
    fun v5ShowDateTimeAlert(
        dateRequest: HBG5DateAlert.DataRequest,
        timeRequest: HBG5TimeAlert.DataRequest,
        onYes: ((HBG5Date, HBG5Time) -> Unit)? = null,
        onNo: (() -> Unit)? = null) {

        v5ShowDateAlert(
            request = dateRequest,
            onYes = { date ->
                v5ShowTimeAlert(
                    request = timeRequest,
                    onYes = { time ->
                        onYes?.let { it(date, time) }
                    },
                    onNo = onNo
                )
            },
            onNo = onNo
        )
    }

    /** 顯示圖片瀏覽窗 */
    fun v5ShowImageAlert(
        request: HBG5ImageAlert.DataRequest,
        onYes: (() -> Unit)? = null,
        onVisibleChange: ((HBG5BaseAlert) -> Unit)? = null) {

        val context = (this as? View)?.context ?: return

        v5ShowAlert(
            cls = HBG5ImageAlert::class.java,
            creator = { HBG5ImageAlert(context = context ) },
            request = request,
            onVisibleChange = onVisibleChange).run {

            v5RegisterBack(onYes)
        }
    }
    /** 隱藏圖片瀏覽窗 */
    fun v5HideImageAlert() = v5HideAlert(HBG5ImageAlert::class.java)
}
