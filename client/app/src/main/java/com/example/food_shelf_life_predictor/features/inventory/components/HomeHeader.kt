package com.example.food_shelf_life_predictor.features.inventory.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.components.NotificationBell
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight
import com.example.food_shelf_life_predictor.core.ui.theme.SafeGreen
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed
import com.example.food_shelf_life_predictor.core.ui.theme.WarningYellow

@Composable
fun HomeHeader(
    hasUnread: Boolean,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Halo, Anak Kost!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Kelola makananmu, hemat pengeluaran",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        NotificationBell(hasUnread = hasUnread, onClick = onNotificationClick)
    }
}

@Composable
fun SummaryCards(total: Int, urgent: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Inventory2,
            accent = BrandGreen,
            container = Color.White,
            border = BrandGreenLight,
            title = "Stok Tersimpan",
            value = "Total Bahan: $total",
            valueColor = MaterialTheme.colorScheme.onSurface
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Warning,
            accent = UrgentRed,
            container = Color(0xFFFEF2F2),
            border = Color(0xFFFECACA),
            title = "Prioritas Eat Me First",
            value = "$urgent Segera Masak",
            valueColor = UrgentRed
        )
    }
}

@Composable
private fun SummaryCard(
    icon: ImageVector,
    accent: Color,
    container: Color,
    border: Color,
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (valueColor == UrgentRed) UrgentRed else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
            }
        }
    }
}

@Composable
fun PriorityHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Urutan Prioritas (Eat Me First)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        LegendDot(UrgentRed, "Lewat")
        LegendDot(WarningYellow, "Segera")
        LegendDot(SafeGreen, "Segar")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}