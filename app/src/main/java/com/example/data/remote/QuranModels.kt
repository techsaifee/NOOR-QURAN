package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChaptersResponse(
    @Json(name = "chapters") val chapters: List<Chapter>
)

@JsonClass(generateAdapter = true)
data class Chapter(
    @Json(name = "id") val id: Int,
    @Json(name = "revelation_place") val revelationPlace: String,
    @Json(name = "revelation_order") val revelationOrder: Int,
    @Json(name = "bismillah_pre") val bismillahPre: Boolean,
    @Json(name = "name_simple") val nameSimple: String,
    @Json(name = "name_complex") val nameComplex: String,
    @Json(name = "name_arabic") val nameArabic: String,
    @Json(name = "verses_count") val versesCount: Int,
    @Json(name = "pages") val pages: List<Int>,
    @Json(name = "translated_name") val translatedName: TranslatedName
)

@JsonClass(generateAdapter = true)
data class TranslatedName(
    @Json(name = "language_name") val languageName: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class VersesResponse(
    @Json(name = "verses") val verses: List<Verse>,
    @Json(name = "pagination") val pagination: Pagination
)

@JsonClass(generateAdapter = true)
data class Verse(
    @Json(name = "id") val id: Int,
    @Json(name = "verse_number") val verseNumber: Int,
    @Json(name = "verse_key") val verseKey: String,
    @Json(name = "juz_number") val juzNumber: Int,
    @Json(name = "page_number") val pageNumber: Int,
    @Json(name = "text_uthmani") val textUthmani: String,
    @Json(name = "translations") val translations: List<Translation> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Translation(
    @Json(name = "id") val id: Int,
    @Json(name = "resource_id") val resourceId: Int,
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class Pagination(
    @Json(name = "per_page") val perPage: Int,
    @Json(name = "current_page") val currentPage: Int,
    @Json(name = "next_page") val nextPage: Int?,
    @Json(name = "total_pages") val totalPages: Int
)
