package priv.liten.hbg5_extension

import android.database.Cursor
import android.text.TextUtils
import androidx.core.database.getIntOrNull
import androidx.core.database.getStringOrNull
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

fun Cursor.getBooleanOrNull(index: Int): Boolean? {
    return this.getIntOrNull(index)?.let { it == 1 }
}

fun Cursor.getBoolean(index: Int): Boolean {
    return this.getIntOrNull(index)?.let { it == 1 } ?: throw NullPointerException("Database Cursor get boolean null")
}

fun <T> Cursor.getJsonOrNull(index: Int, type: Type): T? {
    return this.getStringOrNull(index)?.let { Gson().fromJson(it, type) }
}

inline fun <reified T> Cursor.getJsonArrayOrNull(index: Int): MutableList<T>? {
    val json = this.getStringOrNull(index)
    return if(TextUtils.isEmpty(json)) null
    else Gson().fromJson(json, object: TypeToken<MutableList<T>>(){}.type) ?: mutableListOf()
}

inline fun <reified T> Cursor.getJsonOrNull(index: Int): T? {
    return this.getStringOrNull(index)?.let { Gson().fromJson(it, T::class.java) }
}