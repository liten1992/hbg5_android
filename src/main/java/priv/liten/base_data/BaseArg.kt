package priv.liten.base_data

data class Arg01<out A:Any>(val a: A)

data class Arg02<out A:Any?, out B:Any?>(val a: A, val b: B) {
    fun list(): List<Any?> = listOf(a, b)
}

data class Arg03<out A:Any?, out B:Any?, out C:Any?>(val a: A, val b: B, val c: C) {
    fun list(): List<Any?> = listOf(a, b, c)
}

data class Arg04<out A:Any?, out B:Any?, out C:Any?, out D:Any?>(val a: A, val b: B, val c: C, val d: D) {
    fun list(): List<Any?> = listOf(a, b, c, d)
}

data class Arg05<out A:Any?, out B:Any?, out C:Any?, out D:Any?, out E:Any?>(val a: A, val b: B, val c: C, val d: D, val e: E) {
    fun list(): List<Any?> = listOf(a, b, c, d, e)
}

data class Arg06<out A:Any?, out B:Any?, out C:Any?, out D:Any?, out E:Any?, out F:Any?>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F) {
    fun list(): List<Any?> = listOf(a, b, c, d, e, f)
}

data class Arg07<out A:Any?, out B:Any?, out C:Any?, out D:Any?, out E:Any?, out F:Any?, out G>(val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G) {
    fun list(): List<Any?> = listOf(a, b, c, d, e, f, g)
}