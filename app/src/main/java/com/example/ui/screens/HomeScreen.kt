package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BookmarkEntity
import com.example.data.local.ReadingHistoryEntity
import com.example.data.model.SurahMetadata
import com.example.data.remote.Chapter
import com.example.data.remote.Verse
import com.example.ui.theme.LocalCardGradient
import com.example.ui.viewmodel.QuranViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    onNavigateToReader: (Int, String, Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chapters by viewModel.chaptersState.collectAsStateWithLifecycle()
    val isChaptersLoading by viewModel.isChaptersLoading.collectAsStateWithLifecycle()
    val lastRead by viewModel.lastReadSurah.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val progressStats by viewModel.progressStats.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Surahs, 1: Bookmarks, 2: Statistics

    val filteredChapters = remember(chapters, searchQuery) {
        if (searchQuery.isBlank()) {
            chapters
        } else {
            chapters.filter {
                it.nameSimple.contains(searchQuery, ignoreCase = true) ||
                it.translatedName.name.contains(searchQuery, ignoreCase = true) ||
                it.nameArabic.contains(searchQuery) ||
                it.id.toString() == searchQuery.trim()
            }
        }
    }

    val palette = LocalCardGradient.current

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Noor Quran",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = palette.primary
                        )
                        Text(
                            text = "Assalamu Alaikum, Saifee",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = palette.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Persistent Search Bar with Glassmorphism
            SearchBarComponent(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                palettePrimary = palette.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton(
                    text = "Surahs",
                    icon = Icons.Default.MenuBook,
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    activeColor = palette.primary
                )
                TabButton(
                    text = "Bookmarks",
                    icon = Icons.Default.Bookmark,
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    activeColor = palette.primary
                )
                TabButton(
                    text = "Statistics",
                    icon = Icons.Default.BarChart,
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    activeColor = palette.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (activeTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // Hero / Continuation section
                        item {
                            HeroSection(
                                dailyVerse = viewModel.dailyVerse,
                                lastRead = lastRead,
                                onResumeReading = { lr ->
                                    onNavigateToReader(lr.surahId, lr.surahName, lr.versesCount)
                                },
                                onNavigateToReader = onNavigateToReader
                            )
                        }

                        // Surah Header
                        item {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Search Results" else "All Chapters",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (isChaptersLoading && filteredChapters.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = palette.primary)
                                }
                            }
                        } else if (filteredChapters.isEmpty()) {
                            item {
                                EmptyStateComponent(
                                    message = "No Surahs found matching '$searchQuery'",
                                    icon = Icons.Outlined.SearchOff
                                )
                            }
                        } else {
                            items(filteredChapters, key = { it.id }) { chapter ->
                                SurahItemRow(
                                    chapter = chapter,
                                    onClick = {
                                        onNavigateToReader(chapter.id, chapter.nameSimple, chapter.versesCount)
                                    },
                                    accentColor = palette.primary
                                )
                            }
                        }
                    }
                }
                1 -> {
                    BookmarksTab(
                        bookmarks = bookmarks,
                        onBookmarkClick = { bookmark ->
                            onNavigateToReader(bookmark.surahId, bookmark.surahName, 0) // open surah
                        },
                        onDeleteBookmark = { key -> viewModel.toggleBookmark(
                            verse = Verse(
                                id = 0,
                                verseNumber = 0,
                                verseKey = key,
                                juzNumber = 0,
                                pageNumber = 0,
                                textUthmani = ""
                            ),
                            surahName = ""
                        ) }
                    )
                }
                2 -> {
                    StatisticsTab(
                        progressStats = progressStats,
                        bookmarksCount = bookmarks.size,
                        palette = palette
                    )
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color
) {
    val containerColor = if (selected) activeColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (selected) activeColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                color = contentColor
            )
        }
    }
}

