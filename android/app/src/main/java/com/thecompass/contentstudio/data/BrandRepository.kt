package com.thecompass.contentstudio.data

import android.content.Context
import android.graphics.Color
import com.thecompass.contentstudio.Brand
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class BrandRepository(context: Context) {
    private val brandFile: File = File(context.filesDir, "brand.json")

    @Synchronized
    fun get(): Brand {
        if (!brandFile.exists()) return Brand.DEFAULT
        val text = brandFile.readText()
        if (text.isBlank()) return Brand.DEFAULT
        return try {
            parse(JSONObject(text))
        } catch (e: Exception) {
            Brand.DEFAULT
        }
    }

    @Synchronized
    fun save(brand: Brand) {
        brandFile.writeText(toJson(brand).toString())
    }

    private fun toJson(brand: Brand): JSONObject = JSONObject().apply {
        put("name", brand.name)
        put("logoEmoji", brand.logoEmoji)
        put("tagline", brand.tagline)
        put("defaultCta", brand.defaultCta)
        put("hashtags", JSONArray(brand.hashtags))
        put("primary", String.format("#%06X", 0xFFFFFF and brand.primary))
        put("primaryDark", String.format("#%06X", 0xFFFFFF and brand.primaryDark))
        put("accent", String.format("#%06X", 0xFFFFFF and brand.accent))
        put("light", String.format("#%06X", 0xFFFFFF and brand.light))
    }

    private fun parse(json: JSONObject): Brand {
        val hashtagsArray = json.optJSONArray("hashtags") ?: JSONArray()
        val hashtags = (0 until hashtagsArray.length()).map { hashtagsArray.getString(it) }
        return Brand(
            name = json.optString("name", Brand.DEFAULT.name),
            logoEmoji = json.optString("logoEmoji", Brand.DEFAULT.logoEmoji),
            tagline = json.optString("tagline", Brand.DEFAULT.tagline),
            defaultCta = json.optString("defaultCta", Brand.DEFAULT.defaultCta),
            hashtags = hashtags.ifEmpty { Brand.DEFAULT.hashtags },
            primary = Color.parseColor(json.optString("primary", "#1E293B")),
            primaryDark = Color.parseColor(json.optString("primaryDark", "#0F172A")),
            accent = Color.parseColor(json.optString("accent", "#D4A24C")),
            light = Color.parseColor(json.optString("light", "#F4EFE6")),
        )
    }
}
