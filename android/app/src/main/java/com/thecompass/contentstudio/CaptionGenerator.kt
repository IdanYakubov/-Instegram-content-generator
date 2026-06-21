package com.thecompass.contentstudio

object CaptionGenerator {
    fun buildCaption(headline: String, subheadline: String?, ctaText: String?, hashtags: List<String>): String {
        val lines = mutableListOf<String>()
        lines += "🧭 ${headline.ifBlank { Brand.TAGLINE }}"
        if (!subheadline.isNullOrBlank()) {
            lines += ""
            lines += subheadline
        }
        lines += ""
        lines += "📲 ${ctaText?.takeIf { it.isNotBlank() } ?: Brand.DEFAULT_CTA}"
        lines += ""
        lines += buildHashtags(hashtags)
        return lines.joinToString("\n")
    }

    fun buildHashtags(extra: List<String> = emptyList()): String {
        val tags = (Brand.HASHTAGS + extra).distinct()
        return tags.joinToString(" ") { "#${it.replace(Regex("\\s+"), "")}" }
    }
}
