package com.thecompass.contentstudio.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import com.thecompass.contentstudio.Brand

object SlideRenderer {

    fun renderSlide(
        brand: Brand,
        width: Int,
        height: Int,
        screenshot: Bitmap,
        headline: String,
        subheadline: String,
        ctaText: String,
        showCta: Boolean,
        showLogo: Boolean,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val pad = width * 0.08f
        val contentWidth = (width - pad * 2)

        val logoFontSize = width * 0.034f
        val headlineFontSize = width * 0.062f
        val subFontSize = width * 0.034f
        val ctaFontSize = width * 0.032f

        val logoY = height * 0.075f
        val headlineTopY = height * 0.15f

        val phoneTopY = height * 0.34f
        val phoneHeight = height * 0.44f
        val phoneWidth = phoneHeight * 0.5f
        val phoneX = (width - phoneWidth) / 2f
        val bezel = phoneWidth * 0.045f
        val screenX = phoneX + bezel
        val screenY = phoneTopY + bezel
        val screenW = phoneWidth - bezel * 2
        val screenH = phoneHeight - bezel * 2

        val subY = phoneTopY + phoneHeight + height * 0.05f
        val ctaY = height * 0.93f

        drawBackground(canvas, brand, width, height)
        drawCompassDecoration(canvas, brand, width, height)

        if (showLogo) {
            drawCenteredText(canvas, "${brand.logoEmoji} ${brand.name.uppercase()}", width / 2f, logoY, logoFontSize, brand.accent, bold = true)
        }

        if (headline.isNotBlank()) {
            drawWrappedText(
                canvas, headline, width / 2f, headlineTopY, contentWidth.toInt(),
                headlineFontSize, Color.WHITE, maxLines = 2, bold = true,
            )
        }

        drawPhoneMockup(canvas, phoneX, phoneTopY, phoneWidth, phoneHeight, screenX, screenY, screenW, screenH, screenshot)

        if (subheadline.isNotBlank()) {
            drawWrappedText(
                canvas, subheadline, width / 2f, subY, contentWidth.toInt(),
                subFontSize, brand.light, maxLines = 1, bold = false,
            )
        }

        if (showCta) {
            val label = ctaText.ifBlank { brand.defaultCta }
            val pillWidth = (label.length * ctaFontSize * 0.62f).coerceAtLeast(width * 0.5f).coerceAtMost(contentWidth)
            val pillHeight = ctaFontSize * 2.3f
            val pillTop = ctaY - pillHeight * 0.7f
            drawCtaPill(canvas, brand, width, pillTop, pillHeight, ctaFontSize, label, pillWidth)
        }

        return bitmap
    }

    fun renderOutroSlide(brand: Brand, width: Int, height: Int, headline: String, ctaText: String): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val taglineFontSize = width * 0.038f
        val ctaFontSize = width * 0.034f
        val compassR = width * 0.45f

