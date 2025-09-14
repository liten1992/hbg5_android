package priv.liten.hbg5_manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_extension.decryptAES
import priv.liten.hbg5_extension.encryptAES
import java.lang.ref.WeakReference
import java.util.UUID

/** 讀寫資料管理 */
open class HBG5IOManager {
    // MARK:- ====================== Define
    companion object {
        var AES_KEY = "2VMh5BvMBRSJaQlwNm8S".subSequence(0, 16).toString()
        var instance: WeakReference<HBG5IOManager> = WeakReference(null)
    }

    // MARK:- ====================== Constructor
    constructor(context: Context) {
        this.context = context
        this.sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

        instance = WeakReference(this)
    }

    // MARK:- ====================== Data
    var context : Context private set
    /**標記上鎖狀態 避免死循環*/
    var lock = false
    /**非上鎖狀態執行*/
    fun lock(call: ()->Unit) {
        if(lock) { return }
        lock = true
        call()
        lock = false
    }

    /**裝置資訊*/ // todo hbg
    val device: HBG5DeviceDetail get() = HBG5DeviceDetail()

    /**裝置識別碼*/ // todo hbg
    var uuid: String = ""
        get() {
            if(field.isEmpty()) {
                field = readDisk( // todo hbg
                    key = "Device_UUID",
                    decrypt = false
                )?.trim() ?: ""
                // 未建立
                if(field.isEmpty()) {
                    uuid = UUID.randomUUID().toString()
                }
            }
            return field
        }
        set(value) {
            if(value.isEmpty()) { return }
            field = value
            writeDisk(
                key = "Device_UUID",
                value = field,
                encrypt = false
            )
        }

    private var sharedPreferences : SharedPreferences


    // MARK:- ====================== Method
    /** 讀取 todo hbg */
    fun <T> readDisk (key: String? = null, cls: Class<T>, decrypt: Boolean = false) : T? {
        val jsonString = readDisk(
            key = key ?: cls.name,
            decrypt = decrypt) ?: ""
        return if (jsonString.isEmpty()) null
        else Gson().fromJson(jsonString, cls)
    }
    /** 讀取 */
    fun readDisk(key: String, decrypt: Boolean = false) : String? {

        val value = sharedPreferences.getString(key, null)

        return if(decrypt) value?.decryptAES(AES_KEY) else value
    }

    /** 寫入 */
    fun <T> writeDisk(key: Class<T>, value: T?, encrypt: Boolean = false) {

        val json : String? = if(value != null) Gson().toJson(value) else null

        writeDisk(key.name, json, encrypt)
    }
    /** 寫入 */
    fun writeDisk(key: String, value: String?, encrypt: Boolean = false) {
        value
            ?.let {
                sharedPreferences
                    .edit()
                    .putString(key, if(encrypt) it.encryptAES(AES_KEY) else value)
                    .apply()
            }
            ?:run {
                sharedPreferences
                    .edit()
                    .remove(key)
                    .apply()
            }
    }
}
// todo hbg
class HBG5DeviceDetail {
    /**裝置識別碼 37c2ddc4-e3d0-420d-ac37-79a5b5e99c2b*/
    @SerializedName("Uuid")
    var uuid: String? = HBG5IOManager.instance.get()?.uuid
    /**裝置型號 Samsung S25*/
    @SerializedName("Model")
    var model: String? = "${Build.MANUFACTURER} ${Build.MODEL}" // ${Build.DEVICE}
    /**系統平台 */
    @SerializedName("Type")
    var type: String? = "Android"

    /**APP 版號 */
    @SerializedName("AppVersion")
    var appVersion: String? = BuildConfig.VERSION_NAME
    /**系統版號 */
    @SerializedName("SysVersion")
    var sysVersion: String? = Build.VERSION.RELEASE
}