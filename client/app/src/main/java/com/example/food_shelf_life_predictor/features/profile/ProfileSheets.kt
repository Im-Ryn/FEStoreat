package com.example.food_shelf_life_predictor.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppSheet(
    title: String,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
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
                if (icon != null) {
                    Icon(icon, null, tint = BrandGreen)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Tutup") }
            }
            content()
        }
    }
}

@Composable
private fun GreenButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
        modifier = Modifier.fillMaxWidth().height(52.dp)
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

@Composable
fun GuideSheet(onDismiss: () -> Unit) {
    AppSheet(title = "Panduan Chiller vs Freezer Anak Kost", onDismiss = onDismiss) {
        GuideCard(
            title = "Chiller (0–4°C)",
            body = "Ideal untuk telur (3–5 minggu), sayur segar (3–7 hari), dan ayam mentah yang akan dimasak dalam 1–2 hari.",
            container = Color(0xFFE0F2FE),
            accent = Color(0xFF0284C7)
        )
        GuideCard(
            title = "Freezer (<-18°C)",
            body = "Pindahkan daging ayam mentah ke Freezer jika belum sempat dimasak agar tahan hingga 2–6 bulan.",
            container = Color(0xFFEEF2FF),
            accent = Color(0xFF4F46E5)
        )
        GreenButton("Mengerti", onDismiss)
    }
}

@Composable
private fun GuideCard(title: String, body: String, container: Color, accent: Color) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = accent)
            Text(body, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun StandardSheet(onDismiss: () -> Unit) {
    AppSheet(title = "Standar USDA FoodKeeper & BPOM", onDismiss = onDismiss, icon = Icons.Default.Shield) {
        Text(
            "Estimasi masa simpan otomatis dihitung menggunakan basis data USDA FoodKeeper untuk bahan segar (daging, sayur, buah, telur) dan pedoman label kedaluwarsa BPOM RI untuk produk pangan olahan/kemasan.",
            style = MaterialTheme.typography.bodyMedium
        )
        GreenButton("Tutup", onDismiss)
    }
}

@Composable
fun AccountDetailSheet(
    name: String,
    email: String,
    kost: String,
    onSave: (name: String, email: String, kost: String) -> Unit,
    onDismiss: () -> Unit
) {
    var n by remember { mutableStateOf(name) }
    var e by remember { mutableStateOf(email) }
    var k by remember { mutableStateOf(kost) }
    var error by remember { mutableStateOf<String?>(null) }

    AppSheet(title = "Detail Akun & Profil Kost", onDismiss = onDismiss) {
        SheetField("Nama Mahasiswa", n) { n = it }
        SheetField("Email Kampus", e) { e = it }
        SheetField("Kamar & Nama Kost", k) { k = it }
        error?.let { Text(it, color = UrgentRed, style = MaterialTheme.typography.bodySmall) }
        GreenButton("Simpan Perubahan", onClick = {
            if (n.isBlank() || !e.contains("@")) error = "Nama dan email wajib diisi dengan benar"
            else onSave(n.trim(), e.trim(), k.trim())
        })
    }
}

@Composable
private fun SheetField(label: String, value: String, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun formatTime12(hour: Int, minute: Int): String {
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return "%02d : %02d  %s".format(h12, minute, if (hour < 12) "AM" else "PM")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsSheet(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onSave: (enabled: Boolean, hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var on by remember { mutableStateOf(enabled) }
    var h by remember { mutableStateOf(hour) }
    var m by remember { mutableStateOf(minute) }
    var showPicker by remember { mutableStateOf(false) }

    AppSheet(title = "Notifikasi Pengingat", onDismiss = onDismiss) {
        Text(
            "Atur alarm makanan H-2 sebelum kedaluwarsa",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BrandGreenLight.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, null, tint = BrandGreen)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Pengingat H-2 Kedaluwarsa", fontWeight = FontWeight.Bold)
                    Text("Kirim alarm untuk bahan mendesak", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = on,
                    onCheckedChange = { on = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = BrandGreen)
                )
            }
        }
        Text("Waktu pengiriman pengingat", style = MaterialTheme.typography.labelMedium)
        Surface(
            onClick = { showPicker = true },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(formatTime12(h, m), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Schedule, null, tint = Color.Gray)
            }
        }
        GreenButton("Simpan Pengaturan Notifikasi", onClick = { onSave(on, h, m) })
    }

    if (showPicker) {
        val timeState = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    h = timeState.hour
                    m = timeState.minute
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Batal") } },
            text = { TimePicker(state = timeState) }
        )
    }
}