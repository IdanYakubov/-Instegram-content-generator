package com.thecompass.contentstudio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.thecompass.contentstudio.model.Post

@Composable
fun PostListSection(
    posts: List<Post>,
    onCaptionChange: (String, String) -> Unit,
    onShare: (Post) -> Boolean,
    onDelete: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("תוכן שנוצר", style = MaterialTheme.typography.titleMedium)
        if (posts.isEmpty()) {
            Text("עדיין לא נוצר תוכן")
        } else {
            posts.forEach { post ->
                PostCard(post = post, onCaptionChange = onCaptionChange, onShare = onShare, onDelete = onDelete)
            }
        }
    }
}
