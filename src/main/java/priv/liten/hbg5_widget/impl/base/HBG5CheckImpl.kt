package priv.liten.hbg5_widget.impl.base

import android.content.res.TypedArray
import android.view.View
import android.widget.Checkable
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetBoolean
import priv.liten.hbg5_extension.onAdd
import priv.liten.hbg5_widget.bin.button.HBG5RadioButton

interface HBG5CheckImpl : Checkable {

    override fun setChecked(checked: Boolean) { v5Checked = checked }
    override fun isChecked(): Boolean { return v5Checked }
    override fun toggle() { v5Toggle() }
    /** 選取狀態 */
    var v5Checked: Boolean
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_checked) ?: false
        set(value) {
            if(v5Checked == value) { return }
            if(this !is HBG5TagImpl) { return }
            val lock = v5GetTag<Boolean>(R.id.attr_checked_lock) ?: false
            if(lock) { return }
            v5SetTag(R.id.attr_checked_lock, true)
            v5SetTag(R.id.attr_checked, value)
            v5GetTag<Callback>(R.id.attr_callback_checked_change)?.onChange(this, value)
            v5GetTag<MutableList<HBG5CheckImpl>>(R.id.attr_bind_child)?.forEach { child ->
                child.v5Checked = value
            }
            v5CheckBind?.v5Checked = value
            v5SetTag(R.id.attr_checked_lock, false)
            (this as? HBG5TextImpl)?.refreshTextState()
            (this as? View)?.refreshDrawableState()
        }
    /** 啟用選取 */
    var v5Checkable: Boolean
        get() = (this as? HBG5TagImpl)?.v5GetTag<Boolean>(R.id.attr_checkable) ?: false
        set(value) { (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_checkable, value) }

    /** 同步選取狀態物件 */
    var v5CheckBind: HBG5CheckImpl?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_bind_owner)
        set(value) {
            // unregister
            (this as? HBG5TagImpl)?.v5GetTag<HBG5TagImpl>(R.id.attr_bind_owner)?.let { owner ->
                owner.v5GetTag<MutableList<HBG5CheckImpl>>(R.id.attr_bind_child)?.remove(this)
            }
            // register
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_bind_owner, value)
            (value as? HBG5TagImpl)?.let { owner ->
                val childList: MutableList<HBG5CheckImpl> = owner.v5GetTag(R.id.attr_bind_child) ?: mutableListOf()
                owner.v5SetTag(
                    R.id.attr_bind_child,
                    childList.onAdd(this))
            }
            // sync status
            if(value != null) {
                v5Checked = value.v5Checked
                v5Checkable = value.v5Checkable
            }
        }

    /** 註冊選取監聽 */
    fun v5RegisterCheckedChange(closure: ((HBG5CheckImpl, Boolean) -> Unit)?) {
        (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_callback_checked_change, when(closure) {
            null -> null
            else -> object : Callback {
                override fun onChange(impl: HBG5CheckImpl, checked: Boolean) {
                    closure(impl, checked)
                }
            }
        })
    }

    /** 觸發選取 */
    fun v5Toggle() {
        if (v5Checkable) {
            v5Checked = when(this) {
                is HBG5RadioButton -> true
                else -> !v5Checked
            }
        }
    }

    /** 更新XML */
    fun buildCheckByAttr(
        typedArray: TypedArray,
        @StyleableRes checked: Int? = null,
        @StyleableRes checkable: Int? = null) {
        v5Checked = checked?.let { typedArray.v5GetBoolean(it) } ?: v5Checked
        v5Checkable = checkable?.let { typedArray.v5GetBoolean(it) } ?: v5Checkable
    }

    private interface Callback {
        fun onChange(impl: HBG5CheckImpl, checked: Boolean)
    }
}