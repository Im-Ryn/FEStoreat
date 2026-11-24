package com.example.food_shelf_life_predictor.features.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.food_shelf_life_predictor.core.ui.components.ScreenHeader
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenDark
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed
import com.example.food_shelf_life_predictor.features.inventory.StorageLocation

private enum class ProfileSheet { GUIDE, ACCOUNT, REMINDER, STANDARD }

@Composable
fun ProfileContent(
    totalItems: Int,
    hasUnread: Boolean,
    onNotificationClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.state.collectAsState()
    var sheet by remember { mutableStateOf<ProfileSheet?>(null) }

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ScreenHeader(
                    title = "Profil & Akun",
                    subtitle = "Kelola akun kost & preferensi penyimpanan",
                    hasUnread = hasUnread,
                    onNotificationClick = onNotificationClick
                )
            }
            item { ProfileCard(profile.name, profile.email, totalItems) }
            item { TipsBanner(onClick = { sheet = ProfileSheet.GUIDE }) }

            item { SectionLabel("PENGATURAN AKUN & KOST") }
            item {
                SettingsCard {
                    SettingRow(
                        icon = Icons.Default.ManageAccounts,
                        title = "Detail Akun & Profil Kost",
                        subtitle = profile.kost,
                        onClick = { sheet = ProfileSheet.ACCOUNT }
                    )
                    HorizontalDivider()
                    SettingRow(
                        icon = Icons.Default.NotificationsActive,
                        title = "Notifikasi Pengingat",
                        subtitle = null,
                        onClick = { sheet = ProfileSheet.REMINDER },
                        trailing = {
                            Text(
                                if (profile.reminderEnabled) "Aktif (${profile.reminderTime} WIB)" else "Nonaktif",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (profile.reminderEnabled) BrandGreenDark else Color.Gray,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandGreenLight.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
                        }
                    )
                    HorizontalDivider()
                    SettingRow(
                        icon = Icons.Default.MenuBook,
                        title = "Standar Masa Simpan Pangan",
                        subtitle = "USDA FoodKeeper & BPOM",
                        onClick = { sheet = ProfileSheet.STANDARD }
                    )
                }
            }

            item { SectionLabel("PENGUJIAN & SESI") }
            item {
                SettingsCard {
                    SettingRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Keluar Akun",
                        subtitle = null,
                        onClick = onLogout,
                        tint = UrgentRed,
                        titleColor = UrgentRed
                    )
                }
            }
        }

        when (sheet) {
            ProfileSheet.GUIDE -> GuideSheet(onDismiss = { sheet = null })
            ProfileSheet.ACCOUNT -> AccountDetailSheet(
                name = profile.name,
                email = profile.email,
                kost = profile.kost,
                onSave = { n, e, k -> viewModel.saveAccount(n, e, k); sheet = null },
                onDismiss = { sheet = null }
            )
            ProfileSheet.REMINDER -> NotificationSettingsSheet(
                enabled = profile.reminderEnabled,
                hour = profile.reminderHour,
                minute = profile.reminderMinute,
                onSave = { en, h, m -> viewModel.saveReminder(en, h, m); sheet = null },
                onDismiss = { sheet = null }
            )
            ProfileSheet.STANDARD -> StandardSheet(onDismiss = { sheet = null })
            null -> Unit
        }
    }
}

@Composable
private fun ProfileCard(name: String, email: String, totalItems: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(BrandGreenLight),
                        contentAlignment = Alignment.Center
                    ) { Text("🍍", fontSize = 28.sp) }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(BrandGreen)
                            .align(Alignment.BottomEnd)
                    )
                }
                Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Anak Kost Mandiri",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Box(Modifier.padding(horizontal = 12.dp).width(1.dp).fillMaxHeight().background(Color(0xFFE5E7EB)))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatItem(StorageLocation.entries.size.toString(), "Lokasi Simpan")
                StatItem(totalItems.toString(), "Bahan Terpantau")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TipsBanner(onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandGreen),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tips Cegah Makanan Basi?", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Pelajari panduan masa simpan chiller vs freezer.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = BrandGreenDark)
            ) { Text("Baca Panduan", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    tint: Color = BrandGreen,
    titleColor: Color = Color.Unspecified,
    trailing: @Composable RowScope.() -> Unit = {
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray)
    }
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}