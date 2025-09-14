package priv.liten.hbg5_extension


fun IntArray.toYMDInt(): Int {
    return this[0] * 10000 + this[1] * 100 + this[2]
}

fun IntArray.toLinkString(linkText: String): String {

    val result = StringBuilder()

    for((i, number) in this.withIndex()) {
        if(i > 0) {
            result.append(linkText)
        }
        result.append(number)
    }

    return result.toString()
}