package com.nidcard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nidcard.app.data.entity.NIDCard
import com.nidcard.app.data.repository.NIDCardRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NIDViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NIDCardRepository(
        com.nidcard.app.data.database.AppDatabase.getDatabase(application).nidCardDao()
    )

    private val _allCards = repository.getAllCards()
    val allCards: StateFlow<List<NIDCard>> = _allCards.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _totalCount = repository.getTotalCount()
    val totalCount: StateFlow<Int> = _totalCount.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), 0
    )

    private val _todayCount = MutableStateFlow(0)
    val todayCount: StateFlow<Int> = _todayCount

    // Search with debounce built into the Flow (H4 fix)
    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<NIDCard>> = _searchQuery
        .debounce(500)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchCards(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _quickSearchResult = MutableStateFlow<NIDCard?>(null)
    val quickSearchResult: StateFlow<NIDCard?> = _quickSearchResult

    private val _selectedCard = MutableStateFlow<NIDCard?>(null)
    val selectedCard: StateFlow<NIDCard?> = _selectedCard

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _lastInsertedId = MutableStateFlow(0L)
    val lastInsertedId: StateFlow<Long> = _lastInsertedId

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    // Edit mode support
    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode

    init {
        refreshTodayCount()
    }

    fun refreshTodayCount() {
        viewModelScope.launch {
            try {
                _todayCount.value = repository.getTodayCount()
            } catch (_: Exception) { }
        }
    }

    fun selectCard(card: NIDCard) {
        _selectedCard.value = card
    }

    fun loadCardById(id: Long) {
        viewModelScope.launch {
            try {
                _selectedCard.value = repository.getById(id)
            } catch (_: Exception) { }
        }
    }

    fun clearSelectedCard() {
        _selectedCard.value = null
    }

    fun enterEditMode() {
        _editMode.value = true
    }

    fun exitEditMode() {
        _editMode.value = false
    }

    fun quickSearch(query: String) {
        viewModelScope.launch {
            try {
                _quickSearchResult.value = repository.searchByNidOrPin(query)
            } catch (_: Exception) { }
        }
    }

    fun clearQuickSearch() {
        _quickSearchResult.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveCard(card: NIDCard) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                // Check for duplicate NID before insert (C2 fix)
                val existing = repository.getByNid(card.nid)
                if (existing != null) {
                    _errorMessage.value = "এই NID নম্বরটি ইতিমধ্যে ব্যবহৃত হয়েছে! অনুগ্রহ করে ভিন্ন NID নম্বর দিন।"
                    _isSaving.value = false
                    return@launch
                }

                val sdf = SimpleDateFormat("d/M/yyyy", Locale.US)
                val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                val cardToSave = card.copy(
                    issueDate = sdf.format(Date()),
                    createdAt = sdfFull.format(Date()),
                    nameEn = card.nameEn.uppercase(Locale.US)
                )
                val insertedId = repository.insert(cardToSave)
                val savedCard = cardToSave.copy(id = insertedId)
                _selectedCard.value = savedCard
                _lastInsertedId.value = insertedId
                _saveSuccess.value = true
                refreshTodayCount()
            } catch (e: Exception) {
                if (e.message?.contains("UNIQUE constraint failed", ignoreCase = true) == true ||
                    e.message?.contains("SQLITE_CONSTRAINT_UNIQUE", ignoreCase = true) == true) {
                    _errorMessage.value = "এই NID নম্বরটি ইতিমধ্যে ব্যবহৃত হয়েছে! অনুগ্রহ করে ভিন্ন NID নম্বর দিন।"
                } else {
                    _errorMessage.value = "সংরক্ষণ করতে সমস্যা হয়েছে: ${e.message}"
                }
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun updateCard(card: NIDCard) {
        viewModelScope.launch {
            _isSaving.value = true
            _errorMessage.value = null
            try {
                repository.update(card)
                _selectedCard.value = card
                _updateSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "আপডেট করতে সমস্যা হয়েছে: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearSaveStatus() {
        _saveSuccess.value = false
        _updateSuccess.value = false
        _errorMessage.value = null
    }

    fun deleteCard(card: NIDCard) {
        viewModelScope.launch {
            try {
                repository.delete(card)
                refreshTodayCount()
            } catch (_: Exception) { }
        }
    }

    fun deleteByNid(nid: String) {
        viewModelScope.launch {
            try {
                repository.deleteByNid(nid)
                refreshTodayCount()
            } catch (_: Exception) { }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            try {
                repository.deleteAll()
                refreshTodayCount()
            } catch (_: Exception) { }
        }
    }

    fun deleteByIds(ids: List<Long>) {
        viewModelScope.launch {
            try {
                repository.deleteByIds(ids)
                refreshTodayCount()
            } catch (_: Exception) { }
        }
    }

    // --- Backup / Export (F2 feature) ---
    fun exportAllCardsAsJson(): String? {
        return try {
            val cards = kotlinx.coroutines.runBlocking { repository.getAllCardsList() }
            val gson = com.google.gson.Gson()
            gson.toJson(cards)
        } catch (e: Exception) {
            null
        }
    }
}
