package com.example.food_shelf_life_predictor.features.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodTypeSheet(
    onDismiss: () -> Unit,
    onFresh: () -> Unit,
    onPackaged: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Mau catat makanan apa hari ini?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Pilih jenis bahan untuk pencatatan otomatis atau manual",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            TypeCard(
                icon = Icons.Default.CameraAlt,
                tag = "AI VISION",
                title = "Bahan Segar / Mentah",
                desc = "Sayur, buah, daging segar, telur. Wajib foto untuk deteksi AI & estimasi masa tahan.",
                accent = BrandGreen,
                onClick = onFresh
            )
            TypeCard(
                icon = Icons.Default.Inventory2,
                tag = "PRINTED EXP",
                title = "Makanan / Minuman Kemasan Pabrik",
                desc = "Snack, susu UHT, sosis, biskuit. Cukup input tanggal kedaluwarsa kemasan.",
                accent = Color(0xFFD97706),
                onClick = onPackaged
            )
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Batal") }
        }
    }
}

@Composable
private fun TypeCard(
    icon: ImageVector, tag: String, title: String, desc: String,
    accent: Color, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(16.dp), color = accent.copy(alpha = 0.12f), modifier = Modifier.size(56.dp)) {
                Icon(icon, null, tint = accent, modifier = Modifier.padding(14.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    tag, style = MaterialTheme.typography.labelSmall, color = accent,
                    fontWeight = FontWeight.Bold
                )
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}