        drawBackground(canvas, brand, width, height)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = brand.accent
            alpha = (0.1f * 255).toInt()
        }
        canvas.drawCircle(width / 2f, height * 0.4f, compassR, ringPaint)

        drawCenteredText(canvas, brand.logoEmoji, width / 2f, height * 0.36f, width * 0.07f, brand.accent, bold = true)
        drawCenteredText(canvas, brand.name.uppercase(), width / 2f, height * 0.46f, width * 0.06f, Color.WHITE, bold = true)

        drawWrappedText(
            canvas, headline.ifBlank { brand.tagline }, width / 2f, height * 0.55f, (width * 0.8f).toInt(),
            taglineFontSize, brand.light, maxLines = 2, bold = false,
        )

        val label = ctaText.ifBlank { brand.defaultCta }
        val pillHeight = ctaFontSize * 2.3f
        val pillTop = height * 0.85f
        drawCtaPill(canvas, brand, width, pillTop, pillHeight, ctaFontSize, label, width * 0.6f)

        return bitmap
    }

    private fun drawBackground(canvas: Canvas, brand: Brand, width: Int, height: Int) {
        val paint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, height.toFloat(), brand.primary, brand.primaryDark, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawCompassDecoration(canvas: Canvas, brand: Brand, width: Int, height: Int) {
        val cx = width * 0.85f
        val cy = height * 0.04f
        val r = width * 0.55f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = brand.accent
            alpha = (0.08f * 255).toInt()
        }
        canvas.drawCircle(cx, cy, r, paint)
        canvas.drawLine(cx - r, cy, cx + r, cy, paint)
        canvas.drawLine(cx, cy - r, cx, cy + r, paint)
    }

    private fun drawCenteredText(canvas: Canvas, text: String, x: Float, baselineY: Float, fontSize: Float, color: Int, bold: Boolean) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = fontSize
            this.color = color
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        canvas.drawText(text, x, baselineY, paint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        centerX: Float,
        topY: Float,
        maxWidth: Int,
        fontSize: Float,
        color: Int,
        maxLines: Int,
        bold: Boolean,
    ) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = fontSize
            this.color = color
            typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
        }
        val safeWidth = maxWidth.coerceAtLeast(1)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, safeWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_RTL)
            .setLineSpacing(0f, 1.15f)
            .build()

        canvas.save()
        canvas.translate(centerX - safeWidth / 2f, topY)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun drawPhoneMockup(
        canvas: Canvas,
        phoneX: Float, phoneTopY: Float, phoneWidth: Float, phoneHeight: Float,
        screenX: Float, screenY: Float, screenW: Float, screenH: Float,
        screenshot: Bitmap,
    ) {
        val frameRadius = phoneWidth * 0.12f
        val frameRect = RectF(phoneX, phoneTopY, phoneX + phoneWidth, phoneTopY + phoneHeight)

        val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#10141A") }
        canvas.drawRoundRect(frameRect, frameRadius, frameRadius, framePaint)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.parseColor("#2A2F38")
        }
        canvas.drawRoundRect(frameRect, frameRadius, frameRadius, strokePaint)

        val screenRadius = phoneWidth * 0.07f
        val screenRect = RectF(screenX, screenY, screenX + screenW, screenY + screenH)
        val clipPath = Path().apply { addRoundRect(screenRect, screenRadius, screenRadius, Path.Direction.CW) }

        canvas.save()
        canvas.clipPath(clipPath)
        drawBitmapCenterCrop(canvas, screenshot, screenRect)
        canvas.restore()
    }

    private fun drawBitmapCenterCrop(canvas: Canvas, bitmap: Bitmap, dest: RectF) {
        val srcRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
        val destRatio = dest.width() / dest.height()

        val srcRect = if (srcRatio > destRatio) {
            val cropWidth = (bitmap.height * destRatio).toInt().coerceIn(1, bitmap.width)
            val left = (bitmap.width - cropWidth) / 2
            Rect(left, 0, left + cropWidth, bitmap.height)
        } else {
            val cropHeight = (bitmap.width / destRatio).toInt().coerceIn(1, bitmap.height)
            val top = (bitmap.height - cropHeight) / 2
            Rect(0, top, bitmap.width, top + cropHeight)
        }
        canvas.drawBitmap(bitmap, srcRect, dest, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
    }

    private fun drawCtaPill(
        canvas: Canvas,
        brand: Brand,
        width: Int,
        pillTop: Float,
        pillHeight: Float,
        fontSize: Float,
        label: String,
        pillWidth: Float,
    ) {
        val rect = RectF((width - pillWidth) / 2f, pillTop, (width - pillWidth) / 2f + pillWidth, pillTop + pillHeight)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = brand.accent }
        canvas.drawRoundRect(rect, pillHeight / 2f, pillHeight / 2f, paint)
        val textBaselineY = pillTop + pillHeight / 2f + fontSize * 0.32f
        drawCenteredText(canvas, label, width / 2f, textBaselineY, fontSize, brand.primaryDark, bold = true)
    }
}
