package priv.liten.hbg5_widget.bin.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import priv.liten.base_extension.readFileType
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_extension.BitmapBuilder
import priv.liten.hbg5_extension.copyFile
import priv.liten.hbg5_extension.copyFileToDownload
import priv.liten.hbg5_extension.copyFileToPrivate
import priv.liten.hbg5_extension.fileType
import priv.liten.hbg5_extension.getDownloadUri
import priv.liten.hbg5_extension.getFileSizeMb
import priv.liten.hbg5_extension.getPrivateUri
import priv.liten.hbg5_extension.pathExtension
import priv.liten.hbg5_extension.save
import priv.liten.hbg5_http.HBG5DownloadCall
import priv.liten.hbg5_widget.application.HBG5Application
import priv.liten.hbg5_widget.bin.alert.HBG5BaseAlert
import priv.liten.hbg5_widget.bin.fragment.HBG5Fragment
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.activity.HBG5LaunchActivityImpl
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.NullPointerException

open class HBG5LaunchActivity : AppCompatActivity(), HBG5LaunchActivityImpl {

    // MARK:- ====================== Define
    companion object {
        private val _instance: MutableList<HBG5LaunchActivity> = mutableListOf()
        val instance: List<HBG5LaunchActivity> get() = _instance.toList()
    }

    enum class Permission {
        /**檔案讀寫權限*/
        FILE_RW,
        /**檔案寫權限*/
        FILE_W,
        /**檔案讀權限*/
        FILE_R,
        /**訊息通知權限*/
        NOTIFICATION,
        /**相機權限*/
        CAMERA;

        fun values(): Array<String> {
            return when(this) {
                FILE_RW -> arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                FILE_W -> arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                FILE_R -> arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                NOTIFICATION ->
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) arrayOf(
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                    else arrayOf()
                CAMERA -> arrayOf(
                    Manifest.permission.CAMERA
                )

                else -> arrayOf()
            }
        }
    }


    // MARK:- ====================== Constructor
    @SuppressLint("ApplySharedPref")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        _instance.add(this)

        v5OnCreate()

