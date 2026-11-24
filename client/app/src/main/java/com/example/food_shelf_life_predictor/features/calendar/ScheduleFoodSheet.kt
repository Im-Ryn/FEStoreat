package com.example.food_shelf_life_predictor.features.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenDark
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight
import com.example.food_shelf_life_predictor.features.inventory.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleFoodSheet(
    foods: List<FoodItem>,
    dateLabel: String,
    onAdd: (food: FoodItem, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(foods.firstOrNull()) }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, null, tint = BrandGreen)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Jadwalkan Bahan Kulkas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            if (foods.isEmpty()) {
                Text(
                    "Belum ada bahan di inventaris. Tambahkan makanan dulu dari Beranda.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Pilih Bahan dari Inventaris",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selected?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        foods.forEach { food ->
                            DropdownMenuItem(
                                text = { Text("${food.name} (${food.storageLocation.label})") },
                                onClick = { selected = food; expanded = false }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    foods.take(3).forEach { food ->
                        Surface(
                            onClick = { selected = food },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected?.id == food.id) BrandGreenLight else Color(0xFFF1F5F9),
                            border = BorderStroke(
                                1.dp,
                                if (selected?.id == food.id) BrandGreen else Color.Transparent
                            )
                        ) {
                            Text(
                                food.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected?.id == food.id) BrandGreenDark else Color.Unspecified,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    "Catatan Waktu Makan",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(80) },
                    placeholder = { Text("Rencana: Sarapan Pagi / Malam") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { selected?.let { onAdd(it, note.trim()) } },
                enabled = selected != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Tambahkan ke Jadwal ($dateLabel)", fontWeight = FontWeight.Bold)
            }
        }
    }
}