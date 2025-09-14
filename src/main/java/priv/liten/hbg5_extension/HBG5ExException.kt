package priv.liten.hbg5_extension

import priv.liten.hbg5_http.HBG5HttpException

/**詳細錯誤訊息(請勿直接暴露避免資安問題)
 * @param builder: 如果訊息產生為空 則會啟用預設訊息方案
 */
fun Throwable.log(builder: (() -> String?)? = null): String {
    var message: String = builder?.let { it() }?.trim() ?: ""
    if(message.isNotEmpty()) { return message }

    val maxLength = 2000
    // Custom Information
    when(val exception = this) {
        is HBG5HttpException -> {
            val textBuilder: ((Any?) -> String) = { data ->
                val text = when(data) {
                    is String -> data
                    is Int, is Long -> "$data"
                    is Float, is Double -> "%.4f".format(data )
                    is Boolean -> if(data) "TRUE" else "FALSE"
                    null -> "NULL"
                    else -> data.toJson() ?: "Convert Json Failed"
                }

                text.ifEmpty { "NULL" }.maxLength(len = maxLength)
            }

            message += "Url: ${exception.url?.ifEmpty { null } ?: "NULL"}\n"
            message += "Status: ${exception.statusCode?.ifEmpty { null } ?: "NULL"}\n"
            message += "Request: ${textBuilder(exception.request)}\n"
            message += "Response: ${textBuilder(exception.response)}\n"
        }
        else -> { }
    }
    message += "Message: ${(this.message ?: "NULL").maxLength(len = maxLength)}"
    // System Information
    when(val cause = this.cause) {
        is Throwable -> {
            message += "\n\n"
            message += "Error Description: ${(cause.message ?: "NULL").maxLength(len = maxLength)}"
        }
        else -> { }
    }
    return message
}