package priv.liten.hbg5_data

import com.google.gson.annotations.SerializedName
import priv.liten.hbg5_http.data.HBG5ApiData

/**下載檔案請求回應*/
open class HBG5DownloadData: HBG5ApiData<HBG5DownloadData.Request, String> {
    constructor(
        request: Request,
        headers: Map<String, String>? = null
    ): super(
        request = request,
        action = "",
        method = "",
        headers = headers)

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
    }
}