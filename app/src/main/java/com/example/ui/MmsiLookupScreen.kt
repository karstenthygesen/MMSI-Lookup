package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DirectionsBoat
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MmsiEntity
import com.example.ui.components.MmsiDirectorySheet
import com.example.ui.components.MmsiInfoCard
import com.example.ui.components.MmsiInfoDialog
import com.example.ui.components.MmsiKeypad
import com.example.ui.components.SmallFlag
import com.example.ui.theme.MaritimeBlue
import com.example.ui.theme.MaritimeCyan
import com.example.ui.theme.MaritimeGreen
import com.example.ui.theme.MaritimeTheme
import com.example.ui.theme.NauticalGold
import com.example.ui.theme.OceanNavyDark
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MmsiLookupScreen(
    viewModel: MmsiViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MMSI Country Lookup",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = MaritimeTheme.accent,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = if (uiState.isBridgeNightMode) "Bridge Night Mode (Red) • Offline" else "Offline DB • Updated ${uiState.lastUpdatedDate}",
                                fontSize = 11.sp,
                                color = MaritimeTheme.accent,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setShowInfoDialog(true) },
                        modifier = Modifier.testTag("mmsi_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "MMSI guide, database update and contact",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Coast Guard Bridge Night Mode Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleBridgeNightMode() },
                        modifier = Modifier.testTag("bridge_night_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isBridgeNightMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (uiState.isBridgeNightMode) "Switch to Standard Day Mode" else "Switch to Coast Guard Bridge Night Mode (Preserve Night Vision)",
                            tint = if (uiState.isBridgeNightMode) MaritimeTheme.accent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setShowDirectorySheet(true) },
                        modifier = Modifier.testTag("open_directory_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                            contentDescription = "Browse all ITU codes",
                            tint = MaritimeTheme.accent
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            MmsiKeypad(
                onDigitClick = { viewModel.onDigitEntered(it) },
                onBackspaceClick = { viewModel.onBackspace() },
                onClearClick = { viewModel.onClear() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Background Weekly Auto-Sync In-Progress Banner
            AnimatedVisibility(visible = uiState.isAutoUpdating) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaritimeTheme.accent.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaritimeTheme.accent.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaritimeTheme.accent
                        )
                        Text(
                            text = "Auto-syncing ITU maritime database (Weekly update)...",
                            fontSize = 12.sp,
                            color = MaritimeTheme.accent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3-Digit Input Boxes Display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digit_input_container"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "FIRST 3 DIGITS (MID)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SlateTextSecondary
                    )

                    // 3 Big Digit Slots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val input = uiState.inputDigits
                        val d1 = input.getOrNull(0)?.toString() ?: ""
                        val d2 = input.getOrNull(1)?.toString() ?: ""
                        val d3 = input.getOrNull(2)?.toString() ?: ""

                        DigitSlot(d1, isActive = input.isEmpty())
                        DigitSlot(d2, isActive = input.length == 1)
                        DigitSlot(d3, isActive = input.length == 2)
                    }

                    // If more than 3 digits were entered (full MMSI format)
                    if (uiState.inputDigits.length > 3) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Text(
                                text = "Full MMSI: ${uiState.inputDigits.take(3)} ${uiState.inputDigits.drop(3)}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaritimeTheme.accent
                            )
                        }
                    }

                    // Helper label & quick clear
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                uiState.inputDigits.isEmpty() -> "Type 3 digits to identify vessel country"
                                uiState.inputDigits.length < 3 -> "Enter ${3 - uiState.inputDigits.length} more digit(s)"
                                else -> "Maritime Identification Digit verified"
                            },
                            fontSize = 11.sp,
                            color = if (uiState.isExactMidFound) MaritimeTheme.success else MaritimeTheme.textSecondary
                        )

                        if (uiState.inputDigits.isNotEmpty()) {
                            Text(
                                text = "Reset",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaritimeTheme.gold,
                                modifier = Modifier
                                    .clickable { viewModel.onClear() }
                                    .padding(4.dp)
                                    .testTag("reset_input_btn")
                            )
                        }
                    }
                }
            }

            // Recent Lookups (if any)
            if (uiState.recentLookups.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Lookups",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaritimeTheme.textSecondary
                        )
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            color = MaritimeTheme.textSecondary,
                            modifier = Modifier
                                .clickable { viewModel.clearRecents() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("clear_recents_btn")
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.recentLookups, key = { it.mid }) { recent ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (uiState.isBridgeNightMode) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)) else null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectMid(recent.mid) }
                                    .testTag("recent_${recent.mid}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SmallFlag(
                                        flagEmoji = recent.flagEmoji,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = recent.mid,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RESULTS AREA
            AnimatedContent(
                targetState = uiState,
                label = "mmsi_result_transition"
            ) { state ->
                when {
                    // Exact match found!
                    state.currentResult != null -> {
                        MmsiInfoCard(
                            result = state.currentResult,
                            relatedMids = state.relatedMids,
                            stationTypeDescription = state.stationTypeDescription,
                            onMidClick = { viewModel.selectMid(it) }
                        )
                    }

                    // 3 digits entered, but no match (unallocated ITU code)
                    state.inputDigits.length >= 3 -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("unallocated_code_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 36.sp
                                )
                                Text(
                                    text = "MID ${state.inputDigits.take(3)} is Unallocated",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "This 3-digit code is not currently assigned to any maritime nation under ITU-R Recommendation M.585. Valid maritime identification digits range between 201 and 775.",
                                    fontSize = 13.sp,
                                    color = SlateTextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // 1 or 2 digits entered (Show prefix matches)
                    state.inputDigits.isNotEmpty() && state.prefixMatches.isNotEmpty() -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("prefix_matches_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Countries in '${state.inputDigits}' series (${state.prefixMatches.size}):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateTextSecondary
                                )

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    state.prefixMatches.forEach { match ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.selectMid(match.mid) }
                                                .testTag("match_${match.mid}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = match.flagEmoji,
                                                    fontSize = 20.sp
                                                )
                                                Text(
                                                    text = match.countryName,
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = match.mid,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaritimeTheme.accent
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Empty state (initial clean guide)
                    else -> {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("empty_guide_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaritimeTheme.accent.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBoat,
                                        contentDescription = null,
                                        tint = MaritimeTheme.accent,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Text(
                                    text = "Ready to Identify Vessel",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "Enter any 3-digit MID code (e.g. 235 for United Kingdom, 338 for USA, 351 for Panama) using the keypad below. Full 9-digit MMSIs are also accepted.",
                                    fontSize = 13.sp,
                                    color = MaritimeTheme.textSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaritimeTheme.success,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Works 100% offline at sea with local SQLite Room database",
                                        fontSize = 11.sp,
                                        color = MaritimeTheme.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Directory Bottom Sheet
    if (uiState.showDirectorySheet) {
        MmsiDirectorySheet(
            sheetState = sheetState,
            results = uiState.directoryResults,
            searchQuery = uiState.directorySearchQuery,
            selectedRegion = uiState.selectedRegionFilter,
            onSearchChange = { viewModel.setDirectoryQuery(it) },
            onRegionSelect = { viewModel.setRegionFilter(it) },
            onEntrySelect = {
                viewModel.selectMid(it.mid)
                viewModel.setShowDirectorySheet(false)
            },
            onDismiss = { viewModel.setShowDirectorySheet(false) }
        )
    }

    // Info & Database Update Dialog
    if (uiState.showInfoDialog) {
        MmsiInfoDialog(
            lastUpdatedDate = uiState.lastUpdatedDate,
            databaseVersion = uiState.databaseVersion,
            isUpdating = uiState.isUpdatingDatabase,
            isAutoUpdating = uiState.isAutoUpdating,
            isInternetConnected = uiState.isInternetConnected,
            nextAutoUpdateDueText = uiState.nextAutoUpdateDueText,
            isWeeklyUpdateDue = uiState.isWeeklyUpdateDue,
            isAutoNightModeEnabled = uiState.isAutoNightModeEnabled,
            isAfterSundown = uiState.isAfterSundown,
            solarStatusDescription = uiState.solarStatusDescription,
            solarSunriseTimeText = uiState.solarSunriseTimeText,
            solarSunsetTimeText = uiState.solarSunsetTimeText,
            solarTimeZoneId = uiState.solarTimeZoneId,
            onToggleAutoNightMode = { viewModel.toggleAutoNightMode() },
            updateResultMessage = uiState.updateResultMessage,
            updateResultSuccess = uiState.updateResultSuccess,
            onUpdateClick = { viewModel.updateDatabaseOnline() },
            onDismiss = {
                viewModel.setShowInfoDialog(false)
                viewModel.clearUpdateMessage()
            }
        )
    }
}

@Composable
private fun DigitSlot(
    digit: String,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = MaritimeTheme.accent
    val goldColor = MaritimeTheme.gold
    val borderColor = when {
        digit.isNotEmpty() -> accentColor
        isActive -> goldColor
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Box(
        modifier = modifier
            .size(width = 68.dp, height = 76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .testTag("digit_slot_${if (digit.isNotEmpty()) digit else "empty"}"),
        contentAlignment = Alignment.Center
    ) {
        if (digit.isNotEmpty()) {
            Text(
                text = digit,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = "-",
                fontSize = 28.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
