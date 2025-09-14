package priv.liten.hbg5_widget.application

import android.Manifest
import android.app.Activity
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_config.HBG5HttpConfig
import priv.liten.hbg5_extension.exists
import priv.liten.hbg5_extension.getPrivateUri
import priv.liten.hbg5_extension.toString
import priv.liten.hbg5_extension.toUri
import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import java.io.File
import java.lang.ref.WeakReference
import java.util.Calendar
import kotlin.system.exitProcess

open class HBG5Application: Application() {

    companion object {

        const val CHANNEL_ID = "Default"
        const val CHANNEL_NAME = "Default"

        var instance: HBG5Application? = null
            private set

        /**用於檔案命名解決衝突的計數器*/
        private var FILE_NAME_COUNTER: Long = 0L
        /**用於檔案命名解決衝突的計數器*/
        val FILE_NAME_COUNTER_ABS: Long
            get() {
                if(FILE_NAME_COUNTER < 0) { FILE_NAME_COUNTER = 0 }
                FILE_NAME_COUNTER += 1
                return FILE_NAME_COUNTER
            }
        /**紀錄頁面堆疊*/
        var LAUNCHER_ACTIVITIES: MutableList<WeakReference<Activity>> = mutableListOf()
    }

    override fun onCreate() {

        super.onCreate()

        instance = this

        // 建立 android 8+(26) 通知頻道，須建立才可正常使用通知效果
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NOTIFICATION_SERVICE) as? NotificationManager)?.let { manager ->

                val otherChannel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ) // 通知等級
                otherChannel.description = CHANNEL_NAME // 描述
                otherChannel.enableLights(true) // 閃屏
                otherChannel.enableVibration(true) // 震動
                manager.createNotificationChannel(otherChannel)
            }
        }
        // 嘗試建立暫存資料夾
        this.externalCacheDir?.mkdirs()
        // 監測頁面生命週期
        registerActivityLifecycleCallbacks(object:ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                LAUNCHER_ACTIVITIES.add(WeakReference(activity))
            }

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                LAUNCHER_ACTIVITIES.removeAll { it.get() == null || it.get() == activity }
            }
        })
    }

    /** 顯示推播訊息
     * @return notify id 如果是NULL代表發送通知失敗
     * */
    fun notify(
        id: Int? = null,
        @DrawableRes icon: Int,
        title: String,
        content: String,
        intent: PendingIntent? = null): Int? {

        // 檢查推播權限
        if(!hasPermissionNotification()) { return null }
        val key = "NOTIFICATION_ID"
        val newId = id ?: shared.getInt(key, 0)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return null
        manager.notify(newId, NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(content)
            .setAutoCancel(true)
            .setContentIntent(intent)
            .setDefaults(Notification.DEFAULT_ALL)
            .build())

        shared
            .edit()
            .putInt(key, newId + 1)
            .apply()

        return newId
    }

    private val shared: SharedPreferences by lazy { getSharedPreferences(packageName, Context.MODE_PRIVATE) }

    /**安裝APK*/
    fun install(uri: Uri) {
        // 檔案存在
        if(uri.exists()) {
            val activity = HBG5LaunchActivity.instance.firstOrNull() ?: return
            activity.install(uri = uri)
        }
        // 檔案不存在
        else {
            if(BuildConfig.DEBUG) {
                Log.d("//APK", "Install Not found download app file")
            }
        }
    }

    /** 擁有推播訊息權限 */
    fun hasPermissionNotification(): Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else true
    }

    /**取得網路連接狀態*/
    suspend fun getNetConnectedType(): HBG5HttpConfig.NetConnectedType {
        val connectManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return HBG5HttpConfig.NetConnectedType.NONE
        val network = connectManager.activeNetwork ?: return HBG5HttpConfig.NetConnectedType.NONE
        val capabilities = connectManager.getNetworkCapabilities(network) ?: return HBG5HttpConfig.NetConnectedType.NONE

        if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return HBG5HttpConfig.NetConnectedType.MOBILE
        }
        if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return HBG5HttpConfig.NetConnectedType.WIFI
        }
        return HBG5HttpConfig.NetConnectedType.NONE
    }
}

inline fun <reified T> Context.getJsonArrayByRaw(@RawRes id: Int): List<T>? {
    val json = getStringByRaw(id = id)
    return Gson().fromJson(json, object: TypeToken<List<T>>() {}.type)
}

inline fun <reified T> Context.getJsonObjectByRaw(@RawRes id: Int): T? {
    val json = getStringByRaw(id = id)
    return Gson().fromJson(json, object: TypeToken<T>() {}.type)
}
/** todo hbg */
inline fun <reified T> Context.getJsonObjectByRawNoThrow(@RawRes id: Int): T? {
    return try {
        Gson().fromJson(getStringByRaw(id = id), object: TypeToken<T>() {}.type)
    }
    catch (error: Exception) { null }
}

fun Context.getStringByRaw(@RawRes id: Int): String {
    resources.openRawResource(id).use { stream ->
        return String(stream.readBytes())
    }
}
/** 退出程序 */
fun HBG5Application.exit() {
    // 由上至下移除頁面
    if(HBG5Application.LAUNCHER_ACTIVITIES.isNotEmpty()) {
        for (index in (0 until HBG5Application.LAUNCHER_ACTIVITIES.size).reversed()) {
            val activity = HBG5Application.LAUNCHER_ACTIVITIES[index].get()
            HBG5Application.LAUNCHER_ACTIVITIES.removeAt(index)
            activity?.finish()
        }
    }
    // 晚一點終止程序 確保資料可以被保存
    MainScope().launch {
        delay(500)
        exitProcess(0)
    }
}


//fun Context.



///** todo hbg 私有應用不需讀寫權限，生成快取資料夾路徑 */
//fun Context.buildCacheDirUriString(): String? {
//    val cacheDir = externalCacheDir ?: return null
//    if(!cacheDir.exists() && !cacheDir.mkdirs()) { return null }
//    val cacheDirUriString = cacheDir.absolutePath
//    return cacheDirUriString.ifEmpty { null }
//}
//
///** todo hbg 私有應用不需讀寫權限，取得應用預設保存資料路徑
// * @param fileName is empty will use random name
// * @param fileType .jpg .png
// */
//fun Context.buildCacheFileUriString(fileName: String = "", fileType: String = "", randomNameHeader: String = "Default"): String? {
//    val appFileDirUriString = buildCacheDirUriString() ?: return null
//    if(appFileDirUriString.isEmpty()) { return null }
//    val newFileName = buildFileName(
//        fileName = fileName,
//        fileType = fileType,
//        randomNameHeader = randomNameHeader
//    )
//
//    return File("${appFileDirUriString}${File.separatorChar}${newFileName}")
//        .toUri(context = this)
//        .toString()
//}


/** todo hbg 取得應用檔案名稱 如不輸入則產生隨機檔名
 */
fun Context.buildFileName(fileName: String = "", fileType: String = "", randomNameHeader: String = "Default"): String {
    var newFileName = fileName.trim()

    newFileName =
        if(newFileName.isEmpty()) {
            val counter = "%04d".format(HBG5Application.FILE_NAME_COUNTER_ABS)
            val time = Calendar.getInstance().toString(format = "yyyyMMddHHmmssSSS")
            "${randomNameHeader}_${time}_$counter$fileType"
        }
        else {
            "${newFileName}${fileType}"
        }

    return newFileName
}
