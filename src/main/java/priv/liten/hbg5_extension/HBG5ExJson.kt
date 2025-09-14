package priv.liten.hbg5_extension

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

fun JsonPrimitive.base(): Any? {
    when {
        isString -> return asString
        isNumber -> {
            val vFloat = asFloat
            return if(vFloat % 1 != 0f) vFloat else vFloat.toInt()
        }
        isBoolean -> return asBoolean
        else -> return null
    }
}
fun JsonArray.map(put: MutableList<Any> = mutableListOf()): List<Any> {
    for(jValue in this) {
        when {
            jValue.isJsonObject -> {
                put.add(jValue.asJsonObject.map())
            }
            jValue.isJsonArray -> {
                put.add(jValue.asJsonArray.map())
            }
            jValue.isJsonPrimitive -> {
                val jPrimitive = jValue.asJsonPrimitive.base()
                if(jPrimitive != null) {
                    put.add(jPrimitive)
                }
            }
        }
    }
    return put
}
fun JsonObject.map(put: MutableMap<String, Any> = mutableMapOf()): Map<String, Any> {
    for ((key, value) in this.entrySet()) {
        when {
            value.isJsonObject -> {
                put[key] = value.asJsonObject.map()
            }
            value.isJsonArray -> {
                put[key] = value.asJsonArray.map()
            }
            value.isJsonPrimitive -> {
                put[key] = value.asJsonPrimitive.base() ?: continue
            }
            else -> continue
        }
    }
    return put
}