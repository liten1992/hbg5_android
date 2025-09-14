package priv.liten.hbg5_extension

import android.app.Activity
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Selection
import android.text.Spannable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import androidx.core.view.postDelayed
import priv.liten.hbg.R
import priv.liten.hbg5_widget.drawable.HBG5ExifDrawable
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl
import kotlin.math.max

fun View.hideKeyboard() {

    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { manager ->
        manager.hideSoftInputFromWindow(windowToken, 0)
        clearFocus()
    }
}

fun View.showKeyboard() {

    if(requestFocus()) {
        (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.showSoftInput(this, 0)
    }
}

fun View.removeFromSuperview() {

    val parent = this.parent as? ViewGroup ?: return

    parent.removeView(this)
}

fun View.getLocationInFrame(outArray: IntArray) {

    this.getLocationInWindow(outArray)
    // 偏移StatusBarHeight
    val frameBounds = Rect(0, 0, 0, 0).also {
        this.getWindowVisibleDisplayFrame(it)
    }

    outArray[1] -= frameBounds.top
}


fun View.getString(@StringRes resId: Int): String = context.getString(resId)

fun View.getStringArray(@ArrayRes resId: Int): Array<String> {
    return context.resources.getStringArray(resId)
}

fun View.getColor(@ColorRes resId: Int): Int {
    return context.getColor(resId)
}

fun View.getDrawable(@DrawableRes resId: Int): Drawable? {
    return ContextCompat.getDrawable(context, resId)
}
/**@return px*/
fun View.getDimension(@DimenRes resId: Int): Float {
    return context.resources.getDimension(resId)
}

/**
 * 取得繪製圖像
 * @param autoRotation 根據EXIF資訊協助圖像轉向，部分裝置會將圖片強制轉正，但是轉向資料仍未修正，導致做修正旋轉反而變為錯的
 * */
fun View.getDrawable(uri: Uri?, autoRotation: Boolean = false): Drawable? {
    if(uri == null) { return null }
    try {
        val sourceDrawable: Drawable = context.contentResolver.openInputStream(uri).use {
            Drawable.createFromStream(it, uri.toString())
        } ?: return null

        return if(autoRotation && sourceDrawable is BitmapDrawable)
            HBG5ExifDrawable.build(context = context, drawable = sourceDrawable, uri = uri)
        else
            sourceDrawable

    }catch (error: Exception) {
        return null
    }
}

/**是否支援生物辨識*/
fun View.canBiometric(): Boolean {
    return when(BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
        // 可執行辨識
        BiometricManager.BIOMETRIC_SUCCESS -> true
        // 此裝置不支援生物辨識
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> false
        // 此裝置生物辨識當前不可用，請稍後再試
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> false
        // 此裝置未註冊生物辨識功能
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> false
        // 當前裝置生物辨識功能有漏洞
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> false
        // 當前裝置系統版本無法使用生物辨識
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> false
        // 生物辨識發生未知的錯誤
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> false
        // 其他問題
        else -> false
    }
}

/**讀取XML元件資源結束後自動釋放記憶體*/
fun View.readTypedArray(
    attrs: AttributeSet?,
    styleable: IntArray,
    defStyleAttr: Int,
    defStyleRes: Int,
    read: (TypedArray) -> Unit,
    finish: () -> Unit) {

    val viewImpl = this as? HBG5ViewImpl
    viewImpl?.v5AttrCompleted = false
    context.obtainStyledAttributes(attrs, styleable, defStyleAttr, defStyleRes)
        .apply(read)
        .recycle()
    viewImpl?.v5AttrCompleted = true
    finish()
}

/**
 * 文字元件 插入文字
 * @param index 插入文字的位置
 * @param text 插入的文字
 * @param showKeyboard 插入完畢後是否顯示鍵盤
 * */
fun TextView.insertText(index: Int? = null, text: CharSequence, showKeyboard: Boolean) {
    val nonnullIndex = max(index ?: selectionStart, 0)
    val textBuilder = StringBuilder(this.text)
    if(nonnullIndex >= text.length) {
        textBuilder.append(text)
    }
    else {
        textBuilder.insert(nonnullIndex, text)
    }
    this.text = textBuilder.toString()
    val spannable = this.text as? Spannable
    if(spannable != null) {
        Selection.setSelection(spannable, nonnullIndex + text.length)
    }
    if(showKeyboard && this.isFocused) {
        this.showKeyboard()
    }
}

/**
 * 在顯示後讓 TimePicker 的「小時」取得焦點（API 21~34），同時相容 Spinner 與 Clock 模式。
 */
fun TimePicker.focusHourCompat() {
    // 確保時間元件已經佈局完畢再操作
    post {
        // 1) Spinner 模式：直接找到小時 NumberPicker
        val hourId = Resources.getSystem().getIdentifier("hour", "id", "android")
        val hourNp = findViewById<NumberPicker?>(hourId)
        if (hourNp != null) {
            // 讓 NumberPicker 可聚焦並請求焦點
            hourNp.isFocusableInTouchMode = true
            hourNp.requestFocus()
            hourNp.performClick() // 有些機型需要 click 才會顯示游標/輸入
            return@post
        }

        // 2) Clock 模式：嘗試點擊代表「小時」的 View（多數裝置的 id 是 "hours"）
        val hoursViewId = Resources.getSystem().getIdentifier("hours", "id", "android")
        val hoursView = findViewById<View?>(hoursViewId)
        if (hoursView != null) {
            hoursView.performClick() // 切到小時選取
            hoursView.requestFocus()
            return@post
        }

        // 3) 退路：反射呼叫 delegate.setCurrentItemShowing(0, true)
        //    0 = 小時、1 = 分鐘（內部慣例，多數 ROM 適用）
        try {
            val delegateField = TimePicker::class.java.getDeclaredField("mDelegate").apply {
                isAccessible = true
            }
            val delegate = delegateField.get(this)
            val method = delegate.javaClass.getDeclaredMethod(
                "setCurrentItemShowing",
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType
            ).apply { isAccessible = true }
            method.invoke(delegate, 0, true) // 0 = hour
            // 盡量再要一下焦點，保險
            requestFocus()
        } catch (_: Throwable) {
            // 最兇殘的 ROM 仍失敗就放過：至少不會崩潰
        }
    }
}

fun DatePicker.showDayPicker() {
    postDelayed(1) {
        val names = listOf(
            "date_picker_header_date",  // AOSP
            "android:id/date_picker_header_date", // AOSP
            "date_picker_date",         // MIUI,
            "miui:id/date_picker_date", // MIUI,
            "hwext:id/date_picker_header_date",
            "oppo:id/date_picker_header_date"
        )

        var resId = this.getTag(R.id.picker_date) as? Int
        if(resId == null) {
            for (name in names) {
                val id = resources.getIdentifier(name, "id", context.packageName)
                if (id != 0) {
                    resId = id
                    this.setTag(R.id.picker_date, id)
                }
            }
            if(resId == null) {
                resId = 0
                this.setTag(R.id.picker_date, 0)
            }
        }
        if(resId != 0) {
            findViewById<View>(resId)?.performClick()
        }
        return@postDelayed
    }
}

fun DatePicker.showYearPicker() { }