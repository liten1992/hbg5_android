package priv.liten.hbg5_extension

fun <T> Collection<T>.contains(finder: ((T) -> Boolean)): Boolean {
    return this.firstOrNull(finder) != null
}

fun <T> Collection<T>.toStringList(convert: ((T) -> String)): MutableList<String> {

    val result = mutableListOf<String>()

    for (item in this) {
        result.add(convert(item))
    }

    return result
}

fun <T> Collection<T>.toIntArray(convert: ((T) -> Int)): IntArray {

    val result = IntArray(this.size)

    for((index, item) in this.withIndex()) {
        result[index] = convert(item)
    }

    return result
}

fun <T> Collection<T>.toLongArray(convert: ((T) -> Long)): LongArray {

    val result = LongArray(this.size)

    for((index, item) in this.withIndex()) {
        result[index] = convert(item)
    }

    return result
}

fun <T> MutableList<T>.onAddAll(vararg list: List<T>?): MutableList<T> {
    if(list.isEmpty()) { return this }
    for(item in list) {
        if(item.isNullOrEmpty()) { continue }
        this.addAll(item)
    }
    return this
}
// todo hbg
fun <T> List<T>.onAdd(vararg items: T): MutableList<T> {
    val result = this.toMutableList()
    for(item in items) {
        result.add(item)
    }
    return result
}
// todo hbg
fun <T> List<T>.onAdd(index: Int, item: T): MutableList<T> {
    val result = this.toMutableList()
    result.add(index, item)
    return result
}
// todo hbg
fun <T> List<T>.onRemoveAt(index: Int): MutableList<T> {
    val result = this.toMutableList()
    result.removeAt(index)
    return result
}

fun IntRange.toStringList(convert: ((Int) -> String)): MutableList<String> {

    val result = mutableListOf<String>()

    for (i in this) {
        result.add(convert(i))
    }

    return result
}

fun IntProgression.toStringList(convert: ((Int) -> String)): MutableList<String> {

    val result = mutableListOf<String>()

    for (i in this) {
        result.add(convert(i))
    }

    return result
}

fun <T> MutableList<T>.addUnique(item: T) {
    if(this.contains(item)) { return }
    this.add(item)
}

fun <T> List<T>.isSize(size: Int): Boolean {
    return this.size == size
}

fun <T> List<T>.isSingle(): Boolean {
    return this.size == 1
}

fun <T> List<T>.isMultiple(): Boolean {
    return this.size > 1
}
/**todo hbg 將索引定位至顯示清單的區間內 如果清單為空則 -1*/
fun <T> List<T>.indexOfRange(index: Int): Int {
    if(isEmpty()) { return -1 }
    if(0 <= index && index < count()) { return index }
    if(0 > index) { return 0 }
    if(index > count() - 1) { return count() - 1 }
    return -1
}
/**todo hbg 將索引定位至顯示清單的區間內 如果清單為空則 -1*/
fun <T> List<T>.indexOfRange(call: ((List<T>) -> Int)): Int {
    return indexOfRange(index = call(this))
}
/**todo hbg*/
fun <T, P> Map<T, P>.isSingle(): Boolean = this.size == 1
/**todo hbg*/
fun <T, P> Map<T, P>.isMultiple(): Boolean = this.size > 1
/**todo hbg 合併列表(不重複)*/
fun <T> List<T>.unionUnique(other: List<T>): List<T> {
    if(this.size + other.size == 0) { return emptyList() }
    val result = ArrayList<T>(this.size + other.size)
    for(list in arrayOf(this, other)) {
        for(item in list) {
            if(result.contains(item)) { continue }
            result.add(item)
        }
    }
    return result
}

fun <T> MutableList<T>.update(item: T) {
    val index = this.indexOf(item)
    if(index == -1) { return }
    this[index] = item
}
/**排除重複元素*/
fun <T, R> List<T>.filterUnique(keyCreator: ((T) -> R)): List<T> {
    val keyList = mutableListOf<R>()
    return this.filter { item ->
        val key = keyCreator(item)
        if(keyList.contains(key)) { return@filter false }
        else {
            keyList.add(key)
            return@filter true
        }
    }
}
/**找尋符合類別的項目*/
inline fun <reified T> List<*>.firstInstance(): T? {
    return this.firstOrNull { item -> item is T } as? T
}
/**轉換*/ // todo hbg
inline fun <T, R> Iterable<T>.maps(transform: (T) -> Iterable<R>): List<R> {
    val result: MutableList<R> = mutableListOf()
    for(item in this) {
        result.addAll(transform(item))
    }
    return result
}
/**轉換並且剔除重複*/
inline fun <T, R> List<T>.mapUnique(transform: (T) -> R): List<R> {
    val result = mutableListOf<R>()
    for(item in this) {
        val resultItem = transform(item)
        if(result.contains(resultItem)) { continue }
        result.add(resultItem)
    }

    return result
}

/**列表為空值拋出指定例外*/
@Throws
fun <T, L: List<T>> L.throwIfEmpty(error: (() -> Exception)): L {
    if(this.isEmpty()) { throw error() }
    return this
}