package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.LocalCardGradient
import com.example.ui.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: QuranViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val arabicSize by viewModel.arabicFontSize.collectAsStateWithLifecycle()
    val translationSize by viewModel.translationFontSize.collectAsStateWithLifecycle()
    val lineHeight by viewModel.lineHeight.collectAsStateWithLifecycle()
    val translationId by viewModel.translationId.collectAsStateWithLifecycle()
    val accentColor by viewModel.accentColor.collectAsStateWithLifecycle()

    val palette = LocalCardGradient.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = palette.primary
                    )
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
                actions = {
                    IconButton(
                        onClick = { viewModel.settingsManager.resetSettings() },
                        modifier = Modifier.testTag("reset_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Settings",
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Appearance & Accent Colors
            SettingsSectionCard(title = "Appearance") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Theme Choice
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            text = "Light",
                            selected = themeMode == "light",
                            onClick = { viewModel.settingsManager.setThemeMode("light") },
                            accentColor = palette.primary,
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            text = "Dark",
                            selected = themeMode == "dark",
                            onClick = { viewModel.settingsManager.setThemeMode("dark") },
                            accentColor = palette.primary,
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            text = "Night Reading",
                            selected = themeMode == "night",
                            onClick = { viewModel.settingsManager.setThemeMode("night") },
                            accentColor = palette.primary,
                            modifier = Modifier.weight(1.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Accent Color Choice
                    Text(
                        text = "Accent Color",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val accents = listOf(
                            Pair("Emerald", Color(0xFF0F9D58)),
                            Pair("Gold", Color(0xFFFFD700)),
                            Pair("Teal", Color(0xFF008080)),
                            Pair("RoyalBlue", Color(0xFF4169E1)),
                            Pair("Crimson", Color(0xFFDC143C))
                        )
                        accents.forEach { (name, color) ->
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { viewModel.settingsManager.setAccentColor(name) }
                                    .border(
                                        width = if (accentColor == name) 3.dp else 0.dp,
                                        color = if (accentColor == name) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (accentColor == name) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = if (name == "Gold") Color.Black else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Reading Fonts & Sizes
            SettingsSectionCard(title = "Typography Settings") {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Preview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.primary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontSize = arabicSize.sp,
                                    lineHeight = (arabicSize * lineHeight).sp,
                                    textAlign = TextAlign.Center
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = translationSize.sp,
                                    lineHeight = (translationSize * lineHeight).sp,
                                    textAlign = TextAlign.Center
                                ),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Arabic Font Size
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Arabic Font Size",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${arabicSize.toInt()} sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.primary
                            )
                        }
                        Slider(
                            value = arabicSize,
                            onValueChange = { viewModel.settingsManager.setArabicFontSize(it) },
                            valueRange = 24f..48f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.primary,
                                activeTrackColor = palette.primary
                            )
                        )
                    }

                    // Translation Font Size
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Translation Font Size",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${translationSize.toInt()} sp",
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.primary
                            )
                        }
                        Slider(
                            value = translationSize,
                            onValueChange = { viewModel.settingsManager.setTranslationFontSize(it) },
                            valueRange = 12f..24f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.primary,
                                activeTrackColor = palette.primary
                            )
                        )
                    }

                    // Line Height multiplier
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Line Height Multiplier",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = String.format("%.1f x", lineHeight),
                                style = MaterialTheme.typography.bodySmall,
                                color = palette.primary
                            )
                        }
                        Slider(
                            value = lineHeight,
                            onValueChange = { viewModel.settingsManager.setLineHeight(it) },
                            valueRange = 1.2f..2.2f,
                            colors = SliderDefaults.colors(
                                thumbColor = palette.primary,
                                activeTrackColor = palette.primary
                            )
                        )
                    }
                }
            }

            // Section 3: Translation Sources
            SettingsSectionCard(title = "Translation Source") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TranslationSourceOption(
                        title = "Abdel Haleem (Oxford)",
                        subtitle = "Highly clear, contemporary English translation",
                        selected = translationId == "85",
                        onClick = { viewModel.settingsManager.setTranslationId("85") },
                        accentColor = palette.primary
                    )
                    TranslationSourceOption(
                        title = "Sahih International",
                        subtitle = "Standard modern academic translation",
                        selected = translationId == "20",
                        onClick = { viewModel.settingsManager.setTranslationId("20") },
                        accentColor = palette.primary
                    )
                    TranslationSourceOption(
                        title = "Yusuf Ali",
                        subtitle = "Classic poetic translation",
                        selected = translationId == "22",
                        onClick = { viewModel.settingsManager.setTranslationId("22") },
                        accentColor = palette.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun ThemeOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) accentColor else MaterialTheme.colorScheme.surface
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    val borderColor = if (selected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Card(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TranslationSourceOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color
) {
    val borderColor = if (selected) accentColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val background = if (selected) accentColor.copy(alpha = 0.08f) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = accentColor)
            )
        }
    }
}
