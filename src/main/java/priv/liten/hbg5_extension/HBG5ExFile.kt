package priv.liten.hbg5_extension

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

@SuppressLint("ObsoleteSdkInt")
fun File.toUri(context: Context): Uri {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)
    else
        Uri.fromFile(this)
}