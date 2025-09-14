package priv.liten.hbg5_widget.impl.button

import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.widget.TextView
import androidx.annotation.StyleableRes
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetDimensionPixelSize
import priv.liten.hbg5_extension.hbg5.v5GetDrawable
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.drawable.HBG5PanelDrawable
import priv.liten.hbg5_widget.impl.base.HBG5TagImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl
import priv.liten.hbg5_widget_impl.impl.text.HBG5TextViewImpl

interface HBG5ButtonImpl : HBG5TextViewImpl {
    /** 內容圖片-含狀態機 */
    var v5Images: Drawable?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image, value)
            refreshButton()
        }
    /** 內容圖片-預設 */
    var v5Image: Drawable?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image_normal)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image_normal, value)
            refreshButton()
        }
    /** 內容圖片-按下 */
    var v5ImagePressed: Drawable?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image_pressed)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image_pressed, value)
            refreshButton()
        }
    /** 內容圖片-選取 */
    var v5ImageChecked: Drawable?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image_checked)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image_checked, value)
            refreshButton()
        }

    /** 內容圖片-對齊方法 */
    var v5ImageAlignment: HBG5WidgetConfig.Attrs.ImageAlignment
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image_alignment) ?: HBG5WidgetConfig.Attrs.ImageAlignment.Start
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image_alignment, value)
            refreshButton()
        }

    /** 內容圖片寬度 ( > 0 || null ) */
    var v5ImageWidth: Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image_width)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image_width, value)
            refreshButton()
        }

    /** 內容圖片高度 ( > 0 || null ) */
    var v5ImageHeight: Int?
        get() = (this as? HBG5TagImpl)?.v5GetTag(R.id.attr_image_height)
        set(value) {
            (this as? HBG5TagImpl)?.v5SetTag(R.id.attr_image_height, value)
            refreshButton()
        }

    /** 圖片與內容間距 */
    var v5ImagePadding: Int
        get() = (this as? TextView)?.compoundDrawablePadding ?: 0
        set(value) {
            if(this !is TextView) { return }
            this.compoundDrawablePadding = value
        }

    /** 圖片著色 null: 不套用 */
    var v5ImageTint: Int?
        get() = null
        set(value) { }

    /** 圖片著色 null: 不套用 */
    var v5ImageTintPressed: Int?
        get() = null
        set(value) { }

    /** 圖片著色 null: 不套用 */
    var v5ImageTintChecked: Int?
        get() = null
        set(value) { }

    /** 更新XML */
    fun buildButtonByAttr(
        typedArray: TypedArray,
        @StyleableRes images: Int? = null,
        @StyleableRes imageNormal: Int? = null,
        @StyleableRes imagePressed: Int? = null,
        @StyleableRes imageChecked: Int? = null,
        @StyleableRes imageAlignment: Int? = null,
        @StyleableRes imageWidth: Int? = null,
        @StyleableRes imageHeight: Int? = null,
        @StyleableRes imagePadding: Int? = null,
        @StyleableRes imageTintNormal: Int? = null,
        @StyleableRes imageTintPressed: Int? = null,
        @StyleableRes imageTintChecked: Int? = null) {
        v5Images = images?.let { typedArray.v5GetDrawable(it) }
        v5Image = imageNormal?.let { typedArray.v5GetDrawable(it) }
        v5ImagePressed = imagePressed?.let { typedArray.v5GetDrawable(it) }
        v5ImageChecked = imageChecked?.let { typedArray.v5GetDrawable(it) }
        v5ImageAlignment = imageAlignment?.let { HBG5WidgetConfig.Attrs.ImageAlignment.fromAttr(typedArray.v5GetInt(it)) } ?: HBG5WidgetConfig.Attrs.ImageAlignment.Start
        v5ImageWidth = imageWidth?.let { typedArray.v5GetDimensionPixelSize(it) }
        v5ImageHeight = imageHeight?.let { typedArray.v5GetDimensionPixelSize(it) }
        v5ImagePadding = imagePadding?.let { typedArray.v5GetDimensionPixelSize(it) } ?: 0
//        v5ImageTint = imageTintNormal?.let {  }
//        v5ImageTintPressed = imageTintPressed?.let {  }
//        v5ImageTintChecked = imageTintChecked?.let {  }
    }

    fun refreshButton() {
        if(this !is TextView) { return }
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }

        var hasImage = false

        val imageWidth = v5ImageWidth
        val imageHeight = v5ImageHeight
        val images = v5Images

        val imageAll =
            if(images != null) {
                hasImage = true
//                images
                HBG5PanelDrawable(
                    content = images,
                    intrinsicWidth = imageWidth,
                    intrinsicHeight = imageHeight
                )
            }
            else StateListDrawable().let { imageAll ->
                val imageNormal = v5Image
                val imagePressed = v5ImagePressed
                val imageChecked = v5ImageChecked
                imageChecked?.let {
                    hasImage = true
                    imageAll.addState(
                        intArrayOf(android.R.attr.state_checked),
                        it
                    )
                }
                imagePressed?.let {
                    hasImage = true
                    imageAll.addState(
                        intArrayOf(android.R.attr.state_pressed),
                        it
                    )
                }
                imageNormal?.let {
                    hasImage = true
                    imageAll.addState(
                        intArrayOf(),
                        it
                    )
                }
                HBG5PanelDrawable(
                    content = imageAll,
                    intrinsicWidth = imageWidth,
                    intrinsicHeight = imageHeight
                )
            }


        if(!hasImage) {
            this.setCompoundDrawables(null, null, null, null)
            return
        }

        when(v5ImageAlignment) {
            HBG5WidgetConfig.Attrs.ImageAlignment.Start -> {
                this.setCompoundDrawablesRelativeWithIntrinsicBounds(imageAll, null, null, null)
            }
            HBG5WidgetConfig.Attrs.ImageAlignment.End -> {
                this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, imageAll, null)
            }
            HBG5WidgetConfig.Attrs.ImageAlignment.Top -> {
                this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, imageAll, null, null)
            }
            HBG5WidgetConfig.Attrs.ImageAlignment.Bottom -> {
                this.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, imageAll)
            }
        }
    }
}

