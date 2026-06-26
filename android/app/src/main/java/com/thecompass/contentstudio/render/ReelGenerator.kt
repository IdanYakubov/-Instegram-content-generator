package com.thecompass.contentstudio.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import com.thecompass.contentstudio.Brand
import com.thecompass.contentstudio.data.PostRepository
import java.io.File

object ReelGenerator {
    private const val WIDTH = 1080
    private const val HEIGHT = 1920
    private const val FPS = 25
    private const val SLIDE_DURATION_SECONDS = 2.5
    private const val OUTRO_DURATION_SECONDS = 3.2
    private const val MAX_SCREENSHOTS = 8
    private const val MAX_ZOOM = 1.12f
    private const val ZOOM_STEP = 0.0015f

    private data class Slide(val bitmap: Bitmap, val durationSeconds: Double)

    fun generate(
        repository: PostRepository,
        brand: Brand,
        id: String,
        screenshotFiles: List<File>,
        headline: String,
        subheadline: String,
        ctaText: String,
    ): String {
        val usedScreenshots = screenshotFiles.take(MAX_SCREENSHOTS)
        val slides = mutableListOf<Slide>()

        usedScreenshots.forEachIndexed { index, file ->
            val screenshot = BitmapFactory.decodeFile(file.absolutePath) ?: error("Could not decode screenshot")
            val slideBitmap = SlideRenderer.renderSlide(
                brand = brand,
                width = WIDTH,
                height = HEIGHT,
                screenshot = screenshot,
                headline = if (index == 0) headline else "",
                subheadline = if (index == 0) subheadline else "",
                ctaText = ctaText,
                showCta = false,
                showLogo = true,
            )
            slides += Slide(slideBitmap, SLIDE_DURATION_SECONDS)
        }

        slides += Slide(
            SlideRenderer.renderOutroSlide(brand, WIDTH, HEIGHT, subheadline.ifBlank { headline }, ctaText),
            OUTRO_DURATION_SECONDS,
        )

        val fileName = "$id.mp4"
        val outFile = repository.mediaFile(fileName)

        val encoder = ReelEncoder(WIDTH, HEIGHT, FPS)
        encoder.start(outFile.absolutePath)

        val frameBitmap = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888)
        val frameCanvas = Canvas(frameBitmap)

        for (slide in slides) {
            val frameCount = Math.round(slide.durationSeconds * FPS).toInt()
            for (frame in 0 until frameCount) {
                val zoom = (1f + ZOOM_STEP * frame).coerceAtMost(MAX_ZOOM)
                frameCanvas.save()
                frameCanvas.drawColor(Color.BLACK)
                frameCanvas.scale(zoom, zoom, WIDTH / 2f, HEIGHT / 2f)
                frameCanvas.drawBitmap(slide.bitmap, 0f, 0f, null)
                frameCanvas.restore()
                encoder.encodeFrame(frameBitmap)
            }
        }

        encoder.finish()
        return fileName
    }
}
