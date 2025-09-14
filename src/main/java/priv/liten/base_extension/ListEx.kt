package priv.liten.base_extension

fun <T> MutableList<T>.append(item: T): Boolean {
    return this.add(item)
}