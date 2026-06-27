package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.Chapter
import com.example.data.remote.QuranApiService
import com.example.data.remote.Verse
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class QuranRepository(
    private val quranDao: QuranDao,
    private val apiService: QuranApiService
) {

    // --- Remote Quran API ---

    suspend fun getChapters(): List<Chapter> {
        return apiService.getChapters().chapters
    }

    suspend fun getVerses(chapterId: Int, translationId: String): List<Verse> {
        return apiService.getVersesByChapter(
            chapterId = chapterId,
            translations = translationId
        ).verses
    }

    // --- Bookmarks ---

    val allBookmarks: Flow<List<BookmarkEntity>> = quranDao.getAllBookmarks()

    fun isBookmarkedFlow(verseKey: String): Flow<Boolean> = quranDao.isBookmarkedFlow(verseKey)

    suspend fun isBookmarked(verseKey: String): Boolean = quranDao.isBookmarked(verseKey)

    suspend fun addBookmark(bookmark: BookmarkEntity) = quranDao.insertBookmark(bookmark)

    suspend fun removeBookmark(verseKey: String) = quranDao.deleteBookmark(verseKey)


    // --- Favorites ---

    val allFavorites: Flow<List<FavoriteEntity>> = quranDao.getAllFavorites()

    fun isFavoriteFlow(verseKey: String): Flow<Boolean> = quranDao.isFavoriteFlow(verseKey)

    suspend fun isFavorite(verseKey: String): Boolean = quranDao.isFavorite(verseKey)

    suspend fun addFavorite(favorite: FavoriteEntity) = quranDao.insertFavorite(favorite)

    suspend fun removeFavorite(verseKey: String) = quranDao.deleteFavorite(verseKey)


    // --- Notes ---

    val allNotes: Flow<List<NoteEntity>> = quranDao.getAllNotes()

    suspend fun getNote(verseKey: String): NoteEntity? = quranDao.getNote(verseKey)

    fun getNoteFlow(verseKey: String): Flow<NoteEntity?> = quranDao.getNoteFlow(verseKey)

    suspend fun addNote(note: NoteEntity) = quranDao.insertNote(note)

    suspend fun removeNote(verseKey: String) = quranDao.deleteNote(verseKey)


    // --- Reading History ---

    val readingHistory: Flow<List<ReadingHistoryEntity>> = quranDao.getReadingHistory()

    val lastReadSurah: Flow<ReadingHistoryEntity?> = quranDao.getLastReadSurah()

    suspend fun addReadingHistory(history: ReadingHistoryEntity) = quranDao.insertReadingHistory(history)

    suspend fun removeReadingHistory(surahId: Int) = quranDao.deleteReadingHistory(surahId)


    // --- Reading Progress Stats ---

    val allProgress: Flow<List<ProgressEntity>> = quranDao.getAllProgress()

    private fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun incrementVersesReadToday(count: Int = 1) {
        val today = getCurrentDateString()
        val existingProgress = quranDao.getProgressForDate(today)
        if (existingProgress != null) {
            val updated = existingProgress.copy(
                versesReadCount = existingProgress.versesReadCount + count
            )
            quranDao.insertProgress(updated)
        } else {
            val newProgress = ProgressEntity(
                date = today,
                versesReadCount = count,
                readingTimeSeconds = 0
            )
            quranDao.insertProgress(newProgress)
        }
    }

    suspend fun addReadingTimeToday(seconds: Int) {
        val today = getCurrentDateString()
        val existingProgress = quranDao.getProgressForDate(today)
        if (existingProgress != null) {
            val updated = existingProgress.copy(
                readingTimeSeconds = existingProgress.readingTimeSeconds + seconds
            )
            quranDao.insertProgress(updated)
        } else {
            val newProgress = ProgressEntity(
                date = today,
                versesReadCount = 0,
                readingTimeSeconds = seconds
            )
            quranDao.insertProgress(newProgress)
        }
    }
}
