package priv.liten.hbg5_extension

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import priv.liten.base_extension.readFileType
import priv.liten.hbg.BuildConfig
import priv.liten.hbg5_http.HBG5DownloadCall
import priv.liten.hbg5_widget.application.HBG5Application
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
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
/**移動 todo hbg*/
fun Uri.move(context: Context? = HBG5Application.instance, to: Uri): Boolean {
    val toUri = this.copyFile(context = context, to = to) ?: return false
    val isDeleted = this.delete(context = context)
    return true
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

/**todo hbg 複製指定檔案至應用公開下載資料夾底下
 * @param dirName 在下載資料夾底下創建的子資料夾
 * @param fileName 複製後的檔案名稱
 * */
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
        to = toUri
    )
}

/**todo hbg 壓縮指定檔案至應用公開下載資料夾底下
 * @param dirName 下載資料夾底下創建的子資料夾
 * @param fileName 複製後的檔案名稱
 * @param filter 黑名單邏輯 回傳 false 不納入壓縮檔
 * @param encrypt 將壓縮檔案內部的資料進行加密防止解析
 * @return 壓縮檔案的路徑結果
 * */
fun File.zipFileToDownload(
    context: Context? = HBG5Application.instance,
    dirName: String = "",
    fileName: String,
    filter: ((String) -> Boolean) = { true },
    encrypt: Boolean = false
): Uri? {
    val ctx = context ?: return null
    val resolver = ctx.contentResolver

    // 準備輸出檔名與路徑
    val safeName = run {
        val n = fileName.trim().ifBlank { "archive.zip" }
        if (n.endsWith(".zip", ignoreCase = true)) n else "$n.zip"
    }
    val subDir = dirName.trim().trim('/')
    // 建立 Downloads 目的地（Q+ 走 MediaStore；以下用傳統目錄）
    val outUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val relPath = if (subDir.isBlank())
            Environment.DIRECTORY_DOWNLOADS
        else
            Environment.DIRECTORY_DOWNLOADS + File.separator + subDir

        val cv = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, safeName)
            put(MediaStore.Downloads.MIME_TYPE, "application/zip")
            put(MediaStore.Downloads.RELATIVE_PATH, relPath)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv) ?: return null
    } else {
        val downloads = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetDir = if (subDir.isBlank()) downloads else File(downloads, subDir)
        if (!targetDir.exists()) targetDir.mkdirs()
        Uri.fromFile(File(targetDir, safeName))
    }
    // 輸出 ZIP
    try {
        resolver.openOutputStream(outUri)?.use { os ->
            ZipOutputStream(BufferedOutputStream(os)).use { zos ->

                fun copyStream(input: InputStream, output: OutputStream) {
                    val buf = ByteArray(32 * 1024)

                    // 加密檔案 採用 0x0F XOR 對每一個byte進行運算
                    if(encrypt) {
                        while (true) {
                            val r = input.read(buf)
                            if (r == -1) break
                            // 就地變更：每個 byte XOR 0x0F
                            for (i in 0 until r) {
                                buf[i] = (buf[i].toInt() xor 0x0F).toByte()
                            }
                            output.write(buf, 0, r)
                        }
                    }
                    // 未加密
                    else {
                        while (true) {
                            val r = input.read(buf)
                            if (r == -1) break
                            output.write(buf, 0, r)
                        }
                    }
                }

                // 將 this（來源 File）遞迴寫入，relPath 為 ZIP 內的相對路徑
                fun walk(src: File, relPath: String) {
                    if (src.isDirectory) {
                        val dirEntryName = if (relPath.endsWith("/")) relPath else "$relPath/"
                        // 目錄本身是否納入（保留空資料夾）；即使排除，仍會往下走子項
                        if (filter(dirEntryName)) {
                            zos.putNextEntry(ZipEntry(dirEntryName))
                            zos.closeEntry()
                        }
                        src.listFiles()?.forEach { child ->
                            val childRel = if (relPath.isEmpty()) child.name else "$relPath/${child.name}"
                            walk(child, childRel)
                        }
                    } else if (src.isFile) {
                        if (!filter(relPath)) return
                        zos.putNextEntry(ZipEntry(relPath))
                        FileInputStream(src).use { input -> copyStream(input, zos) }
                        zos.closeEntry()
                    }
                }

                if (!this.exists()) throw FileNotFoundException("Source not found: $this")

                // 若希望 ZIP 內包含根資料夾名稱（一般常見），保持如下：
                //val startRel = this.name
                //walk(this, startRel)

                // 若想把內容「平鋪」在 ZIP 根目錄（不含最外層資料夾），改成：
                 if (this.isDirectory) this.listFiles()?.forEach { walk(it, it.name) } else walk(this, this.name)
            }
        } ?: return null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val done = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
            resolver.update(outUri, done, null, null)
        }
        return outUri
    } catch (e: Exception) {
        try { resolver.delete(outUri, null, null) } catch (_: Exception) {}
        e.printStackTrace()
        return null
    }
}
/**todo hbg 解壓縮檔案至指定的路徑下
 * @param toFile 解壓縮的目的地 通常為應用私有資料夾 因而採用 File 形式 不需要讀寫權限
 * @param encrypt 將壓縮檔案內部的資料進行加密防止解析
 * @return 是否解壓縮成功
 * */
