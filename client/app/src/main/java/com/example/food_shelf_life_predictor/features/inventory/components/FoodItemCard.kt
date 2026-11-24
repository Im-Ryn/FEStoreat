package com.example.food_shelf_life_predictor.features.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight
import com.example.food_shelf_life_predictor.core.ui.theme.SafeGreen
import com.example.food_shelf_life_predictor.core.ui.theme.SafeGreen
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed
import com.example.food_shelf_life_predictor.core.ui.theme.WarningYellow
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed
import com.example.food_shelf_life_predictor.core.ui.theme.WarningYellow
import com.example.food_shelf_life_predictor.features.inventory.FoodItem
import com.example.food_shelf_life_predictor.features.inventory.FoodStatus
import com.example.food_shelf_life_predictor.features.inventory.StorageLocation
import java.io.File

@Composable
fun FoodItemCard(
    item: FoodItem,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = item.status.color()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Garis warna urgensi di sisi kiri
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FoodThumbnail(item.photoPath)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        DaysLeftBadge(text = item.daysLeftText(), color = statusColor)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        LocationRow(item.storageLocation)
                    }
                }

                Spacer(Modifier.height(12.dp))

                QuickActionButton(
                    onClick = onFinished,
                    color = statusColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Pengganti foto sampai fitur gambar ada
@Composable
private fun FoodThumbnail(photoPath: String?) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BrandGreenLight),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun DaysLeftBadge(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 2.dp)
    )
}

@Composable
private fun LocationRow(location: StorageLocation) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = BrandGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = location.label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun FoodItem.daysLeftText(): String = when {
    daysLeft < 0 -> "Lewat ${-daysLeft} Hari"
    daysLeft == 0 -> "Hari Ini"
    else -> "Sisa $daysLeft Hari"
}

fun FoodStatus.color(): Color = when (this) {
    FoodStatus.FRESH -> SafeGreen
    FoodStatus.EXPIRING_SOON -> WarningYellow
    FoodStatus.EXPIRED -> UrgentRed
}

@Preview(showBackground = true)
@Composable
private fun FoodItemCardPreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        FoodItemCard(
            FoodItem("1", "Susu UHT", StorageLocation.CHILLER, "2026-11-04", 0.9, 5, "2026-11-09", 1, FoodStatus.EXPIRING_SOON),
            onFinished = {}
        )
        Spacer(Modifier.height(12.dp))
        FoodItemCard(
            FoodItem("2", "Tomat", StorageLocation.CHILLER, "2026-11-04", 0.85, 7, "2026-11-11", 7, FoodStatus.FRESH),
            onFinished = {}
        )
    }
}