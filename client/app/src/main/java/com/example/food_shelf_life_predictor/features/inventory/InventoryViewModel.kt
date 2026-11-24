package com.example.food_shelf_life_predictor.features.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_shelf_life_predictor.core.storage.ImageStorage
import com.example.food_shelf_life_predictor.features.inventory.data.FoodApi
import com.example.food_shelf_life_predictor.features.inventory.data.toFriendlyMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import com.example.food_shelf_life_predictor.core.network.ApiConfig
import com.example.food_shelf_life_predictor.features.inventory.data.CreateFoodRequest
import kotlinx.coroutines.launch

private val mockFoods = listOf(
    FoodItem("5", "Telur", StorageLocation.CHILLER, "2026-11-03", 0.8, 14, "2026-11-17", 8, FoodStatus.FRESH),
    FoodItem("6", "Yogurt", StorageLocation.CHILLER, "2026-11-05", 0.9, 7, "2026-11-12", 3, FoodStatus.EXPIRING_SOON),
    FoodItem("7", "Ayam Fillet", StorageLocation.FREEZER, "2026-11-01", 0.95, 60, "2026-12-31", 52, FoodStatus.FRESH),
    FoodItem("8", "Mie Instan", StorageLocation.ROOM, "2026-10-20", null, 90, "2027-01-20", 72, FoodStatus.FRESH),
    FoodItem("9", "Biskuit", StorageLocation.ROOM, "2026-10-25", null, 60, "2026-12-25", 46, FoodStatus.FRESH),
    FoodItem("10", "Pisang", StorageLocation.ROOM, "2026-11-06", 0.7, 4, "2026-11-13", 4, FoodStatus.FRESH),
    FoodItem("11", "Apel", StorageLocation.CHILLER, "2026-11-02", 0.85, 14, "2026-11-16", 7, FoodStatus.FRESH),
    FoodItem("12", "Keju", StorageLocation.CHILLER, "2026-11-01", 0.9, 30, "2026-12-01", 22, FoodStatus.FRESH),
    FoodItem("13", "Sosis", StorageLocation.FREEZER, "2026-10-28", 0.9, 45, "2026-12-12", 33, FoodStatus.FRESH),
    FoodItem("14", "Roti Tawar", StorageLocation.ROOM, "2026-11-07", 0.6, 3, "2026-11-10", 1, FoodStatus.EXPIRING_SOON),
    FoodItem("15", "Brokoli", StorageLocation.CHILLER, "2026-11-05", 0.75, 5, "2026-11-10", 1, FoodStatus.EXPIRING_SOON),
    FoodItem("16", "Es Krim", StorageLocation.FREEZER, "2026-10-15", 0.95, 90, "2027-01-15", 67, FoodStatus.FRESH),
    FoodItem("17", "Cokelat", StorageLocation.ROOM, "2026-10-01", null, 120, "2027-02-01", 84, FoodStatus.FRESH))

private val _pendingFinished = MutableStateFlow<Set<String>>(emptySet())

data class InventorySummary(
    val total: Int = 0,
    val urgent: Int = 0,
    val countByLocation: Map<StorageLocation, Int> = emptyMap()
)

class InventoryViewModel : ViewModel() {

    private val _selectedLocation = MutableStateFlow<StorageLocation?>(null)

    val selectedLocation: StateFlow<StorageLocation?> = _selectedLocation.asStateFlow()

    private val api = FoodApi()

    private val _foods = MutableStateFlow<List<FoodItem>>(
        if (ApiConfig.USE_MOCK) mockFoods else emptyList()
    )

    val allFoods: StateFlow<List<FoodItem>> =
        combine(_foods, _pendingFinished) { foods, pending ->
            foods.filter { it.id !in pending }.sortedBy { it.daysLeft }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _errors = MutableSharedFlow<String>()
    val errors = _errors.asSharedFlow()

    init {
        if (!ApiConfig.USE_MOCK) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            try {
                val old = _foods.value.associateBy { it.id }
                _foods.value = api.getFoods().map { dto ->
                    // pertahankan path foto lokal untuk item yang sama
                    dto.toFoodItem().copy(photoPath = old[dto.id]?.photoPath)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errors.emit(e.toFriendlyMessage())
            }
        }
    }

    // Terfilter lalu terurut dari paling mendesak (days_left terkecil)
    val visibleFoods: StateFlow<List<FoodItem>> =
        combine(_foods, _selectedLocation, _pendingFinished) { foods, location, pending ->
            foods
                .filter { it.id !in pending }
                .filter { location == null || it.storageLocation == location }
                .sortedBy { it.daysLeft }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val summary: StateFlow<InventorySummary> =
        combine(_foods, _pendingFinished) { foods, pending ->
            val active = foods.filter { it.id !in pending }
            InventorySummary(
                total = active.size,
                urgent = active.count { it.status != FoodStatus.FRESH },
                countByLocation = active.groupingBy { it.storageLocation }.eachCount()
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InventorySummary()
        )

    fun selectLocation(location: StorageLocation?) {
        _selectedLocation.value = location
    }

    fun addPackagedFood(name: String, location: StorageLocation, expiryDate: LocalDate) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val days = ChronoUnit.DAYS.between(today, expiryDate).toInt()
            try {
                val dto = api.createFood(
                    CreateFoodRequest(
                        name = name,
                        storageLocation = location.apiValue,
                        storageDate = today.toString(),
                        estimatedDaysLeft = days
                    )
                )
                _foods.update { it + dto.toFoodItem() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _errors.emit(e.toFriendlyMessage())
            }
        }
    }

    fun addFood(item: FoodItem) {
        _foods.update { it + item }
    }

    // 1-tap: sembunyikan langsung (optimistic)
    fun markFinished(id: String) {
        _pendingFinished.update { it + id }
    }

    // Undo: kembalikan ke daftar
    fun undoFinished(id: String) {
        _pendingFinished.update { it - id }
    }

    // Snackbar habis tanpa Undo: konfirmasi final
    fun confirmFinished(id: String) {
        viewModelScope.launch {
            try {
                api.deleteFood(id)
                _foods.value.firstOrNull { it.id == id }?.let { ImageStorage.delete(it.photoPath) }
                _foods.update { list -> list.filterNot { it.id == id } }
                _pendingFinished.update { it - id }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _pendingFinished.update { it - id }   // rollback: kartu muncul lagi
                _errors.emit("Gagal menandai habis. ${e.toFriendlyMessage()}")
            }
        }
    }
}