fun Uri.unzip(
    context: Context? = HBG5Application.instance,
    toFile: File,
    decrypt: Boolean = false
): Boolean {
    if(context == null) { return false }

    val BUFFER_SIZE = 64 * 1024

    /** 防 Zip Slip：將 entryName 安全解析到 root 下；若超出 root，回傳 null（不輸出 Log.w） */
    fun safeResolve(root: File, entryName: String): File? {
        val normalized = entryName.replace('\\', File.separatorChar)

        // 禁止絕對路徑或根起始
        if (normalized.startsWith(File.separator) || normalized.startsWith("/")) {
            return null
        }

        val dest = File(root, normalized)
        val destCanonical = try {
            dest.canonicalPath
        } catch (_: IOException) {
            return null
        }

        val rootCanonical = try {
            val base = root.canonicalPath
            if (base.endsWith(File.separator)) base else base + File.separator
        } catch (_: IOException) {
            return null
        }

        return if (destCanonical.startsWith(rootCanonical)) dest else null
    }

    // 1) 準備目的資料夾：若是資料夾則清空，若是檔案則刪除後改為資料夾
    try {
        if (toFile.exists()) {
            if (toFile.isDirectory) {
                toFile.listFiles()?.forEach { child ->
                    if (!child.deleteRecursively()) {
                        return false
                    }
                }
            } else {
                if (!toFile.delete()) return false
                if (!toFile.mkdirs()) return false
            }
        } else {
            if (!toFile.mkdirs()) return false
        }
    } catch (e: Exception) {
        return false
    }

    // 2) 正式解壓（不做名稱衝突檢查，因為已清空目的地）
    return try {
        context.contentResolver.openInputStream(this)?.use { input ->
            val zis = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ZipInputStream(BufferedInputStream(input), StandardCharsets.UTF_8)
            } else {
                ZipInputStream(BufferedInputStream(input))
            }

            zis.use { zip ->
                val buffer = ByteArray(BUFFER_SIZE)
                var entry: ZipEntry? = zip.nextEntry

                while (entry != null) {
                    val entryName = entry.name
                    if (!entryName.isNullOrEmpty()) {
                        val outFile = safeResolve(toFile, entryName) ?: return false

                        if (entry.isDirectory) {
                            if (!outFile.exists() && !outFile.mkdirs()) {
                                return false
                            }
                        }
                        else {
                            // 確保父層目錄存在
                            outFile.parentFile?.let { parent ->
                                if (!parent.exists() && !parent.mkdirs()) {
                                    return false
                                }
                            }
                            // 寫出檔案
                            FileOutputStream(outFile).use { fos ->
                                var read: Int
                                // decrypt 0x0F XOR 對個別檔案解密
                                if(decrypt) {
                                    while (zip.read(buffer).also { read = it } != -1) {
                                        for(i in 0 until read) {
                                            buffer[i] = (buffer[i].toInt() xor 0x0F).toByte()
                                        }
                                        fos.write(buffer, 0, read)
                                    }
                                }
                                // 未加密檔案寫出
                                else {
                                    while (zip.read(buffer).also { read = it } != -1) {
                                        fos.write(buffer, 0, read)
                                    }
                                }
                                fos.flush()

                                if (entry!!.time > 0) outFile.setLastModified(entry!!.time)
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
            return@use true
        } ?: false
    } catch (e: Exception) {
        false
    }
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
        to = toUri
    )
}
/**todo hbg 複製URI檔案至指定位置 耗時操作
 * @param to content:///xxx/xxx/xxx*/
fun Uri.copyFile(context: Context? = HBG5Application.instance, to: Uri): Uri? {
    context ?: return null

    val resolver = context.contentResolver

    return try {
        // 移除目的地檔案 讓目的地可以正常接收資料
        if (!to.delete(context = context)) {
            throw Exception("覆蓋檔案失敗")
        }
        resolver.openInputStream(this)?.use { input ->
            BufferedOutputStream(resolver.openOutputStream(to)).use { output ->
                val buf = ByteArray(1024)
                input.read(buf)
                do {
                    output.write(buf)
                } while (input.read(buf) != -1)
            }
        }
        to
    } catch (error: Exception) { null }
}
/**todo hbg 複製URI檔案至指定位置 耗時操作*/
fun Uri.copyFile(context: Context? = HBG5Application.instance, to: File): Uri? = context?.let { copyFile(context = context, to =  to.toUri(it)) }

// todo hbg deprecated
//fun Uri.copyFile(context: Context? = HBG5Application.instance, dirPath: String, rename: String? = null): Uri?

/** todo hbg 取得應用私有資料夾路徑 通常不需要讀寫權限
 * @param dirName: 子資料夾 空值: 無子資料夾
 * @param fileName: 檔案名稱 空值: 僅返回資料夾路徑 "example.pdf"
 * @return content:///storage/emulated/0/Android/data/{app package name}/files/{dirName}/{fileName}
 * */
fun Context.getPrivateUri(dirName: String = "", fileName: String = ""): Uri? {
    val path = getPrivatePath(dirName = dirName, fileName = fileName) ?: return null
    return File(path).toUri(this)
}

/** todo hbg 取得應用私有資料夾路徑 通常不需要讀寫權限
 * @param dirName: 子資料夾 空值: 無子資料夾
 * @param fileName: 檔案名稱 空值: 僅返回資料夾路徑
 * @return /storage/emulated/0/Android/data/{app package name}/files/{dirName}/{fileName}
 * */
fun Context.getPrivatePath(dirName: String = "", fileName: String = ""): String? {
    val rootDirPath = getExternalFilesDir(null)?.absolutePath ?: ""
    if(rootDirPath.isEmpty()) { return null }
    val childDirPath =
        if(dirName.isEmpty()) rootDirPath
        else "${rootDirPath}${File.separator}${dirName}"
    val childDir = File(childDirPath)
    if(!childDir.exists()) {
        childDir.mkdirs()
    }
    return if(fileName.isEmpty()) childDirPath
    else "$childDirPath${File.separator}$fileName"
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
    val bytes = this.getFileSizeBytes(context = context) ?: return null
    return (bytes * 0.0009765625 * 0.0009765625).toFloat()
}
// todo hbg
fun Uri.getFileSizeBytes(context: Context? = HBG5Application.instance): Long? {
    try {
        if(context == null) { throw NullPointerException("Not found context") }
        return context.contentResolver.openAssetFileDescriptor(this, "r")?.use { it.length } ?: return null
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


