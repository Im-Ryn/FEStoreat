package com.example.food_shelf_life_predictor.features.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food_shelf_life_predictor.core.ui.theme.AppBackground
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenDark
import com.example.food_shelf_life_predictor.core.ui.theme.SafeGreen
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed
import com.example.food_shelf_life_predictor.core.ui.theme.WarningYellow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class NotificationType { URGENT, MEAL_PLAN, WARNING }

data class AppNotification(
    val id: Int,
    val type: NotificationType,
    val title: String,
    val body: String,
    val time: String,
    val unread: Boolean,
    val badge: String? = null
)

class NotificationsViewModel : ViewModel() {
    private val _items = MutableStateFlow(
        listOf(
            AppNotification(1, NotificationType.URGENT, "Daging Ayam Mentah hampir kedaluwarsa!", "Sisa 1 hari lagi di Chiller Kulkas Kost. Segera olah hari ini.", "10 mnt lalu", true, "Mendesak • Eat Me First"),
            AppNotification(2, NotificationType.MEAL_PLAN, "Pengingat Rencana Makan Hari Ini", "Hari ini jadwal mengolah Telur Ayam & Tomat.", "Pagi tadi 07:00", true),
            AppNotification(3, NotificationType.WARNING, "Roti Tawar Gandum", "Sisa 3 hari lagi di Meja Pantry.", "Kemarin", false)
        )
    )
    val items: StateFlow<List<AppNotification>> = _items.asStateFlow()

    val hasUnread: StateFlow<Boolean> = _items
        .map { list -> list.any { it.unread } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun markAllRead() {
        _items.update { list -> list.map { it.copy(unread = false) } }
    }
}

private enum class NotificationFilter(val label: String) {
    ALL("Semua"), URGENT("Mendesak (Kritis)"), MEAL("Rencana Makan")
}

private val MealBlue = Color(0xFF0EA5E9)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onSchedule: () -> Unit,
    viewModel: NotificationsViewModel = viewModel()
) {
    val items by viewModel.items.collectAsState()
    var filter by remember { mutableStateOf(NotificationFilter.ALL) }

    val shown = items.filter {
        when (filter) {
            NotificationFilter.ALL -> true
            NotificationFilter.URGENT -> it.type == NotificationType.URGENT
            NotificationFilter.MEAL -> it.type == NotificationType.MEAL_PLAN
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                }
                Text(
                    "Notifikasi & Pengingat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = viewModel::markAllRead) {
                    Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(18.dp), tint = BrandGreen)
                    Spacer(Modifier.width(4.dp))
                    Text("Tandai Dibaca", color = BrandGreen, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationFilter.entries.forEach { f ->
                    FilterPill(f.label, filter == f) { filter = f }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (shown.isEmpty()) {
                    item {
                        Text(
                            "Tidak ada notifikasi",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                        )
                    }
                }
                items(shown, key = { it.id }) { n ->
                    NotificationCard(n, onSchedule)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        shape = RoundedCornerShape(20.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            selectedContainerColor = BrandGreen,
            selectedLabelColor = Color.White
        )
    )
}

@Composable
private fun NotificationCard(n: AppNotification, onSchedule: () -> Unit) {
    val accent: Color
    val icon: ImageVector
    when (n.type) {
        NotificationType.URGENT -> { accent = UrgentRed; icon = Icons.Default.Warning }
        NotificationType.MEAL_PLAN -> { accent = MealBlue; icon = Icons.Default.CalendarMonth }
        NotificationType.WARNING -> { accent = WarningYellow; icon = Icons.Default.Schedule }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(accent))
            Column(
                modifier = Modifier.weight(1f).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(icon, null, tint = accent) }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(n.title, fontWeight = FontWeight.Bold)
                        Text(n.body, style = MaterialTheme.typography.bodySmall)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                n.time,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            n.badge?.let {
                                Text(
                                    it,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(accent.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    if (n.unread) {
                        Box(Modifier.padding(start = 8.dp, top = 4.dp).size(8.dp).clip(CircleShape).background(SafeGreen))
                    }
                }
                if (n.type == NotificationType.URGENT) {
                    OutlinedButton(
                        onClick = onSchedule,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Jadwalkan ke Kalender", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}