package priv.liten.hbg5_widget.bin.sign

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import priv.liten.hbg5_extension.dpToPx
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**簽名元件
 * 使用save保存並提取當前的簽名圖像
 * 使用clear清除當前的簽名圖像
 * */
class HBG5SignView: View {
    // MARK:- ====================== Constructor
    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        // 強制元件執行繪圖
        setWillNotDraw(false)
    }


    // MARK:- ====================== Data
    /**簽名路徑*/
    val signPath = Path()
    /**簽名路徑集合*/
    val signPathRecord = mutableListOf<Path>()

    /**簽名畫筆*/
    val signPaint = Paint().also { paint ->
        paint.color = Color.BLACK
        paint.strokeWidth = 4f.dpToPx()
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = false // 抗鋸齒 繪製時取消提升效能
    }

    /**繪製坐標上次點X,Y*/
    val signLastPoint = floatArrayOf(0f, 0f)

    /**繪製坐標左上角值*/
    var signMaxPointLT: FloatArray? = null

    /**繪製坐標右下角值*/
    var signMaxPointRB: FloatArray? = null

    /**簽名畫布*/
    var signBitmap: Bitmap? = null

    /**簽名畫布操作層*/
    var signCanvas: Canvas? = null

    /**簽名路徑紀錄閾值*/
    val signRecordLimit: Float by lazy { 4f.dpToPx() }

    /**簽名圖片背景色*/
    val signBackgroundColor: Int = Color.WHITE


    // MARK:- ====================== Event
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if(event == null) { return false }
        val width = width
        val height = height
        if(width <= 0 || height <= 0) { return false }

        val touchX = event.x
        val touchY = event.y

        // 單指觸控
        when (event.action.and(MotionEvent.ACTION_MASK)) {
            // Down
            MotionEvent.ACTION_DOWN -> {
                // 畫布初始化檢查，原先規劃在onSizeChanged
                val bitmapWidth = signBitmap?.width ?: 0
                val bitmapHeight = signBitmap?.height ?: 0
                // 畫布範圍比已生成圖像範圍大 重新生成
                if(width > bitmapWidth || height > bitmapHeight) {
                    // 移除舊的繪製圖像
                    signBitmap?.recycle()
                    signBitmap = null
                    signCanvas = null
                    // 生成新的繪製圖像 補上歷史繪製筆跡
                    val bitmapMax = max(width, height)
                    val newBitmap = Bitmap.createBitmap(bitmapMax, bitmapMax, Bitmap.Config.ARGB_8888).also { bitmap ->
                        signCanvas = Canvas(bitmap).also { canvas ->
                            canvas.drawColor(signBackgroundColor)
                            signPaint.isAntiAlias = true // 抗鋸齒
                            for(path in signPathRecord) {
                                canvas.drawPath(path, signPaint)
                            }
                        }
                        // 保存
                        signCanvas?.drawPath(signPath, signPaint.also { paint -> paint.isAntiAlias = true })
                        // 塗銷路徑紀錄
                        signPath.reset()
                    }
                    signBitmap = newBitmap
                }

                signBitmap ?: return true
                signCanvas ?: return true

                signLastPoint[0] = touchX
                signLastPoint[1] = touchY
                // 更新繪製起點
                signPaint.isAntiAlias = false
                signPath.reset()
                signPath.moveTo(touchX, touchY)
                // 保存繪製範圍
                saveMaxPoint(x = touchX, y = touchY)
            }
            // Move
            MotionEvent.ACTION_MOVE -> {
                // 不滿閾值不紀錄
                if (abs(touchX - signLastPoint[0]) < signRecordLimit && abs(touchY - signLastPoint[1]) < signRecordLimit) {
                    return true
                }
                // 更新繪製點
                val lastX = signLastPoint[0]
                val lastY = signLastPoint[1]
                signLastPoint[0] = touchX
                signLastPoint[1] = touchY
                // 實現平滑曲線；lastX, lastY 為操作點，centerX, centerY為終點
                signPath.quadTo(lastX, lastY, (lastX + touchX) * 0.5f, (lastY + touchY) * 0.5f)
                // 繪製
                invalidate()
                // 保存繪製範圍
                saveMaxPoint(x = touchX, y = touchY)
            }
            // Up
            MotionEvent.ACTION_UP,
            // Cancel
            MotionEvent.ACTION_CANCEL -> {
                // 保存
                signPathRecord.add(Path(signPath))
                signCanvas?.drawPath(signPath, signPaint.also { paint -> paint.isAntiAlias = true })
                // 塗銷臨時路徑紀錄
                signPath.reset()
                // 繪製
                invalidate()
                // 保存繪製範圍
                saveMaxPoint(x = touchX, y = touchY)
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(canvas == null) { return }
        // 繪製已記錄簽名圖片
        signBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, signPaint)
        }
        // 繪製快取簽名路徑
        canvas.drawPath(signPath, signPaint)
    }


    // MARK:- ====================== Method
    /**保存*/
    fun save(watermark: String? = null): Bitmap? {
        //拷貝新的點陣圖檔
        val signMaxPointLT = signMaxPointLT ?: return null
        val signMaxPointRB = signMaxPointRB ?: return null
        val signBitmap = signBitmap ?: return null

        // 計算邊界
        val signPadding = signPaint.strokeWidth

        val clipSX = max((signMaxPointLT[0] - signPadding), 0f)
        val clipSY = max((signMaxPointLT[1] - signPadding), 0f)
        val clipEX = min((signMaxPointRB[0] + signPadding), signBitmap.width.toFloat())
        val clipEY = min((signMaxPointRB[1] + signPadding), signBitmap.height.toFloat())

        val signWidth = clipEX - clipSX
        val signHeight = clipEY - clipSY
        val signSize = max(max(signWidth, signHeight), 192f.dpToPx()) // 避免繪製圖片過小

        // 簽名圖像尺寸不存在
        if(signWidth <= 0 || signHeight <= 0) { return null }

        // 創建新畫布
        return Bitmap.createBitmap(
            signSize.toInt(),
            signSize.toInt(),
            Bitmap.Config.ARGB_8888).also { bitmap ->

            var drawSX = 0f
            var drawSY = 0f

            if(signSize > signHeight) {
                drawSY = (signSize - signHeight) * 0.5f
            }
            if(signSize > signWidth) {
                drawSX = (signSize - signWidth) * 0.5f
            }
            // 繪製作業
            Canvas(bitmap).let { canvas ->
                // 被景色
                canvas.drawColor(signBackgroundColor)
                // 僅繪簽名
                canvas.drawBitmap(
                    signBitmap,
                    Rect(
                        clipSX.toInt(),
                        clipSY.toInt(),
                        clipEX.toInt(),
                        clipEY.toInt()
                    ),
                    RectF(
                        drawSX,
                        drawSY,
                        drawSX + signWidth,
                        drawSY + signHeight),
                    signPaint)
                // 繪製浮水印
                watermark?.let { mark ->
                    val markPaint = Paint().also { paint ->
                        paint.isAntiAlias = true
                        paint.textSize = signSize * 0.0625f
                        paint.color = 0x44cc0000
                    }
                    val markWidth = markPaint.measureText(mark)
                    val markXPadding = 64f.dpToPx()
                    val markYPadding = 64f.dpToPx()
                    val markHeight = -markPaint.ascent() + markPaint.descent()
                    val angleSignSize = signSize * 1.5f

                    val markLineSize = (angleSignSize / (markWidth + markXPadding)).toInt() + 1
                    var markLineY = 0f
                    var markLineIndex = 0

                    // 旋轉45度
                    canvas.rotate(-45f, signSize * 0.5f, signSize * 0.5f)

                    // 繪製浮水印
                    while (true) {

                        for(i in 0 until markLineSize) {
                            canvas.drawText(mark, i * (markWidth + markXPadding), markLineY + markHeight, markPaint)
                        }

                        markLineY += (markHeight + markYPadding)
                        markLineIndex += 1

                        if(markLineY >= angleSignSize) { break }
                        // 不斷移動畫布來達成浮水印文字交替效果
                        canvas.translate(
                            (markWidth + markXPadding) * 0.5f * if(markLineIndex % 2 != 0) -1f else 1f,
                            0f)
                    }
                }
            }
        }
    }
    /**清除*/
    fun clear() {
        signMaxPointLT = null
        signMaxPointRB = null
        signPathRecord.clear()
        signPath.reset()
        signCanvas?.drawColor(signBackgroundColor)
        invalidate()
    }
    /**保存最大繪製範圍*/
    fun saveMaxPoint(x: Float, y: Float) {
        val bitmapWidth = signBitmap?.width?.toFloat() ?: return
        val bitmapHeight = signBitmap?.height?.toFloat() ?: return
        // 禁止繪製坐標超出邊界
        val rangeX = min(max(0f, x), bitmapWidth)
        val rangeY = min(max(0f, y), bitmapHeight)

        // 初始化
        if(signMaxPointLT == null || signMaxPointRB == null) {
            signMaxPointLT = floatArrayOf(rangeX, rangeY)
            signMaxPointRB = floatArrayOf(rangeX, rangeY)
        }
        // 範圍比較
        else {
            if(rangeX < signMaxPointLT!![0]) {
                signMaxPointLT!![0] = rangeX
            }
            if(rangeY < signMaxPointLT!![1]) {
                signMaxPointLT!![1] = rangeY
            }
            if(rangeX > signMaxPointRB!![0]) {
                signMaxPointRB!![0] = rangeX
            }
            if(rangeY > signMaxPointRB!![1]) {
                signMaxPointRB!![1] = rangeY
            }
        }
    }
}