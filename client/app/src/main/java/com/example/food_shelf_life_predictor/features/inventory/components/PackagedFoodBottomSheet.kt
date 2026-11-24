package com.example.food_shelf_life_predictor.features.inventory.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.food_shelf_life_predictor.features.inventory.StorageLocation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagedFoodBottomSheet(
    photoUri: Uri? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, location: StorageLocation, expiryDate: LocalDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var nameTouched by remember { mutableStateOf(false) }
    var location by remember { mutableStateOf(StorageLocation.ROOM) } // default Pantry
    var expiryDate by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var dateTouched by remember { mutableStateOf(false) }

    val nameError = nameTouched && name.isBlank()
    val dateError = dateTouched && expiryDate == null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tambah Camilan", style = MaterialTheme.typography.titleLarge)

            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Pratinjau foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(100); nameTouched = true },
                label = { Text("Nama makanan/snack") },
                isError = nameError,
                supportingText = { if (nameError) Text("Nama tidak boleh kosong") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            LocationDropdown(selected = location, onSelect = { location = it })

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    expiryDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        ?: "Pilih tanggal kedaluwarsa"
                )
            }
            if (dateError) {
                Text(
                    "Tanggal wajib dipilih",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    nameTouched = true
                    dateTouched = true
                    val date = expiryDate
                    if (name.isNotBlank() && date != null) {
                        onSave(name.trim(), location, date)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Simpan") }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val todayMillis = LocalDate.now(ZoneOffset.UTC)
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        val pickerState = rememberDatePickerState(
            selectableDates = object : SelectableDates {
                // Kunci tanggal masa lalu
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayMillis
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        expiryDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    dateTouched = true
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDropdown(
    selected: StorageLocation,
    onSelect: (StorageLocation) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Lokasi simpan") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            StorageLocation.entries.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.label) },
                    onClick = {
                        onSelect(location)
                        expanded = false
                    }
                )
            }
        }
    }
}