package priv.liten.hbg5_config

class HBG5HttpConfig {

    enum class Method {
        /** 方法請求展示指定資源。使用 GET 的請求只應用於取得資料  */
        GET,

        /** 方法請求與 GET 方法相同的回應，但它沒有回應主體（response body）  */
        HEAD,

        /** 方法用於提交指定資源的實體，通常會改變伺服器的狀態或副作用（side effect）  */
        POST,

        /** 方法會取代指定資源所酬載請求（request payload）的所有表現  */
        PUT,

        /** 方法會刪除指定資源  */
        DELETE,

        /** 方法會和指定資源標明的伺服器之間，建立隧道（tunnel）  */
        CONNECT,

        /** 方法描述指定資源的溝通方法（communication option）  */
        OPTIONS,

        /** 方法會與指定資源標明的伺服器之間，執行迴路返回測試（loop-back test）  */
        TRACE,

        /** 方法套用指定資源的部份修改  */
        PATCH;

        companion object {
            const val key = "URL-METHOD"
        }

        val value: String
            get() {
                return when (this) {
                    GET -> "GET"
                    HEAD -> "HEAD"
                    POST -> "POST"
                    PUT -> "PUT"
                    DELETE -> "DELETE"
                    CONNECT -> "CONNECT"
                    OPTIONS -> "OPTIONS"
                    TRACE -> "TRACE"
                    PATCH -> "PATCH"
                    else -> ""
                }
            }

        fun equals(value: String): Boolean {
            return this.value == value
        }
    }

    enum class ContentType {
        JSON, TEXT, WWW_URL_ENCODED;

        companion object {
            const val key = "Content-Type"
        }

        val value: String
            get() {
                return when (this) {
                    JSON -> "application/json; charset=UTF-8"
                    TEXT -> "text/html; charset=UTF-8"
                    WWW_URL_ENCODED -> "application/x-www-form-urlencoded; charset=UTF-8"
                }
            }
    }

    enum class StatusCode(val value: Int) {
        // - 成功回應 -
        /**
         * 請求成功。成功的意義依照 HTTP 方法而定：
         * GET：資源成功獲取並於訊息主體中發送。
         * HEAD：entity 標頭已於訊息主體中。
         * POST：已傳送訊息主體中的 resource describing the result of the action。
         * TRACE：伺服器已接收到訊息主體內含的請求訊息。 */
        OK(200),  // - 重定向訊息 -

        // - 用戶端錯誤回應 -
        Forbidden(403),
        NotFound(404);
    }

    enum class NetConnectedType(val text: String) {
        NONE("NONE"),
        MOBILE("MOBILE"),
        WIFI("WIFI");
    }
}