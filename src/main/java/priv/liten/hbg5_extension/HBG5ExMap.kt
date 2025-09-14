package priv.liten.hbg5_extension


fun <K, V> MutableMap<K, V>.cache(key: K, default: V): V {

    return this[key]
        ?.let { return it }
        ?:run {
            this[key] = default
            return default
        }
}

fun <K, V> MutableMap<K, V>.cache(key: K, defaultCreator: ((K) -> V)): V {

    return this[key]
        ?.let { return it }
        ?:run {
            val default = defaultCreator(key)
            this[key] = default
            return default
        }
}

class HBG5CacheMap<K, V> {

    constructor(creator: (K) -> V?) {
        this.creator = creator
    }

    var content = mutableMapOf <K, V?>()
    var creator : (K) -> V?

    fun get(key: K): V? {
        return if(content.containsKey(key)) content[key]
        else creator(key).also { content[key] = it }
    }

    fun clear() {
        content.clear()
    }
}

/** 深度查詢欄位數值 */
fun Map<*, *>.getDeep(key: String): Any? {
    for((k, v) in this) {
        if(k == key) { return v }
        if(v is Map<*, *>) {
            val temp = v.getDeep(key = key)
            if(temp != null) { return temp }
        }
    }
    return null
}