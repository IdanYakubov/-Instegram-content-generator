package com.thecompass.contentstudio.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thecompass.contentstudio.CaptionGenerator
import com.thecompass.contentstudio.data.PostRepository
import com.thecompass.contentstudio.model.Post
import com.thecompass.contentstudio.model.PostStatus
import com.thecompass.contentstudio.model.PostType
import com.thecompass.contentstudio.render.PostImageGenerator
import com.thecompass.contentstudio.render.ReelGenerator
import com.thecompass.contentstudio.share.InstagramShare
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.UUID

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository(application)

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _posts.value = repository.getAll()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun createPost(
        type: PostType,
        screenshotUris: List<Uri>,
        headline: String,
        subheadline: String,
        ctaText: String,
        extraHashtags: List<String>,
    ) {
        if (screenshotUris.isEmpty() || headline.isBlank()) {
            _errorMessage.value = "יש לבחור לפחות צילום מסך אחד ולמלא כותרת"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            var screenshotFiles: List<File> = emptyList()
            try {
                val id = UUID.randomUUID().toString().take(10)
                screenshotFiles = withContext(Dispatchers.IO) {
                    screenshotUris.map { copyUriToCache(it) }
                }

                val mediaFileName = withContext(Dispatchers.Default) {
                    if (type == PostType.REEL) {
                        ReelGenerator.generate(repository, id, screenshotFiles, headline, subheadline, ctaText)
                    } else {
                        PostImageGenerator.generate(repository, id, screenshotFiles.first(), headline, subheadline, ctaText)
                    }
                }

                val caption = CaptionGenerator.buildCaption(headline, subheadline, ctaText, extraHashtags)
                val post = Post(
                    id = id,
                    type = type,
                    mediaFileName = mediaFileName,
                    headline = headline,
                    subheadline = subheadline,
                    ctaText = ctaText,
                    caption = caption,
                    status = PostStatus.DRAFT,
                    createdAt = Instant.now().toString(),
                )
                withContext(Dispatchers.IO) { repository.insert(post) }
                refresh()
            } catch (e: Exception) {
                _errorMessage.value = "יצירת התוכן נכשלה: ${e.message}"
            } finally {
                withContext(Dispatchers.IO) {
                    screenshotFiles.forEach { it.delete() }
                }
                _isGenerating.value = false
            }
        }
    }

    fun updateCaption(id: String, caption: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCaption(id, caption)
            refresh()
        }
    }

    fun deletePost(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(id)
            refresh()
        }
    }

    fun shareToInstagram(post: Post): Boolean {
        val mediaFile = repository.mediaFile(post.mediaFileName)
        val context = getApplication<Application>()
        InstagramShare.copyCaptionToClipboard(context, post.caption)
        val launched = InstagramShare.share(context, mediaFile, post.type, post.caption)
        if (launched) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.markShared(post.id)
                refresh()
            }
        } else {
            _errorMessage.value = "לא נמצאה אפליקציה לשיתוף"
        }
        return launched
    }

    private fun copyUriToCache(uri: Uri): File {
        val context = getApplication<Application>()
        val outFile = File.createTempFile("upload_", ".img", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            outFile.outputStream().use { output -> input.copyTo(output) }
        }
        return outFile
    }
}
