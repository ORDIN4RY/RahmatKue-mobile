package ordinary.rahmatbakery.admin.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import ordinary.rahmatbakery.R

/**
 * Custom Animated Pie Chart View
 * Dengan smooth animation saat data berubah
 */
class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Data untuk pie chart
    private var selesai = 0
    private var proses = 0
    private var dibatalkan = 0
    private var total = 0

    // Animation progress (0.0 to 1.0)
    private var animationProgress = 0f

    // Paint objects
    private val paintSelesai = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.green_success)
        style = Paint.Style.FILL
    }

    private val paintProses = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.orange_warning)
        style = Paint.Style.FILL
    }

    private val paintDibatalkan = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.red_error)
        style = Paint.Style.FILL
    }

    private val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        style = Paint.Style.FILL
    }

    private val paintInnerCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.background)
        style = Paint.Style.FILL
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_dark)
        textSize = 48f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val paintSubText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.text_gray)
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private val rectF = RectF()
    private var animator: ValueAnimator? = null

    /**
     * Set data dengan animasi
     */
    fun setData(selesai: Int, proses: Int, dibatalkan: Int, animate: Boolean = true) {
        this.selesai = selesai
        this.proses = proses
        this.dibatalkan = dibatalkan
        this.total = selesai + proses + dibatalkan

        if (animate) {
            startAnimation()
        } else {
            animationProgress = 1f
            invalidate()
        }
    }

    /**
     * Start smooth animation
     */
    private fun startAnimation() {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800 // 800ms animation
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                animationProgress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (total == 0) {
            drawEmptyChart(canvas)
            return
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 2f * 0.85f

        // Set bounds
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Hitung angle dengan animation progress
        val selesaiAngle = (selesai.toFloat() / total) * 360f * animationProgress
        val prosesAngle = (proses.toFloat() / total) * 360f * animationProgress
        val dibatalkanAngle = (dibatalkan.toFloat() / total) * 360f * animationProgress

        var startAngle = -90f // Start from top

        // Draw Selesai segment
        if (selesai > 0 && selesaiAngle > 0) {
            canvas.drawArc(rectF, startAngle, selesaiAngle, true, paintSelesai)
            startAngle += selesaiAngle
        }

        // Draw Proses segment
        if (proses > 0 && prosesAngle > 0) {
            canvas.drawArc(rectF, startAngle, prosesAngle, true, paintProses)
            startAngle += prosesAngle
        }

        // Draw Dibatalkan segment
        if (dibatalkan > 0 && dibatalkanAngle > 0) {
            canvas.drawArc(rectF, startAngle, dibatalkanAngle, true, paintDibatalkan)
        }

        // Draw inner circle (donut hole)
        val innerRadius = radius * 0.6f
        canvas.drawCircle(centerX, centerY, innerRadius, paintCenter)

        // Draw subtle inner shadow
        val shadowRadius = innerRadius * 0.95f
        canvas.drawCircle(centerX, centerY, shadowRadius, paintInnerCircle)

        // Draw total number (animated)
        val displayTotal = (total * animationProgress).toInt()
        canvas.drawText(
            displayTotal.toString(),
            centerX,
            centerY + 12f,
            paintText
        )

        // Draw "Order" text
        canvas.drawText(
            "Order",
            centerX,
            centerY + 42f,
            paintSubText
        )

        // Draw percentage labels (optional)
        if (animationProgress >= 0.8f) {
            drawPercentageLabels(canvas, centerX, centerY, radius)
        }
    }

    /**
     * Draw percentage labels di luar pie chart
     */
    private fun drawPercentageLabels(canvas: Canvas, centerX: Float, centerY: Float, radius: Float) {
        val labelRadius = radius * 1.2f

        if (selesai > 0) {
            val percentage = (selesai.toFloat() / total * 100).toInt()
            val angle = Math.toRadians((-90 + (selesai.toFloat() / total * 360f / 2)).toDouble())
            val x = centerX + (labelRadius * Math.cos(angle)).toFloat()
            val y = centerY + (labelRadius * Math.sin(angle)).toFloat()

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.green_success)
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("$percentage%", x, y, labelPaint)
        }
        if (proses > 0) {
            val percentage = (proses.toFloat() / total * 100).toInt()
            val angle = Math.toRadians((45 + (proses.toFloat() / total * 360f / 2)).toDouble())
            val x = centerX + (labelRadius * Math.cos(angle)).toFloat()
            val y = centerY + (labelRadius * Math.sin(angle)).toFloat()

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.orange_warning)
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("$percentage%", x, y, labelPaint)
        }
        if (dibatalkan > 0) {
            val percentage = (dibatalkan.toFloat() / total * 100).toInt()
            val angle = Math.toRadians((-180 + (dibatalkan.toFloat() / total * 360f / 2)).toDouble())
            val x = centerX + (labelRadius * Math.cos(angle)).toFloat()
            val y = centerY + (labelRadius * Math.sin(angle)).toFloat()

            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ContextCompat.getColor(context, R.color.red_error)
                textSize = 20f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("$percentage%", x, y, labelPaint)
        }
    }

    /**
     * Draw empty chart
     */
    private fun drawEmptyChart(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = minOf(width, height) / 2f * 0.85f

        val paintEmpty = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ContextCompat.getColor(context, R.color.gray_light)
            style = Paint.Style.FILL
        }

        canvas.drawCircle(centerX, centerY, radius, paintEmpty)

        val innerRadius = radius * 0.6f
        canvas.drawCircle(centerX, centerY, innerRadius, paintCenter)

        canvas.drawText("0", centerX, centerY + 12f, paintText)
        canvas.drawText("Order", centerX, centerY + 42f, paintSubText)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = minOf(measuredWidth, measuredHeight)
        setMeasuredDimension(size, size)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}