package priv.liten.hbg5_http

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import priv.liten.base_extension.readFileType
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_extension.encodeBase64
import priv.liten.hbg5_extension.md5
import priv.liten.hbg5_extension.toJson
import priv.liten.hbg5_extension.toUri
import priv.liten.hbg5_data.HBG5DownloadData
import priv.liten.hbg5_extension.getPrivatePath
import priv.liten.hbg5_extension.getPrivateUri
import priv.liten.hbg5_http.bin.HBG5ApiCall
import priv.liten.hbg5_widget.application.HBG5Application
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import retrofit2.Call
import java.io.File
import java.io.FileInputStream
import kotlin.Exception

class HBG5DownloadCall: HBG5ApiCall<HBG5DownloadData> {

    // MARK:- ====================== Define
    companion object {

        private val SERVICE_FILE = HBG5HttpService
            .Builder()
            .setBaseUrl("https://download")
            .setLogEnable(true)
            .setLogBodyEnable(true)
            .setLogDecodeBodyEnable(false)
            .build()
        // 有兩種格式 JFIF(ff d8 ff e0) EXIF(ff d8 ff e1)
        val FILE_TYPE_HEADER_JPG = Pair(".jpg", arrayOf(
            byteArrayOf(
                0.toByte(),
                0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xe0.toByte()
            ),
            byteArrayOf(
                0.toByte(),
                0xff.toByte(), 0xd8.toByte(), 0xff.toByte(), 0xe1.toByte()
            )
        ))
        val FILE_TYPE_HEADER_PNG = Pair(".png", arrayOf(
            byteArrayOf(
                0.toByte(),
                0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
                0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte()
            )
        ))
        val FILE_TYPE_HEADER_BMP = Pair(".bmp", arrayOf(
            byteArrayOf(
                0.toByte(),
                0x42.toByte(), 0x4d.toByte()
            )
        ))
        val FILE_TYPE_HEADER_MP4 = Pair(".mp4", arrayOf(
            byteArrayOf(
                4.toByte(),
                0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte(),
                0x6d.toByte(), 0x6d.toByte(), 0x70.toByte(), 0x34.toByte()
            ),
            byteArrayOf(
                4.toByte(),
                0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte(),
                0x6d.toByte(), 0x70.toByte(), 0x34.toByte()
            ),
            // todo hbg ftypisom
            byteArrayOf(
                4.toByte(),
                0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte(),
                0x69.toByte(), 0x73.toByte(), 0x6F.toByte(), 0x6D.toByte()
            )
        ))
        val FILE_TYPE_HEADER_MOV = Pair(".mov", arrayOf(
            byteArrayOf(
                4.toByte(),
                0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte(),
                0x71.toByte(), 0x74.toByte(), 0x20.toByte(), 0x20.toByte()
            )
        ))
        val FILE_TYPE_HEADER_MP3 = Pair(".mp3", arrayOf(
            byteArrayOf(
                0.toByte(),
                0x49.toByte(), 0x44.toByte(), 0x33.toByte()
            )
        ))
        val FILE_TYPE_HEADER_PDF = Pair(".pdf", arrayOf(
            byteArrayOf(
                0.toByte(),
                0x25.toByte(), 0x50.toByte(), 0x44.toByte(), 0x46.toByte()
            )
        ))
        val FILE_TYPE_HEADER_ZIP = Pair(".zip", arrayOf(
            byteArrayOf(
                0.toByte(),
                0x50.toByte(), 0x4B.toByte(), 0x03.toByte(), 0x04.toByte()
            )
        ))
        // 連拍型 jpg 特殊格式
        val FILE_TYPE_HEADER_HEIC = Pair(".heic", arrayOf(
            byteArrayOf(
                4.toByte(),
                0x66.toByte(), 0x74.toByte(), 0x79.toByte(), 0x70.toByte(),
                0x68.toByte(), 0x65.toByte(), 0x69.toByte(), 0x63.toByte(),
            )
        ))
        // todo hbg
        val FILE_TYPE_HEADER_SQLLITE = Pair(".sqlite3", arrayOf(
            byteArrayOf(
                0.toByte(),
                0x53.toByte(), 0x51.toByte(), 0x4C.toByte(), 0x69.toByte(),
                0x74.toByte(), 0x65.toByte(), 0x20.toByte(), 0x66.toByte(),
                0x6F.toByte(), 0x72.toByte(), 0x6D.toByte(), 0x61.toByte(),
                0x74.toByte(), 0x20.toByte(), 0x33.toByte(), 0x00.toByte()
            )
        ))

        // 必須調整方法 index: 0 代表start index
        val FILE_TYPE_HEADERS = mutableMapOf<String, Array<ByteArray>>(
            FILE_TYPE_HEADER_JPG,
            FILE_TYPE_HEADER_PNG,
            FILE_TYPE_HEADER_BMP,
            FILE_TYPE_HEADER_MP4,
            FILE_TYPE_HEADER_MOV,
            FILE_TYPE_HEADER_MP3,
            FILE_TYPE_HEADER_PDF,
            FILE_TYPE_HEADER_ZIP,
            FILE_TYPE_HEADER_HEIC,
            // todo hbg
            FILE_TYPE_HEADER_SQLLITE
        )
    }

    // MARK:- ====================== Constructor
    constructor(data: HBG5DownloadData): super(service = SERVICE_FILE, data = data) {
        this.log.callType = "DOWNLOAD"
    }

    // MARK:- ====================== Data
    /** API 執行實體  */
    private var action: Call<ResponseBody?>? = null

