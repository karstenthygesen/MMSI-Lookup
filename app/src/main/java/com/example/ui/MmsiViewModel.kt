package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MmsiDataSeed
import com.example.data.MmsiEntity
import com.example.data.MmsiRepository
import com.example.data.RecentLookupEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MmsiUiState(
    val inputDigits: String = "",
    val currentResult: MmsiEntity? = null,
    val prefixMatches: List<MmsiEntity> = emptyList(),
    val relatedMids: List<MmsiEntity> = emptyList(),
    val stationTypeDescription: String? = null,
    val isExactMidFound: Boolean = false,
    val recentLookups: List<RecentLookupEntity> = emptyList(),
    val showDirectorySheet: Boolean = false,
    val directorySearchQuery: String = "",
    val selectedRegionFilter: String = "All",
    val directoryResults: List<MmsiEntity> = emptyList()
)

class MmsiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MmsiRepository

    private val _inputDigits = MutableStateFlow("")
    val inputDigits: StateFlow<String> = _inputDigits.asStateFlow()

    private val _showDirectorySheet = MutableStateFlow(false)
    val showDirectorySheet: StateFlow<Boolean> = _showDirectorySheet.asStateFlow()

    private val _directorySearchQuery = MutableStateFlow("")
    val directorySearchQuery: StateFlow<String> = _directorySearchQuery.asStateFlow()

    private val _selectedRegionFilter = MutableStateFlow("All")
    val selectedRegionFilter: StateFlow<String> = _selectedRegionFilter.asStateFlow()

    private val _currentResult = MutableStateFlow<MmsiEntity?>(null)
    private val _relatedMids = MutableStateFlow<List<MmsiEntity>>(emptyList())
    private val _prefixMatches = MutableStateFlow<List<MmsiEntity>>(emptyList())
    private val _stationType = MutableStateFlow<String?>(null)

    val uiState: StateFlow<MmsiUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MmsiRepository(db.mmsiDao())

        // Ensure database has seeds in background
        viewModelScope.launch {
            repository.ensureDatabaseSeeded()
        }

        val allCodesFlow = repository.getAllCodes()
        val recentLookupsFlow = repository.getRecentLookups()

        val directoryFilteredFlow = combine(
            allCodesFlow,
            _directorySearchQuery,
            _selectedRegionFilter
        ) { allCodes, query, region ->
            val list = if (allCodes.isEmpty()) MmsiDataSeed.allCodes else allCodes
            list.filter { entity ->
                val matchesRegion = (region == "All" || entity.region.contains(region, ignoreCase = true))
                val matchesQuery = if (query.isBlank()) {
                    true
                } else {
                    entity.mid.startsWith(query) ||
                    entity.countryName.contains(query, ignoreCase = true) ||
                    entity.isoCode.contains(query, ignoreCase = true)
                }
                matchesRegion && matchesQuery
            }
        }

        uiState = combine(
            _inputDigits,
            _currentResult,
            _prefixMatches,
            _relatedMids,
            _stationType,
            recentLookupsFlow,
            _showDirectorySheet,
            _directorySearchQuery,
            _selectedRegionFilter,
            directoryFilteredFlow
        ) { args ->
            val input = args[0] as String
            val currentResult = args[1] as MmsiEntity?
            val prefixMatches = args[2] as List<MmsiEntity>
            val relatedMids = args[3] as List<MmsiEntity>
            val stationType = args[4] as String?
            val recents = args[5] as List<RecentLookupEntity>
            val showSheet = args[6] as Boolean
            val dirQuery = args[7] as String
            val regionFilter = args[8] as String
            val dirResults = args[9] as List<MmsiEntity>

            MmsiUiState(
                inputDigits = input,
                currentResult = currentResult,
                prefixMatches = prefixMatches,
                relatedMids = relatedMids,
                stationTypeDescription = stationType,
                isExactMidFound = currentResult != null,
                recentLookups = recents,
                showDirectorySheet = showSheet,
                directorySearchQuery = dirQuery,
                selectedRegionFilter = regionFilter,
                directoryResults = dirResults
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MmsiUiState()
        )

        // Seed default selection with a prominent maritime code: 235 (United Kingdom) or empty
        // Let's start with empty so user sees the clean prompt "Enter first 3 digits"
    }

    fun onDigitEntered(digit: Char) {
        if (!digit.isDigit()) return
        if (_inputDigits.value.length >= 9) return
        val newDigits = _inputDigits.value + digit
        updateInput(newDigits)
    }

    fun onBackspace() {
        if (_inputDigits.value.isNotEmpty()) {
            val newDigits = _inputDigits.value.dropLast(1)
            updateInput(newDigits)
        }
    }

    fun onClear() {
        updateInput("")
    }

    fun setInput(digits: String) {
        val sanitized = digits.filter { it.isDigit() }.take(9)
        updateInput(sanitized)
    }

    fun selectMid(mid: String) {
        setInput(mid)
    }

    private fun updateInput(newDigits: String) {
        _inputDigits.value = newDigits
        processMmsi(newDigits)
    }

    private fun processMmsi(input: String) {
        if (input.isEmpty()) {
            _currentResult.value = null
            _relatedMids.value = emptyList()
            _prefixMatches.value = emptyList()
            _stationType.value = null
            return
        }

        // Determine effective 3-digit MID and station format
        val (effectiveMid, stationType) = parseMmsiMid(input)
        _stationType.value = stationType

        if (effectiveMid.length == 3) {
            val result = repository.findInMemory(effectiveMid)
            _currentResult.value = result
            _prefixMatches.value = emptyList()

            if (result != null) {
                // Record in recent lookups
                viewModelScope.launch {
                    repository.recordLookup(result.mid, result.countryName, result.flagEmoji)
                    val countryMids = repository.getCodesForCountry(result.countryName)
                    _relatedMids.value = countryMids.filter { it.mid != result.mid }
                }
            } else {
                _relatedMids.value = emptyList()
            }
        } else {
            // Partial 1 or 2 digits
            _currentResult.value = null
            _relatedMids.value = emptyList()
            val matches = MmsiDataSeed.allCodes.filter { it.mid.startsWith(effectiveMid) }
            _prefixMatches.value = matches.take(12)
        }
    }

    private fun parseMmsiMid(input: String): Pair<String, String?> {
        // Ship Station: MIDxxxxxx (9 digits) -> MID is first 3 digits
        // Coast Station: 00MIDxxxx -> MID is digits 3..5
        // Group Station: 0MIDxxxxx -> MID is digits 2..4
        // SAR Aircraft: 111MIDxxx -> MID is digits 4..6
        // AIS AtoN: 99MIDxxxx -> MID is digits 3..5
        // SART / MOB: 970 / 972 / 974 ...

        if (input.startsWith("00")) {
            val remaining = input.removePrefix("00")
            val mid = remaining.take(3)
            val desc = "Coastal Radio Station (00 MID xxxx)"
            return Pair(mid, desc)
        } else if (input.startsWith("0")) {
            val remaining = input.removePrefix("0")
            val mid = remaining.take(3)
            val desc = "Group Ship Station (0 MID xxxxx)"
            return Pair(mid, desc)
        } else if (input.startsWith("111")) {
            val remaining = input.removePrefix("111")
            val mid = remaining.take(3)
            val desc = "SAR Aircraft Station (111 MID xxx)"
            return Pair(mid, desc)
        } else if (input.startsWith("99")) {
            val remaining = input.removePrefix("99")
            val mid = remaining.take(3)
            val desc = "AIS Aid to Navigation (AtoN) (99 MID xxxx)"
            return Pair(mid, desc)
        } else {
            val mid = input.take(3)
            val desc = if (input.length > 3) {
                "Individual Ship Station (MID xxx xxx) • ${input.length}/9 digits"
            } else null
            return Pair(mid, desc)
        }
    }

    fun setShowDirectorySheet(show: Boolean) {
        _showDirectorySheet.value = show
    }

    fun setDirectoryQuery(query: String) {
        _directorySearchQuery.value = query
    }

    fun setRegionFilter(region: String) {
        _selectedRegionFilter.value = region
    }

    fun deleteRecent(mid: String) {
        viewModelScope.launch {
            repository.deleteRecent(mid)
        }
    }

    fun clearRecents() {
        viewModelScope.launch {
            repository.clearRecents()
        }
    }
}
