package priv.liten.hbg5_widget.impl.base

import android.view.View
import androidx.annotation.IdRes
import priv.liten.hbg5_widget.bin.list.HBG5ListView

interface HBG5TagImpl {
    fun <T> v5GetTag(@IdRes id: Int): T? {
        return when(this) {
            is HBG5ListView.Holder -> tag[id] as? T
            is View -> getTag(id) as? T
            else -> null
        }
    }
    fun v5SetTag(@IdRes id: Int, data: Any?) {
        when(this) {
            is HBG5ListView.Holder -> tag[id] = data
            is View -> setTag(id, data)
            else -> { }
        }
    }
}