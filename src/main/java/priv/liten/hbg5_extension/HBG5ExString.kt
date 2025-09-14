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

fun String.toCalendar(format: String, local: Locale = Locale.ENGLISH): Calendar? {
    try {
        val sdf = SimpleDateFormat(format, local)

        val date = sdf.parse(this) ?: return null

        val calendar = Calendar.getInstance(Locale.getDefault())

        calendar.timeInMillis = date.time

        return calendar
    }
    catch (error: Exception) { return null }
}
// todo hbg
/**yyyy-MM-ddTHH:mm:ss.SSSZ*/
fun String.toFullIso8601(): String? {
    var date = ""
    var time = "00:00:00:000"

    val dateTime = this
        .replace("/", "-")
        .replace(" ", "T")
        .replace("Z", "")
        .replace(".", ":")

    val timeIndex = dateTime.indexOf("T")
    if(timeIndex != -1) {
        date = dateTime.substring(0, timeIndex)
        if(timeIndex + 1 < dateTime.length) {
            time = dateTime
                .substring(timeIndex + 1, dateTime.length)
                .replace(".", ":")
        }
    }
    else {
        if(dateTime.contains("-")) {
            date = dateTime
        }
        else if(dateTime.contains(":")) {
            time = dateTime
        }
    }
    if(date.isEmpty() || time.isEmpty()) { return null }

    val dateList = mutableListOf(1900, 1, 1)
    val timeList = mutableListOf(0, 0, 0, 0)

    for((index, value) in date.split("-").withIndex()) {
        if(index >= dateList.size) { return null }
        val intValue = value.toIntOrNull() ?: return null
        dateList[index] = intValue
    }
    for((index, value) in time.split(":").withIndex()) {
        if(index >= timeList.size) { return null }
        val intValue = value.toIntOrNull() ?: return null
        timeList[index] = intValue
    }

    return "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ".format(
        dateList[0],
        dateList[1],
        dateList[2],
        timeList[0],
        timeList[1],
        timeList[2],
        timeList[3]
    )
}

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
