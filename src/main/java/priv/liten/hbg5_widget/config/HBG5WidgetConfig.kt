package priv.liten.hbg5_widget.config

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.net.toFile
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer
import com.nostra13.universalimageloader.core.download.BaseImageDownloader
import priv.liten.hbg5_extension.BitmapBuilder
import priv.liten.hbg5_extension.getPrivatePath
import priv.liten.hbg5_extension.getPrivateUri
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*


class HBG5WidgetConfig {

    companion object {
        const val PRIVATE_DIR_DOWNLOAD = "Download"
        const val PRIVATE_DIR_CACHE = "Cache"
        const val PRIVATE_DIR_CACHE_IMAGES = "Cache/Images"
        const val PRIVATE_DIR_DOCUMENTS = "Documents"

        /** 略過SSL憑證檢查 直接下載圖片 */const
        val SSL_UNSAFE_ENABLE = true
        /** 顯示下載圖片的設定 */
        val IMAGE_OPTIONS_BUILDER = DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(SimpleBitmapDisplayer())
            .showImageOnLoading(null)
            .showImageForEmptyUri(null)
            .showImageOnFail(null)

        @SuppressLint("CustomX509TrustManager")
        fun imageLoader(context: Context): ImageLoader {

            val instance = ImageLoader.getInstance()

            if (!instance.isInited) {
                val downloader: BaseImageDownloader = object : BaseImageDownloader(
                    // connectTimeout (5 s), readTimeout (20 s)
                    context, 5 * 1000, 20 * 1000) {
                    @Throws(IOException::class)
                    override fun createConnection(url: String, extra: Any?): HttpURLConnection {
                        val httpConn = super.createConnection(url, extra)
                        if (!SSL_UNSAFE_ENABLE) {
                            return httpConn
                        }

                        // Install unsafe
                        (httpConn as? HttpsURLConnection)?.let { httpsConn ->
                            try {
                                val manager: X509TrustManager = object : X509TrustManager {
                                    @SuppressLint("TrustAllX509TrustManager")
                                    @Throws(CertificateException::class)
                                    override fun checkClientTrusted(
                                        chain: Array<X509Certificate>,
                                        authType: String) { }

                                    @SuppressLint("TrustAllX509TrustManager")
                                    @Throws(CertificateException::class)
                                    override fun checkServerTrusted(
                                        chain: Array<X509Certificate>,
                                        authType: String) { }

                                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                                        return arrayOf()
                                    }
                                }
                                val trustAllCerts = arrayOf<TrustManager>(manager)

                                // Install the all-trusting trust manager
                                val sslContext = SSLContext.getInstance("SSL")
                                sslContext.init(null, trustAllCerts, SecureRandom())

                                // Create an ssl socket factory with our all-trusting manager
                                val sslSocketFactory = sslContext.socketFactory
                                httpsConn.sslSocketFactory = sslSocketFactory
                                httpsConn.hostnameVerifier =
                                    HostnameVerifier { hostname, session -> true }
                            } catch (error: Exception) {
                                error.printStackTrace()
                            }
                        }

                        // Install header
                        when(extra) {
                            is Map<*, *> -> {
                                for((key, value) in extra) {
                                    if(key !is String) { continue }
                                    if(value !is String) { continue }
                                    httpConn.setRequestProperty(key, value)
                                }
                            }
                        }

                        return httpConn
                    }
                }
                val configBuilder = ImageLoaderConfiguration.Builder(context)
                    .memoryCacheExtraOptions(1024, 1024) // max width, max height
                    //.discCacheExtraOptions(480, 800, CompressFormat.JPEG, 75) // Can slow ImageLoader, use it carefully (Better don't use it)
                    .threadPoolSize(4) // 線程數量
                    .threadPriority(Thread.NORM_PRIORITY - 1) // 優先程級
                    .denyCacheImageMultipleSizesInMemory() //.offOutOfMemoryHandling()
                    .memoryCache(UsingFreqLimitedMemoryCache(10 * 1024 * 1024)) // You can pass your own memory cache implementation
                    .diskCacheSize(50 * 1024 * 1024)
                    .diskCacheFileNameGenerator(HashCodeFileNameGenerator())
                    .imageDownloader(downloader)
                    //.imageDownloader(BaseImageDownloader(context,5 * 1000, 30 * 1000))  // connectTimeout (5 s), readTimeout (20 s)
                    .defaultDisplayImageOptions(IMAGE_OPTIONS_BUILDER.build())

                val cacheDir = context.getPrivatePath(dirName = PRIVATE_DIR_CACHE_IMAGES)?.let { path -> File(path) }
                if (cacheDir != null) {
                    configBuilder.diskCache(UnlimitedDiskCache(cacheDir)) // You can pass your own disc cache implementation
                }
                instance.init(configBuilder.build())
            }

            return instance
        }
    }

    object Attrs {
        /** 可見性 */
        enum class Visibility {
            /** 隱藏(無布局) */
            Gone,
            /** 隱藏(占布局) */
            Invisible,
            /** 顯示 */
            Visible;

            fun setView(view: View) {
                view.visibility = when(this) {
                    Visible -> { View.VISIBLE }
                    Invisible -> { View.INVISIBLE }
                    Gone -> { View.GONE }
                }
            }

            companion object {
                fun fromView(view: View): Visibility {
                    return when(view.visibility) {
                        View.VISIBLE -> { Visible }
                        View.INVISIBLE -> { Invisible }
                        View.GONE -> { Gone }
                        else -> { Gone }
                    }
                }

                fun fromAttr(index: Int?): Visibility? {

                    val existIndex = index ?: return null

                    val values = Visibility.values()

                    return if(0 <= existIndex && existIndex < values.size) values[existIndex]
                    else null
                }
            }
        }
        /** 圖像顯示位置 start、end、top、bottom */
        enum class ImageAlignment {
            Start, End, Top, Bottom;

            val value: Int
                get() {
                    return when (this) {
                        Start               -> 0
                        End                 -> 1
                        Top                 -> 2
                        Bottom              -> 3
                    }
                }

            companion object {
                fun fromAttr(attr: Int?): ImageAlignment? {

                    val index = attr ?: return null

                    val values = values()

                    return if (0<=index && index<values.size) values[index] else null
                }
            }
        }
        /** 圖片填充方式 */
        enum class ImageScaleType {
            /** 填滿 破壞比例 */
            Fill,
            /** 置中 維持比例 */
            Fit,
            /** 置中 填滿溢出 維持比例 */
            Crop;

            val value: ImageView.ScaleType
                get() {
                    return when(this) {
                        Fill -> ImageView.ScaleType.FIT_XY
                        Fit -> ImageView.ScaleType.FIT_CENTER
                        Crop -> ImageView.ScaleType.CENTER_CROP
                    }
                }

            companion object {

                fun fromUIType(type: ImageView.ScaleType): ImageScaleType {
                    return when(type) {
                        ImageView.ScaleType.FIT_XY -> Fill
                        ImageView.ScaleType.FIT_CENTER -> Fit
                        ImageView.ScaleType.CENTER_CROP -> Crop
                        else -> Fit
                    }
                }

                fun fromAttr(attr: Int?): ImageScaleType? {
                    return when(attr) {
                        1 -> Fill
                        2 -> Fit
                        3 -> Crop
                        else -> null
                    }
                }
            }
        }
        /** 列表滾動方向 垂直(vertical) 水平(horizontal) */
        enum class ListOrientation {
            VERTICAL, HORIZONTAL;

            val value: Int
                get() {
                    return when(this) {
                        VERTICAL -> RecyclerView.VERTICAL
                        HORIZONTAL -> RecyclerView.HORIZONTAL
                    }
                }

            companion object {
                fun fromAttr(attr: Int?): ListOrientation? {
                    return when(attr) {
                        1 -> { VERTICAL }
                        2 -> { HORIZONTAL }
                        else -> { null }
                    }
                }
            }
        }
        /** 列表排列方式 線性(linear)、網格(grid) */
        enum class ListLayoutType {
            /** 線性 */
            Linear,
            /** 網格 */
            Grid;

            companion object {
                fun fromAttr(attr: Int?): ListLayoutType? {
                    return when(attr) {
                        1 -> { Linear }
                        2 -> { Grid }
                        else -> { null }
                    }
                }
            }
        }
        /** 列表項目對齊方式 起始、置中、結尾 (非項目內容對齊) */
        enum class ListGravity {
            /** 起始 */
            Start,
            /** 置中 */
            Center,
            /** 結尾 */
            End;

            companion object {
                fun fromAttr(attr: Int?): ListGravity? {
                    return when(attr) {
                        1 -> { Start }
                        2 -> { Center }
                        3 -> { End }
                        else -> { null }
                    }
                }
            }
        }
        /** 文字垂直對齊 */
        enum class TextAlignmentVertical(val gravity: Int) {
            Top(Gravity.TOP),
            Center(Gravity.CENTER_VERTICAL),
            Bottom(Gravity.BOTTOM),
            Fill(Gravity.FILL_VERTICAL);

            companion object {

                fun fromAttr(attr: Int?): TextAlignmentVertical? {

                    val index = attr ?: return null

                    val values = values()

                    return if (0<=index && index<values.size) values[index] else null
                }

                fun fromGravity(gravity: Int): TextAlignmentVertical? {
                    return when(gravity.and(Gravity.VERTICAL_GRAVITY_MASK)) {
                        Gravity.TOP -> Top
                        Gravity.CENTER_VERTICAL -> Center
                        Gravity.BOTTOM -> Bottom
                        Gravity.FILL_VERTICAL -> Fill
                        else -> Top
                    }
                }
            }
        }
        /** 文字水平對齊 */
        enum class TextAlignmentHorizontal(val gravity: Int) {
            Start(Gravity.START),
            Center(Gravity.CENTER_HORIZONTAL),
            End(Gravity.END),
            Fill(Gravity.FILL_HORIZONTAL);

            companion object {

                fun fromAttr(attr: Int?): TextAlignmentHorizontal? {

                    val index = attr ?: return null

                    val values = values()

                    return if (0<=index && index<values.size) values[index] else null
                }

                fun fromGravity(gravity: Int): TextAlignmentHorizontal? {
                    return when(gravity.and(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK)) {
                        Gravity.START -> Start
                        Gravity.CENTER_HORIZONTAL -> Center
                        Gravity.END -> End
                        Gravity.FILL_HORIZONTAL -> Fill
                        else -> when(gravity.and(Gravity.HORIZONTAL_GRAVITY_MASK)) {
                            Gravity.LEFT -> Start
                            Gravity.RIGHT -> End
                            else -> null
                        }
                    }
                }
            }
        }
        /** 文字輸入種類 */
        enum class TextInputType {
            Text, Number, TextUppercase;

            companion object {

                fun fromAttr(attr: Int?): TextInputType? {

                    val index = attr ?: return null

                    val values = values()

                    return if (0<=index && index<values.size) values[index] else null
                }
            }
        }
    }

    object Request {
        /**新增檔案請求參數*/ // todo hbg
        open class InsertFile {
            enum class Root {
                PRIVATE, DOWNLOADED
            }

            var from: File? = null
            var toDir: String = ""
            var toRoot: Root = Root.PRIVATE
            var toName: String? = null

            open class File { }
            open class TextFile: File {
                constructor() {  }
                constructor(text: String?) {
                    this.text = text
                }
                /***/
                var text: String? = null
            }
            open class UriFile: File {
                constructor() { }
                constructor(uri: Uri?) {
                    this.uri = uri
                }
                var uri: Uri? = null
            }
        }
        /**取得檔案請求參數*/
        open class SelectFile {
            /**檔案容量最大限制 MB*/
            var maxMb: Float? = null
        }
        /**取得圖片請求參數*/
        open class SelectImage: SelectFile() {
            enum class QUALITY(val max: Int) {
                /** 512PX */
                LOW(max = 512),
                /** 1920PX */
                NORMAL(max = 1920),
                /** 4096PX */
                HIGH(max = 4096);
            }
            /**選取的圖像品質*/
            var quality = QUALITY.NORMAL
            /**禁止heic格式*/
            var noTypeHeic = true
        }
        /**取得影片請求參數*/
        open class SelectVideo: SelectFile() {
            /**錄影時間最大限制 秒*/
            var maxDurationSecond: Int = 60
            /**錄影品質 0 ~ 1*/
            var quality: Float = 0.8f
        }
    }

    class MessageShareType {
        companion object {
            val TEXT = MessageShareType()
            val MAIL = MessageShareType()
            val CLIPBOARD = MessageShareType()
            val VALUES = listOf(TEXT, MAIL, CLIPBOARD)
        }

        private constructor() { }

        val name: String get() = when(this) {
            TEXT -> "訊息"
            MAIL -> "信箱"
            CLIPBOARD -> "剪貼簿"
            else -> "錯誤"
        }

        override fun equals(other: Any?): Boolean {
            if(other === this) { return true }
            return false
        }
    }
}