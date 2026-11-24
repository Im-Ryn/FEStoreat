package com.example.food_shelf_life_predictor.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food_shelf_life_predictor.features.auth.AuthScreen
import com.example.food_shelf_life_predictor.features.inventory.InventoryScreen
import com.example.food_shelf_life_predictor.features.profile.ProfileViewModel

@Composable
fun AppRoot(profileViewModel: ProfileViewModel = viewModel()) {
    var loggedIn by rememberSaveable { mutableStateOf(false) }

    if (loggedIn) {
        InventoryScreen(onLogout = { loggedIn = false })
    } else {
        AuthScreen(onAuthenticated = { name, email ->
            profileViewModel.setUser(name, email)
            loggedIn = true
        })
    }
}