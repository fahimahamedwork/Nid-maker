package com.nidcard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.data.repository.NIDCardRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.MessageDigest

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("nid_admin_prefs", android.content.Context.MODE_PRIVATE)

    private val repository = NIDCardRepository(
        com.nidcard.app.data.database.AppDatabase.getDatabase(application).nidCardDao()
    )

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    private val _resetCode = "32423"

    // searchResults serves both search and full listing — no need for _allCards (H5 fix)
    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<NIDCard>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllCards()
            else repository.searchCards(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deleteMessage = MutableStateFlow<String?>(null)
    val deleteMessage: StateFlow<String?> = _deleteMessage

    private val _selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedIds: StateFlow<Set<Long>> = _selectedIds

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess

    // --- Admin password persistence (SHA-256 + salt) ---
    private val _adminPasswordHash = MutableStateFlow(
        prefs.getString("admin_password_hash", null) ?: hashPassword("fahad")
    )
    val adminPasswordHash: StateFlow<String> = _adminPasswordHash

    // --- Auto-delete timer ---
    private val _timerActive = MutableStateFlow(
        prefs.getBoolean("timer_active", false)
    )
    val timerActive: StateFlow<Boolean> = _timerActive

    private val _timerType = MutableStateFlow(
        prefs.getString("timer_type", "hours") ?: "hours"
    )
    val timerType: StateFlow<String> = _timerType

    private val _timerValue = MutableStateFlow(
        prefs.getInt("timer_value", 24)
    )
    val timerValue: StateFlow<Int> = _timerValue

    private val _targetDeleteTime = MutableStateFlow(
        prefs.getLong("target_delete_time", 0L)
    )
    val targetDeleteTime: StateFlow<Long> = _targetDeleteTime

    private val _timerCountdownText = MutableStateFlow("")
    val timerCountdownText: StateFlow<String> = _timerCountdownText

    private val _timerExpired = MutableStateFlow(false)
    val timerExpired: StateFlow<Boolean> = _timerExpired

    // Flag to signal UI to show confirmation before deleting
    private val _pendingAutoDelete = MutableStateFlow(false)
    val pendingAutoDelete: StateFlow<Boolean> = _pendingAutoDelete

    // Ticker job for proper cancellation (H6 fix)
    private var tickerJob: Job? = null

    init {
        // Save hashed password on first init
        if (prefs.getString("admin_password_hash", null) == null) {
            prefs.edit().putString("admin_password_hash", _adminPasswordHash.value).apply()
        }
        checkTimerOnStart()
        startOrStopTicker()
    }

    // --- SHA-256 password hashing ---
    companion object {
        private const val HASH_SALT = "NID_MAKER_SECURE_SALT_2024"

        fun hashPassword(password: String): String {
            val salted = password + HASH_SALT
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(salted.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }

    // --- Timer countdown ticker (H6 fix: only active when timer is on) ---
    private fun startOrStopTicker() {
        if (_timerActive.value) {
            if (tickerJob?.isActive != true) {
                tickerJob = viewModelScope.launch {
                    while (true) {
                        updateCountdownText()
                        kotlinx.coroutines.delay(1000L)
                    }
                }
            }
        } else {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    private fun updateCountdownText() {
        if (!_timerActive.value || _targetDeleteTime.value == 0L) {
            _timerCountdownText.value = ""
            return
        }
        val remaining = _targetDeleteTime.value - System.currentTimeMillis()
        if (remaining <= 0) {
            _timerCountdownText.value = "মেয়াদ উত্তীর্ণ! ডাটা ডিলিট হবে।"
            if (!_timerExpired.value) {
                _timerExpired.value = true
            }
            return
        }
        val seconds = remaining / 1000
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        _timerCountdownText.value = when {
            days > 0 -> "${days}d ${hours}h ${minutes}m ${secs}s বাকি"
            hours > 0 -> "${hours}h ${minutes}m ${secs}s বাকি"
            else -> "${minutes}m ${secs}s বাকি"
        }
    }

    // Sets flag instead of silently deleting (C2 safety fix)
    private fun checkTimerOnStart() {
        if (_timerActive.value && _targetDeleteTime.value > 0L) {
            if (System.currentTimeMillis() >= _targetDeleteTime.value) {
                _timerExpired.value = true
                _pendingAutoDelete.value = true
            }
        }
    }

    fun confirmPendingAutoDelete() {
        _pendingAutoDelete.value = false
        viewModelScope.launch {
            try {
                repository.deleteAll()
                clearTimer()
                _deleteMessage.value = "টাইমার মেয়াদ উত্তীর্ণ হয়েছে — সব NID ডাটা অটো-ডিলিট হয়েছে"
            } catch (e: Exception) {
                _deleteMessage.value = "ডিলিট করতে সমস্যা: ${e.message}"
            }
        }
    }

    fun dismissPendingAutoDelete() {
        _pendingAutoDelete.value = false
        clearTimer()
    }

    fun setTimer(type: String, value: Int) {
        val now = System.currentTimeMillis()
        val target = when (type) {
            "hours" -> now + (value * 3600 * 1000L)
            "days" -> now + (value * 86400 * 1000L)
            "date" -> value * 1000L
            else -> now + (value * 3600 * 1000L)
        }
        _timerType.value = type
        _timerValue.value = value
        _targetDeleteTime.value = target
        _timerActive.value = true
        _timerExpired.value = false

        prefs.edit()
            .putBoolean("timer_active", true)
            .putString("timer_type", type)
            .putInt("timer_value", value)
            .putLong("target_delete_time", target)
            .apply()

        startOrStopTicker()
    }

    fun setTimerByDate(epochMillis: Long) {
        _timerType.value = "date"
        _timerValue.value = 0
        _targetDeleteTime.value = epochMillis
        _timerActive.value = true
        _timerExpired.value = false

        prefs.edit()
            .putBoolean("timer_active", true)
            .putString("timer_type", "date")
            .putInt("timer_value", 0)
            .putLong("target_delete_time", epochMillis)
            .apply()

        startOrStopTicker()
    }

    fun clearTimer() {
        _timerActive.value = false
        _timerType.value = "hours"
        _timerValue.value = 24
        _targetDeleteTime.value = 0L
        _timerExpired.value = false
        _timerCountdownText.value = ""

        prefs.edit()
            .putBoolean("timer_active", false)
            .putString("timer_type", "hours")
            .putInt("timer_value", 24)
            .putLong("target_delete_time", 0L)
            .apply()

        startOrStopTicker()
    }

    // --- Login / Password (SHA-256 hash comparison) ---
    fun login(password: String) {
        val inputHash = hashPassword(password)
        if (inputHash == _adminPasswordHash.value) {
            _isLoggedIn.value = true
            _loginError.value = null
        } else {
            _loginError.value = "ভুল পাসওয়ার্ড!"
        }
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun resetPassword(code: String, newPassword: String, confirmPassword: String) {
        when {
            code != _resetCode -> _loginError.value = "ভুল রিসেট কোড!"
            newPassword.length < 3 -> _loginError.value = "পাসওয়ার্ড কমপক্ষে ৩ অক্ষরের হতে হবে!"
            newPassword != confirmPassword -> _loginError.value = "পাসওয়ার্ড মিলছে না!"
            else -> {
                val newHash = hashPassword(newPassword)
                _adminPasswordHash.value = newHash
                prefs.edit().putString("admin_password_hash", newHash).apply()
                prefs.edit().remove("admin_password").apply()
                _resetSuccess.value = true
                _loginError.value = null
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun clearResetSuccess() {
        _resetSuccess.value = false
    }

    fun deleteCard(nid: String) {
        viewModelScope.launch {
            try {
                repository.deleteByNid(nid)
                _deleteMessage.value = "NID $nid ডিলিট হয়েছে"
            } catch (e: Exception) {
                _deleteMessage.value = "ডিলিট করতে সমস্যা: ${e.message}"
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
                _deleteMessage.value = "সব NID ডাটা ডিলিট হয়েছে"
            } catch (e: Exception) {
                _deleteMessage.value = "ডিলিট করতে সমস্যা: ${e.message}"
            }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            try {
                repository.deleteByIds(_selectedIds.value.toList())
                _deleteMessage.value = "${_selectedIds.value.size}টি NID ডাটা ডিলিট হয়েছে"
                _selectedIds.value = emptySet()
            } catch (e: Exception) {
                _deleteMessage.value = "ডিলিট করতে সমস্যা: ${e.message}"
            }
        }
    }

    fun toggleSelection(id: Long) {
        val current = _selectedIds.value.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _selectedIds.value = current
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun selectAll(cards: List<NIDCard>) {
        _selectedIds.value = cards.map { it.id }.toSet()
    }

    fun clearDeleteMessage() {
        _deleteMessage.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Backup / Import ---
    fun exportAllCardsAsJson(): String? {
        return try {
            val cards = kotlinx.coroutines.runBlocking { repository.getAllCardsList() }
            val gson = com.google.gson.Gson()
            gson.toJson(cards)
        } catch (e: Exception) {
            null
        }
    }

    fun importCardsFromJson(json: String): Int {
        return try {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<NIDCard>>() {}.type
            val cards: List<NIDCard> = gson.fromJson(json, type)
            var imported = 0
            kotlinx.coroutines.runBlocking {
                for (card in cards) {
                    try {
                        repository.insert(card)
                        imported++
                    } catch (_: Exception) {
                        // Skip duplicates
                    }
                }
            }
            imported
        } catch (e: Exception) {
            -1
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
