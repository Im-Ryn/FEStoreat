package com.example.food_shelf_life_predictor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.food_shelf_life_predictor.core.ui.theme.FoodshelflifepredictorTheme
import com.example.food_shelf_life_predictor.features.inventory.InventoryScreen
import com.example.food_shelf_life_predictor.navigation.AppRoot

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodshelflifepredictorTheme {
                AppRoot()
            }
        }
    }
}

