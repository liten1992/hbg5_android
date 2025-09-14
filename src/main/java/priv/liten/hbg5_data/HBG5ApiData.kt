package priv.liten.hbg5_http.data

import priv.liten.hbg5_config.HBG5HttpConfig
import priv.liten.hbg5_data.HBG5Data

open class HBG5ApiData<TRequest, TResponse>: HBG5Data<TRequest, TResponse> {

    constructor(
        request: TRequest?,
        action: String,
        method: String,
        contentType: HBG5HttpConfig.ContentType = HBG5HttpConfig.ContentType.JSON,
        headers: Map<String, String>? = null): super() {

        this.request = request
        this.action = action
        this.method = method
        this.headers["User-Agent"] = "Mobile_Android"
        this.headers[HBG5HttpConfig.ContentType.key] = contentType.value
        headers?.let { this.headers.putAll(it) }
    }

    /** http headers */
    var headers = mutableMapOf<String, String>()
    /** https:xxx/{action} */
    var action = ""
    /** POST、GET ... */
    var method = ""
    /** 連結狀態代號 */
    var statusCode = ""
    /** 連接狀態說明 */
    var statusDescription = ""
}