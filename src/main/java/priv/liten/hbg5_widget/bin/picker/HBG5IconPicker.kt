package priv.liten.hbg5_widget.bin.picker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import priv.liten.hbg.R
import priv.liten.hbg5_widget.bin.activity.HBG5LaunchActivity
import priv.liten.hbg5_widget.bin.image.HBG5ImageView
import priv.liten.hbg5_widget.bin.layout.HBG5FrameLayout
import priv.liten.hbg5_widget.bin.list.HBG5ListView

/** 圖示選擇器 */
class HBG5IconPicker : HBG5FrameLayout {
    // MARK:- ====================== Define
    companion object {
        val EDIT_MODE_ICON = mapOf(
            "drawable://hbg5_light_ic_arrow_left" to R.drawable.hbg5_light_ic_arrow_left,
            "drawable://hbg5_light_ic_arrow_right" to R.drawable.hbg5_light_ic_arrow_right,
            "drawable://hbg5_light_ic_arrow_back" to R.drawable.hbg5_light_ic_arrow_back,
            "drawable://hbg5_light_ic_arrow_done" to R.drawable.hbg5_light_ic_arrow_done)
    }


    // MARK:- ====================== Constructor
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int): super(context, attrs, defStyleAttr) {

        inflate(context, R.layout.hbg5_widget_picker_icon, this)

        // Data
        run {
            uiIconListView.v5Adapter = uiIconAdapter
        }

        // Event
        run {

        }

        // Init
        run {
            if(isInEditMode) {
                setIcons(EDIT_MODE_ICON.keys.toList())
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)


    // MARK:- ====================== View
    /**預設色彩清單*/
    private val uiIconListView : HBG5ListView by lazy { findViewById(R.id.list_icon) }
    private val uiIconAdapter = HBG5ListView.Adapter<String>(
        v5CreateHolder = { adapter, parent, type -> IconHolder(parent.context) },
        v5InitHolder = { adapter, holder ->
            when(holder) {
                is IconHolder -> {
                    holder.v5RegisterClick { it?.let {
                        val color = adapter.v5Search(it.bindingAdapterPosition) ?: return@v5RegisterClick
                        onIconClickListener?.let { closure -> closure(color) }
                    } }
                }
            }
        })


    // MARK:- ====================== Event
    /**圖示點擊*/
    fun registerIconClick(closure: ((String) -> Unit)?) {
        onIconClickListener = closure
    }
    private var onIconClickListener: ((String) -> Unit)? = null


    // MARK:- ====================== Method
    /**設定圖示*/
    fun setIcons(list: List<String>) {
        uiIconAdapter.v5List = list
    }


    // MARK:- ====================== Class
    class IconHolder : HBG5ListView.Holder {

        // MARK:- ====================== Constructor
        constructor(context: Context): super(View.inflate(context, R.layout.hbg5_widget_item_icon_picker, null))


        // MARK:- ====================== View
        private val uiIconImage: HBG5ImageView by lazy { findViewById(R.id.image_icon) }


        // MARK:- ====================== Method
        override fun v5LoadData(data: Any?) {

            super.v5LoadData(data)

            when(data) {
                is String -> {
                    if(uiIconImage.isInEditMode) {
                        uiIconImage.v5Image = EDIT_MODE_ICON[data]?.let { id -> ContextCompat.getDrawable(context, id) }
                    }
                    else {
                        uiIconImage.v5AsyncImage(
                            url = data,
                            loading = null,
                            failed = null)
                    }
                }
            }
        }
    }
}