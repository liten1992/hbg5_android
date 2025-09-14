package priv.liten.hbg

object BuildConfig {
    var DEBUG = false
    var APPLICATION_ID = ""
    var BUILD_TYPE = ""
    var VERSION_CODE = 0
    var VERSION_NAME = "0.0.0"

    fun init(
        debug: Boolean = DEBUG,
        application_id: String = APPLICATION_ID,
        build_type: String = BUILD_TYPE,
        version_code: Int = VERSION_CODE,
        version_name: String = VERSION_NAME,
    ) {
        DEBUG = debug
        APPLICATION_ID = application_id
        BUILD_TYPE = build_type
        VERSION_CODE = version_code
        VERSION_NAME = version_name
    }
}