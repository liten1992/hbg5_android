package priv.liten.hbg5_extension

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toFile
import priv.liten.base_extension.readFileType
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_http.HBG5DownloadCall
import priv.liten.hbg5_widget.application.HBG5Application
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.Exception

/**讀取串流*/
fun Uri.loadBytes(context: Context? = HBG5Application.instance): ByteArray? {
    if(context == null) { return null }

    context.contentResolver?.openInputStream(this)?.use { stream ->
        ByteArrayOutputStream().use { buffer ->
            val bufferSize = 1024
            val bufferBytes = ByteArray(bufferSize)
            var len = stream.read(bufferBytes)
            if(len == -1) { return null }
            do {
                buffer.write(bufferBytes, 0, len)
                len = stream.read(bufferBytes)
            } while(len != -1)
            return buffer.toByteArray()
        }
    }

    return null
}
/**刪除*/
fun Uri.delete(context: Context? = HBG5Application.instance): Boolean {
    if(context == null) { return false }
    return try {
        // todo hbg
        if(this.exists(context = context)) {
            context.contentResolver.delete(this, null, null)
        }
        true
    }
    catch (error: Exception) {
        if(BuildConfig.DEBUG) { Log.d("//Uri", "Delete fail ${error.message}") }
        false
    }
}
/**判斷檔案是否存在*/
fun Uri.exists(context: Context? = HBG5Application.instance): Boolean {
    if(context == null) { return false }
    return try {
        val scheme = (this.scheme ?: "").lowercase()
        // 資源串流判斷
        if(scheme.contains("content")) {
            context.contentResolver.openInputStream(this).use { true }
        }
        // 檔案判斷
        else {
            this.toFile().exists()
        }
    }
    catch (error: Exception) { false }
}
/**複製URI檔案至指定位置 耗時操作*/ // todo hbg deprecated
fun Uri.copyFile(
    context: Context? = HBG5Application.instance,
    dir: String,
    rename: String? = null
): Uri? {
    context ?: return null

    val oldPath = this.toString()
    if(oldPath.isEmpty()) { return null }

    var newFileRename = rename?.trim() ?: ""
    if(newFileRename.isEmpty()) {
        val counter = "%04d".format(HBG5Application.FILE_NAME_COUNTER_ABS)
        val time = Calendar.getInstance().toString(format = "yyyyMMddHHmmssSSS")
        val newFileName = "Default_${time}_$counter"
        // todo hbg debug
        val newFileType = ".${this.pathExtension.ifEmpty { "ept" }}"
//            run {
//            val fileIndex = oldPath.lastIndexOf('.')
//            val dirIndex = oldPath.lastIndexOf(File.separator)
//            return@run if(dirIndex > fileIndex) ""
//            else oldPath.substring(fileIndex)
//        }
        newFileRename = "${newFileName}${newFileType}"
    }

    val newFileDir = File(dir)
    newFileDir.mkdirs()
    val newFilePath = "${dir}${File.separator}${newFileRename}"

    return try {
        context.contentResolver.openInputStream(this)?.use { bis ->
            BufferedOutputStream(FileOutputStream(newFilePath)).use { bos ->
                val buf = ByteArray(1024)
                bis.read(buf)
                do { bos.write(buf) } while(bis.read(buf) != -1)
            }
        }
        File(newFilePath).toUri(context)
    }
    catch (error: Exception) {
        null
    }
}

/**todo hbg 複製指定檔案至應用公開下載資料夾底下*/
fun Uri.copyFileToDownload(
    context: Context? = HBG5Application.instance,
    dirName: String = "",
    fileName: String
): Uri? {
    context ?: return null
    if((this.path ?: "").isEmpty()) { return null }

    val toUri: Uri = context.getDownloadUri(dirName = dirName, fileName = fileName) ?: return null

    return this.copyFile(
        context = context,
        toUri = toUri
    )
}
/**todo hbg 複製指定檔案至應用私有資料夾底下*/
fun Uri.copyFileToPrivate(
    context: Context? = HBG5Application.instance,
    dirName: String = "",
    fileName: String
): Uri? {
    context ?: return null
    if((this.path ?: "").isEmpty()) { return null }

    val toUri = context.getPrivateUri(dirName = dirName, fileName = fileName) ?: return null

    return this.copyFile(
        context = context,
        toUri = toUri
    )
}
/**todo hbg*/
fun Uri.copyFile(
    context: Context? = HBG5Application.instance,
    toUri: Uri
): Uri? {
    context ?: return null

    val resolver = context.contentResolver

    return try {
        // 無法完成檔案清除
        if (!toUri.delete(context = context)) {
            throw Exception("覆蓋檔案失敗")
        }
        resolver.openInputStream(this)?.use { input ->
            BufferedOutputStream(resolver.openOutputStream(toUri)).use { output ->
                val buf = ByteArray(1024)
                input.read(buf)
                do {
                    output.write(buf)
                } while (input.read(buf) != -1)
            }
        }

        toUri
    } catch (error: Exception) {
        null
    }
}

