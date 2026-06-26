package com.thecompass.contentstudio

import android.graphics.Color

data class Brand(
    val name: String,
    val logoEmoji: String,
    val tagline: String,
    val defaultCta: String,
    val hashtags: List<String>,
    val primary: Int,
    val primaryDark: Int,
    val accent: Int,
    val light: Int,
) {
    companion object {
        val DEFAULT = Brand(
            name = "המותג שלי",
            logoEmoji = "✨",
            tagline = "התוכן שמייצג אתכם נכון",
            defaultCta = "עקבו אחרינו",
            hashtags = listOf("תוכן", "שיווק_דיגיטלי", "רשתות_חברתיות"),
            primary = Color.parseColor("#1E293B"),
            primaryDark = Color.parseColor("#0F172A"),
            accent = Color.parseColor("#D4A24C"),
            light = Color.parseColor("#F4EFE6"),
        )
    }
}