        v5LifeStatus = HBG5Fragment.LifeStatus.CREATED

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            permissionCallback?.also {

                for(key in result.keys) {
                    val value = result[key] ?: false
                    // 無權限
                    if(!value) {
                        it(false, "$key is denied")
                        return@also
                    }
                }

                it(true, null)
            }
            permissionCallback = null
        }

        uriByCameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            val uri = cameraOutputUriList.firstOrNull()
            cameraOutputUriList.clear()
            val callback = onFragmentResultListener ?: return@registerForActivityResult
            onFragmentResultListener = null

            val result =
                if(uri == null) HBG5Fragment.Result.CANCELED
                else HBG5Fragment.Result.parse(data)

            val uriResponse =
                if(result == HBG5Fragment.Result.CANCELED) HBG5Fragment.UriResponse()
                else HBG5Fragment.UriResponse(data = uri!!)

            callback(result, uriResponse)
        }

        uriByFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->
            val callback = onFragmentResultListener ?: return@registerForActivityResult
            onFragmentResultListener = null

            val result = HBG5Fragment.Result.parse(data)

            val uriResponse = HBG5Fragment.UriResponse(data = data)

            // 嘗試獲取Uri永久讀取權限
            for(uri in uriResponse.uriList) {
                try {
                    contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                catch (error: Exception) {
                    error.printStackTrace()
                }
            }

            callback(result, uriResponse)
        }

        installLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = installUri ?: return@registerForActivityResult
            installUri = null
            // 8.0 以上版本需要請求安裝不明來源程式權限
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // 已授權
                if(packageManager.canRequestPackageInstalls()) {
                    val intent = Intent(Intent.ACTION_VIEW)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .setDataAndType(uri, HBG5Fragment.FileType.APK.mime)
                    this.startActivity(intent)
                }
            }
            else {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setDataAndType(uri, HBG5Fragment.FileType.APK.mime)
                this.startActivity(intent)
            }
        }

        uriBySaveFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { data ->

            val result = HBG5Fragment.Result.parse(data)

            val uriResponse =
                if(result == HBG5Fragment.Result.CANCELED) HBG5Fragment.UriResponse(error = null)
                else HBG5Fragment.UriResponse(data = data)

            onFragmentResultListener?.let { it(
                result,
                uriResponse
            ) }
            onFragmentResultListener = null
        }
        // 新式註冊返回方式 https://developer.android.google.cn/guide/navigation/navigation-custom-back?hl=zh-cn
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                v5OnBack()
            }
        })
    }

    override fun onResume() {
        super.onResume()

        v5LifeStatus = HBG5Fragment.LifeStatus.RESUME
    }

    override fun onPause() {
        super.onPause()
        v5LifeStatus = HBG5Fragment.LifeStatus.PAUSE
    }

    override fun onDestroy() {
        super.onDestroy()

        _instance.remove(this)

        v5LifeStatus = HBG5Fragment.LifeStatus.DESTROY

        v5OnDestroy()
    }

    private lateinit var permissionLauncher : ActivityResultLauncher<Array<String>>

    /** Boolean(true:GRANTED, false:DENIED), String(DENIED Message) */
    private var permissionCallback: ((Boolean, String?) -> Unit)? = null
    @SuppressLint("ObsoleteSdkInt", "ApplySharedPref")
    fun v5askPermission(permissionList: Array<String>, onResult: ((Boolean, String?) -> Unit)?) {
        // 版本過低不需要確認權限
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onResult?.let { it(true, null) }
            return
        }

        // Android 13 版本 檔案判定方案有調整 2024-08-12
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val readPermissionIndex = permissionList.indexOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermissionIndex = permissionList.indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if(readPermissionIndex != -1 || writePermissionIndex != -1) {
                // 無讀取檔案權限
                if(!Environment.isExternalStorageManager()) {
                    startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    onResult?.let { it(false, "請同意檔案管理權限") }
                    return
                }
                // 有權限 移除請求
                else {
                    if(readPermissionIndex != -1) {
                        permissionList[readPermissionIndex] = ""
                    }
                    if(writePermissionIndex != -1) {
                        permissionList[writePermissionIndex] = ""
                    }
                }
            }
        }

        // 檢查不再詢問默認拒絕
        val needAskPermission = mutableListOf<String>()
        for(permission in permissionList) {
            if(permission.isEmpty()) { continue }
            if(checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) { continue }
            needAskPermission.add(permission)
        }

        // 不需請求權限
        if(needAskPermission.isEmpty()) {
            permissionCallback = null
            onResult?.also { it(true, null) }
        }
        // 需要請求權限
        else {
            permissionCallback = onResult
            permissionLauncher.launch(needAskPermission.toTypedArray())
        }
    }

    /** 照相生成圖片請求 */
    private lateinit var uriByCameraLauncher : ActivityResultLauncher<Intent>

    /** 取得已存在的檔案路徑請求 */
    private lateinit var uriByFileLauncher : ActivityResultLauncher<Intent>

    /** 取得可生成的檔案路徑請求 */
    private lateinit var uriBySaveFileLauncher : ActivityResultLauncher<Intent>
    /** 取得可生成的檔案路徑 */
    fun v5SaveFile(saveFileName: String, onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)) {
        v5askPermission(
            permissionList = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }

                onFragmentResultListener = { result, data ->
                    onResult(result, data as? HBG5Fragment.UriResponse ?: HBG5Fragment.UriResponse(error = "${result.text} data null"))
                }
                uriBySaveFileLauncher.launch(Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType(HBG5Fragment.FileType.ALL.mime)
                    .putExtra(Intent.EXTRA_TITLE, saveFileName))
            }
        )
    }

    /** 取得安裝未知來源應用許可 */
    private lateinit var installLauncher : ActivityResultLauncher<Intent>
    private var installUri: Uri? = null
    fun install(uri: Uri) {
        // 8.0 以上版本需要請求安裝不明來源程式權限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 已授權
            if(packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Intent.ACTION_VIEW)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setDataAndType(uri, HBG5Fragment.FileType.APK.mime)
                this.startActivity(intent)
            }
            // 未授權
            else {
                installUri = uri
                val packageUri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                installLauncher.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri))
            }
        }
        else {
            val intent = Intent(Intent.ACTION_VIEW)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setDataAndType(uri, HBG5Fragment.FileType.APK.mime)
            this.startActivity(intent)
        }
    }


    // MARK:- ====================== View
    override var v5ContentFragment: HBG5Fragment? = null


    // MARK:- ====================== Data
    override var v5LifeStatus = HBG5Fragment.LifeStatus.NONE
        set(value) {

            when(value) {
                HBG5Fragment.LifeStatus.CREATED -> {
                    if(field != HBG5Fragment.LifeStatus.NONE) { return }
                    v5ContentFragment = v5CreateFragment()
                    setContentView(v5ContentFragment!!)
                }
                HBG5Fragment.LifeStatus.RESUME -> {
                    if(field != HBG5Fragment.LifeStatus.CREATED && field != HBG5Fragment.LifeStatus.PAUSE) { return }
                }
                HBG5Fragment.LifeStatus.PAUSE -> {
                    if(field != HBG5Fragment.LifeStatus.RESUME) { return }
                }
                HBG5Fragment.LifeStatus.DESTROY -> {
                    if(field != HBG5Fragment.LifeStatus.PAUSE) { return }
                }
                else -> { return }
            }

            field = value

            when(value) {
                HBG5Fragment.LifeStatus.CREATED,
                HBG5Fragment.LifeStatus.RESUME,
                HBG5Fragment.LifeStatus.PAUSE -> v5ContentFragment?.v5FinalFragment?.v5LifeStatus = value
                HBG5Fragment.LifeStatus.DESTROY -> v5ContentFragment?.v5Finish()
                else -> { }
            }
        }

    private val cameraOutputUriList = mutableListOf<Uri>()

    // MARK:- ====================== Event
    override fun v5OnCreate() {

    }

    override fun v5OnDestroy() {

    }

    override fun v5OnBack() {
        v5ContentFragment
            ?.let { v5ContentFragment ->

                // Exist Alert
                val launchFragment = v5ContentFragment.v5LaunchFragment
                run {
                    val layout: ViewGroup = launchFragment.v5AlertLayout
                    for (i in layout.childCount - 1 downTo 0) {
                        val view = layout.getChildAt(i)
                        if (view is HBG5BaseAlert) {
                            view.v5Cancel()
                            return
                        }
                    }
                }

                // Exist Fragment
                val finalFragment = v5ContentFragment.v5FinalFragment
                if(finalFragment == v5ContentFragment) {
                    finish()
                }
                else {
                    finalFragment.v5OnBack()
                }
            }
            ?: finish()
    }
    /** 頁面請求結果監聽 */
    private var onFragmentResultListener: ((HBG5Fragment.Result, Any?) -> Unit)? = null
    /** 選擇檔案結果返回 */
    fun onSelectFileResult(
        request: HBG5WidgetConfig.Request.SelectFile,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit),
        result: HBG5Fragment.Result,
        uriResponse: HBG5Fragment.UriResponse?,
        canOverwrite: Boolean
    ) {
        // 路徑為空值 直接返回
        if(uriResponse?.uriList?.isEmpty() ?: true) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse().also { data ->
                data.error = uriResponse?.error
            })
            return
        }

        val app = HBG5Application.instance
        if(app == null) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "Not found application"))
            return
        }
        if(uriResponse == null) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "${result.text} data null"))
            return
        }

        val dir = app.buildDirUriString() ?: ""
        if(dir.isEmpty()) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "Build cache dir failed"))
            return
        }

        val errorMessage = mutableListOf<String>()
        // 返回結果
        uriResponse.uriList = uriResponse.uriList
            // Filter Uri Select Request
            .map { uri ->
                val message = getFileRequestError(request = request, uri = uri)
                if(message.isNotEmpty()) {
                    errorMessage.add(message)
                    return@map null
                }
                // 允許複寫
                if(canOverwrite) {
                    return@map uri
                }

                val rename = app.buildFileName(fileType = ".${uri.fileType}")
                val newUri = uri.copyFile(dir = dir, rename = rename)
                if(newUri == null) {
                    errorMessage.add("Copy file to dir failed")
                    return@map null
                }
                return@map newUri
            }
            // Remove Null Uri
            .filter { uri ->
                return@filter uri != null
            }
            // Build Exist Uri
            .map { uri ->
                return@map uri!!
            }
            .toMutableList()

        if(uriResponse.uriList.isEmpty()) {
            onResult(
                HBG5Fragment.Result.CANCELED,
                HBG5Fragment.UriResponse(error = errorMessage.firstOrNull() ?: "Not found select file")
            )
            return
        }
        onResult(HBG5Fragment.Result.OK, uriResponse)
    }
    /** 選擇圖像結果返回 */
    fun onSelectImageResult(
        request: HBG5WidgetConfig.Request.SelectImage,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit),
        result: HBG5Fragment.Result,
        uriResponse: HBG5Fragment.UriResponse?,
        canOverwrite: Boolean
    ) {
        // 路徑為空值 直接返回
        if(uriResponse?.uriList?.isEmpty() ?: true) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse().also { data ->
                data.error = uriResponse?.error
            })
            return
        }

        val app = HBG5Application.instance
        if(app == null) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "Not found application"))
            return
        }
        if(uriResponse == null) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "${result.text} data null"))
            return
        }

        val dir = app.buildDirUriString() ?: ""
        if(dir.isEmpty()) {
            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "Build cache dir failed"))
            return
        }

        val errorMessage = mutableListOf<String>()
        // 返回結果
        uriResponse.uriList = uriResponse.uriList
            // 重建圖像尺寸
            .map { uri ->
                // 讀取影像規格如果符合需求直接複製儲存否則執行壓縮儲存
                val bitmapConfig = BitmapBuilder.buildConfig(uri = uri)
                // 讀取影像資訊失敗
                if(!bitmapConfig.exist()) {
                    errorMessage.add("Get image information failed")
                    return@map null
                }
                // 需要重製
                var needReset = false
                if(request.noTypeHeic && BitmapBuilder.BitmapConfig.Format.HEIC == bitmapConfig.format) {
                    needReset = true
                }
                // 不需要影像壓縮或重建 複製存檔
                if(!needReset
                    && bitmapConfig.width <= request.quality.max
                    && bitmapConfig.height <= request.quality.max) {
                    // 允許原址複寫 代表保持原樣
                    if(canOverwrite) {
                        return@map uri
                    }

                    val rename = app.buildFileName(fileType = ".${bitmapConfig.format!!.type}")
                    if(rename.isEmpty()) {
                        errorMessage.add("Build image file name failed")
                        return@map null
                    }

                    val newUri = uri.copyFile(dir = dir, rename = rename)
                    if(newUri == null) {
                        errorMessage.add("Copy image file failed")
                        return@map null
                    }

                    return@map newUri
                }
                // 需要影像壓縮或重建
                else {
                    var newUri: Uri? = uri
                    if(!canOverwrite) {
                        val newUriStr = app.buildFileUriString(fileType = ".png") ?: ""
                        if(newUriStr.isEmpty()) {
                            errorMessage.add("Build new image path failed")
                            return@map null
                        }
                        newUri = Uri.parse(newUriStr)
                    }
                    if(newUri == null) {
                        errorMessage.add("Build new image path failed")
                        return@map null
                    }

                    val bitmap = BitmapBuilder.build(
                        uri = uri,
                        bmpConfig = bitmapConfig,
                        maxSize = request.quality.max
                    )
                    if(bitmap == null) {
                        errorMessage.add("Build new image failed")
                        return@map null
                    }

                    if(!bitmap.save(fileUri = newUri.toString(), isPng = true)) {
                        errorMessage.add("Save new image failed")
                        return@map null
                    }

                    return@map newUri
                }
            }
            // Remove Null Uri
            .filter { uri ->
                return@filter uri != null
            }
            // Build Exist Uri
            .map { uri ->
                return@map uri!!
            }
            // Filter Uri Select Request
            .filter { uri ->
                val message = getFileRequestError(request = request, uri = uri)
                if(message.isNotEmpty()) {
                    errorMessage.add(message)
                    return@filter false
                }
                return@filter true
            }
            .toMutableList()

        if(uriResponse.uriList.isEmpty()) {
            onResult(
                HBG5Fragment.Result.CANCELED,
                HBG5Fragment.UriResponse(error = errorMessage.firstOrNull() ?: "Not found select image")
            )
            return
        }
        onResult(HBG5Fragment.Result.OK, uriResponse)
    }


    // MARK:- ====================== Method
    override fun v5CreateFragment(): HBG5Fragment {
        return HBG5Fragment(context = this, dataRequest = HBG5Fragment.DataRequest())
    }

    override fun v5Restart() {

    }
    /** 通用選擇檔案 */
    private fun selectFile(fileType: String = HBG5Fragment.FileType.ALL.mime) {
//        uriByFileLauncher.launch(Intent(Intent.ACTION_GET_CONTENT)
//            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            .setType("image/*"))

        uriByFileLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(fileType)
        )
    }
    /** 檔案不符合請求條件原因
     * @return isEmpty: 符合
     * */
    private fun getFileRequestError(request: HBG5WidgetConfig.Request.SelectFile, uri: Uri): String {
        val maxMb = request.maxMb
        // 檢查選擇檔案是否超出條件
        if(maxMb != null) {
            val fileMb = uri.getFileSizeMb()
            // 檔案資訊讀取失敗
            if(fileMb == null) {
                return "Not found file size"
            }
            // 檔案大小超出限制
            else if(fileMb > maxMb) {
                return "File size is too large"
            }
        }

        return ""
    }

    /**寫入檔案*/ // todo hbg
    fun v5InsertFile(
        request: HBG5WidgetConfig.Request.InsertFile,
        onResult: (HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit
    ) {
        v5askPermission(
            permissionList = Permission.FILE_W.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }

                val toName = request.toName?.trim() ?: ""
                if(toName.isEmpty()) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "InsertFile name not be null"))
                    return@v5askPermission
                }

                // todo hbg
                when(val from = request.from) {
                    is HBG5WidgetConfig.Request.InsertFile.UriFile -> {
                        val fromUri = from.uri
                        if(fromUri == null) {
                            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "InsertFile from path not be null"))
                            return@v5askPermission
                        }

                        MainScope().launch {

                            var toUri: Uri? = null

                            when(request.toRoot) {
                                HBG5WidgetConfig.Request.InsertFile.Root.PRIVATE -> {
                                    toUri = withContext(Dispatchers.IO) {
                                        fromUri.copyFileToPrivate(
                                            context = this@HBG5LaunchActivity,
                                            dirName = request.toDir,
                                            fileName = toName
                                        )
                                    }
                                }
                                HBG5WidgetConfig.Request.InsertFile.Root.DOWNLOADED -> {
                                    toUri = withContext(Dispatchers.IO) {
                                        fromUri.copyFileToDownload(
                                            context = this@HBG5LaunchActivity,
                                            dirName = request.toDir,
                                            fileName = toName
                                        )
                                    }
                                }
                            }

                            if(toUri != null) {
                                onResult(HBG5Fragment.Result.OK, HBG5Fragment.UriResponse(data = toUri))
                            }
                            else {
                                onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "InsertFile copy file to location failed"))
                            }
                        }
                    }
                    is HBG5WidgetConfig.Request.InsertFile.TextFile -> {
                        val fromText = from.text
                        if(fromText == null) {
                            onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "InsertFile from text not be null"))
                            return@v5askPermission
                        }

                        MainScope().launch {

                            val toUri: Uri? = when(request.toRoot) {
                                HBG5WidgetConfig.Request.InsertFile.Root.PRIVATE -> {
                                    this@HBG5LaunchActivity.getPrivateUri(dirName = request.toDir, fileName = toName)
                                }
                                HBG5WidgetConfig.Request.InsertFile.Root.DOWNLOADED -> {
                                    this@HBG5LaunchActivity.getDownloadUri(dirName = request.toDir, fileName = toName)
                                }
                            }
                            if(toUri != null) {
                                // 執行文字檔案寫入
                                withContext(Dispatchers.IO) {
                                    try {
                                        contentResolver.openOutputStream(toUri)
                                            ?.use { stream -> stream.write(fromText.toByteArray()) }
                                            ?: throw Exception()
                                    }
                                    catch (error: Exception) {
                                        onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "InsertFile copy file to location failed"))
                                    }
                                }
                            }
                            else {
                                onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "InsertFile write uri build failed"))
                            }
                        }
                    }
                    else -> {
                        onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "InsertFile from file type no match"))
                        return@v5askPermission
                    }
                }
            }
        )
    }

    /**拍照取得圖片*/
    fun v5SelectImageByCamera(
        request: HBG5WidgetConfig.Request.SelectImage,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        v5askPermission(
            permissionList = Permission.CAMERA.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }
                val outUri = HBG5Fragment.URI_GET_CAMERA_IMAGE
                if(outUri == null) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "URI_GET_CAMERA_IMAGE null"))
                    return@v5askPermission
                }

                if(!isCameraSupported) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "Not support camera"))
                    return@v5askPermission
                }

                cameraOutputUriList.add(outUri)

                onFragmentResultListener = { result, data ->
                    val uriResponse = data as? HBG5Fragment.UriResponse
                    onSelectImageResult(
                        request = request,
                        onResult = onResult,
                        result = result,
                        uriResponse = uriResponse,
                        canOverwrite = true
                    )
                }

                uriByCameraLauncher.launch(
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        .putExtra(MediaStore.EXTRA_OUTPUT, outUri)
                )
            }
        )
    }
    /**檔案取得圖片*/
    fun v5SelectImageByFile(
        request: HBG5WidgetConfig.Request.SelectImage,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        v5askPermission(
            permissionList = Permission.FILE_R.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }
                onFragmentResultListener = { result, data ->
                    val uriResponse = data as? HBG5Fragment.UriResponse
                    onSelectImageResult(
                        request = request,
                        onResult = onResult,
                        result = result,
                        uriResponse = uriResponse,
                        canOverwrite = false
                    )
                }
                selectFile(fileType = HBG5Fragment.FileType.IMAGE.mime)
            }
        )
    }

    /**錄影取得影像*/
    fun v5SelectVideoByCamera(
        request: HBG5WidgetConfig.Request.SelectVideo,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        v5askPermission(
            permissionList = Permission.CAMERA.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }
                val outUri = HBG5Fragment.URI_GET_CAMERA_VIDEO
                if(outUri == null) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "URI_GET_CAMERA_VIDEO null"))
                    return@v5askPermission
                }

                if(!isCameraSupported) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = "Not support camera"))
                    return@v5askPermission
                }

                cameraOutputUriList.add(outUri)

                onFragmentResultListener = { result, data ->
                    val uriResponse = data as? HBG5Fragment.UriResponse
                    onSelectFileResult(
                        request = request,
                        onResult = onResult,
                        result = result,
                        uriResponse = uriResponse,
                        canOverwrite = true
                    )
                }

                uriByCameraLauncher.launch(
                    Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                        .putExtra(MediaStore.EXTRA_OUTPUT, outUri)
                        .putExtra(MediaStore.EXTRA_VIDEO_QUALITY, request.quality)
                        .putExtra(MediaStore.EXTRA_DURATION_LIMIT, request.maxDurationSecond)
                        .putExtra(MediaStore.EXTRA_SIZE_LIMIT, request.maxMb?.let { maxMb ->
                            return@let (maxMb.toDouble() * 1024.0 * 1024.0).toLong() // byteLength
                        })
                )
            }
        )
    }
    /**檔案取得影像*/
    fun v5SelectVideoByFile(
        request: HBG5WidgetConfig.Request.SelectVideo,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        v5askPermission(
            permissionList = Permission.FILE_R.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }
                onFragmentResultListener = { result, data ->
                    val uriResponse = data as? HBG5Fragment.UriResponse
                    onSelectFileResult(
                        request = request,
                        onResult = onResult,
                        result = result,
                        uriResponse = uriResponse,
                        canOverwrite = false
                    )
                }
                selectFile(fileType = HBG5Fragment.FileType.VIDEO.mime)
            }
        )
    }

    /**取得PDF*/
    fun v5SelectPdf(
        request: HBG5WidgetConfig.Request.SelectFile,
        onResult: ((HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit)
    ) {
        // 2024-11-11 補上權限請求
        v5askPermission(
            permissionList = Permission.FILE_R.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }
                onFragmentResultListener = { result, data ->
                    val uriResponse = data as? HBG5Fragment.UriResponse
                    onSelectFileResult(
                        request = request,
                        onResult = onResult,
                        result = result,
                        uriResponse = uriResponse,
                        canOverwrite = false
                    )
                }
                selectFile(fileType = HBG5Fragment.FileType.PDF.mime)
            }
        )
    }

    /**取得資料庫*/ // todo hbg
    fun v5SelectSqlite(
        onResult: (HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit
    ) {
        v5askPermission(
            permissionList = Permission.FILE_R.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }
                onFragmentResultListener = { result, data ->
                    val uriResponse = data as? HBG5Fragment.UriResponse
                    onSelectFileResult(
                        request = HBG5WidgetConfig.Request.SelectFile().also { request ->
                            request.maxMb = null
                        },
                        onResult = { result, uriResponse ->
                            // 自行檢測 db 格式
                            val uri = uriResponse.uriList.firstOrNull()
                            if(uri == null) {
                                onResult(
                                    HBG5Fragment.Result.CANCELED,
                                    HBG5Fragment.UriResponse(error = "查無檔案路徑")
                                )
                                return@onSelectFileResult
                            }
                            val sqliteType = HBG5DownloadCall.FILE_TYPE_HEADER_SQLLITE.first.replace(".", "")
                            val uriType = uri.fileType(mapOf(HBG5DownloadCall.FILE_TYPE_HEADER_SQLLITE))
                            if(uriType != sqliteType) {
                                onResult(
                                    HBG5Fragment.Result.CANCELED,
                                    HBG5Fragment.UriResponse(error = "所選檔案非資料庫格式")
                                )
                                return@onSelectFileResult
                            }
                            onResult(
                                HBG5Fragment.Result.OK,
                                HBG5Fragment.UriResponse(data = uri)
                            )
                        },
                        result = result,
                        uriResponse = uriResponse,
                        canOverwrite = true
                    )
                }
                selectFile(fileType = HBG5Fragment.FileType.SQLITE.mime)
            }
        )
    }

    /**取得Csv*/ // todo hbg 好像無法判斷檔頭類型
    fun v5SelectCsv(
        onResult: (HBG5Fragment.Result, HBG5Fragment.UriResponse) -> Unit
    ) {
        v5askPermission(
            permissionList = Permission.FILE_R.values(),
            onResult = { granted, error ->
                if(!granted) {
                    onResult(HBG5Fragment.Result.CANCELED, HBG5Fragment.UriResponse(error = error))
                    return@v5askPermission
                }
                onFragmentResultListener = { result, data ->
                    val uriResponse = data as? HBG5Fragment.UriResponse
                    onSelectFileResult(
                        request = HBG5WidgetConfig.Request.SelectFile().also { request ->
                            request.maxMb = null
                        },
                        onResult = { result, uriResponse ->
                            // 自行檢測 db 格式
                            val uri = uriResponse.uriList.firstOrNull()
                            if(uri == null) {
                                onResult(
                                    HBG5Fragment.Result.CANCELED,
                                    HBG5Fragment.UriResponse(error = "查無檔案路徑")
                                )
                                return@onSelectFileResult
                            }
                            // 讀取資源型檔案
                            try {
                                var firstLine = ""

                                HBG5Application.instance?.contentResolver?.openInputStream(uri)?.use { stream ->
                                    stream.bufferedReader().use { reader ->
                                        firstLine = reader.readLine().trim()
                                    }
                                }

                                if(firstLine.isEmpty()) {
                                    throw NullPointerException()
                                }

                                onResult(
                                    HBG5Fragment.Result.OK,
                                    HBG5Fragment.UriResponse(data = uri)
                                )
                            }
                            catch (error: Exception) {
                                onResult(
                                    HBG5Fragment.Result.CANCELED,
                                    HBG5Fragment.UriResponse(error = "所選檔案非文字格式或內容為空")
                                )
                            }
                        },
                        result = result,
                        uriResponse = uriResponse,
                        canOverwrite = true
                    )
                }
                selectFile(fileType = HBG5Fragment.FileType.CSV.mime)
            }
        )
    }
}

val HBG5LaunchActivity.isCameraSupported: Boolean
    get() {
        if(!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            return false
        }
        val handleName = Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(packageManager)
        if(handleName == null) {
            return false
        }

        return true
    }