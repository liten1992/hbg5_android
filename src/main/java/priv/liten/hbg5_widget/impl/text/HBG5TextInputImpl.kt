package priv.liten.hbg5_widget.impl.text

import android.content.res.TypedArray
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetBoolean
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_widget.bin.text.HBG5TextEditor
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.config.HBG5WidgetConfig.Attrs.TextInputType
import priv.liten.hbg5_widget.impl.base.*

/** 文字單行輸入介面 */
interface HBG5TextInputImpl:
    HBG5ViewImpl,
    HBG5BackgroundImpl,
    HBG5TextImpl,
    HBG5HintImpl {

    /** 文字輸入進行安全遮蔽 */
    var v5TextInputSecurityEnable: Boolean
        get() = this.v5GetTag(R.id.attr_input_security) ?: false
        set(value) {
            this.v5SetTag(R.id.attr_input_security, value)
            refreshInput()
        }
    /** 文字輸入內容限制 文字、數字 */
    var v5TextInputType: TextInputType
        get() = this.v5GetTag(R.id.attr_input_type) ?: TextInputType.Text
        set(value) {
            this.v5SetTag(R.id.attr_input_type, value)
            refreshInput()
        }

    /** 註冊輸入文字異動監聽 */
    fun v5RegisterTextChanged(listener: ((String) -> Unit)?) {
        if(this !is TextView) { return }
        // Unregister OLD
        this.v5GetTag<TextWatcher>(R.id.attr_text_watcher)?.let { this.removeTextChangedListener(it) }
        this.v5SetTag(R.id.attr_text_watcher, null)
        // Register NEW
        listener?.let {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    it(p0?.toString() ?: "")
                }
            }
            this.v5SetTag(R.id.attr_text_watcher, watcher)
            this.addTextChangedListener(watcher)
        }
    }

    /** 更新XML */
    fun buildInputByAttr(
        typedArray: TypedArray,
        @StyleableRes inputSecurityEnable: Int? = null,
        @StyleableRes inputType: Int? = null,
    ) {
        v5TextInputSecurityEnable = inputSecurityEnable?.let { typedArray.v5GetBoolean(it) } ?: false
        v5TextInputType = inputType?.let { TextInputType.fromAttr(typedArray.v5GetInt(it)) } ?: TextInputType.Text
    }

    fun refreshInput() {
        if(this !is TextView) { return }
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }

        if(!v5Touchable) { inputType = InputType.TYPE_NULL }
        //if(!isEnabled) { inputType = InputType.TYPE_NULL }

        this.inputType = when(v5TextInputSecurityEnable) {
            // 密碼模式
            true -> when(v5TextInputType) {
                TextInputType.Text -> InputType.TYPE_CLASS_TEXT
                    .or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                TextInputType.TextUppercase -> InputType.TYPE_CLASS_TEXT
                    .or(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
                    .or(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                TextInputType.Number -> InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }
            // 文字模式
            else -> {
                val type = when (v5TextInputType) {
                    TextInputType.Text -> InputType.TYPE_CLASS_TEXT
                    TextInputType.TextUppercase -> InputType.TYPE_CLASS_TEXT.or(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS)
                    TextInputType.Number -> InputType.TYPE_CLASS_NUMBER.or(InputType.TYPE_NUMBER_FLAG_DECIMAL)
                }
                when (this is HBG5TextEditor) {
                    // 多行
                    true -> type.or(InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                    // 單行
                    else -> type
                }
            }
        }
    }
}