package com.thecompass.contentstudio.render

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.thecompass.contentstudio.data.PostRepository
import java.io.File
import java.io.FileOutputStream

object PostImageGenerator {
    private const val WIDTH = 1080
    private const val HEIGHT = 1080

    fun generate(
        repository: PostRepository,
        id: String,
        screenshotFile: File,
        headline: String,
        subheadline: String,
        ctaText: String,
    ): String {
        val screenshot = BitmapFactory.decodeFile(screenshotFile.absolutePath)
            ?: error("Could not decode screenshot")
        val bitmap = SlideRenderer.renderSlide(
            width = WIDTH,
            height = HEIGHT,
            screenshot = screenshot,
            headline = headline,
            subheadline = subheadline,
            ctaText = ctaText,
            showCta = true,
            showLogo = true,
        )
        val fileName = "$id.jpg"
        val outFile = repository.mediaFile(fileName)
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        return fileName
    }
}
