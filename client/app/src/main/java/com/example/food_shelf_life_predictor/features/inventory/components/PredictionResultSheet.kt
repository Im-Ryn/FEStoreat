package com.example.food_shelf_life_predictor.features.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.food_shelf_life_predictor.core.ui.theme.*
import com.example.food_shelf_life_predictor.features.inventory.StorageLocation
import com.example.food_shelf_life_predictor.features.inventory.scan.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionResultSheet(
    state: ScanState.Result,
    onFoodChange: (String) -> Unit,
    onLocationChange: (StorageLocation) -> Unit,
    onSave: () -> Unit,
    onRetake: () -> Unit,
    onDismiss: () -> Unit
) {
    val p = state.prediction
    val lowConfidence = p.confidence < CONFIDENCE_THRESHOLD
    val tooRotten = p.freshnessScore < FRESHNESS_THRESHOLD
    var showWarning by remember { mutableStateOf(tooRotten) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Konfirmasi Bahan Segar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Koreksi jenis bahan & cek kelayakan simpan", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "${(p.confidence * 100).toInt()}% Yakin",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (lowConfidence) WarningYellow else BrandGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = File(state.photoPath),
                        contentDescription = "Foto bahan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp))
                    )
                    FoodClassDropdown(
                        selectedKey = state.foodKey,
                        highlight = lowConfidence,
                        onSelect = onFoodChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                FreshnessCard(p.freshnessScore)

                Text("Pilih Zona Penyimpanan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val order = listOf(StorageLocation.CHILLER, StorageLocation.FREEZER, StorageLocation.ROOM)
                order.forEach { location ->
                    val days = p.storageRecommendations
                        .firstOrNull { StorageLocation.fromApi(it.storageLocation) == location }
                        ?.estimatedDaysLeft
                    if (days != null) {
                        ZoneOption(location, days, state.location == location) { onLocationChange(location) }
                    }
                }
            }

            state.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onSave,
                enabled = !tooRotten && !state.saving,
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.saving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Simpan ke Kulkas")
            }
            OutlinedButton(onClick = onRetake, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Foto Ulang")
            }
        }
    }

    if (showWarning) {
        AlertDialog(
            onDismissRequest = { showWarning = false },
            title = { Text("Bahan Tidak Layak Simpan") },
            text = { Text("Skor kesegaran sangat rendah (${"%.2f".format(p.freshnessScore)}). Bahan ini sebaiknya tidak disimpan, tombol simpan dikunci.") },
            confirmButton = { TextButton(onClick = { showWarning = false }) { Text("Mengerti") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodClassDropdown(
    selectedKey: String, highlight: Boolean, onSelect: (String) -> Unit, modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = if (FOOD_CLASSES.any { it.key == selectedKey }) FOOD_CLASSES
    else FOOD_CLASSES + FoodClass(selectedKey, selectedKey)

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = labelFor(selectedKey),
            onValueChange = {},
            readOnly = true,
            label = { Text("Koreksi jenis bahan") },
            isError = highlight,
            supportingText = { if (highlight) Text("Keyakinan AI rendah, mohon periksa") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text("${it.key} (${it.label})") }, onClick = { onSelect(it.key); expanded = false })
            }
        }
    }
}

@Composable
private fun FreshnessCard(score: Double) {
    val (label, color, verdict) = when {
        score >= 0.70 -> Triple("Segar", SafeGreen, "Layak Simpan")
        score >= FRESHNESS_THRESHOLD -> Triple("Cukup Segar", WarningYellow, "Layak Simpan")
        else -> Triple("Tidak Segar", UrgentRed, "Tidak Layak")
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("ANALISIS KESEGARAN VISUAL", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
                Text("Status: $label (${"%.2f".format(score)})", fontWeight = FontWeight.SemiBold)
            }
            Text(verdict, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ZoneOption(location: StorageLocation, days: Int, selected: Boolean, onClick: () -> Unit) {
    val (title, icon) = when (location) {
        StorageLocation.CHILLER -> "Chiller (0–4°C)" to Icons.Default.Kitchen
        StorageLocation.FREEZER -> "Freezer (≤ -18°C)" to Icons.Default.AcUnit
        StorageLocation.ROOM -> "Pantry / Suhu Ruang (20–25°C)" to Icons.Default.Inventory2
    }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) BrandGreenLight.copy(alpha = 0.5f) else Color.White),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) BrandGreen else Color(0xFFE5E7EB))
    ) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = BrandGreen)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text("Estimasi AI: Tahan s.d $days Hari", style = MaterialTheme.typography.bodySmall)
            }
            RadioButton(selected = selected, onClick = onClick)
        }
    }
}