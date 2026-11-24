package com.example.food_shelf_life_predictor.features.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.components.ScreenHeader
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenDark
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight
import com.example.food_shelf_life_predictor.core.ui.theme.SafeGreen
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed
import com.example.food_shelf_life_predictor.core.ui.theme.WarningYellow
import com.example.food_shelf_life_predictor.features.inventory.FoodItem
import com.example.food_shelf_life_predictor.features.inventory.FoodStatus
import com.example.food_shelf_life_predictor.features.inventory.components.color
import java.time.LocalDate

data class PlannedMeal(
    val id: String,
    val name: String,
    val place: String,
    val daysLeft: Int,
    val tag: String,
    val accent: Color,
    val plan: String?,
    val actionLabel: String
)

private fun mockPlans(today: LocalDate): Map<LocalDate, List<PlannedMeal>> = mapOf(
    today to listOf(
        PlannedMeal("m1", "Daging Ayam Mentah", "Chiller", 1, "Kritis • Segera Habiskan", UrgentRed, "Rencana: Dimasak siang/malam", "Sudah Dimasak Hari Ini"),
        PlannedMeal("m2", "Roti Tawar Gandum", "Pantry", 3, "Camilan • Pantau Tanggal", WarningYellow, null, "Sudah Dimakan")
    ),
    today.plusDays(1) to listOf(
        PlannedMeal("m3", "Telur Ayam", "Chiller", 8, "Segar • Rencana Masak", SafeGreen, "Rencana: Digoreng", "Sudah Dimasak Hari Ini")
    )
)

private val dayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
private val shortNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
private val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")

private fun formatDateLabel(date: LocalDate): String =
    "${dayNames[date.dayOfWeek.value - 1]}, ${"%02d".format(date.dayOfMonth)} ${monthNames[date.monthValue - 1]}"

@Composable
fun CalendarContent(
    foods: List<FoodItem>,
    hasUnread: Boolean,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val plans = remember {
        mutableStateMapOf<LocalDate, List<PlannedMeal>>().apply { putAll(mockPlans(today)) }
    }
    var showSchedule by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(today) }
    var done by remember { mutableStateOf(setOf<String>()) }

    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val days = (0..6).map { monday.plusDays(it.toLong()) }
    val meals = plans[selected].orEmpty().filter { it.id !in done }
    val dayName = dayNames[selected.dayOfWeek.value - 1]
    val heading = if (selected == today) "Rencana Makan Hari Ini ($dayName)"
    else "Rencana Makan $dayName, ${selected.dayOfMonth}"

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenHeader(
                title = "Jadwal & Pengingat Makan",
                subtitle = "Rencanakan konsumsi agar makanan tidak terbuang",
                hasUnread = hasUnread,
                onNotificationClick = onNotificationClick
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                days.forEach { date ->
                    DayCell(
                        label = shortNames[date.dayOfWeek.value - 1],
                        dayNumber = date.dayOfMonth,
                        selected = date == selected,
                        dotColor = plans[date]?.firstOrNull()?.accent ?: Color(0xFFD1D5DB),
                        onClick = { selected = date },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    heading,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${meals.size} Bahan Dijadwalkan",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreenDark,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandGreenLight.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
        if (meals.isEmpty()) {
            item {
                Text(
                    "Belum ada rencana makan di hari ini",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                )
            }
        }
        items(meals, key = { it.id }) { meal ->
            MealCard(meal = meal, onDone = { done = done + meal.id })
        }
        item {
            Button(
                onClick = { showSchedule = true },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Jadwalkan Makanan Lain", fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showSchedule) {
        ScheduleFoodSheet(
            foods = foods,
            dateLabel = formatDateLabel(selected),
            onAdd = { food, note ->
                val meal = PlannedMeal(
                    id = "s-${java.util.UUID.randomUUID()}",
                    name = food.name,
                    place = food.storageLocation.label,
                    daysLeft = food.daysLeft,
                    tag = when (food.status) {
                        FoodStatus.EXPIRED -> "Kritis • Segera Habiskan"
                        FoodStatus.EXPIRING_SOON -> "Segera • Pantau Tanggal"
                        FoodStatus.FRESH -> "Segar • Rencana Masak"
                    },
                    accent = food.status.color(),
                    plan = note.ifBlank { null },
                    actionLabel = "Sudah Dimasak Hari Ini"
                )
                plans[selected] = plans[selected].orEmpty() + meal
                showSchedule = false
            },
            onDismiss = { showSchedule = false }
        )
    }
}

@Composable
private fun DayCell(
    label: String,
    dayNumber: Int,
    selected: Boolean,
    dotColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) BrandGreenLight.copy(alpha = 0.5f) else Color.White,
        border = BorderStroke(1.dp, if (selected) BrandGreen else Color(0xFFE5E7EB)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "%02d".format(dayNumber),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) BrandGreenDark else MaterialTheme.colorScheme.onSurface
            )
            Box(Modifier.size(6.dp).clip(CircleShape).background(dotColor))
        }
    }
}

@Composable
private fun MealCard(meal: PlannedMeal, onDone: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(6.dp).fillMaxHeight().background(meal.accent))
            Column(
                modifier = Modifier.weight(1f).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BrandGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Restaurant, null, tint = BrandGreen, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            meal.tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = meal.accent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(meal.accent.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Text(meal.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            "${meal.place} • Sisa ${meal.daysLeft} Hari",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                meal.plan?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp), tint = BrandGreen)
                        Spacer(Modifier.width(4.dp))
                        Text(it, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                    }
                }

                OutlinedButton(
                    onClick = onDone,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = BrandGreenLight.copy(alpha = 0.4f),
                        contentColor = BrandGreenDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(meal.actionLabel, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}