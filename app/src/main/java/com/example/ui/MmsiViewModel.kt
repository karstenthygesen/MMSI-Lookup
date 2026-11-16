package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MmsiDataSeed
import com.example.data.MmsiEntity
import com.example.data.MmsiRepository
import com.example.data.NetworkMonitor
import com.example.data.RecentLookupEntity
import com.example.util.SolarTimeCalculator
import com.example.util.SolarTwilightInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MmsiUiState(
    val inputDigits: String = "",
    val currentResult: MmsiEntity? = null,
    val prefixMatches: List<MmsiEntity> = emptyList(),
    val relatedMids: List<MmsiEntity> = emptyList(),
    val stationTypeDescription: String? = null,
    val isExactMidFound: Boolean = false,
    val recentLookups: List<RecentLookupEntity> = emptyList(),
    val showDirectorySheet: Boolean = false,
    val showInfoDialog: Boolean = false,
    val directorySearchQuery: String = "",
    val selectedRegionFilter: String = "All",
    val directoryResults: List<MmsiEntity> = emptyList(),
    val lastUpdatedDate: String = AppDatabase.DEFAULT_LAST_UPDATED,
    val databaseVersion: String = AppDatabase.DEFAULT_DB_VERSION,
    val isBridgeNightMode: Boolean = false,
    val isAutoNightModeEnabled: Boolean = true,
    val isAfterSundown: Boolean = false,
    val solarStatusDescription: String = "",
    val solarSunriseTimeText: String = "",
    val solarSunsetTimeText: String = "",
    val solarTimeZoneId: String = "",
    val isUpdatingDatabase: Boolean = false,
    val isAutoUpdating: Boolean = false,
    val updateResultMessage: String? = null,
    val updateResultSuccess: Boolean? = null,
    val isInternetConnected: Boolean = false,
    val nextAutoUpdateDueText: String = "",
    val isWeeklyUpdateDue: Boolean = false
)

class MmsiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MmsiRepository
    private val networkMonitor = NetworkMonitor(application)

    private val _inputDigits = MutableStateFlow("")
    val inputDigits: StateFlow<String> = _inputDigits.asStateFlow()

    private val _showDirectorySheet = MutableStateFlow(false)
    val showDirectorySheet: StateFlow<Boolean> = _showDirectorySheet.asStateFlow()

    private val _showInfoDialog = MutableStateFlow(false)
    val showInfoDialog: StateFlow<Boolean> = _showInfoDialog.asStateFlow()

    private val _directorySearchQuery = MutableStateFlow("")
    val directorySearchQuery: StateFlow<String> = _directorySearchQuery.asStateFlow()

    private val _selectedRegionFilter = MutableStateFlow("All")
    val selectedRegionFilter: StateFlow<String> = _selectedRegionFilter.asStateFlow()

    private val _currentResult = MutableStateFlow<MmsiEntity?>(null)
    private val _relatedMids = MutableStateFlow<List<MmsiEntity>>(emptyList())
    private val _prefixMatches = MutableStateFlow<List<MmsiEntity>>(emptyList())
    private val _stationType = MutableStateFlow<String?>(null)

    private val _isUpdatingDatabase = MutableStateFlow(false)
    private val _isAutoUpdating = MutableStateFlow(false)
    private val _updateResultMessage = MutableStateFlow<String?>(null)
    private val _updateResultSuccess = MutableStateFlow<Boolean?>(null)
    private val _isInternetConnected = MutableStateFlow(networkMonitor.hasCurrentInternetConnection())
    private val _nextAutoUpdateDueText = MutableStateFlow("Checking schedule...")
    private val _isWeeklyUpdateDue = MutableStateFlow(false)

    private val _solarTwilightInfo = MutableStateFlow(SolarTimeCalculator.getSolarTwilightInfo(application))

    private var lastAutoUpdateAttemptTime = 0L

    val uiState: StateFlow<MmsiUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MmsiRepository(db.mmsiDao())

        // Calculate astronomical sundown/sunrise based on device TimeZone (no location permission required)
        val initialTwilight = SolarTimeCalculator.getSolarTwilightInfo(application)
        _solarTwilightInfo.value = initialTwilight

        viewModelScope.launch {
            repository.ensureDatabaseSeeded()

            // If auto night mode is enabled, sync bridge night mode with current astronomical time:
            // Turns ON during night, turns OFF during daytime!
            val autoNightEnabled = repository.isAutoNightModeEnabledDirect()
            if (autoNightEnabled) {
                repository.setBridgeNightMode(initialTwilight.isAfterSundown)
            }
        }

        // Real-time network detection: automatically trigger update when internet is connected and eligible (once per week)
        viewModelScope.launch {
            networkMonitor.isConnected.collect { connected ->
                _isInternetConnected.value = connected
                if (connected) {
                    checkAndAutoUpdateIfEligible()
                }
            }
        }

        // Continuously update schedule information based on last update timestamp
        viewModelScope.launch {
            repository.getLastUpdateTimestamp().collect { lastTimestamp ->
                updateScheduleInfo(lastTimestamp)
            }
        }

        val allCodesFlow = repository.getAllCodes()
        val recentLookupsFlow = repository.getRecentLookups()
        val lastUpdatedFlow = repository.getLastUpdatedDate()
        val dbVersionFlow = repository.getDatabaseVersion()
        val bridgeNightModeFlow = repository.getBridgeNightMode()
        val autoNightModeFlow = repository.getAutoNightModeEnabled()

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

        val baseInfoFlow = combine(
            _inputDigits,
            _currentResult,
            _prefixMatches,
            _relatedMids,
            _stationType
        ) { input, result, matches, related, stationType ->
            BaseLookupState(input, result, matches, related, stationType)
        }

        val syncStateFlow = combine(
            _isUpdatingDatabase,
            _isAutoUpdating,
            _updateResultMessage,
            _updateResultSuccess,
            _isInternetConnected
        ) { isUpdating, isAuto, updateMsg, updateSuccess, isConnected ->
            SyncState(isUpdating, isAuto, updateMsg, updateSuccess, isConnected)
        }

        val scheduleStateFlow = combine(
            lastUpdatedFlow,
            dbVersionFlow,
            _nextAutoUpdateDueText,
            _isWeeklyUpdateDue
        ) { lastUpdated, dbVersion, nextDueText, isDue ->
            ScheduleState(lastUpdated, dbVersion, nextDueText, isDue)
        }

        val nightModeFlow = combine(
            bridgeNightModeFlow,
            autoNightModeFlow,
            _solarTwilightInfo
        ) { isBridgeNight, isAutoNight, solarInfo ->
            NightModeState(
                isBridgeNight = isBridgeNight,
                isAutoNight = isAutoNight,
                isAfterSundown = solarInfo.isAfterSundown,
                statusDescription = solarInfo.statusDescription,
                sunriseTime = solarInfo.sunriseTimeText,
                sunsetTime = solarInfo.sunsetTimeText,
                timeZoneId = solarInfo.timeZoneId
            )
        }

        val metaFlow = combine(syncStateFlow, scheduleStateFlow, nightModeFlow) { sync, sched, night ->
            MetaState(
                lastUpdated = sched.lastUpdated,
                dbVersion = sched.dbVersion,
                isBridgeNightMode = night.isBridgeNight,
                isAutoNightModeEnabled = night.isAutoNight,
                isAfterSundown = night.isAfterSundown,
                solarStatusDescription = night.statusDescription,
                solarSunriseTimeText = night.sunriseTime,
                solarSunsetTimeText = night.sunsetTime,
                solarTimeZoneId = night.timeZoneId,
                nextAutoUpdateDueText = sched.nextDueText,
                isWeeklyUpdateDue = sched.isDue,
                isUpdating = sync.isUpdating,
                isAutoUpdating = sync.isAutoUpdating,
                updateMsg = sync.updateMsg,
                updateSuccess = sync.updateSuccess,
                isInternetConnected = sync.isConnected
            )
        }

        val dialogFlow = combine(
            _showDirectorySheet,
            _showInfoDialog,
            _directorySearchQuery,
            _selectedRegionFilter
        ) { showDir, showInfo, dirQuery, regFilter ->
            DialogState(showDir, showInfo, dirQuery, regFilter)
        }

        uiState = combine(
            baseInfoFlow,
            recentLookupsFlow,
            directoryFilteredFlow,
            dialogFlow,
            metaFlow
        ) { base, recents, dirResults, dialog, meta ->
            MmsiUiState(
                inputDigits = base.input,
                currentResult = base.result,
                prefixMatches = base.matches,
                relatedMids = base.related,
                stationTypeDescription = base.stationType,
                isExactMidFound = base.result != null,
                recentLookups = recents,
                showDirectorySheet = dialog.showDir,
                showInfoDialog = dialog.showInfo,
                directorySearchQuery = dialog.dirQuery,
                selectedRegionFilter = dialog.regFilter,
                directoryResults = dirResults,
                lastUpdatedDate = meta.lastUpdated,
                databaseVersion = meta.dbVersion,
                isBridgeNightMode = meta.isBridgeNightMode,
                isAutoNightModeEnabled = meta.isAutoNightModeEnabled,
                isAfterSundown = meta.isAfterSundown,
                solarStatusDescription = meta.solarStatusDescription,
                solarSunriseTimeText = meta.solarSunriseTimeText,
                solarSunsetTimeText = meta.solarSunsetTimeText,
                solarTimeZoneId = meta.solarTimeZoneId,
                isUpdatingDatabase = meta.isUpdating,
                isAutoUpdating = meta.isAutoUpdating,
                updateResultMessage = meta.updateMsg,
                updateResultSuccess = meta.updateSuccess,
                isInternetConnected = meta.isInternetConnected,
                nextAutoUpdateDueText = meta.nextAutoUpdateDueText,
                isWeeklyUpdateDue = meta.isWeeklyUpdateDue
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MmsiUiState()
        )
    }

    private fun updateScheduleInfo(lastTimestamp: Long) {
        val now = System.currentTimeMillis()
        val buildTime = AppDatabase.BUILD_TIMESTAMP
        val effectiveLast = if (lastTimestamp > 0L) lastTimestamp else buildTime
        if (effectiveLast <= 0L) {
            _nextAutoUpdateDueText.value = "Eligible to update on internet connection"
            _isWeeklyUpdateDue.value = true
        } else {
            val dueTime = effectiveLast + AppDatabase.ONE_WEEK_MILLIS
            if (now >= dueTime) {
                _nextAutoUpdateDueText.value = "Weekly update due (will auto-sync when online)"
                _isWeeklyUpdateDue.value = true
            } else {
                val daysRemaining = maxOf(1L, ((dueTime - now) / (1000L * 60 * 60 * 24)))
                val dateFormatted = SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH).format(Date(dueTime))
                _nextAutoUpdateDueText.value = "In $daysRemaining day(s) ($dateFormatted)"
                _isWeeklyUpdateDue.value = false
            }
        }
    }

    fun checkAndAutoUpdateIfEligible() {
        if (_isUpdatingDatabase.value) return

        viewModelScope.launch {
            val isDue = repository.isWeeklyUpdateDue()
            if (!isDue) return@launch

            val now = System.currentTimeMillis()
            if (now - lastAutoUpdateAttemptTime < 30 * 60 * 1000L) {
                return@launch
            }
            lastAutoUpdateAttemptTime = now

            _isUpdatingDatabase.value = true
            _isAutoUpdating.value = true

            val result = repository.updateDatabaseOnline(getApplication(), isAutoSync = true)

            _isUpdatingDatabase.value = false
            _isAutoUpdating.value = false

            if (result.success) {
                _updateResultMessage.value = "Weekly auto-update complete: Synchronized latest ITU maritime registration codes."
                _updateResultSuccess.value = true
                updateScheduleInfo(now)
            }
        }
    }

    private data class BaseLookupState(
        val input: String,
        val result: MmsiEntity?,
        val matches: List<MmsiEntity>,
        val related: List<MmsiEntity>,
        val stationType: String?
    )

    private data class SyncState(
        val isUpdating: Boolean,
        val isAutoUpdating: Boolean,
        val updateMsg: String?,
        val updateSuccess: Boolean?,
        val isConnected: Boolean
    )

    private data class ScheduleState(
        val lastUpdated: String,
        val dbVersion: String,
        val nextDueText: String,
        val isDue: Boolean
    )

    private data class NightModeState(
        val isBridgeNight: Boolean,
        val isAutoNight: Boolean,
        val isAfterSundown: Boolean,
        val statusDescription: String,
        val sunriseTime: String,
        val sunsetTime: String,
        val timeZoneId: String
    )

    private data class MetaState(
        val lastUpdated: String,
        val dbVersion: String,
        val isBridgeNightMode: Boolean,
        val isAutoNightModeEnabled: Boolean,
        val isAfterSundown: Boolean,
        val solarStatusDescription: String,
        val solarSunriseTimeText: String,
        val solarSunsetTimeText: String,
        val solarTimeZoneId: String,
        val nextAutoUpdateDueText: String,
        val isWeeklyUpdateDue: Boolean,
        val isUpdating: Boolean,
        val isAutoUpdating: Boolean,
        val updateMsg: String?,
        val updateSuccess: Boolean?,
        val isInternetConnected: Boolean
    )

    private data class DialogState(
        val showDir: Boolean,
        val showInfo: Boolean,
        val dirQuery: String,
        val regFilter: String
    )

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

        val (effectiveMid, stationType) = parseMmsiMid(input)
        _stationType.value = stationType

        if (effectiveMid.length == 3) {
            val result = repository.findInMemory(effectiveMid)
            _currentResult.value = result
            _prefixMatches.value = emptyList()

            if (result != null) {
                viewModelScope.launch {
                    repository.recordLookup(result.mid, result.countryName, result.flagEmoji)
                    val countryMids = repository.getCodesForCountry(result.countryName)
                    _relatedMids.value = countryMids.filter { it.mid != result.mid }
                }
            } else {
                _relatedMids.value = emptyList()
            }
        } else {
            _currentResult.value = null
            _relatedMids.value = emptyList()
            val matches = MmsiDataSeed.allCodes.filter { it.mid.startsWith(effectiveMid) }
            _prefixMatches.value = matches.take(12)
        }
    }

    private fun parseMmsiMid(input: String): Pair<String, String?> {
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

    fun setShowInfoDialog(show: Boolean) {
        _showInfoDialog.value = show
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

    fun updateDatabaseOnline() {
        if (_isUpdatingDatabase.value) return
        _isUpdatingDatabase.value = true
        _isAutoUpdating.value = false
        _updateResultMessage.value = null
        _updateResultSuccess.value = null

        viewModelScope.launch {
            val result = repository.updateDatabaseOnline(getApplication(), isAutoSync = false)
            _isUpdatingDatabase.value = false
            _updateResultMessage.value = result.message
            _updateResultSuccess.value = result.success
            if (result.success) {
                updateScheduleInfo(System.currentTimeMillis())
            }
        }
    }

    fun clearUpdateMessage() {
        _updateResultMessage.value = null
        _updateResultSuccess.value = null
    }

    fun toggleBridgeNightMode() {
        val next = !uiState.value.isBridgeNightMode
        viewModelScope.launch {
            repository.setBridgeNightMode(next)
        }
    }

    fun setBridgeNightMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setBridgeNightMode(enabled)
        }
    }

    fun toggleAutoNightMode() {
        val next = !uiState.value.isAutoNightModeEnabled
        setAutoNightMode(next)
    }

    fun setAutoNightMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.setAutoNightModeEnabled(enabled)
            if (enabled) {
                val info = SolarTimeCalculator.getSolarTwilightInfo(getApplication())
                _solarTwilightInfo.value = info
                repository.setBridgeNightMode(info.isAfterSundown)
            }
        }
    }
}
