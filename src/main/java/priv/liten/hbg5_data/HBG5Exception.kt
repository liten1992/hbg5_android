package priv.liten.hbg5_data

/**避免使用聲明 Message 標籤 例外錯誤*/ // todo hbg5 change ios exception to hbg5 exception
open class HBG5Exception : Exception {
    constructor(
        message: String? = null,
        cause: Throwable? = null
    ): super(message, cause)
}