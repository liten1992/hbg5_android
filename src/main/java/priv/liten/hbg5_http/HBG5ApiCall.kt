package priv.liten.hbg5_http.bin

import android.util.Log
import com.google.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType
import okhttp3.RequestBody
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_async.HBG5BaseCall
import priv.liten.hbg5_config.HBG5HttpConfig
import priv.liten.hbg5_extension.encodeBase64
import priv.liten.hbg5_extension.toJson
import priv.liten.hbg5_extension.toUrlMap
import priv.liten.hbg5_extension.toUrlParams
import priv.liten.hbg5_http.HBG5HttpException
import priv.liten.hbg5_http.HBG5HttpService
import priv.liten.hbg5_http.data.HBG5ApiData
import priv.liten.hbg5_manager.HBG5ApiManager
import retrofit2.Call
import java.lang.StringBuilder
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.Exception

//<TData extends HBG4ApiDataImpl<?, ?>> extends HBG4BaseCall
open class HBG5ApiCall<TData: HBG5ApiData<*, *>>: HBG5BaseCall {

    // MARK:- ====================== Define
    companion object {
        const val TAG = "////API"
    }

    // MARK:- ====================== Constructor
    constructor(service: HBG5HttpService = HBG5ApiManager.SERVICE_NONE, data: TData): super() {
        this.service = service
        this.data = data
        this.log.callType = "API"
    }

    // MARK:- ====================== Data
    /** API 基礎服務 */
    val service: HBG5HttpService

    /** API 請求回應資料 */
    val data: TData

    /** API 打印訊息 */
    val log = LogDetail()

    /** API 執行實體  */
    private var action: Call<String?>? = null

    // MARK:- ====================== Event

    override fun onExecute() {
        val gson = GsonBuilder().disableHtmlEscaping().create()

        var baseUrl: String? = null

        try {
            // 清空紀錄
            log.clear()
            log.startMillis = System.currentTimeMillis()

            if(data.response != null) {
                log.callType = "DEFINE"
                log.url = data.action
                log.requestBody = data.baseRequest?.toJson() ?: ""
                log.responseBody = data.baseResponse?.toJson() ?: ""
                throwFailExceptionIfNeed()
                return
            }

            val apiRequest: Any = data.baseRequest ?: throw Exception("Api ${data.action} request is null")

            val method = data.method
            val headersMap = service.headers.let {
                val map = mutableMapOf<String, String>()
                it.forEach { (key, value) -> map[key] = value() }
                map
            } + data.headers
            val headers: String = gson.toJson(headersMap).encodeBase64()
            val contentType: String = headersMap[HBG5HttpConfig.ContentType.key] ?: ""
            val url: String = run url@{

                val urlBuff = StringBuilder()
                // 是否強制根據action鎖定網址
                if (!data.action.startsWith(prefix = "http", ignoreCase = true)) {
                    urlBuff.append(service.baseUrl.trim())
                }
                urlBuff.append(data.action)
                baseUrl = urlBuff.toString()

                // 建立呼叫 RawBody 如果是 "GET" 取基礎型別接入 URL 當中
                if (HBG5HttpConfig.Method.GET.value == method) {

                    val urlParams = apiRequest.toUrlParams()

                    if (urlParams.isNotEmpty()) {
                        // 刪除結尾符號
                        if (urlBuff.endsWith("/")) {
                            urlBuff.setCharAt(urlBuff.length - 1, '?')
                        } else if (!urlBuff.endsWith("?")) {
                            urlBuff.append('?')
                        }
                        urlBuff.append(urlParams)
                    }
                }

                return@url urlBuff.toString()
            }
            if(BuildConfig.DEBUG && service.logEnable) {
                log.url = url
                log.requestHeaders.addAll(headersMap.map { (key, value) -> "$key $value" })
            }
            val requestBody: RequestBody?
            // 表單式資料
            if(HBG5HttpConfig.ContentType.WWW_URL_ENCODED.value == contentType) {
                val urlMap = data.request?.toUrlMap()
                if (BuildConfig.DEBUG && service.logEnable && service.logBodyEnable) {
                    log.requestBody = urlMap?.toJson() ?: "null"
                }
                requestBody = FormBody
                    .Builder().also { builder ->
                        urlMap?.onEach { (key, value) -> builder.addEncoded(key, value) }
                    }
                    .build()
            }
            // 連結方式
            else when(method) {
                // GET格式呼叫
                HBG5HttpConfig.Method.GET.value -> {
                    if (BuildConfig.DEBUG && service.logEnable && service.logBodyEnable) {
                        log.requestBody = data.request?.toUrlMap()?.toJson() ?: "null"
                    }
                    requestBody = RequestBody.create(
                        MediaType.parse(contentType),
                        ""
                    )
                }
                // 一般JSON格式呼叫
                else -> {
                    val requestString = buildRequest(data)
                    if (BuildConfig.DEBUG && service.logEnable && service.logBodyEnable) {
                        log.requestBody = requestString ?: "null"
                    }
                    requestBody = RequestBody.create(
                        MediaType.parse(contentType),
                        requestString ?: ""
                    )
                }
            }

            // 呼叫實體邏輯建立
            action = service.value?.call(
                url,
                headers,
                method,
                requestBody
            )

            val exeResponse = action?.execute() ?: throw NullPointerException("Execute response is null")
            data.statusCode = exeResponse.code().toString()

            if (BuildConfig.DEBUG && service.logEnable) {
                log.httpStatusCode = data.statusCode
            }

            if (!exeResponse.isSuccessful) {
                val errorMessage = exeResponse.errorBody()?.string() ?: ""
                throw Exception("Code ${data.statusCode} ${errorMessage.ifEmpty { "error body is empty" }}")
            }
            val apiResponseString = exeResponse.body()
            if(BuildConfig.DEBUG && service.logEnable && service.logBodyEnable) {
                log.responseBody = apiResponseString ?: "null"
            }

            // 中斷判斷
            if (status != Status.RUNNING) { throw Exception("Execute cancel") }

            data.baseResponse = buildResponse(apiResponseString)
        }
        catch (error: Exception) {
            val message = error.message ?: "Could not connect to the server."
            if (BuildConfig.DEBUG && service.logEnable) {
                log.responseError = message
            }

            throw when(error) {
                is HBG5HttpException -> HBG5HttpException(
                    statusCode = error.statusCode,
                    message = message,
                    cause = error.cause,
                    url = baseUrl,
                    request = data.request,
                    response = data.response
                )
                else -> HBG5HttpException(
                    statusCode = data.statusCode,
                    message = message,
                    cause = error,
                    url = baseUrl,
                    request = data.request,
                    response = data.response
                )
            }
        }
        finally {
            action = null
            log.endMillis = System.currentTimeMillis()
            if (BuildConfig.DEBUG && service.logEnable && service.logDecodeBodyEnable) {
                log.requestDecodeBody = data.baseRequest?.toJson(gson) ?: "null"
                log.responseDecodeBody = data.baseResponse?.toJson(gson) ?: "null"
            }
            logAll()
        }
    }

