package com.example.food_shelf_life_predictor.features.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileState(
    val name: String = "Samuel",
    val email: String = "samuel@binus.ac.id",
    val kost: String = "Kost Anggrek • Kamar No. 12",
    val reminderEnabled: Boolean = true,
    val reminderHour: Int = 7,
    val reminderMinute: Int = 0
) {
    val reminderTime: String get() = "%02d:%02d".format(reminderHour, reminderMinute)
}

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    fun setUser(name: String, email: String) {
        _state.update { s ->
            s.copy(
                name = name.ifBlank { s.name },
                email = email.ifBlank { s.email }
            )
        }
    }

    fun saveAccount(name: String, email: String, kost: String) {
        _state.update { it.copy(name = name, email = email, kost = kost) }
    }

    fun saveReminder(enabled: Boolean, hour: Int, minute: Int) {
        _state.update {
            it.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute)
        }
    }
}