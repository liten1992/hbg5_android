package priv.liten.hbg5_widget.impl.image

import android.annotation.SuppressLint
import android.content.res.TypedArray
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.StyleableRes
import androidx.core.text.isDigitsOnly
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware
import priv.liten.hbg.R
import priv.liten.hbg5_extension.hbg5.v5GetColor
import priv.liten.hbg5_extension.hbg5.v5GetDrawable
import priv.liten.hbg5_extension.hbg5.v5GetInt
import priv.liten.hbg5_widget.bin.image.HBG5ImageView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.drawable.HBG5EmptyDrawable
import priv.liten.hbg5_widget.impl.base.HBG5BackgroundImpl
import priv.liten.hbg5_widget.impl.base.HBG5ClickImpl
import priv.liten.hbg5_widget.impl.base.HBG5TagImpl
import priv.liten.hbg5_widget.impl.base.HBG5ViewImpl

interface HBG5ImageViewImpl:
    HBG5ViewImpl,
    HBG5ClickImpl,
    HBG5BackgroundImpl {

    /** 圖片 預設 */
    var v5Image: Drawable?
        get() = this.v5GetTag(R.id.attr_image_normal)
        set(value) {
            this.v5SetTag(R.id.attr_image_normal, value)
            refreshImage()
        }
    /** 圖片 按下 */
    var v5ImagePressed: Drawable?
        get() = this.v5GetTag(R.id.attr_image_pressed)
        set(value) {
            this.v5SetTag(R.id.attr_image_pressed, value)
            refreshImage()
        }
    /** 圖片 選取 */
    var v5ImageChecked: Drawable?
        get() = this.v5GetTag(R.id.attr_image_checked)
        set(value) {
            this.v5SetTag(R.id.attr_image_checked, value)
            refreshImage()
        }
    /** 圖片 未啟用*/
    var v5ImageUnable: Drawable?
        get() = this.v5GetTag(R.id.attr_image_unable)
        set(value) {
            this.v5SetTag(R.id.attr_image_unable, value)
            refreshImage()
        }

    /** 圖片著色 null: 不套用 */
    var v5ImageTint: Int?
        get() = this.v5GetTag(R.id.attr_image_tint_normal)
        set(value) {
            this.v5SetTag(R.id.attr_image_tint_normal, value)
            refreshImage()
        }
    /** 圖片著色 null: 不套用 */
    var v5ImageTintPressed: Int?
        get() = this.v5GetTag(R.id.attr_image_tint_pressed)
        set(value) {
            this.v5SetTag(R.id.attr_image_tint_pressed, value)
            refreshImage()
        }
    /** 圖片著色 null: 不套用 */
    var v5ImageTintChecked: Int?
        get() = this.v5GetTag(R.id.attr_image_tint_checked)
        set(value) {
            this.v5SetTag(R.id.attr_image_tint_checked, value)
            refreshImage()
        }
    /** 圖片著色 null: 不套用 */
    var v5ImageTintUnable: Int?
        get() = this.v5GetTag(R.id.attr_image_tint_unable)
        set(value) {
            this.v5SetTag(R.id.attr_image_tint_unable, value)
            refreshImage()
        }

    /** 圖片填充方式 */
    var v5ImageScaleType: HBG5WidgetConfig.Attrs.ImageScaleType
        get() = (this as? ImageView)?.let { return HBG5WidgetConfig.Attrs.ImageScaleType.fromUIType(scaleType) } ?: HBG5WidgetConfig.Attrs.ImageScaleType.Fit
        set(value) { (this as? ImageView)?.scaleType = value.value }

    /** 異步加載圖片 */
    @SuppressLint("DiscouragedApi")
    fun v5AsyncImage(
        headers: Map<String, String> = emptyMap(),
        url: String?,
        loading: Drawable?,
        failed: Drawable?,
        quality: HBG5ImageView.Quality = HBG5ImageView.Quality.MEDIUM) {

        if(this !is HBG5ImageView) { return }

        val defaultImage = HBG5EmptyDrawable()

        val options = DisplayImageOptions
            .Builder()
            .cloneFrom(HBG5WidgetConfig.IMAGE_OPTIONS_BUILDER.build())
            .extraForDownloader(headers)
            .showImageOnLoading(loading ?: defaultImage)
            .showImageOnFail(failed ?: defaultImage)
            .showImageForEmptyUri(failed ?: defaultImage)
            .build()

        val imageMaxSize = ImageSize(500 * quality.lv, 500 * quality.lv)

        val schema = "drawable://"
        if(url != null && url.startsWith(schema, true)) {

            val fileName = url.substring(schema.length)
            val drawableId =
                if(fileName.isDigitsOnly()) fileName.toIntOrNull() ?: 0
                else HBG5ImageView.DRAWABLE_CACHE[fileName] ?: run {
                    val id = resources.getIdentifier(fileName, "drawable", context.packageName)
                    HBG5ImageView.DRAWABLE_CACHE[fileName] = id
                    id
                }
            // 取消此物件正在下載的圖片引用關係
            HBG5WidgetConfig
                .imageLoader(context)
                .displayImage(
                    "",
                    ImageViewAware(this),
                    options,
                    imageMaxSize,
                    null,
                    null
                )
            // 設置系統資源
            setImageResource(drawableId)

            return
        }

        HBG5WidgetConfig
            .imageLoader(context)
            .displayImage(
                url,
                ImageViewAware(this),
                options,
                imageMaxSize,
                null,
                null
            )
    }

    /** 更新XML */
    fun buildImageByAttr(
        typedArray: TypedArray,
        @StyleableRes imageNormal: Int? = null,
        @StyleableRes imagePressed: Int? = null,
        @StyleableRes imageChecked: Int? = null,
        @StyleableRes imageUnable: Int? = null,
        @StyleableRes imageTintNormal: Int? = null,
        @StyleableRes imageTintPressed: Int? = null,
        @StyleableRes imageTintChecked: Int? = null,
        @StyleableRes imageTintUnable: Int? = null,
        @StyleableRes imageScaleType: Int? = null) {

        v5Image = imageNormal?.let { typedArray.v5GetDrawable(it) }
        v5ImagePressed = imagePressed?.let { typedArray.v5GetDrawable(it) }
        v5ImageChecked = imageChecked?.let { typedArray.v5GetDrawable(it) }
        v5ImageUnable = imageUnable?.let { typedArray.v5GetDrawable(it) }
        v5ImageTint = imageTintNormal?.let {  typedArray.v5GetColor(it) }
        v5ImageTintPressed = imageTintPressed?.let {  typedArray.v5GetColor(it) }
        v5ImageTintChecked = imageTintChecked?.let {  typedArray.v5GetColor(it) }
        v5ImageTintUnable = imageTintUnable?.let {  typedArray.v5GetColor(it) }
        v5ImageScaleType = imageScaleType?.let { HBG5WidgetConfig.Attrs.ImageScaleType.fromAttr(typedArray.v5GetInt(it)) } ?: v5ImageScaleType
    }

    /**更新圖像*/
    fun refreshImage() {
        if(this !is ImageView) { return }
        if((this as? HBG5ViewImpl)?.v5AttrCompleted != true) { return }
        val stateImage = StateListDrawable()
        var hasImage = false
        val tintMode = PorterDuff.Mode.SRC_IN

        val tintDrawableBuilder: (Int?, Drawable) -> Drawable = { color, drawable ->
            when(color) {
                null -> drawable
                else -> drawable.mutate().apply { colorFilter = PorterDuffColorFilter(color, tintMode) }
            }
        }

        v5ImageChecked?.let { drawable ->
            hasImage = true
            stateImage.addState(
                intArrayOf(android.R.attr.state_checked),
                tintDrawableBuilder(v5ImageTintChecked, drawable))
        }
        v5ImageUnable?.let { drawable ->
            hasImage = true
            stateImage.addState(
                intArrayOf(-android.R.attr.state_enabled),
                tintDrawableBuilder(v5ImageTintUnable, drawable))
        }
        v5ImagePressed?.let { drawable ->
            hasImage = true
            stateImage.addState(
                intArrayOf(android.R.attr.state_pressed),
                tintDrawableBuilder(v5ImageTintPressed, drawable))
        }
        v5Image?.let { drawable ->
            hasImage = true
            stateImage.addState(
                intArrayOf(),
                tintDrawableBuilder(v5ImageTint, drawable))
        }

        if(!this.isInEditMode) {
            v5AsyncImage(url = "", loading = null, failed = null)
        }

        if(hasImage) {
            setImageDrawable(stateImage)
        }
        else {
            setImageDrawable(null)
        }
    }
}