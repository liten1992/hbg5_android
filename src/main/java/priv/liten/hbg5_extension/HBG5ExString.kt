package priv.liten.hbg5_extension

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.TextUtils
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** flag: android.util.Base64.NO_WRAP */
fun String.encodeBase64(flag: Int = Base64.NO_WRAP): String {
    return Base64.encodeToString(this.toByteArray(Charset.defaultCharset()), flag)
}

/** flag: android.util.Base64.NO_WRAP */
fun String.decodeBase64(flag: Int = Base64.NO_WRAP): String {
    return String(Base64.decode(this, flag), Charset.defaultCharset())
}

@SuppressLint("GetInstance")
fun String.encryptAES(key: String, iv: String = ""): String {
    return when {
        iv.isEmpty() -> {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "AES"))

            String(
                Base64.encode(cipher.doFinal(this.toByteArray(Charset.forName("UTF-8"))), Base64.NO_WRAP),
                Charset.forName("UTF-8"))
        }
        else -> {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

            cipher.init(
                Cipher.ENCRYPT_MODE,
                SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "AES"),
                IvParameterSpec(iv.toByteArray(Charset.forName("UTF-8"))))

            String(
                Base64.encode(cipher.doFinal(this.toByteArray(Charset.forName("UTF-8"))), Base64.NO_WRAP),
                Charset.forName("UTF-8"))
        }
    }
}

@SuppressLint("GetInstance")
fun String.decryptAES(key: String, iv: String = ""): String {
    return when {
        iv.isEmpty() -> {
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "AES"))

            String(
                cipher.doFinal(Base64.decode(this, Base64.NO_WRAP)),
                Charset.forName("UTF-8"))
        }
        else -> {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")

            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key.toByteArray(Charset.forName("UTF-8")), "AES"),
                IvParameterSpec(iv.toByteArray(Charset.forName("UTF-8"))))

            String(
                cipher.doFinal(Base64.decode(this, Base64.NO_WRAP)),
                Charset.forName("UTF-8"))
        }
    }
}
// todo hbg
fun String.encryptECB(key: ByteArray): String {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

    cipher.init(
        Cipher.ENCRYPT_MODE,
        SecretKeySpec(key, "AES"))

    return String(
        Base64.encode(cipher.doFinal(this.toByteArray(Charset.forName("UTF-8"))), Base64.NO_WRAP),
        Charset.forName("UTF-8"))
}
// todo hbg
fun String.decryptECB(key: ByteArray): String {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")

    cipher.init(
        Cipher.DECRYPT_MODE,
        SecretKeySpec(key, "AES"))

    return String(
        cipher.doFinal(Base64.decode(this, Base64.NO_WRAP)),
        Charset.forName("UTF-8"))
}

fun String.md5(): String {

    if(isEmpty()) { return "" }

    val instance = MessageDigest.getInstance("MD5")

    return BigInteger(1, instance.digest(this.toByteArray())).toString(16).padStart(32, '0')
}

fun String.link(linkText: String? = null, value: String?): String {
    return if ((value?:"").isEmpty()) {
        this
    }
    else {
        "$this${linkText?:""}$value"
    }
}

fun <T> List<T>.toLinkString (linkText: String, convert: ((T?) -> String) = { it?.toString() ?: "null" }): String {

    val result = StringBuilder()

    for((i, obj) in this.withIndex()) {
        if(i > 0 && linkText.isNotEmpty()) {
            result.append(linkText)
        }
        result.append(convert(obj))
    }

    return result.toString()
}

fun String.toColor(): Int {
    return Color.parseColor(this)
}

fun String.toCalendar(
    format: String,
    timeZone: Int? = null,
    local: Locale = Locale.getDefault()): Calendar? {
    try {
        val sdf = SimpleDateFormat(format, local)
        val zone: TimeZone? = when {
            timeZone == null -> null
            else -> TimeZone.getTimeZone("GMT%+03d".format(timeZone))
        }

        if(zone != null) {
            sdf.timeZone = zone
        }
        val date = sdf.parse(this) ?: return null

        val calendar =
            if(zone == null) Calendar.getInstance(local)
            else Calendar.getInstance(zone, local)

        calendar.timeInMillis = date.time

        return calendar
    }
    catch (error: Exception) { return null }
}
/**
 * 預設 日期 1900-01-01 時間 00:00:00.000 如果輸入的日期時間格式中未表明 時區偏移 則使用 裝置預設時區
 * @param outUtc 協助轉換 UTC 偏移量 如不填寫 則以當前設備的偏移量為主
 * @param defInUtc 當文字格式沒有顯示 UTC 偏移量時預設的偏移量 null: 使用設備預設偏移量
 * @return null: 轉換失敗, yyyy-MM-ddTHH:mm:ss.SSS+-HH:mm(UTC)
 * */
