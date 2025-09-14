package priv.liten.hbg5_http

import android.annotation.SuppressLint
import com.google.gson.Gson
import okhttp3.*
import priv.liten.hbg5_config.HBG5HttpConfig
import priv.liten.hbg5_extension.decodeBase64
import retrofit2.Call
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.http.*
import java.io.IOException
import java.lang.Exception
import java.lang.reflect.Type
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class HBG5HttpService private constructor() {

    /** 服務介面實體  */
    var value: Builder.ServiceImpl? = null
        private set

    /** 是否顯示所有內容 (關閉將無法顯示所有 Log)  */
    var logEnable = true
        private set

    /** 是否顯示原始的傳輸內容  */
    var logBodyEnable = false
        private set

    /** 是否顯示解碼的傳輸內容  */
    var logDecodeBodyEnable = true
        private set

    /** 基礎連結網址  */
    var baseUrl = ""
        private set

    /** 讀寫資料最大等待時間  */
    var timeoutInterval = 30
        private set

    /** Http 檔頭內容  */
    var headers: MutableMap<String, (() -> String)> = mutableMapOf()
        private set


    class Builder {

        private var owner: HBG5HttpService = HBG5HttpService()

        fun setLogEnable(value: Boolean): Builder {
            owner.logEnable = value
            return this
        }

        fun setLogBodyEnable(value: Boolean): Builder {
            owner.logBodyEnable = value
            return this
        }

        fun setLogDecodeBodyEnable(value: Boolean): Builder {
            owner.logDecodeBodyEnable = value
            return this
        }

        fun setBaseUrl(url: String): Builder {
            owner.baseUrl = url
            return this
        }

        fun setTimeoutInterval(second: Int): Builder {
            owner.timeoutInterval = second
            return this
        }

        fun setHeaders(headers: Map<String, (()->String)>): Builder {
            owner.headers = headers.toMutableMap()
            return this
        }

        fun setHeaders(key: String, value: (()->String)?): Builder {

            value
                ?.let {
                    owner.headers[key] = it
                }
                ?:run {
                    owner.headers.remove(key)
                }

            return this
        }

        fun setHeaders(key: String, value: String): Builder {
            owner.headers[key] = { value }
            return this
        }

        /** 使用自定義憑證 */
        fun buildSslTrust(
            sslContext: SSLContext?,
            trustManager: X509TrustManager?,
            hostnameVerifier: HostnameVerifier?): HBG5HttpService {

            val client = OkHttpClient()
                .newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(owner.timeoutInterval.toLong(), TimeUnit.SECONDS)
                .writeTimeout(owner.timeoutInterval.toLong(), TimeUnit.SECONDS)
                .addInterceptor(object : Interceptor {
                    @Throws(IOException::class)
                    override fun intercept(chain: Interceptor.Chain): Response {
                        val request = chain.request()
                        val builder = request
                            .newBuilder()
                            .removeHeader("HEADERS")
                            .removeHeader(HBG5HttpConfig.Method.key)
                            .removeHeader(HBG5HttpConfig.ContentType.key)

                        // Headers
                        run {
                            val headers: MutableMap<*, *> = Gson().fromJson(
                                request.header("HEADERS")?.decodeBase64() ?: "{}",
                                MutableMap::class.java)

                            for (_key in headers.keys) {
                                val key = _key as? String ?: continue
                                val value = headers[key] as? String ?: continue
                                builder.header(key, value)
                            }
                        }

                        // URL-Method
                        run {
                            val urlMethod = request.header(HBG5HttpConfig.Method.key) ?: HBG5HttpConfig.Method.GET.value

                            if (HBG5HttpConfig.Method.GET.value == urlMethod) {
                                builder.method(urlMethod, null)
                            }
                            else {
                                builder.method(urlMethod, request.body())
                            }
                        }

                        return chain.proceed(builder.build())
                    }
                })

            sslContext?.let {
                trustManager?.let {
                    client.sslSocketFactory(sslContext.socketFactory, trustManager)
                }
            }
            hostnameVerifier?.let {
                client.hostnameVerifier(hostnameVerifier)
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(owner.baseUrl)
                .client(client.build())
            retrofit.converterFactories().clear()
            retrofit.addConverterFactory(object : Converter.Factory() {

                override fun requestBodyConverter(
                    type: Type,
                    parameterAnnotations: Array<Annotation>,
                    methodAnnotations: Array<Annotation>,
                    retrofit: Retrofit): Converter<*, RequestBody> {

                    return Converter<Any?, RequestBody> { value ->

                        return@Converter when(value) {
                            is RequestBody -> { value }
                            else -> {
                                RequestBody.create(
                                    MediaType.parse("application/json"),
                                    value.toString())
                            }
                        }
                    }
                }

                override fun responseBodyConverter(
                    type: Type,
                    annotations: Array<Annotation>,
                    retrofit: Retrofit): Converter<ResponseBody, *> {

                    return Converter { value ->
                        val valueString = value.string()
                        if (String::class.java == type) {
                            valueString
                        }
                        else {
                            Gson().fromJson<Any>(valueString, type)
                        }
                    }
                }
            })
            owner.value = retrofit.build().create(ServiceImpl::class.java)
            return owner
        }

        /** 忽略憑證 */
        fun buildIgnoreSslTrust(): HBG5HttpService {
            return try {
                val manager: X509TrustManager = object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                }
                val trustAllCerts = arrayOf<TrustManager>(manager)

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                buildSslTrust(
                    sslContext,
                    manager,
                    { hostname, session -> true })
            }
            catch (error: Exception) {
                error.printStackTrace()
                throw error
            }
        }

        /** 使用預設憑證 */
        fun build(): HBG5HttpService {
            return buildSslTrust(null, null, null)
        }

        interface ServiceImpl {
            /**
             * 通用呼叫
             */
            @POST
            fun call(
                @Url url: String?,
                @Header("HEADERS") headers: String?,
                @Header("URL-METHOD") urlMethod: String?,
                @Body body: RequestBody?
            ): Call<String?>?

            /**下載檔案 todo hbg*/
            @Streaming
            @POST
            fun download(
                @Url url: String?,
                @Header("HEADERS") headers: String?,
                @Header("URL-METHOD") urlMethod: String?,
                @Body body: RequestBody?
            ): Call<ResponseBody?>?
        }
    }
}

fun HBG5HttpService.builder(): HBG5HttpService.Builder {
    return HBG5HttpService.Builder()
        .setLogEnable(this.logEnable)
        .setLogBodyEnable(this.logBodyEnable)
        .setLogDecodeBodyEnable(this.logDecodeBodyEnable)
        .setBaseUrl(this.baseUrl)
        .setTimeoutInterval(this.timeoutInterval)
        .setHeaders(this.headers)
}