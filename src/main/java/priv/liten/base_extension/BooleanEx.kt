package priv.liten.base_extension

/**true:1 false:0*/
fun Boolean.toInt(): Int { return if(this) 1 else 0 }