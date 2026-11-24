package com.example.food_shelf_life_predictor.features.inventory.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_shelf_life_predictor.core.storage.ImageStorage
import com.example.food_shelf_life_predictor.features.inventory.FoodItem
import com.example.food_shelf_life_predictor.features.inventory.StorageLocation
import com.example.food_shelf_life_predictor.features.inventory.data.CreateFoodRequest
import com.example.food_shelf_life_predictor.features.inventory.data.FoodApi
import com.example.food_shelf_life_predictor.features.inventory.data.PredictionDto
import com.example.food_shelf_life_predictor.features.inventory.data.toFriendlyMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

sealed interface ScanState {
    data object Idle : ScanState
    data class Loading(val photoPath: String) : ScanState
    data class Result(
        val photoPath: String,
        val prediction: PredictionDto,
        val foodKey: String,
        val location: StorageLocation,
        val saving: Boolean = false,
        val errorMessage: String? = null
    ) : ScanState
    data class Error(val message: String) : ScanState
}

class ScanViewModel : ViewModel() {
    private val api = FoodApi()
    private val _state = MutableStateFlow<ScanState>(ScanState.Idle)
    val state: StateFlow<ScanState> = _state.asStateFlow()
    private var job: Job? = null

    fun analyze(photoPath: String) {
        _state.value = ScanState.Loading(photoPath)
        job = viewModelScope.launch {
            try {
                val prediction = api.predictImage(File(photoPath))
                _state.value = ScanState.Result(
                    photoPath = photoPath,
                    prediction = prediction,
                    foodKey = prediction.detectedFood,
                    location = StorageLocation.CHILLER
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ImageStorage.delete(photoPath)
                _state.value = ScanState.Error(e.toFriendlyMessage())
            }
        }
    }

    fun selectFood(key: String) = updateResult { it.copy(foodKey = key) }
    fun selectLocation(location: StorageLocation) = updateResult { it.copy(location = location) }

    private fun updateResult(block: (ScanState.Result) -> ScanState.Result) {
        (_state.value as? ScanState.Result)?.let { _state.value = block(it) }
    }

    fun save(onSaved: (FoodItem) -> Unit) {
        val s = _state.value as? ScanState.Result ?: return
        if (s.saving || s.prediction.freshnessScore < FRESHNESS_THRESHOLD) return
        val days = s.prediction.storageRecommendations
            .firstOrNull { StorageLocation.fromApi(it.storageLocation) == s.location }
            ?.estimatedDaysLeft ?: return

        _state.value = s.copy(saving = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val dto = api.createFood(
                    CreateFoodRequest(
                        name = labelFor(s.foodKey),
                        storageLocation = s.location.apiValue,
                        storageDate = LocalDate.now().toString(),
                        freshnessScore = s.prediction.freshnessScore,
                        estimatedDaysLeft = days
                    )
                )
                onSaved(dto.toFoodItem().copy(photoPath = s.photoPath))
                _state.value = ScanState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.value = s.copy(saving = false, errorMessage = e.toFriendlyMessage())
            }
        }
    }

    /** Batalkan pemindaian / tutup sheet: foto sementara dihapus. */
    fun cancel() {
        job?.cancel()
        when (val s = _state.value) {
            is ScanState.Loading -> ImageStorage.delete(s.photoPath)
            is ScanState.Result -> ImageStorage.delete(s.photoPath)
            else -> Unit
        }
        _state.value = ScanState.Idle
    }

    fun clearError() { _state.value = ScanState.Idle }
}