fun String.toIso8601(outUtc: Int? = null, defInUtc: Int? = null): String? {
    var date = "1900-01-01"
    var time = "00:00:00"
    var inUtc = defInUtc ?: (Calendar.getInstance().timeZone.rawOffset / 3600000)
    // 將時間日期正規化 2020/01/01 23:59:59.999Z -> 2020-01-01T23:59:59:999Z
    var dateTime = this
        .uppercase()
        .replace("/", "-")
        .replace(" ", "T")
        .replace(".", ":")
    // utc 計算 只有在包含時間時 時區偏移才有作用
    if(dateTime.contains("T")) {
        val timeIndex = dateTime.indexOf("T")
        var foundIndex = -1
        // "Z"
        if(foundIndex == -1) {
            foundIndex = dateTime.lastIndexOf("Z")
            if(foundIndex != -1 && timeIndex < foundIndex) {
                inUtc = 0
            }
        }
        // "+"
        if(foundIndex == -1) {
            foundIndex = dateTime.lastIndexOf("+")
            if(foundIndex != -1 && timeIndex < foundIndex) {
                val args = dateTime.substring(foundIndex + 1, dateTime.length).split(":")
                inUtc = args[0].toIntOrNull() ?: inUtc
            }
        }
        // "-"
        if(foundIndex == -1) {
            foundIndex = dateTime.lastIndexOf("-")
            if(foundIndex != -1 && timeIndex < foundIndex) {
                val args = dateTime.substring(foundIndex + 1, dateTime.length).split(":")
                inUtc = args[0].toIntOrNull()?.let { -it } ?: inUtc
            }
        }
        // 移除 UTC 字串資訊
        if(foundIndex != -1 && timeIndex < foundIndex) {
            dateTime = dateTime.substring(0, foundIndex)
        }
    }

    // 文字包含時間資訊
    if(dateTime.contains("T")) {
        val args = dateTime.split("T")
        // 更新日期資訊
        date = args[0]
        // 更新時間資訊
        time = args[1]
    }
    // 文字僅日期資訊
    else if(dateTime.contains("-")) {
        date = dateTime
    }
    // 文字僅時間資訊
    else if(dateTime.contains(":")) {
        time = dateTime
    }

    if(date.isEmpty() || time.isEmpty()) { return null }

    var year = 1900
    var month = 1
    var day = 1
    var hour = 0
    var minute = 0
    var second = 0
    var millis = 0

    for((index, value) in date.split("-").withIndex()) {
        when(index) {
            0 -> {
                year = value.toIntOrNull() ?: year
            }
            1 -> {
                month = value.toIntOrNull() ?: month
            }
            2 -> {
                day = value.toIntOrNull() ?: day
            }
        }
    }
    for((index, value) in time.split(":").withIndex()) {
        when(index) {
            0 -> {
                hour = value.toIntOrNull() ?: hour
            }
            1 -> {
                minute = value.toIntOrNull() ?: minute
            }
            2 -> {
                second = value.toIntOrNull() ?: second
            }
            3 -> {
                millis = value.toIntOrNull() ?: millis
            }
        }
    }

    val outCalendar = Calendar.getInstance(
        TimeZone.getTimeZone("GMT%+03d".format(inUtc))
    )
    outCalendar.set(year, month - 1, day, hour, minute, second)
    outCalendar.set(Calendar.MILLISECOND, millis)

    return outCalendar.toString(
        format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        utc = outUtc
    )
}
// todo hbg
/**
 * @param defInUtc 當文字格式沒有顯示 UTC 偏移量時預設的偏移量 null: 使用設備預設偏移量
 * */
