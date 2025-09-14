package priv.liten.hbg5_extension

@Throws // todo hbg
fun Boolean.throwIfFalse(error: (() -> Exception)): Boolean {
    if(!this) {
        throw error()
    }
    return true
}