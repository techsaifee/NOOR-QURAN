package com.example.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuranApiService {

    @GET("chapters")
    suspend fun getChapters(
        @Query("language") language: String = "en"
    ): ChaptersResponse

    @GET("verses/by_chapter/{chapter_id}")
    suspend fun getVersesByChapter(
        @Path("chapter_id") chapterId: Int,
        @Query("language") language: String = "en",
        @Query("words") words: Boolean = false,
        @Query("translations") translations: String = "85", // Abdel Haleem
        @Query("fields") fields: String = "text_uthmani,chapter_id,verse_key,verse_number,juz_number,page_number",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 300 // Retrieve complete surah (max is 286 verses)
    ): VersesResponse
}
