package priv.liten.hbg5_widget.bin.button

import android.content.Context
import android.util.AttributeSet
import priv.liten.hbg5_extension.getDrawable
import priv.liten.hbg5_widget.bin.fragment.HBG5Fragment
import priv.liten.hbg5_widget.impl.fragment.HBG5FragmentImpl
import priv.liten.hbg5_widget.impl.tab.HBG5FragmentTabImpl

open class HBG5NavigationButton : HBG5RadioButton, HBG5FragmentTabImpl {
    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    // MARK:- ====================== Method
    override fun v5LoadData(tab: HBG5Fragment.Tab) {
        v5Text = tab.title
        v5Image = when(val icon = tab.icon) {
            null -> null
            else -> getDrawable(icon)
        }
    }
}