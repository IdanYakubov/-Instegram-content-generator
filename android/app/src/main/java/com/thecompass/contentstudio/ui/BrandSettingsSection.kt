package com.thecompass.contentstudio.ui

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.thecompass.contentstudio.Brand

@Composable
fun BrandSettingsSection(
    brand: Brand,
    onSave: (Brand) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    if (!expanded) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("⚙️ הגדרות מותג (${brand.name})")
        }
        return
    }

    var name by remember { mutableStateOf(brand.name) }
    var logoEmoji by remember { mutableStateOf(brand.logoEmoji) }
    var tagline by remember { mutableStateOf(brand.tagline) }
    var defaultCta by remember { mutableStateOf(brand.defaultCta) }
    var hashtagsText by remember { mutableStateOf(brand.hashtags.joinToString(", ")) }
    var primaryHex by remember { mutableStateOf(hex(brand.primary)) }
    var primaryDarkHex by remember { mutableStateOf(hex(brand.primaryDark)) }
    var accentHex by remember { mutableStateOf(hex(brand.accent)) }
    var lightHex by remember { mutableStateOf(hex(brand.light)) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("הגדרות מותג", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("שם המותג") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = logoEmoji,
                onValueChange = { logoEmoji = it },
                label = { Text("אמוג'י לוגו") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = tagline,
                onValueChange = { tagline = it },
                label = { Text("סלוגן") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = defaultCta,
                onValueChange = { defaultCta = it },
                label = { Text("קריאה לפעולה (ברירת מחדל)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = hashtagsText,
                onValueChange = { hashtagsText = it },
                label = { Text("האשטגים קבועים (מופרדים בפסיק)") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = primaryHex,
                    onValueChange = { primaryHex = it },
                    label = { Text("צבע ראשי") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = primaryDarkHex,
                    onValueChange = { primaryDarkHex = it },
                    label = { Text("צבע ראשי כהה") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = accentHex,
                    onValueChange = { accentHex = it },
                    label = { Text("צבע הדגשה") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = lightHex,
                    onValueChange = { lightHex = it },
                    label = { Text("צבע בהיר") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val updated = Brand(
                            name = name.ifBlank { brand.name },
                            logoEmoji = logoEmoji.ifBlank { brand.logoEmoji },
                            tagline = tagline.ifBlank { brand.tagline },
                            defaultCta = defaultCta.ifBlank { brand.defaultCta },
                            hashtags = hashtagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                .ifEmpty { brand.hashtags },
                            primary = parseColorOr(primaryHex, brand.primary),
                            primaryDark = parseColorOr(primaryDarkHex, brand.primaryDark),
                            accent = parseColorOr(accentHex, brand.accent),
                            light = parseColorOr(lightHex, brand.light),
                        )
                        onSave(updated)
                        expanded = false
                    },
                ) {
                    Text("שמירה")
                }
                OutlinedButton(onClick = { expanded = false }) {
                    Text("ביטול")
                }
            }
        }
    }
}

private fun hex(color: Int): String = String.format("#%06X", 0xFFFFFF and color)

private fun parseColorOr(value: String, fallback: Int): Int = try {
    Color.parseColor(value.trim())
} catch (e: IllegalArgumentException) {
    fallback
}
