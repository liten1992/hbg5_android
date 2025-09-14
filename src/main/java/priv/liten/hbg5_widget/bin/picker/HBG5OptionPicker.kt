package priv.liten.hbg5_widget.bin.picker

import android.content.Context
import android.util.AttributeSet
import android.widget.NumberPicker
import priv.liten.hbg.R
import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import kotlin.math.max

/**選項選擇器*/
class HBG5OptionPicker: NumberPicker {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, android.R.attr.numberPickerStyle)

    constructor(context: Context): this(context, null)

    var v5Items: List<String> = emptyList()
        set(value) {
            field = value
            this.minValue = 0
            this.maxValue = max(0, field.size - 1)
            this.displayedValues = field.toTypedArray()
        }

    var v5SelectedIndex: Int
        get() {
            return this.value
        }
        set(value) {
            this.value = value
        }
}