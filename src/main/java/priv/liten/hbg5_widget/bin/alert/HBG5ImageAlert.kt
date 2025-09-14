package priv.liten.hbg5_widget.bin.alert

import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import priv.liten.hbg.R
import priv.liten.hbg5_extension.dpToPx
import priv.liten.hbg5_extension.hideKeyboard
import priv.liten.hbg5_widget.bin.image.HBG5ImageButton
import priv.liten.hbg5_widget.bin.image.HBG5ImageView
import priv.liten.hbg5_widget.bin.text.HBG5TextView
import priv.liten.hbg5_widget.config.HBG5WidgetConfig
import priv.liten.hbg5_widget.impl.alert.HBG5ImageAlertImpl
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class HBG5ImageAlert: HBG5BaseAlert, HBG5ImageAlertImpl {

    // MARK:- ====================== Define
    companion object {
        // 無
        const val MODE_NONE  = 0
        // 位移
        const val MODE_TRANS = 1
        // 縮放
        const val MODE_SCALE = 2
    }

    // MARK:- ====================== Constructor
    constructor(context: Context): super(context = context) {
        // Root
        run {
            inflate(context, R.layout.hbg5_widget_alert_image, this)
        }

        // Event
        run {
            // onTouchScaleImage
            imageTouchView.setOnTouchListener { view, motionEvent ->

                val image = imageView.drawable ?: return@setOnTouchListener true

                val imageWidth = image.intrinsicWidth
                val imageHeight = image.intrinsicHeight

                val imageVWidth = imageView.width
                val imageVHeight = imageView.height

                val imageScale = imageWidth.toFloat() / imageHeight.toFloat()
                val viewScale = imageView.width.toFloat() / imageView.height.toFloat()

                if (imageWidth <= 0 || imageHeight <= 0) { return@setOnTouchListener true }

                when (motionEvent.action.and(MotionEvent.ACTION_MASK)) {
                    // First Down
                    MotionEvent.ACTION_DOWN -> {

                        touchMode = MODE_NONE

                        // imageFillSize
                        run {
                            // 圖片較寬
                            if (imageScale > viewScale) {
                                imageSize[0] = imageVWidth
                                imageSize[1] = (imageVWidth / imageScale).toInt()
                            }
                            // 圖片較長
                            else {
                                imageSize[0] = (imageVHeight * imageScale).toInt()
                                imageSize[1] = imageVHeight
                            }
                        }

                        imageStartTrans.x = imageView.translationX
                        imageStartTrans.y = imageView.translationY

                        val index = motionEvent.actionIndex
                        downPointId0 = motionEvent.getPointerId(index)
                        downPoint0.set(motionEvent.getX(index), motionEvent.getY(index))
                    }

                    // Second Down
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        if (motionEvent.pointerCount != 2) {
                            return@setOnTouchListener true
                        }
                        if (touchMode != MODE_NONE) {
                            return@setOnTouchListener true
                        }
                        touchMode = MODE_SCALE

                        imageStartScale = imageView.scaleX

                        val index = motionEvent.actionIndex
                        downPointId1 = motionEvent.getPointerId(index)
                        downPoint1.set(motionEvent.getX(index), motionEvent.getY(index))
                    }

                    // Move
                    MotionEvent.ACTION_MOVE -> {

                        var newX = imageView.translationX
                        var newY = imageView.translationY
                        var newScale = imageView.scaleX

                        when (touchMode) {
                            MODE_NONE -> {
                                downPointId0?.let {
                                    val index = motionEvent.findPointerIndex(it)

                                    if (index != -1) {
                                        val x = motionEvent.getX(index)
                                        val y = motionEvent.getY(index)

                                        if (abs(downPoint0.x - x) > touchEnableLimit ||
                                            abs(downPoint0.y - y) > touchEnableLimit) {

                                            touchMode = MODE_TRANS

                                            downPoint0.x = x
                                            downPoint0.y = y
                                        }
                                    }
                                }
                            }
                            MODE_TRANS -> {
                                downPointId0?.let {
                                    val index = motionEvent.findPointerIndex(it)

                                    if (index != -1) {
                                        newX = imageStartTrans.x + motionEvent.getX(index) - downPoint0.x
                                        newY = imageStartTrans.y + motionEvent.getY(index) - downPoint0.y
                                    }
                                }
                            }
                            MODE_SCALE -> {

                                val index0 = motionEvent.findPointerIndex(downPointId0!!)
                                val index1 = motionEvent.findPointerIndex(downPointId1!!)

                                if (index0 != -1 && index1 != -1) {
                                    val oriSpace = abs(downPoint0.x - downPoint1.x) +
                                            abs(downPoint0.y - downPoint1.y)
                                    val tchSpace =
                                        abs(motionEvent.getX(index0) - motionEvent.getX(index1)) +
                                                abs(motionEvent.getY(index0) - motionEvent.getY(index1))

                                    newScale = min(
                                        max(
                                            imageStartScale + ((tchSpace - oriSpace) / touchEnableLimit * 0.25f),
                                            1f), 4f)
                                }
                            }
                        }

                        when (touchMode) {
                            MODE_TRANS,
                            MODE_SCALE -> {

                                run {
                                    val wLimit = max((imageSize[0]*newScale-imageVWidth)* 0.5f, 0f)
                                    newX = min(newX,  wLimit)
                                    newX = max(newX, -wLimit)

                                    val hLimit = max((imageSize[1]*newScale-imageVHeight)* 0.5f, 0f)
                                    newY = min(newY,  hLimit)
                                    newY = max(newY, -hLimit)
                                }

                                imageView.translationX = newX
                                imageView.translationY = newY

                                imageView.scaleX = newScale
                                imageView.scaleY = newScale
                            }
                        }
                    }

                    // All Up
                    MotionEvent.ACTION_UP -> {
                        if(touchMode == MODE_NONE) {
                            view.performClick()
                        }
                    }
                }

                return@setOnTouchListener true
            }

            // onBack
            backButton.v5RegisterClick { _ -> v5Cancel() }

            // onImageChange
            flipButtons.forEach { button -> button.v5RegisterClick {
                // 無照片資料，不進行翻頁
                val size = dataRequest?.imageList?.size ?: return@v5RegisterClick
                if(size <= 1) { return@v5RegisterClick }

                when(flipButtons.indexOf(button)) {
                    0->onImageChange(-1)
                    1->onImageChange( 1)
                }
            } }
        }
    }


    // MARK:- ====================== View
    /** 返回按鈕 */
    private val backButton: HBG5ImageButton by lazy { findViewById(R.id.button_back) }
    /** 圖像觸控元件 */
    private val imageTouchView: View by lazy { findViewById(R.id.layout_image) }
    /** 圖像顯示元件 */
    private val imageView: HBG5ImageView by lazy { findViewById(R.id.image_content) }
    /** 翻頁按鈕 */
    private val flipButtons: Array<HBG5ImageButton> by lazy { arrayOf(
            findViewById(R.id.button_priv),
            findViewById(R.id.button_next)) }
    /** 頁碼元件 */
    private val pageNumberView: HBG5TextView by lazy { findViewById(R.id.text_page) }


    // MARK:- ====================== Data
    private var touchMode = MODE_NONE
    // 第一點按下位置
    private var downPoint0 = PointF(0f, 0f)
    // 第二點按下位置
    private var downPoint1 = PointF(0f, 0f)

    private var downPointId0 : Int? = null
    private var downPointId1 : Int? = null

    // 圖片起始位置位移座標
    private var imageStartTrans = PointF(0f, 0f)
    // 圖片起始縮放比例
    private var imageStartScale = 1f
    // 圖片大小
    private var imageSize = arrayOf(0, 0)
    // 圖片開始進行動作的觸控臨界值
    private val touchEnableLimit : Float by lazy { 20f.dpToPx() }

    private val cacheImageMap : MutableMap<String, Drawable?> = mutableMapOf()

    private var dataRequest : DataRequest? = null
    private val dataResponse = DataResponse()

    // MARK:- ====================== Event
    override fun v5OnShow() {
        super.v5OnShow()
        hideKeyboard()
    }
    override fun v5OnHide() {
        super.v5OnHide()
        onBackListener = null
        cacheImageMap.clear()
    }



    private var onBackListener: (() -> Unit)? = null
    override fun v5RegisterBack(listener: (() -> Unit)?) {
        onBackListener = listener
    }

    private fun onImageChange(add:Int) {

        val imageView = imageView

        resetImageView()

        val dataRequest = dataRequest ?: return

        val size = dataRequest.imageList.size

        dataResponse.index =
            if(size > 0) (dataResponse.index + add + size) % size
            else 0

        refreshPageText()

        imageView.setImageDrawable(null)

        when(val imageObject = dataRequest.imageList.getOrNull(dataResponse.index)) {
            is String -> {
                imageView.v5AsyncImage(
                    headers = dataRequest.imageHeaders,
                    url = imageObject,
                    loading = null,
                    failed = null)
            }
            is Drawable -> {
                imageView.setImageDrawable(imageObject)
            }
            else -> { }
        }
    }


    // MARK:- ====================== Method
    override fun v5LoadRequest(request: HBG5BaseAlert.DataRequest) {
        super.v5LoadRequest(request)

        var flipVisibility = HBG5WidgetConfig.Attrs.Visibility.Invisible

        (request as? DataRequest)
            ?.let { dataRequest ->
                this.dataRequest = dataRequest
                this.dataResponse.index = dataRequest.index

                flipVisibility =
                    if(dataRequest.imageList.size <= 1) HBG5WidgetConfig.Attrs.Visibility.Invisible
                    else HBG5WidgetConfig.Attrs.Visibility.Visible

            }
            ?:run { }

        // Update UI
        pageNumberView.v5Visibility = flipVisibility
        flipButtons.forEach { it.v5Visibility = flipVisibility }

        onImageChange(0)
    }

    override fun v5Confirm() {
        onBackListener?.let { it() }
        super.v5Confirm()
    }

    override fun v5Cancel() { v5Confirm() }

    override fun createShowAnimation() : Animation {

        val amSet = AnimationSet(false)
        amSet.duration = 150

        run {
            val am = AlphaAnimation(0f, 1f)
            am.interpolator = LinearInterpolator()

            amSet.addAnimation(am)
        }

        return amSet
    }

    /** 重置圖像顯示的位置與縮放 */
    private fun resetImageView() {
        val imageView = imageView
        imageView.scaleX = 1f
        imageView.scaleY = 1f
        imageView.translationX = 0f
        imageView.translationY = 0f
    }
    /** 更新顯示頁碼 */
    private fun refreshPageText() {

        val emptyText = "0 / 0"

        val dataRequest = dataRequest ?: run {
            pageNumberView.text = emptyText
            return
        }

        pageNumberView.text = if(dataRequest.imageList.isNotEmpty())
            String.format("%d / %d", dataResponse.index + 1, dataRequest.imageList.size) else
            emptyText
    }

    // MARK:- ====================== Class
    open class DataRequest: HBG5BaseAlert.DataRequest() {

        var index = 0

        var imageHeaders : MutableMap<String,String> = mutableMapOf()

        var imageList : MutableList<Any> = mutableListOf()
    }
    open class DataResponse: HBG5BaseAlert.DataResponse() {
        var index = 0
    }
}