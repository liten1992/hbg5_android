package priv.liten.hbg5_manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_extension.logD
import priv.liten.hbg5_extension.toJson
import priv.liten.hbg5_http.HBG5HttpService
import priv.liten.hbg5_http.bin.HBG5ApiCall
import priv.liten.hbg5_http.HBG5DownloadCall
import priv.liten.hbg5_http.data.HBG5ApiData
import priv.liten.hbg5_data.HBG5DownloadData


@Suppress("UNUSED_ANONYMOUS_PARAMETER")
open class HBG5ApiManager {
    // MARK:- ====================== Define
    companion object {

        val SERVICE_NONE = HBG5HttpService
            .Builder()
            .setBaseUrl("https://nothing")
            .setTimeoutInterval(30)
            .setLogEnable(BuildConfig.DEBUG)
            .setLogBodyEnable(BuildConfig.DEBUG)
            .setLogDecodeBodyEnable(false)
            .build()
    }

    /**
     * @param success ex: content://storage/emulated/0/Android/data/your_app/files/119bd327483e7ea6b1241f6f4bdcf0ec.pdf
     * */
    fun downloadFile(
        apiData: HBG5DownloadData,
        success:    ((String) -> Unit)?,
        failed:     ((String) -> Unit)?,
        completed:  (() -> Unit)?,
        progress: ((Long, Long) -> Unit)?) {
        //apiData.request?.useCache = true
        MainScope().launch {
            progress?.let { it(0L, 100L) }
            callFlow(apiCall = HBG5DownloadCall(apiData))
                .map { apiData ->
                    val path = apiData.response?.trim() ?: ""
                    if(path.isEmpty()) { throw NullPointerException("Not found download local path") }
                    return@map path
                }
                .catch { error -> failed?.let { it(error.message ?: "Unknown Exception") } }
                .onCompletion {
                    progress?.let { it(100L, 100L) }
                    completed?.let { it() }
                }
                .collect { path -> success?.let { it(path) } }
        }
    }

    /**呼叫單一的API的Flow 如果想要讀取測試本機資料 只要在apiData埋入response就會在0.05秒後返回*/
    fun <DATA:HBG5ApiData<*,*>> callFlow(
        service: HBG5HttpService = SERVICE_NONE,
        apiCall: HBG5ApiCall<DATA>) = callFlow(
            service = service,
            apiData = apiCall.data,
            apiCallBuilder = { apiCall }
        )

    /**呼叫單一的API的Flow 如果想要讀取測試本機資料 只要在apiData埋入response就會在0.05秒後返回*/
    fun <DATA:HBG5ApiData<*,*>> callFlow(
        service: HBG5HttpService = SERVICE_NONE,
        apiData: DATA,
        apiCallBuilder: (() -> HBG5ApiCall<DATA>) = { HBG5ApiCall(service, apiData) }): Flow<DATA> = callFlow(
            service = service,
            apiDataBuilder = { apiData },
            apiCallBuilder = apiCallBuilder
        )

    /**呼叫單一的API的Flow 如果想要讀取測試本機資料 只要在apiData埋入response就會在0.05秒後返回*/
    fun <DATA:HBG5ApiData<*,*>> callFlow(
        service: HBG5HttpService = SERVICE_NONE,
        apiDataBuilder: ()-> DATA,
        apiCallBuilder: (() -> HBG5ApiCall<DATA>) = { HBG5ApiCall(service, apiDataBuilder()) }): Flow<DATA> =
        flow {
            val apiCall = apiCallBuilder()
            val apiData = apiCall.data
            apiCall.execute()
            emit(apiData)
        }.flowOn(Dispatchers.IO)

    /**
     * 呼叫單一的API的Flow
     * @param map: 資料轉換 REQUEST與RESPONSE階段分別執行
     * @param retry: 發生錯誤重試次數
     * */
    fun <RES, DATA:HBG5ApiData<*, RES>> call(
        scope: CoroutineScope = MainScope(),
        service: HBG5HttpService = SERVICE_NONE,
        apiData: DATA,
        success: ((RES) -> Unit)?,
        failed: ((Throwable) -> Unit)?,
        failedThrow: ((RES) -> Throwable?)? = null,
        completed: (() -> Unit)?,
        map: ((DATA) -> Unit)? = null,
        retry: Long = 0) {

        scope.launch {
            map?.let { it(apiData) }
            val call = callFlow(service, apiData)
            if(retry > 0) { call.retry(retry) }

            call.map { apiData ->
                    map?.let { it(apiData) }
                    apiData.response!!
                }
                .catch { error -> failed?.let { it(error) } }
                .onCompletion { completed?.let { it() } }
                .collect { apiResponse ->
                    val error = failedThrow?.let { it(apiResponse) }
                    if(error == null) {
                        success?.let { it(apiResponse) }
                    }
                    else {
                        failed?.let { it(error) }
                    }
                }
        }
    }
}