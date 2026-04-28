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

    private val _searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<NIDCard>> = _searchQuery
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        refreshTodayCount()
    }

    fun refreshTodayCount() {
        viewModelScope.launch {
            _todayCount.value = repository.getTodayCount()
        }
    }

    fun selectCard(card: NIDCard) {
        _selectedCard.value = card
    }

    fun clearSelectedCard() {
        _selectedCard.value = null
    }

    fun quickSearch(query: String) {
        viewModelScope.launch {
            _quickSearchResult.value = repository.searchByNidOrPin(query)
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
            try {
                val sdf = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
                val sdfFull = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val cardToSave = card.copy(
                    issueDate = sdf.format(Date()),
                    createdAt = sdfFull.format(Date()),
                    nameEn = card.nameEn.uppercase(Locale.getDefault())
                )
                repository.insert(cardToSave)
                _saveSuccess.value = true
                refreshTodayCount()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearSaveStatus() {
        _saveSuccess.value = false
        _errorMessage.value = null
    }

    fun deleteCard(card: NIDCard) {
        viewModelScope.launch {
            repository.delete(card)
            refreshTodayCount()
        }
    }

    fun deleteByNid(nid: String) {
        viewModelScope.launch {
            repository.deleteByNid(nid)
            refreshTodayCount()
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
            refreshTodayCount()
        }
    }

    fun deleteByIds(ids: List<Long>) {
        viewModelScope.launch {
            repository.deleteByIds(ids)
            refreshTodayCount()
        }
    }
}
