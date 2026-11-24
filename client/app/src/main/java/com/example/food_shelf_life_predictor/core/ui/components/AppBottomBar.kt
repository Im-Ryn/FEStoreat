package com.example.food_shelf_life_predictor.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenDark
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight

enum class AppTab { HOME, CALENDAR, PROFILE }

@Composable
fun AppBottomBar(
    selected: AppTab,
    onTabSelected: (AppTab) -> Unit,
    onCameraClick: () -> Unit
) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = BrandGreenDark,
        selectedTextColor = BrandGreenDark,
        indicatorColor = BrandGreenLight
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        NavigationBar(
            modifier = Modifier.padding(top = 28.dp),
            containerColor = Color.White
        ) {
            NavigationBarItem(
                selected = selected == AppTab.HOME,
                onClick = { onTabSelected(AppTab.HOME) },
                icon = { Icon(Icons.Default.Home, contentDescription = null) },
                label = { Text("Beranda") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = selected == AppTab.CALENDAR,
                onClick = { onTabSelected(AppTab.CALENDAR) },
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                label = { Text("Kalender") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = false,
                onClick = { },
                icon = { },
                enabled = false
            )
            NavigationBarItem(
                selected = false,
                onClick = { /* TODO: layar Lokasi */ },
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                label = { Text("Lokasi") },
                colors = itemColors
            )
            NavigationBarItem(
                selected = selected == AppTab.PROFILE,
                onClick = { onTabSelected(AppTab.PROFILE) },
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("Profil") },
                colors = itemColors
            )
        }

        FloatingActionButton(
            onClick = onCameraClick,
            shape = CircleShape,
            containerColor = BrandGreen,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(68.dp)
                .border(5.dp, Color.White, CircleShape)
        ) {
            Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = "Tambah makanan",
                    modifier = Modifier.size(28.dp)
                )
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-2).dp)
                )
            }
        }
    }
}