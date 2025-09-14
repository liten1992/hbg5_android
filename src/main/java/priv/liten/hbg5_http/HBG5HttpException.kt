package priv.liten.hbg5_http

import kotlin.Exception

/**發生的網路錯誤*/
class HBG5HttpException: Exception {

    constructor(
        statusCode: String?,
        message: String?,
        cause: Throwable? = null,
        url: String? = null,
        request: Any? = null,
        response: Any? = null
    ): super(message, cause)
    {
        this.statusCode = statusCode
        this.url = url
        this.request = request
        this.response = response
    }

    /**網路連接代號*/
    var statusCode: String? = null; private set

    var url: String? = null; private set

    var request: Any? = null; private set

    var response: Any? = null; private set
}