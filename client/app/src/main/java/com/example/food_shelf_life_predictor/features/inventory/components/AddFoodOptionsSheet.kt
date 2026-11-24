package com.example.food_shelf_life_predictor.features.inventory.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodOptionsSheet(
    onDismiss: () -> Unit,
    onCamera: () -> Unit,
    onGallery: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        ListItem(
            headlineContent = { Text("Ambil foto") },
            leadingContent = { Icon(Icons.Default.CameraAlt, null) },
            modifier = Modifier.clickable(onClick = onCamera)
        )
        ListItem(
            headlineContent = { Text("Pilih dari galeri") },
            leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
            modifier = Modifier
                .clickable(onClick = onGallery)
                .navigationBarsPadding()
        )
    }
}