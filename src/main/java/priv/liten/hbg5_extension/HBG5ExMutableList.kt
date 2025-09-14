package priv.liten.hbg5_extension

class HBG5ExMutableList {
}

fun <T> MutableList<T>.insertOrReplace(element: T) {
    val index = this.indexOf(element)

    if(index == -1) {
        this.add(element)
    }
    else {
        this[index] = element
    }
}

fun <T> MutableList<T>.replace(element: T) {
    val index = this.indexOf(element)

    if(index == -1) { return }

    this[index] = element
}