package priv.liten.hbg5_extension

import android.database.Cursor
import android.text.TextUtils
import androidx.core.database.getStringOrNull
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

//.setPrettyPrinting().serializeNulls().setLongSerializationPolicy(LongSerializationPolicy.STRING)
fun Any.toJson(
    // 關閉Json轉換時會將部分特殊服務轉換為Unicode碼問題 ex: & > \u0026
    gson: Gson = GsonBuilder().disableHtmlEscaping().create()
) : String {
    return gson.toJson(this)
}
/**version=1.0&arg1=1&arg2=2*/
fun Any.toUrlParams() : String = toUrlMap().map { (key, value) -> "$key=$value" }.toLinkString("&")

fun Any.toUrlMap() : Map<String, String> {
    return mutableMapOf<String, String>().also { result ->

        val json = Gson().toJson(this)

        val mapType = object : TypeToken<MutableMap<String, Any>>() {}.type

        val map: MutableMap<String, Any> = GsonBuilder().registerTypeAdapter(
            mapType,
            object : JsonDeserializer<MutableMap<String, Any>> {
                override fun deserialize(
                    json: JsonElement?,
                    typeOfT: Type?,
                    context: JsonDeserializationContext?
                ): MutableMap<String, Any> {

                    val result = mutableMapOf<String, Any>()

                    json?.let {
                        if (json.isJsonObject) {
                            for (entry in json.asJsonObject.entrySet()) {
                                val jKey = entry.key
                                val jObj = entry.value

                                if (!jObj.isJsonPrimitive) {
                                    continue
                                }

                                val jBase = jObj.asJsonPrimitive

                                if (jBase.isBoolean) {
                                    result[jKey] = jBase.asBoolean
                                } else if (jBase.isString) {
                                    result[jKey] = jBase.asString
                                } else if (jBase.isNumber) {
                                    if (jBase.asString.contains(".")) {
                                        result[jKey] = jBase.asDouble
                                    } else {
                                        result[jKey] = jBase.asLong
                                    }
                                }
                            }
                        }
                    }

                    return result
                }
            })
            .create()
            .fromJson(json, mapType)

        for ((key, value) in map) {
            when(value) {
                is Int, is Long, is Float, is Double, is String, is Boolean -> { // todo hbg
                    result[key] = value.toString()
                }
            }
        }
    }
}

fun <T> T.region(block: () -> Unit): T {
    block()
    return this
}
/**使用JSON技術克隆物件*/
inline fun <reified T> Any.jsonClone(): T {
    return Gson().fromJson(toJson(), T::class.java)
}
/**使用JSON技術克隆物件*/
inline fun <reified T> Any.jsonArrayClone(): List<T> {
    return Gson().fromJson(toJson(), object: TypeToken<MutableList<T>>(){ }.type)
}