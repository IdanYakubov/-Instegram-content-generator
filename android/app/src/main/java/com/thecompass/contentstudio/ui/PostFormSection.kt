package com.thecompass.contentstudio.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thecompass.contentstudio.model.PostType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFormSection(
    isGenerating: Boolean,
    onSubmit: (PostType, List<Uri>, String, String, String, List<String>) -> Unit,
) {
    var type by remember { mutableStateOf(PostType.POST) }
    var headline by remember { mutableStateOf("") }
    var subheadline by remember { mutableStateOf("") }
    var ctaText by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }
    var screenshotUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        screenshotUris = if (type == PostType.REEL) uris.take(8) else uris.take(1)
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("יצירת תוכן חדש", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = type == PostType.POST,
                    onClick = {
                        type = PostType.POST
                        screenshotUris = screenshotUris.take(1)
                    },
                    label = { Text("פוסט (תמונה)") },
                )
                FilterChip(
                    selected = type == PostType.REEL,
                    onClick = { type = PostType.REEL },
                    label = { Text("רילס (וידאו)") },
                )
            }

            OutlinedButton(onClick = { pickMedia.launch("image/*") }) {
                Text(
                    if (screenshotUris.isEmpty()) "בחירת צילומי מסך"
                    else "${screenshotUris.size} צילומי מסך נבחרו",
                )
            }

            OutlinedTextField(
                value = headline,
                onValueChange = { headline = it },
                label = { Text("כותרת") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = subheadline,
                onValueChange = { subheadline = it },
                label = { Text("כותרת משנה") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = ctaText,
                onValueChange = { ctaText = it },
                label = { Text("טקסט לכפתור הקריאה לפעולה") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = hashtags,
                onValueChange = { hashtags = it },
                label = { Text("האשטגים נוספים (מופרדים בפסיק)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val extra = hashtags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    onSubmit(type, screenshotUris, headline, subheadline, ctaText, extra)
                },
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isGenerating) "יוצר תוכן..." else "צרו תוכן")
            }
        }
    }
}
