package com.example.food_shelf_life_predictor.features.inventory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.features.inventory.StorageLocation

@Composable
fun LocationFilterRow(
    selected: StorageLocation?,                 // null = "Semua"
    total: Int,
    counts: Map<StorageLocation, Int>,
    onSelect: (StorageLocation?) -> Unit,
    modifier: Modifier = Modifier
) {
    val order = listOf(StorageLocation.CHILLER, StorageLocation.FREEZER, StorageLocation.ROOM)

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CountChip("Semua ($total)", selected == null) { onSelect(null) }
        }
        items(order) { location ->
            CountChip(
                label = "${location.label} (${counts[location] ?: 0})",
                selected = selected == location
            ) { onSelect(location) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else null,
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            selectedContainerColor = BrandGreen,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}