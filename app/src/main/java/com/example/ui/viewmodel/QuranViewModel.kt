package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.SurahMetadata
import com.example.data.remote.Chapter
import com.example.data.remote.QuranRetrofitClient
import com.example.data.remote.Verse
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

sealed interface VersesUiState {
    object Loading : VersesUiState
    data class Success(val verses: List<Verse>) : VersesUiState
    data class Error(val message: String) : VersesUiState
}

data class DailyVerse(
    val verseKey: String,
    val surahName: String,
    val textArabic: String,
    val textTranslation: String
)

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val database = QuranDatabase.getDatabase(application)
    private val repository = QuranRepository(
        quranDao = database.quranDao(),
        apiService = QuranRetrofitClient.apiService
    )
    val settingsManager = SettingsManager(application)

    // --- Settings UI State ---
    val themeMode = settingsManager.themeMode
    val arabicFontSize = settingsManager.arabicFontSize
    val translationFontSize = settingsManager.translationFontSize
    val lineHeight = settingsManager.lineHeight
    val translationId = settingsManager.translationId
    val accentColor = settingsManager.accentColor

    // --- Chapters ---
    private val _chaptersState = MutableStateFlow<List<Chapter>>(SurahMetadata.fallbackChapters)
    val chaptersState: StateFlow<List<Chapter>> = _chaptersState.asStateFlow()

    private val _isChaptersLoading = MutableStateFlow(false)
    val isChaptersLoading: StateFlow<Boolean> = _isChaptersLoading.asStateFlow()

    // --- Verses ---
    private val _versesState = MutableStateFlow<VersesUiState>(VersesUiState.Loading)
    val versesState: StateFlow<VersesUiState> = _versesState.asStateFlow()

    // --- Local Data Flows ---
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = repository.allFavorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val readingHistory: StateFlow<List<ReadingHistoryEntity>> = repository.readingHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastReadSurah: StateFlow<ReadingHistoryEntity?> = repository.lastReadSurah
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val progressStats: StateFlow<List<ProgressEntity>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Daily Verse (Instant, Offline-first, dynamic) ---
    val dailyVerse: DailyVerse
        get() {
            val dailyVerses = listOf(
                DailyVerse(
                    "2:152",
                    "Al-Baqarah",
                    "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
                    "So remember Me; I will remember you. And be grateful to Me and do not deny Me."
                ),
                DailyVerse(
                    "94:6",
                    "Ash-Sharh",
                    "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
                    "Indeed, with hardship [will be] ease."
                ),
                DailyVerse(
                    "14:7",
                    "Ibrahim",
                    "لَئِنْ شَكَرْتُمْ لَأَزِيدَنَّكُمْ ۖ وَلَئِنْ كَفَرْتُمْ إِنَّ عَذَابِي لَشَدِيدٌ",
                    "If you are grateful, I will surely increase you [in favor]; but if you deny, indeed, My punishment is severe."
                ),
                DailyVerse(
                    "39:53",
                    "Az-Zumar",
                    "قُلْ يَا عِبَادِيَ الَّذِينَ أَسْرَفُوا عَلَىٰ أَنْفُسِهِمْ لَا تَقْنَطُوا مِنْ رَحْمَةِ اللَّهِ",
                    "Say, 'O My servants who have transgressed against themselves, do not despair of the mercy of Allah.'"
                ),
                DailyVerse(
                    "2:286",
                    "Al-Baqarah",
                    "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا",
                    "Allah does not burden a soul beyond that it can bear."
                )
            )
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            return dailyVerses[dayOfYear % dailyVerses.size]
        }

    init {
        loadChapters()
    }

    fun loadChapters() {
        viewModelScope.launch {
            _isChaptersLoading.value = true
            try {
                val remoteChapters = repository.getChapters()
                if (remoteChapters.isNotEmpty()) {
                    _chaptersState.value = remoteChapters
                }
            } catch (e: Exception) {
                // Keep fallbacks on exception, fail gracefully
                _chaptersState.value = SurahMetadata.fallbackChapters
            } finally {
                _isChaptersLoading.value = false
            }
        }
    }

    fun loadVerses(chapterId: Int) {
        viewModelScope.launch {
            _versesState.value = VersesUiState.Loading
            try {
                val loadedVerses = repository.getVerses(chapterId, translationId.value)
                _versesState.value = VersesUiState.Success(loadedVerses)
            } catch (e: Exception) {
                _versesState.value = VersesUiState.Error(e.localizedMessage ?: "Failed to load verses. Check connection.")
            }
        }
    }

    // --- Bookmarks ---
    fun toggleBookmark(verse: Verse, surahName: String) {
        viewModelScope.launch {
            val key = verse.verseKey
            if (repository.isBookmarked(key)) {
                repository.removeBookmark(key)
            } else {
                val translationText = verse.translations.firstOrNull()?.text ?: ""
                repository.addBookmark(
                    BookmarkEntity(
                        verseKey = key,
                        surahId = verse.id, // using id or chapter_id mapping
                        surahName = surahName,
                        verseNumber = verse.verseNumber,
                        arabicText = verse.textUthmani,
                        translationText = translationText
                    )
                )
            }
        }
    }

    fun isBookmarkedFlow(verseKey: String): Flow<Boolean> = repository.isBookmarkedFlow(verseKey)

    // --- Favorites ---
    fun toggleFavorite(verse: Verse, surahName: String) {
        viewModelScope.launch {
            val key = verse.verseKey
            if (repository.isFavorite(key)) {
                repository.removeFavorite(key)
            } else {
                val translationText = verse.translations.firstOrNull()?.text ?: ""
                repository.addFavorite(
                    FavoriteEntity(
                        verseKey = key,
                        surahId = verse.id,
                        surahName = surahName,
                        verseNumber = verse.verseNumber,
                        arabicText = verse.textUthmani,
                        translationText = translationText
                    )
                )
            }
        }
    }

    fun isFavoriteFlow(verseKey: String): Flow<Boolean> = repository.isFavoriteFlow(verseKey)

    // --- Notes ---
    fun saveNote(verseKey: String, surahName: String, verseNumber: Int, surahId: Int, noteText: String) {
        viewModelScope.launch {
            if (noteText.isBlank()) {
                repository.removeNote(verseKey)
            } else {
                repository.addNote(
                    NoteEntity(
                        verseKey = verseKey,
                        surahId = surahId,
                        surahName = surahName,
                        verseNumber = verseNumber,
                        noteText = noteText
                    )
                )
            }
        }
    }

    fun getNoteFlow(verseKey: String): Flow<NoteEntity?> = repository.getNoteFlow(verseKey)

    // --- Reading Progress/History ---
    fun recordReadingHistory(surahId: Int, surahName: String, versesCount: Int, verseNumber: Int) {
        viewModelScope.launch {
            repository.addReadingHistory(
                ReadingHistoryEntity(
                    surahId = surahId,
                    surahName = surahName,
                    versesCount = versesCount,
                    lastReadVerse = verseNumber
                )
            )
            // Log that a verse was read today
            repository.incrementVersesReadToday(1)
        }
    }

    fun addReadingTime(seconds: Int) {
        viewModelScope.launch {
            repository.addReadingTimeToday(seconds)
        }
    }
}
