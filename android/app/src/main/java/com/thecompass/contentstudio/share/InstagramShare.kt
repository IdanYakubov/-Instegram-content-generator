package com.thecompass.contentstudio.share

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.thecompass.contentstudio.model.PostType
import java.io.File

/**
 * Instagram's feed-post composer does not reliably accept a pre-filled caption via
 * EXTRA_TEXT on ACTION_SEND (that extra only really lands for Stories stickers), so the
 * caption is copied to the clipboard as well and the user pastes it once inside Instagram.
 */
object InstagramShare {
    private const val INSTAGRAM_PACKAGE = "com.instagram.android"

    fun copyCaptionToClipboard(context: Context, caption: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("caption", caption))
    }

    fun share(context: Context, mediaFile: File, type: PostType, caption: String): Boolean {
        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, mediaFile)
        val mimeType = if (type == PostType.REEL) "video/mp4" else "image/jpeg"

        val targeted = Intent(Intent.ACTION_SEND).apply {
            this.type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage(INSTAGRAM_PACKAGE)
        }

        return try {
            context.startActivity(targeted)
            true
        } catch (e: ActivityNotFoundException) {
            shareViaChooser(context, uri, mimeType, caption)
        }
    }

    private fun shareViaChooser(context: Context, uri: android.net.Uri, mimeType: String, caption: String): Boolean {
        val fallback = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return try {
            context.startActivity(Intent.createChooser(fallback, "שליחה לאינסטגרם"))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
