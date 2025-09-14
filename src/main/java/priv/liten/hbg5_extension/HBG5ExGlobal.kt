package priv.liten.hbg5_extension

import android.util.Log
import priv.liten.hbg.BuildConfig

fun logD(tag:String, content:String) {
    if(BuildConfig.DEBUG) {
        Log.d(tag, content)
    }
}