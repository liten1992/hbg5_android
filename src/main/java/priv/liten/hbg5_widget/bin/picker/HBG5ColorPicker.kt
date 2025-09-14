package priv.liten.hbg5_widget.bin.picker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import priv.liten.hbg.R
import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import priv.liten.hbg5_widget.bin.layout.HBG5FrameLayout
import priv.liten.hbg5_widget.bin.list.HBG5ListView

/** 色彩選擇器 */
class HBG5ColorPicker : HBG5FrameLayout {
    // MARK:- ====================== Define
    companion object {
        fun COLOR_LIST(context: Context): List<Int> = listOf(
            R.color.hbg5_white,
            R.color.hbg5_light,
            R.color.hbg5_silver,

            R.color.hbg5_iron,
            R.color.hbg5_dark,
            R.color.hbg5_black,

            R.color.hbg5_red_light,
            R.color.hbg5_red,
            R.color.hbg5_red_dark,

            R.color.hbg5_yellow,
            R.color.hbg5_orange,
            R.color.hbg5_coffee,

            R.color.hbg5_green_light,
            R.color.hbg5_green,
            R.color.hbg5_green_dark,

            R.color.hbg5_cyan,
            R.color.hbg5_turquoise,
            R.color.hbg5_teal,

            R.color.hbg5_blue_light,
            R.color.hbg5_blue,
            R.color.hbg5_blue_dark,

            R.color.hbg5_violet,
            R.color.hbg5_purple,
            R.color.hbg5_pansy,

            R.color.hbg5_orchid,
            R.color.hbg5_camellia,
            R.color.hbg5_carmine
        )
            .map { id -> context.getColor(id) }
    }

    // MARK:- ====================== Constructor
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes) {

        inflate(context, R.layout.hbg5_widget_picker_color, this)

        // Data
        run {
            uiColorListView.v5Adapter = uiColorAdapter
            uiColorAdapter.v5List = COLOR_LIST(context)
        }

        // Event
        run {

        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)


    // MARK:- ====================== View
    /**預設色彩清單*/
    private val uiColorListView : HBG5ListView by lazy { findViewById(R.id.list_color) }
    private val uiColorAdapter = HBG5ListView.Adapter<Int>(
        v5CreateHolder = { adapter, parent, type -> ColorHolder(parent.context) },
        v5InitHolder = { adapter, holder ->
            when(holder) {
                is ColorHolder -> {
                    holder.v5RegisterClick { it?.let {
                        val color = adapter.v5Search(it.bindingAdapterPosition) ?: return@v5RegisterClick
                        onColorClickListener?.let { closure -> closure(color) }
                    } }
                }
            }
        })


    // MARK:- ====================== Event
    /**色彩點擊*/
    fun registerColorClick(closure: ((Int) -> Unit)?) {
        onColorClickListener = closure
    }
    private var onColorClickListener: ((Int) -> Unit)? = null


    // MARK:- ====================== Method
    /**設置色彩*/
    fun setColors(list: List<Int>) {
        uiColorAdapter.v5List = list
    }


    // MARK:- ====================== Class
    class ColorHolder : HBG5ListView.Holder {

        // MARK:- ====================== Constructor
        constructor(context: Context): super(View.inflate(context, R.layout.hbg5_widget_item_color_picker, null))


        // MARK:- ====================== View
        private val uiColorImage: HBG5FrameLayout by lazy { findViewById(R.id.image_color) }


        // MARK:- ====================== Method
        override fun v5LoadData(data: Any?) {

            super.v5LoadData(data)

            when(data) {
                is Int -> {
                    uiColorImage.v5BackgroundColor = data
                }
            }
        }
    }
}