fun String.toIso8601Calendar(defInUtc: Int? = null): Calendar? = toIso8601(defInUtc = defInUtc)?.toCalendar(format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
// todo hbg

fun String.toCsvFields(): List<String> {

    val line = this.trim()
    if(line.isEmpty()) { return mutableListOf() }

    val result = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex()).toMutableList()

    for((i, text) in result.withIndex()) {
        val len = text.length
        val start = if(text.startsWith("\"")) 1 else 0
        val end = if(text.endsWith("\"")) len - 1 else len
        var newText = text
        if(start != 0 || end != len) {
            newText = newText.substring(start, end)
        }

        result[i] = newText.replace("\"\"", "\"")
    }
    return result
}

fun CharSequence.toTrimString(): String {
    return toString().trim()
}
/** indexStr="@" -> Example@mail.com -> Example */
fun String.subStart(indexStr: String? = null, ignoreCase: Boolean = false): String {
    return if(indexStr == null) this
    else run {
        val index = this.indexOf(string = indexStr, ignoreCase = ignoreCase)
        return@run if(index == -1) ""
        else this.substring(0, index)
    }
}
/** todo hbg
 * @param diffIntFloat 如果轉換成Map<String,Any>的時候 true:整數與浮點數(可能降低效能) 視為不同的參數 false:遇到數字一律轉出浮點數(GSON套件預設值)
 * */
inline fun <reified T> String.toJsonObject(diffIntFloat: Boolean = false): T? {
    val gson =
        if(diffIntFloat) GsonBuilder()
            .registerTypeHierarchyAdapter(Map::class.java, object :
                JsonDeserializer<Map<String, Any>> {
                // 準確的識別浮點數或整數
                fun parseValue(primitive: JsonPrimitive): Any {
                    return if(primitive.isNumber)
                        if(primitive.asString.contains('.')) primitive.asDouble // 浮點數
                        else primitive.asLong // 整數
                    else if(primitive.isBoolean)
                        primitive.asBoolean
                    else
                        primitive.asString
                }

                override fun deserialize(
                    json: JsonElement,
                    typeOfT: Type,
                    context: JsonDeserializationContext
                ): Map<String,Any> {
                    if(json.isJsonObject) {
                        val result = mutableMapOf<String,Any>()
                        val jObj = json.asJsonObject
                        for((k,v) in jObj.entrySet()) {
                            if(v.isJsonPrimitive){
                                result[k] = parseValue(v.asJsonPrimitive)
                            }
                            else if(v.isJsonObject){
                                result[k] = context.deserialize(v, typeOfT) as Any
                            }
                            else if(v.isJsonArray){
                                val list = mutableListOf<Any>()
                                for(vv in v.asJsonArray) {
                                    if(vv.isJsonPrimitive) {
                                        list.add(parseValue(vv.asJsonPrimitive))
                                    }
                                    else if(vv.isJsonObject) {
                                        list.add(context.deserialize(vv, Map::class.java))
                                    }
                                    else if(vv.isJsonArray){
                                        list.add(context.deserialize(vv, List::class.java))
                                    }
                                }
                                result[k] = list
                            }
                        }
                        return result
                    }
                    else {
                        return emptyMap()
                    }
                }
            })
            .create()
        else Gson()
    if(this.trim().isEmpty()) { return null }
    return gson.fromJson(this, T::class.java)
}
/** todo hbg
 * */
inline fun <reified T> String.toJsonArray(): MutableList<T>? {
    val json = this
    return if(TextUtils.isEmpty(json)) null
    else Gson().fromJson(json, object: TypeToken<MutableList<T>>(){}.type) ?: mutableListOf()
}

fun String.replace(oldValues: List<String>, newValue: String): String {
    var result = this
    for(oldValue in oldValues) {
        result = result.replace(oldValue, newValue)
    }
    return result
}

// todo hbg
fun String.ifNotEmpty(action:((String) -> String)): String {
    return if(this.isEmpty()) this else action(this)
}
// todo hbg
/**如果字串為空值拋出例外*/
@Throws
fun String.throwIfEmpty(error: (() -> Exception)): String {
    if(this.isEmpty()) {
        throw error()
    }
    return this
}

fun String.maxLength(len: Int): String {
    if(len <= 0) { return "" }

    if(len > this.length) { return this }

    return this.substring(0, len)
}
