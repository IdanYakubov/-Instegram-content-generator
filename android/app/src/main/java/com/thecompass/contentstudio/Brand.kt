package com.thecompass.contentstudio

import android.graphics.Color

object Brand {
    const val TAGLINE = "מצאו את הכיוון שלכם"
    const val DEFAULT_CTA = "הורידו את The Compass ומצאו את הדרך שלכם"

    val HASHTAGS = listOf(
        "TheCompass",
        "מצאתי_כיוון",
        "יזמות",
        "חיילים_משוחררים",
        "הכוונה_אישית",
        "התחלה_מחדש",
        "מנטורינג",
    )

    val PRIMARY: Int = Color.parseColor("#0E2A4A")
    val PRIMARY_DARK: Int = Color.parseColor("#081A30")
    val ACCENT: Int = Color.parseColor("#D4A24C")
    val LIGHT: Int = Color.parseColor("#F4EFE6")
}