    @Throws(Exception::class)
    override fun onCancel() {
        action?.let {
            if (it.isCanceled) {
                return
            }
            if (it.isExecuted) {
                it.cancel()
            }
        }
    }

    override fun onException(exception: Exception?) {
        data.statusDescription = exception?.message ?: "Exception null information"
    }

    // MARK:- ====================== Method
    open fun buildRequest(apiData: TData): String? {
        val apiRequest: Any? = apiData.baseRequest
        return if (apiRequest is String) {
            apiRequest
        } else {
            GsonBuilder().disableHtmlEscaping().create().toJson(apiRequest)
        }
    }

    open fun buildResponse(text: String?): Any? {
        val apiSuperType = data.javaClass.genericSuperclass as ParameterizedType
        val apiResType = apiSuperType.actualTypeArguments.getOrNull(1)

        if (apiResType == String::class.java) {
            return text
        }

        if(text.isNullOrEmpty()) {
            return null
        }

        try {
            return Gson().fromJson(text, apiResType)
        }
        catch (error: Exception) {
            val message = "Json物件解析失敗\n"
            if(text.length > 256) {
                throw Exception(message + text.substring(0, 256))
            }
            else {
                throw Exception(message + text)
            }
        }
    }

    override fun throwFailExceptionIfNeed() {
        if(data.response == null) {
            val statusCode = data.statusCode
            if(statusCode != HBG5HttpConfig.StatusCode.OK.value.toString()) {
                throw HBG5HttpException(statusCode = statusCode, message = "查無回應資料 ($statusCode)")
            }
        }
    }

    /** 打印 */
    fun logAll() {
        if(BuildConfig.DEBUG && service.logEnable) {
            // 將打印資訊整合至統一執行緒中避免訊息交互穿插
            MainScope().launch {
                logDivider(log, "START ${log.callType}")
                logRequest(log)
                logResponse(log)
                logError(log)
                logHttpStatusCode(log)
                logDivider(log, "END ${log.callType}")
                log.clear()
            }
        }
        else {
            log.clear()
        }
    }