/**todo hbg*/
fun Context.getPrivateUri(dirName: String = "", fileName: String): Uri? {
    if(fileName.isEmpty()) { return null }
    // "Cache"為根目錄
    //context.externalCacheDir?.toString()

    val outputRootUriString = getExternalFilesDir(null)?.toString() ?: ""
    if(outputRootUriString.isEmpty()) {
        return null
    }
    val saveFolderFile = File(
        if(dirName.isNotEmpty()) "${outputRootUriString}${File.separator}${dirName}"
        else outputRootUriString
    )
    if(!saveFolderFile.exists()) {
        saveFolderFile.mkdirs()
    }

    return File(saveFolderFile, fileName).toUri(this)
}
/**todo hbg*/
fun Context.getDownloadUri(dirName: String = "", fileName: String): Uri? {
    if(fileName.isEmpty()) { return null }

    val saveFolder =
        if(dirName.isNotEmpty()) "${Environment.DIRECTORY_DOWNLOADS}${File.separator}${dirName}"
        else Environment.DIRECTORY_DOWNLOADS

    // 高版本拷貝
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, saveFolder)
        }
        return contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
    }
    // 低版本拷貝
    else {
        val saveFolderFile = Environment.getExternalStoragePublicDirectory(saveFolder)
        if(!saveFolderFile.exists()) {
            saveFolderFile.mkdirs()
        }
        return File(saveFolderFile, fileName).toUri(this)
    }
}

/**取得本機檔案大小*/
fun Uri.getFileSizeMb(context: Context? = HBG5Application.instance): Float? {
    try {
        if(context == null) { throw NullPointerException("Not found context") }
        val bytes = context.contentResolver.openAssetFileDescriptor(this, "r")?.use { it.length } ?: return null
        return (bytes * 0.0009765625 * 0.0009765625).toFloat()
    }
    catch (error: Exception) {
        return null
    }
}
/**取得路徑最後一次.後段文字 /data/0/example.mp4 -> mp4*/
val Uri.pathExtension: String
    get() {
        val lastPath = this.lastPathSegment ?: return ""
        val splitIndex = lastPath.lastIndexOf('.')
        if(splitIndex == -1) { return "" }
        return lastPath.substring(splitIndex + 1)
    }

/**讀取檔案屬性 如果是資料夾或者讀取失敗為空 ex jpg png pdf*/
val Uri.fileType: String
    get() {
        return this.fileType(matchList = HBG5DownloadCall.FILE_TYPE_HEADERS)
    }

/**含有過濾功能的副檔名取得 ex jpg png pdf*/ // todo hbg
fun Uri.fileType(matchList: Map<String, Array<ByteArray>>): String {
    val scheme = this.scheme?.lowercase() ?: ""
    // 讀取資源型檔案
    val result = if (scheme == "content") {
        try {
            HBG5Application.instance?.contentResolver?.openInputStream(this)?.use { stream ->
                return@use stream.readFileType(matchList)
            } ?: ""
        }
        catch (error: Exception) {
            ""
        }
    }
    // 其他路徑種類
    else {
        this.pathExtension
    }

    return result
        .replace(".", "")
        .lowercase()
}

/**讀取指定路徑檔案內容*/ // todo hbg
fun Uri.readStringLines(): List<String> {
    return HBG5Application.instance?.contentResolver?.openInputStream(this)?.use { stream ->

        val result = mutableListOf<String>()

        stream.bufferedReader().use { reader ->
            reader.forEachLine { line ->
                result.add(line)
            }
        }

        return@use result
    } ?: emptyList()
}

/**讀取指定路徑檔案內容*/ // todo hbg
fun Uri.readCsv(): List<List<String>> {
    val lines = this.readStringLines()
    val result = mutableListOf<List<String>>()

    for(content in lines) {
        val lineFields = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        val currentField = StringBuilder()

        var inQuotes = false
        var i = 0

        while (i < content.length) {
            when (val c = content[i]) {
                '"' -> {
                    if (inQuotes && i + 1 < content.length && content[i + 1] == '"') {
                        // 處理轉義雙引號 ""
                        currentField.append('"')
                        i++ // 跳過下一個引號
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                ',' -> {
                    if (inQuotes) {
                        currentField.append(c)
                    } else {
                        currentRow.add(currentField.toString())
                        currentField.clear()
                    }
                }

                '\r' -> {
                    // 忽略 Windows 換行符的一部分
                }

                '\n' -> {
                    if (inQuotes) {
                        currentField.append('\n')
                    } else {
                        currentRow.add(currentField.toString())
                        lineFields.add(currentRow)
                        currentRow = mutableListOf()
                        currentField.clear()
                    }
                }

                else -> {
                    currentField.append(c)
                }
            }
            i++
        }

        // 處理最後一行（如果不是以換行結尾）
        if (currentField.isNotEmpty() || currentRow.isNotEmpty()) {
            currentRow.add(currentField.toString())
            lineFields.add(currentRow)
        }

        result.addAll(lineFields)
    }

    return result
}


/**判定字串是檔案名稱*/
val Uri.isName: Boolean
    get() {
        return !isLink
    }

/**判定字串是檔案連結*/
val Uri.isLink: Boolean
    get() {
        val scheme = this.scheme?.lowercase() ?: ""
        if(scheme.isEmpty()) {
            val path = (this.path ?: "").trim()
            return path.startsWith("/")
        }
        return true
    }

/**判定是否為本機路徑*/
val Uri.isLocal: Boolean
    get() {
        val scheme = this.scheme?.lowercase() ?: ""
        return scheme == "" || scheme == "file" || scheme == "content"
    }
/**判定是否為網路路徑*/
val Uri.isHttp: Boolean
    get() {
        val scheme = this.scheme?.lowercase() ?: ""
        return scheme == "http" || scheme == "https"
    }


object URL {
    fun build(path: String?): Uri? {
        if((path ?: "").isEmpty()) { return null }
        return Uri.parse(path!!)
    }
}