package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val verseKey: String, // e.g. "1:1"
    val surahId: Int,
    val surahName: String,
    val verseNumber: Int,
    val arabicText: String,
    val translationText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val verseKey: String, // e.g. "1:1"
    val surahId: Int,
    val surahName: String,
    val verseNumber: Int,
    val arabicText: String,
    val translationText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val verseKey: String, // e.g. "1:1"
    val surahId: Int,
    val surahName: String,
    val verseNumber: Int,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val surahId: Int,
    val surahName: String,
    val versesCount: Int,
    val lastReadVerse: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_progress")
data class ProgressEntity(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val versesReadCount: Int,
    val readingTimeSeconds: Int
)
