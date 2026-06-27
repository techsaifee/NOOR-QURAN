package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.Verse
import com.example.ui.theme.LocalCardGradient
import com.example.ui.viewmodel.QuranViewModel
import com.example.ui.viewmodel.VersesUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderScreen(
    viewModel: QuranViewModel,
    chapterId: Int,
    chapterName: String,
    versesCount: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val versesState by viewModel.versesState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val palette = LocalCardGradient.current

    // Font size configurations from settings
    val arabicSize by viewModel.arabicFontSize.collectAsStateWithLifecycle()
    val translationSize by viewModel.translationFontSize.collectAsStateWithLifecycle()
    val lineHeightFactor by viewModel.lineHeight.collectAsStateWithLifecycle()
    val translationId by viewModel.translationId.collectAsStateWithLifecycle()

    // Completion State to trigger celebration dialog
    var showCelebration by remember { mutableStateOf(false) }
    var hasCelebrated by remember { mutableStateOf(false) }

    // Notes Bottom Sheet State
    var selectedVerseForNote by remember { mutableStateOf<Verse?>(null) }
    var showNoteSheet by remember { mutableStateOf(false) }
    var currentNoteText by remember { mutableStateOf("") }

    // Load verses on enter or when translation changes
    LaunchedEffect(chapterId, translationId) {
        viewModel.loadVerses(chapterId)
    }

    // Save reading progress dynamically as the user scrolls
    val activeIndex = remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(activeIndex.value, versesState) {
        val state = versesState
        if (state is VersesUiState.Success && state.verses.isNotEmpty()) {
            val idx = activeIndex.value
            if (idx in state.verses.indices) {
                val currentVerse = state.verses[idx]
                viewModel.recordReadingHistory(
                    surahId = chapterId,
                    surahName = chapterName,
                    versesCount = versesCount,
                    verseNumber = currentVerse.verseNumber
                )

                // Check for completion when reaching the very last verse of the surah
                if (currentVerse.verseNumber == versesCount && !hasCelebrated) {
                    hasCelebrated = true
                    showCelebration = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = chapterName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = palette.primary
                        )
                        Text(
                            text = "$versesCount Verses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = versesState) {
                is VersesUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = palette.primary)
                    }
                }
                is VersesUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = "Offline",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadVerses(chapterId) },
                                colors = ButtonDefaults.buttonColors(containerColor = palette.primary)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is VersesUiState.Success -> {
                    val verses = state.verses
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp)
                    ) {
                        // Optional Bismillah header (skip for Surah Al-Fatihah and At-Tawbah)
                        if (chapterId != 1 && chapterId != 9) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            textAlign = TextAlign.Center
                                        ),
                                        color = palette.primary
                                    )
                                }
                            }
                        }

                        itemsIndexed(verses, key = { _, v -> v.id }) { index, verse ->
                            val isBookmarked by viewModel.isBookmarkedFlow(verse.verseKey)
                                .collectAsStateWithLifecycle(initialValue = false)

                            val isFavorite by viewModel.isFavoriteFlow(verse.verseKey)
                                .collectAsStateWithLifecycle(initialValue = false)

                            val noteEntity by viewModel.getNoteFlow(verse.verseKey)
                                .collectAsStateWithLifecycle(initialValue = null)

                            VerseCardComponent(
                                verse = verse,
                                chapterName = chapterName,
                                arabicSize = arabicSize,
                                translationSize = translationSize,
                                lineHeightFactor = lineHeightFactor,
                                isBookmarked = isBookmarked,
                                isFavorite = isFavorite,
                                hasNote = noteEntity != null,
                                onBookmarkToggle = { viewModel.toggleBookmark(verse, chapterName) },
                                onFavoriteToggle = { viewModel.toggleFavorite(verse, chapterName) },
                                onAddNote = {
                                    selectedVerseForNote = verse
                                    currentNoteText = noteEntity?.noteText ?: ""
                                    showNoteSheet = true
                                },
                                onCopyClick = { option ->
                                    val textToCopy = when (option) {
                                        "arabic" -> verse.textUthmani
                                        "translation" -> verse.translations.firstOrNull()?.text ?: ""
                                        else -> "${verse.textUthmani}\n\n${verse.translations.firstOrNull()?.text ?: ""}"
                                    }
                                    copyToClipboard(context, textToCopy)
                                },
                                onShareClick = {
                                    val shareText = "$chapterName\nVerse ${verse.verseKey}\n\n${verse.textUthmani}\n\n${verse.translations.firstOrNull()?.text ?: ""}\n\nSource: Quran.com"
                                    shareVerse(context, shareText)
                                },
                                accentColor = palette.primary
                            )
                        }
                    }

                    // Progress indicator overlay at the bottom
                    val readProgress = if (verses.isNotEmpty()) {
                        (activeIndex.value.toFloat() / verses.size.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(16.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Juz ${verses.firstOrNull()?.juzNumber ?: "-"} • Page ${verses.firstOrNull()?.pageNumber ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = palette.primary
                            )
                            Text(
                                text = "Reading Progress: ${(readProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            // Note Bottom Dialog
            if (showNoteSheet && selectedVerseForNote != null) {
                Dialog(onDismissRequest = { showNoteSheet = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Add Note for Verse ${selectedVerseForNote?.verseKey}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = palette.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = currentNoteText,
                                onValueChange = { currentNoteText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .testTag("note_input"),
                                placeholder = { Text("Write your thoughts or reflection here...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = palette.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showNoteSheet = false }) {
                                    Text("Cancel")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.saveNote(
                                            verseKey = selectedVerseForNote!!.verseKey,
                                            surahName = chapterName,
                                            verseNumber = selectedVerseForNote!!.verseNumber,
                                            surahId = chapterId,
                                            noteText = currentNoteText
                                        )
                                        showNoteSheet = false
                                        Toast.makeText(context, "Note Saved Successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = palette.primary),
                                    modifier = Modifier.testTag("save_note_button")
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }

            // Premium Completion Celebration Dialog
            if (showCelebration) {
                CelebrationDialog(
                    onDismiss = { showCelebration = false },
                    onContinue = {
                        showCelebration = false
                        onNavigateBack()
                    }
                )
            }
        }
    }
}

@Composable
fun VerseCardComponent(
    verse: Verse,
    chapterName: String,
    arabicSize: Float,
    translationSize: Float,
    lineHeightFactor: Float,
    isBookmarked: Boolean,
    isFavorite: Boolean,
    hasNote: Boolean,
    onBookmarkToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onAddNote: () -> Unit,
    onCopyClick: (String) -> Unit,
    onShareClick: () -> Unit,
    accentColor: Color
) {
    var expandedCopyMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("verse_card_${verse.verseNumber}")
    ) {
        // Verse Info Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = verse.verseNumber.toString(),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Juz ${verse.juzNumber} • Page ${verse.pageNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. Arabic Verse Glass Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = verse.textUthmani,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontSize = arabicSize.sp,
                        lineHeight = (arabicSize * lineHeightFactor).sp,
                        textAlign = TextAlign.End
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. English Translation Glass Card
        val translationText = verse.translations.firstOrNull()?.text ?: ""
        // Clean HTML tags if present (Quran.com sometimes includes <sup> tags)
        val cleanTranslation = translationText.replace(Regex("<[^>]*>"), "")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = cleanTranslation,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = translationSize.sp,
                        lineHeight = (translationSize * lineHeightFactor).sp,
                        textAlign = TextAlign.Start
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                // Favorite Button
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.testTag("favorite_button_${verse.verseNumber}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Bookmark Button
                IconButton(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.testTag("bookmark_button_${verse.verseNumber}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Notes Button
                IconButton(
                    onClick = onAddNote,
                    modifier = Modifier.testTag("notes_button_${verse.verseNumber}")
                ) {
                    Icon(
                        imageVector = if (hasNote) Icons.Default.Notes else Icons.Outlined.Notes,
                        contentDescription = "Add Reflection Note",
                        tint = if (hasNote) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Audio Playback button (Architecture ready for future support)
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.VolumeUp,
                        contentDescription = "Play verse audio",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }

                // Copy Menu Trigger
                Box {
                    IconButton(onClick = { expandedCopyMenu = true }) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy options",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedCopyMenu,
                        onDismissRequest = { expandedCopyMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy Arabic") },
                            onClick = {
                                onCopyClick("arabic")
                                expandedCopyMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy Translation") },
                            onClick = {
                                onCopyClick("translation")
                                expandedCopyMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy Both") },
                            onClick = {
                                onCopyClick("both")
                                expandedCopyMenu = false
                            }
                        )
                    }
                }

                // Share Button
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun CelebrationDialog(
    onDismiss: () -> Unit,
    onContinue: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, Color(0xFFF1C40F))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Beautiful interactive confetti rising & falling behind
                ConfettiCanvas(modifier = Modifier.matchParentSize())

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉",
                        fontSize = 54.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "WELL DONE SAIFEE!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = Color(0xFFD4AF37),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "May Allah bless you with beneficial knowledge.\n\nMay He make the Quran the light of your heart, the guide of your life, and increase you in understanding.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Keep learning and keep reading.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close")
                        }
                        Button(
                            onClick = onContinue,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Continue", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiCanvas(modifier: Modifier = Modifier) {
    val particles = remember {
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 1.5f - 0.5f,
                speed = Random.nextFloat() * 2f + 1f,
                color = Color(
                    red = Random.nextFloat(),
                    green = Random.nextFloat(),
                    blue = Random.nextFloat(),
                    alpha = 1f
                ),
                size = Random.nextFloat() * 12f + 8f
            )
        }
    }

    var animateTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            particles.forEach { p ->
                p.y += p.speed * 0.005f
                if (p.y > 1f) {
                    p.y = -0.1f
                    p.x = Random.nextFloat()
                }
            }
            animateTrigger++
        }
    }

    Canvas(modifier = modifier) {
        // Force redraw on animateTrigger change
        animateTrigger.let { }
        particles.forEach { p ->
            val px = p.x * size.width
            val py = p.y * size.height
            drawCircle(
                color = p.color,
                radius = p.size,
                center = Offset(px, py)
            )
        }
    }
}

class ConfettiParticle(
    var x: Float,
    var y: Float,
    val speed: Float,
    val color: Color,
    val size: Float
)

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Noor Quran", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
}

private fun shareVerse(context: Context, shareText: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }
    context.startActivity(Intent.createChooser(intent, "Share Quran Verse"))
}
