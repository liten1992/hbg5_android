package priv.liten.hbg5_data

import com.google.gson.annotations.SerializedName
import priv.liten.hbg5_config.HBG5HttpConfig
import priv.liten.hbg5_http.data.HBG5ApiData

/**下載檔案請求回應*/
open class HBG5DownloadData: HBG5ApiData<HBG5DownloadData.Request, String> {
    constructor(
        request: Request,
        method: String = HBG5HttpConfig.Method.GET.value,
        headers: Map<String, String>? = null
    ): super(
        request = request,
        action = "",
        method = method,
        headers = headers)
    /**簡易 GET 下載方式*/
    constructor(
        url: String,
        headers: Map<String, String>? = null) : this(
        request = Request().also { apiRequest -> apiRequest.url = url },
        headers = headers)

    open class Request {
        /**下載位址*/
        @SerializedName("Url")
        var url: String? = null
        /**允許快取*/
        @SerializedName("UseCache")
        var useCache: Boolean = true
        /**當 POST 模式 會注入 BODY 的資料*/
        @SerializedName("Body")
        var body: Any? = null
    }
}