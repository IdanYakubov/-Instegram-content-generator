package com.thecompass.contentstudio.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.thecompass.contentstudio.model.Post
import com.thecompass.contentstudio.model.PostStatus
import com.thecompass.contentstudio.model.PostType
import java.io.File

@Composable
fun PostCard(
    post: Post,
    onCaptionChange: (String, String) -> Unit,
    onShare: (Post) -> Boolean,
    onDelete: (String) -> Unit,
) {
    val context = LocalContext.current
    var caption by remember(post.id) { mutableStateOf(post.caption) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val mediaFile = remember(post.id) { File(File(context.filesDir, "generated"), post.mediaFileName) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (post.type == PostType.REEL) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.fromFile(mediaFile))
                            setOnPreparedListener { player ->
                                player.isLooping = true
                                start()
                            }
                        }
                    },
                )
            } else {
                val bitmap = remember(post.id) { BitmapFactory.decodeFile(mediaFile.absolutePath) }
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = post.headline,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(if (post.status == PostStatus.SHARED) "נשלח לאינסטגרם" else "טיוטה") },
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (post.type == PostType.REEL) "רילס" else "פוסט") },
                )
            }

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 6,
                maxLines = 12,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (caption != post.caption) {
                    Button(onClick = { onCaptionChange(post.id, caption) }) { Text("שמירת כיתוב") }
                }
                Button(onClick = { onShare(post) }, enabled = caption == post.caption) { Text("שיתוף באינסטגרם") }
                OutlinedButton(onClick = { showDeleteConfirm = true }) { Text("מחיקה") }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("למחוק את הפוסט הזה?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(post.id)
                }) { Text("מחיקה") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("ביטול") }
            },
        )
    }
}
