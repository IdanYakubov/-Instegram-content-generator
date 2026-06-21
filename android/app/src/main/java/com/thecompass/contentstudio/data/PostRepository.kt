package com.thecompass.contentstudio.data

import android.content.Context
import com.thecompass.contentstudio.model.Post
import com.thecompass.contentstudio.model.PostStatus
import com.thecompass.contentstudio.model.PostType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.Instant

class PostRepository(context: Context) {
    private val dbFile: File = File(context.filesDir, "posts.json")
    val generatedDir: File = File(context.filesDir, "generated").apply { mkdirs() }

    @Synchronized
    fun getAll(): List<Post> {
        if (!dbFile.exists()) return emptyList()
        val text = dbFile.readText()
        if (text.isBlank()) return emptyList()
        val array = JSONArray(text)
        return (0 until array.length()).map { parsePost(array.getJSONObject(it)) }
    }

    @Synchronized
    fun insert(post: Post) {
        val posts = getAll().toMutableList()
        posts.add(0, post)
        writeAll(posts)
    }

    @Synchronized
    fun updateCaption(id: String, caption: String): Post? {
        val posts = getAll().toMutableList()
        val index = posts.indexOfFirst { it.id == id }
        if (index == -1) return null
        val updated = posts[index].copy(caption = caption)
        posts[index] = updated
        writeAll(posts)
        return updated
    }

    @Synchronized
    fun markShared(id: String): Post? {
        val posts = getAll().toMutableList()
        val index = posts.indexOfFirst { it.id == id }
        if (index == -1) return null
        val updated = posts[index].copy(status = PostStatus.SHARED, sharedAt = Instant.now().toString())
        posts[index] = updated
        writeAll(posts)
        return updated
    }

    @Synchronized
    fun delete(id: String) {
        val posts = getAll().toMutableList()
        val index = posts.indexOfFirst { it.id == id }
        if (index == -1) return
        val removed = posts.removeAt(index)
        File(generatedDir, removed.mediaFileName).delete()
        writeAll(posts)
    }

    fun mediaFile(fileName: String): File = File(generatedDir, fileName)

    private fun writeAll(posts: List<Post>) {
        val array = JSONArray()
        posts.forEach { array.put(toJson(it)) }
        dbFile.writeText(array.toString())
    }

    private fun toJson(post: Post): JSONObject = JSONObject().apply {
        put("id", post.id)
        put("type", post.type.name)
        put("mediaFileName", post.mediaFileName)
        put("headline", post.headline)
        put("subheadline", post.subheadline)
        put("ctaText", post.ctaText)
        put("caption", post.caption)
        put("status", post.status.name)
        put("createdAt", post.createdAt)
        put("sharedAt", post.sharedAt)
    }

    private fun parsePost(json: JSONObject): Post = Post(
        id = json.getString("id"),
        type = PostType.valueOf(json.getString("type")),
        mediaFileName = json.getString("mediaFileName"),
        headline = json.getString("headline"),
        subheadline = json.optString("subheadline", ""),
        ctaText = json.optString("ctaText", ""),
        caption = json.getString("caption"),
        status = PostStatus.valueOf(json.getString("status")),
        createdAt = json.getString("createdAt"),
        sharedAt = if (json.has("sharedAt") && !json.isNull("sharedAt")) json.optString("sharedAt") else null,
    )
}
