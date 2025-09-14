package priv.liten.hbg5_extension

fun CharSequence.isInt(): Boolean {
    return toString().toIntOrNull() != null
}
fun CharSequence.isNotInt(): Boolean {
    return !isInt()
}

fun CharSequence.isFloat(): Boolean {
    return toString().toFloatOrNull() != null
}
fun CharSequence.isNotFloat(): Boolean {
    return !isFloat()
}