@Composable
fun SearchBarComponent(
    query: String,
    onQueryChange: (String) -> Unit,
    palettePrimary: Color
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("search_bar"),
        placeholder = {
            Text(
                "Search by Surah name, translation or verse...",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = palettePrimary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = palettePrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
    )
}

@Composable
fun HeroSection(
    dailyVerse: com.example.ui.viewmodel.DailyVerse,
    lastRead: ReadingHistoryEntity?,
    onResumeReading: (ReadingHistoryEntity) -> Unit,
    onNavigateToReader: (Int, String, Int) -> Unit
) {
    val palette = LocalCardGradient.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Continue Reading Card (Dynamic)
        if (lastRead != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResumeReading(lastRead) }
                    .testTag("continue_reading_card"),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.12f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    palette.cardGradientStart,
                                    palette.cardGradientEnd
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = "Last Read",
                                tint = palette.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CONTINUE READING",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = palette.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = lastRead.surahName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Verse ${lastRead.lastReadVerse} of ${lastRead.versesCount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Linear progress indicator
                        val progress = if (lastRead.versesCount > 0) {
                            lastRead.lastReadVerse.toFloat() / lastRead.versesCount.toFloat()
                        } else 0f

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = palette.primary,
                            trackColor = palette.primary.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        } else {
            // Default Continue Reading banner pointing to Al-Fatihah
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToReader(1, "Al-Fatihah", 7) },
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.12f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    palette.cardGradientStart,
                                    palette.cardGradientEnd
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "GET STARTED",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = palette.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Al-Fatihah (The Opening)",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Begin reading the Holy Quran beautifully.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Verse Card (Beautiful Hero Banner)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, palette.primary.copy(alpha = 0.2f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                palette.primary.copy(alpha = 0.08f),
                                palette.primary.copy(alpha = 0.02f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Daily Verse",
                                tint = palette.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VERSE OF THE DAY",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = palette.primary
                            )
                        }
                        Text(
                            text = "${dailyVerse.surahName} ${dailyVerse.verseKey}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = dailyVerse.textArabic,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            lineHeight = 42.sp,
                            fontFamily = FontFamily.Serif
                        ),
                        color = palette.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "“${dailyVerse.textTranslation}”",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SurahItemRow(
    chapter: Chapter,
    onClick: () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("surah_item_${chapter.id}"),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index number inside a styled circle outline
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.1f), CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chapter.id.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Surah details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chapter.nameSimple,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chapter.translatedName.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${chapter.versesCount} Verses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Arabic name on the right
            Text(
                text = chapter.nameArabic,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = accentColor,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun BookmarksTab(
    bookmarks: List<BookmarkEntity>,
    onBookmarkClick: (BookmarkEntity) -> Unit,
    onDeleteBookmark: (String) -> Unit
) {
    if (bookmarks.isEmpty()) {
        EmptyStateComponent(
            message = "No Bookmarks Saved Yet",
            icon = Icons.Outlined.BookmarkBorder
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 16.dp)
        ) {
            items(bookmarks, key = { it.verseKey }) { bookmark ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onBookmarkClick(bookmark) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = bookmark.surahName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Verse ${bookmark.verseKey}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            IconButton(onClick = { onDeleteBookmark(bookmark.verseKey) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove bookmark",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = bookmark.arabicText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                textAlign = TextAlign.End,
                                lineHeight = 30.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = bookmark.translationText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticsTab(
    progressStats: List<com.example.data.local.ProgressEntity>,
    bookmarksCount: Int,
    palette: com.example.ui.theme.Palette
) {
    val totalVersesRead = progressStats.sumOf { it.versesReadCount }
    val totalTimeMinutes = progressStats.sumOf { it.readingTimeSeconds } / 60

    // Compute streak
    var currentStreak = 0
    if (progressStats.isNotEmpty()) {
        currentStreak = progressStats.takeWhile { it.versesReadCount > 0 }.size
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Overall Dashboard Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Read",
                    value = "$totalVersesRead",
                    subtitle = "Verses",
                    icon = Icons.Default.MenuBook,
                    accentColor = palette.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Reading Streak",
                    value = "$currentStreak",
                    subtitle = "Days",
                    icon = Icons.Default.LocalFireDepartment,
                    accentColor = Color(0xFFF39C12),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Reading Time",
                    value = "$totalTimeMinutes",
                    subtitle = "Minutes",
                    icon = Icons.Default.Timer,
                    accentColor = palette.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Bookmarks",
                    value = "$bookmarksCount",
                    subtitle = "Saved",
                    icon = Icons.Default.Bookmark,
                    accentColor = palette.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Percentage completed (Quran has 6236 verses)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Quran Completion Progress",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val completionPercentage = if (totalVersesRead >= 6236) 100f else {
                        (totalVersesRead.toFloat() / 6236f) * 100f
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(140.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { completionPercentage / 100f },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 12.dp,
                            color = palette.primary,
                            trackColor = palette.primary.copy(alpha = 0.15f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format("%.2f%%", completionPercentage),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.primary
                            )
                            Text(
                                text = "Completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$totalVersesRead of 6,236 Verses Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Weekly Reading Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Weekly Activity",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    WeeklyChart(progressStats = progressStats, accentColor = palette.primary)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun WeeklyChart(
    progressStats: List<com.example.data.local.ProgressEntity>,
    accentColor: Color
) {
    val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Map last 7 days of reading counts
    val counts = remember(progressStats) {
        val list = MutableList(7) { 0 }
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = Calendar.getInstance()

        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            val stat = progressStats.find { it.date == dateStr }
            // Day of week mapping (Sunday is 1, Monday is 2 in Calendar API)
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2 // Mon:0 to Sun:6
            if (stat != null && index in 0..6) {
                list[index] = stat.versesReadCount
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        list
    }

    val maxVal = maxOf(counts.maxOrNull() ?: 1, 10).toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        counts.forEachIndexed { index, valForDay ->
            val ratio = valForDay.toFloat() / maxVal
            val barHeight = (ratio * 100).coerceAtLeast(4f).dp

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (valForDay > 0) "$valForDay" else "",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    accentColor,
                                    accentColor.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = weekDays[index],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun EmptyStateComponent(
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
