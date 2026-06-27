package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE verseKey = :verseKey)")
    fun isBookmarkedFlow(verseKey: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE verseKey = :verseKey)")
    suspend fun isBookmarked(verseKey: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE verseKey = :verseKey")
    suspend fun deleteBookmark(verseKey: String)


    // Favorites
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE verseKey = :verseKey)")
    fun isFavoriteFlow(verseKey: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE verseKey = :verseKey)")
    suspend fun isFavorite(verseKey: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE verseKey = :verseKey")
    suspend fun deleteFavorite(verseKey: String)


    // Notes
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE verseKey = :verseKey LIMIT 1")
    suspend fun getNote(verseKey: String): NoteEntity?

    @Query("SELECT * FROM notes WHERE verseKey = :verseKey LIMIT 1")
    fun getNoteFlow(verseKey: String): Flow<NoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE verseKey = :verseKey")
    suspend fun deleteNote(verseKey: String)


    // Reading History (Recently Opened and Last Read Surah)
    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC")
    fun getReadingHistory(): Flow<List<ReadingHistoryEntity>>

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 1")
    fun getLastReadSurah(): Flow<ReadingHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadingHistory(history: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history WHERE surahId = :surahId")
    suspend fun deleteReadingHistory(surahId: Int)


    // User Progress (Daily Reading Stats)
    @Query("SELECT * FROM user_progress ORDER BY date DESC")
    fun getAllProgress(): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM user_progress WHERE date = :date LIMIT 1")
    suspend fun getProgressForDate(date: String): ProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)
}
