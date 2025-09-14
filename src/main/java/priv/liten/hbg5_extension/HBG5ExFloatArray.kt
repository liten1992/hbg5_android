package priv.liten.hbg5_extension

fun FloatArray.equalsEach(equal: (Float) -> Boolean, emptyResult: Boolean = false): Boolean {
    if(this.isEmpty()) {
        return emptyResult
    }
    for(value in this) {
        if(!equal(value)) {
            return false
        }
    }
    return true
}

fun FloatArray.contains(equal: (Float) -> Boolean): Boolean {
    for(value in this) {
        if(equal(value)) {
            return true
        }
    }
    return false
}