    // MARK:- ====================== Method
    /** todo hbg ex: /sdcard/{pkg}/... */
    fun buildFileDirPath() : String? = HBG5Application.instance?.getPrivatePath(dirName = HBG5WidgetConfig.PRIVATE_DIR_DOWNLOAD)

    fun buildFileName(url:String?) : String? {
        return url?.md5()
    }

    fun buildFileType(file: File?) : String {

        file?.let {
                if (!file.exists()) { return "" }
                return FileInputStream(file).use { stream -> stream.readFileType() }
            }
            ?:run {
                return ""
            }
    }


    // MARK:- ====================== Event
    private var onProgressListener: ((Long, Long) -> Unit)? = null
    /** 當下載進度異動 (當前進度, 總進度) */
    fun registerOnProgress(listener: ((Long, Long) -> Unit)?) {
        onProgressListener = listener
    }

    override fun onExecute() {
        val gson = GsonBuilder().disableHtmlEscaping().create()

        try {
            val app = HBG5Application.instance!!

            // 清空紀錄
            log.clear()
            log.startMillis = System.currentTimeMillis()

            val apiRequest = data.request ?: throw Exception("Download request is null")

            val method = data.method
            val headersMap = service.headers.let {
                val map = mutableMapOf<String, String>()
                it.forEach { (key, value) -> map[key] = value() }
                map
            } + data.headers
            val headers: String = gson.toJson(headersMap).encodeBase64()
            val url = apiRequest.url

            log.url = url ?: ""
            if(BuildConfig.DEBUG && service.logEnable) {
                headersMap.forEach{ (key, value) -> log.requestHeaders.add("$key $value") }
                if(service.logDecodeBodyEnable) {
                    log.requestDecodeBody = apiRequest.toJson(gson)
                }
            }

            // 中斷判斷
            if (status != Status.RUNNING) { throw Exception("Execute cancel") }

            val fileDirPath = buildFileDirPath() ?: throw NullPointerException("Not found save file dir")
            val fileDir = File(fileDirPath); File(fileDirPath).mkdirs()
            val fileName = buildFileName(url) ?: throw NullPointerException("Can not build file name")
            val filePath = "$fileDirPath${File.separator}$fileName"

            data.response = File(filePath).toUri(app).toString()

            if(BuildConfig.DEBUG && service.logDecodeBodyEnable) {
                log.responseDecodeBody = log.responseBody
            }
            // Check File Exist
            for (file in fileDir.listFiles() ?: arrayOf()) {
                // 檔案已存在
                if (file.name.startsWith(fileName)) {
                    // 允許使用快取
                    if (apiRequest.useCache) {
                        val filePath = file.absolutePath
                        data.response = File(filePath).toUri(app).toString()
                        return
                    }
                    // 移除檔案失敗
                    if (!file.delete()) { throw NullPointerException("Download file again failed, File exist and can't be delete") }
                    // 跳出檔案存在檢查
                    break
                }
            }

            // 下載檔案 todo hbg5
            action = service.value?.download(
                url = url,
                headers = headers,
                urlMethod = method,
                body = apiRequest.body?.let { body ->
                    val json = Gson().toJson(body)
                    RequestBody.create(
                        MediaType.parse("application/json"),
                        json
                    )
                }
            )


            val exeResponse = action?.execute() ?: throw NullPointerException("Not found execute response")
            data.statusCode = exeResponse.code().toString()

            if (!exeResponse.isSuccessful) {
                val errorMessage = exeResponse.errorBody()?.string() ?: ""
                throw Exception("Code ${data.statusCode} ${errorMessage.ifEmpty { "error body is null" }}")
            }

            val httpBody = exeResponse.body() ?: throw NullPointerException("Download body is null")

            // 串流
            run {
                val fileReader = ByteArray(4096)
                val fileSize: Long = httpBody.contentLength()
                var fileSizeDownloaded: Long = 0
                // Write File
                httpBody.byteStream().use { inputStream ->
                    app.contentResolver.openOutputStream(Uri.parse(data.response!!))?.use { outputStream ->
                        var read = inputStream.read(fileReader)
                        while (read != -1) {
                            outputStream.write(fileReader, 0, read)
                            fileSizeDownloaded += read
                            read = inputStream.read(fileReader)

                            val sendFileSizeDownloaded = fileSizeDownloaded

                            MainScope().launch {
                                onProgressListener?.let { it(sendFileSizeDownloaded, fileSize) }
                            }
                        }
                        outputStream.flush()
                    } ?: throw Exception("Can't save ${data.response!!}")
                }
                // Update File Type
                val fileWrite = File(filePath)
                val fileType = buildFileType(fileWrite)
                if (fileType.isNotEmpty()) {
                    val newFilePath = "$filePath$fileType"
                    if (fileWrite.renameTo(File(newFilePath))) { }
                    data.response = File(newFilePath).toUri(app).toString()
                }
            }
        }
        catch (error: Exception) {
            data.statusDescription = error.message ?: "Download file failed: unknown"
            if (BuildConfig.DEBUG && service.logEnable) {
                log.responseError = data.statusDescription
            }

            throw when(error) {
                is HBG5HttpException -> error
                else -> HBG5HttpException(statusCode = data.statusCode, message = error.message)
            }
        }
        finally {
            action = null
            log.endMillis = System.currentTimeMillis()
            log.httpStatusCode = data.statusCode
            log.responseBody = data.response ?: ""
            if (BuildConfig.DEBUG && service.logEnable && service.logDecodeBodyEnable) {
                log.responseDecodeBody = log.responseBody
            }
            logAll()
        }
    }

    override fun onCancel() {
        if (action == null || action!!.isCanceled) {
            return
        }
        if (action!!.isExecuted) {
            action!!.cancel()
        }
    }
}