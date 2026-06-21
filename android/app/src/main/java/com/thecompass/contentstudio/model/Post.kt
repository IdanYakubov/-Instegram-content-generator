package com.thecompass.contentstudio.model

enum class PostType { POST, REEL }
enum class PostStatus { DRAFT, SHARED }

data class Post(
    val id: String,
    val type: PostType,
    val mediaFileName: String,
    val headline: String,
    val subheadline: String,
    val ctaText: String,
    val caption: String,
    val status: PostStatus,
    val createdAt: String,
    val sharedAt: String? = null,
)
