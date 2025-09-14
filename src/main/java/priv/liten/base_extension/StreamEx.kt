package priv.liten.base_extension

import android.util.Log
import priv.liten.hbg5_extension.toLinkString
import priv.liten.hbg5_http.HBG5DownloadCall
import java.io.InputStream

/**讀取物件檔頭判斷檔案類型
 * @return isEmpty()無法判斷或讀取失敗 success: .png .jpg ... */
fun InputStream.readFileType(
    matchList: Map<String, Array<ByteArray>> = HBG5DownloadCall.FILE_TYPE_HEADERS
): String
{
    try {
        val fileHeader = ByteArray(16)
        this.read(fileHeader, 0, fileHeader.size)
        // 檔案類型
        for((fileType, codeCollection) in matchList) {
            // 類型底下符合的子型
            for(codes in codeCollection) {
                // 起始讀取位置
                val startIndex = codes.getOrNull(0)?.toInt() ?: continue
                // 判斷長度大於讀取內容
                if(fileHeader.size < (startIndex + codes.size - 1)) { break }
                // 判斷符合
                for(codeIndex in 1 until codes.size) {
                    val code = codes[codeIndex]
                    if(fileHeader[startIndex + codeIndex - 1] != code) {
                        break
                    }
                    if(codeIndex == codes.size - 1) {
                        return fileType
                    }
                }
            }
        }

        return ""
    }
    catch (error: Exception) {
        return ""
    }
}