    /** 打印分隔線 */
    fun logDivider(log: LogDetail, text: String) {
        if (service.logEnable) {
            if(!text.contains("END")) {
                Log.d(TAG, "　")
                Log.d(TAG, "　")
            }
            Log.d(TAG, "========================================")
            Log.d(TAG, "$text: ${log.url}")
            Log.d(TAG, "========================================")
        }
    }

    /** 打印請求 */
    fun logRequest(log: LogDetail) {
        if (service.logEnable) {

            // Headers
            for (header in log.requestHeaders) {
                if (header.isEmpty()) {
                    continue
                }
                Log.d(TAG, "Request Headers: %s".format(header))
            }

            // BODY
            if (service.logBodyEnable) {
                val maxLength = 1024
                var printCount = 0

                if(log.requestBody.length > maxLength) {
                    for(i in 0 until 5) {
                        if(printCount + maxLength < log.requestBody.length) {
                            Log.d(TAG, String.format("Request Body: %s", log.requestBody.substring(printCount, printCount + maxLength)))
                        }
                        else {
                            Log.d(TAG, String.format("Request Body: %s", log.requestBody.substring(printCount)))
                            break
                        }
                        printCount += maxLength
                    }
                }
                else {
                    Log.d(TAG, String.format("Request Body: %s", log.requestBody))
                }
            }
            if (service.logDecodeBodyEnable) {
                Log.d(
                    TAG,
                    String.format("Request Decode Body: %s", log.requestDecodeBody)
                )
            }
        }
    }

    /** 打印回應 */
    fun logResponse(log: LogDetail) {
        if (service.logEnable) {
            val split = 1024
            val max = 5
            if (service.logBodyEnable) {
                val text: String = log.responseBody
                if (text.isEmpty()) {
                    Log.d(TAG, "Response Body: null")
                    return
                }

                var showCount = 0
                var logSize = 0
                val textSize = text.length
                var count = 0
                while (logSize < textSize && count < max) {
                    count += 1
                    if (logSize + split >= text.length) {
                        Log.d(
                            TAG,
                            String.format("Response Body: %s", text.substring(logSize, textSize))
                        )
                    }
                    else {
                        Log.d(
                            TAG,
                            String.format(
                                "Response Body: %s",
                                text.substring(logSize, logSize + split)
                            )
                        )
                    }
                    logSize += split

                    showCount += 1
                    if(showCount >= log.showBodyMaxCount) {
                        break
                    }
                }
            }
            if (service.logDecodeBodyEnable) {
                val text: String = log.responseDecodeBody
                if (text.isEmpty()) {
                    Log.d(TAG, "Response Decode Body: null")
                    return
                }

                var showCount = 0
                var logSize = 0
                val textSize = text.length
                while (logSize < textSize) {
                    if (logSize + split >= text.length) {
                        Log.d(TAG, "Response Decode Body: ${text.substring(logSize, textSize)}")
                    } else {
                        Log.d(TAG, "Response Decode Body: ${text.substring(logSize, logSize + split)}")
                    }
                    logSize += split

                    showCount += 1
                    if(showCount >= log.showBodyMaxCount) {
                        break
                    }
                }
            }
        }
    }

    /** 打印錯誤 */
    fun logError(log: LogDetail) {
        if (service.logEnable) {
            if (log.responseError.isNotEmpty()) {
                Log.d(TAG, String.format("Response Error: %s", log.responseError))
            }
        }
    }

    /** 打印連線狀態 */
    fun logHttpStatusCode(log: LogDetail) {
        if (service.logEnable) {
            Log.d(TAG, "Http Status: ${log.httpStatusCode} (${log.endMillis - log.startMillis} ms)")
        }
    }

    /** Execute -> ApiData */ @Throws
    suspend fun data(): TData {
        try {
            withContext(Dispatchers.IO) { execute() }
        }
        catch (e: Exception) {
            throw e
        }
        return data
    }
    // todo hbg
    @Throws
    fun executeData(): TData {
        execute()
        return data
    }

    // MARK:- ====================== Class
    class LogDetail {
        var callType = "Unknown"
        var startMillis = 0L
        var endMillis = 0L
        var url = ""
        val requestHeaders: MutableList<String> = mutableListOf()
        var requestBody = ""
        var requestDecodeBody = ""
        var responseBody = ""
        var responseDecodeBody = ""
        var responseError = ""
        var httpStatusCode = ""
        var showBodyMaxCount = 1

        fun clear() {
            startMillis = 0L
            endMillis = 0L
            url = ""
            requestHeaders.clear()
            requestBody = ""
            requestDecodeBody = ""
            responseBody = ""
            responseDecodeBody = ""
            responseError = ""
            httpStatusCode = ""
        }
    }
}
