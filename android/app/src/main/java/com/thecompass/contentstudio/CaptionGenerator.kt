package com.thecompass.contentstudio

object CaptionGenerator {
    fun buildCaption(brand: Brand, headline: String, subheadline: String?, ctaText: String?, hashtags: List<String>): String {
        val lines = mutableListOf<String>()
        lines += "${brand.logoEmoji} ${headline.ifBlank { brand.tagline }}"
        if (!subheadline.isNullOrBlank()) {
            lines += ""
            lines += subheadline
        }
        lines += ""
        lines += "📲 ${ctaText?.takeIf { it.isNotBlank() } ?: brand.defaultCta}"
        lines += ""
        lines += buildHashtags(brand, hashtags)
        return lines.joinToString("\n")
    }

    fun buildHashtags(brand: Brand, extra: List<String> = emptyList()): String {
        val tags = (brand.hashtags + extra).distinct()
        return tags.joinToString(" ") { "#${it.replace(Regex("\\s+"), "")}" }
    }
}
