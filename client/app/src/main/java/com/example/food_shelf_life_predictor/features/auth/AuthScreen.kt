package com.example.food_shelf_life_predictor.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.food_shelf_life_predictor.core.ui.theme.AppBackground
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreen
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenDark
import com.example.food_shelf_life_predictor.core.ui.theme.BrandGreenLight
import com.example.food_shelf_life_predictor.core.ui.theme.UrgentRed

@Composable
fun AuthScreen(onAuthenticated: (name: String, email: String) -> Unit) {
    var isRegister by remember { mutableStateOf(false) }
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        error = when {
            isRegister && nickname.isBlank() -> "Nama panggilan tidak boleh kosong"
            !email.contains("@") -> "Format email tidak valid"
            password.length < 6 -> "Kata sandi minimal 6 karakter"
            else -> null
        }
        if (error == null) {
            val name = if (isRegister) nickname.trim() else email.substringBefore("@")
            onAuthenticated(name, email.trim())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier.size(72.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(14.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BrandGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Kitchen, contentDescription = null, tint = Color.White)
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandGreenLight.copy(alpha = 0.6f),
            border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = BrandGreenDark, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "SDG 12 • Smart Kost Food Management",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreenDark
                )
            }
        }

        Text("StorEat", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Atur stok makanan kulkas kost, kurangi mubazir & hemat uang saku.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuthTabs(isRegister = isRegister, onChange = { isRegister = it; error = null })

                if (isRegister) {
                    AuthField(
                        label = "Nama Panggilan & Kamar Kost",
                        value = nickname,
                        onValueChange = { nickname = it },
                        icon = Icons.Default.Home,
                        placeholder = "Anak Kost A-03"
                    )
                }
                AuthField(
                    label = "Email Kampus / Akun Kost",
                    value = email,
                    onValueChange = { email = it },
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email,
                    placeholder = "nama@mahasiswa.ac.id"
                )
                AuthField(
                    label = "Kata Sandi",
                    value = password,
                    onValueChange = { password = it },
                    icon = Icons.Default.Lock,
                    keyboardType = KeyboardType.Password,
                    isPassword = true,
                    showPassword = showPassword,
                    onTogglePassword = { showPassword = !showPassword }
                )

                if (!isRegister) {
                    Text(
                        "Lupa Kata Sandi?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { /* TODO: lupa kata sandi */ }
                    )
                }

                error?.let {
                    Text(it, color = UrgentRed  , style = MaterialTheme.typography.bodySmall)
                }

                Button(
                    onClick = ::submit,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text(
                        if (isRegister) "Daftar & Masuk ke Dapur Kost" else "Masuk ke Dapur Kost",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        "  atau masuk dengan  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedButton(
                    onClick = { /* TODO: login Google */ },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("G", fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                    Spacer(Modifier.width(8.dp))
                    Text("Masuk dengan Google", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onAuthenticated("Tamu", "") },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BrandGreen.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = BrandGreenLight.copy(alpha = 0.5f),
                    contentColor = BrandGreenDark
                ),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Coba Mode Tamu (Tanpa Akun)", fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
            Button(
                onClick = { onAuthenticated("Tamu", "") /* TODO: langsung buka scan */ },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
            ) {
                Text("Langsung Scan", fontWeight = FontWeight.Bold)
            }
        }

        Text(
            "Dengan masuk, kamu menyetujui Ketentuan Layanan StorEat.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AuthTabs(isRegister: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF1F5F9))
            .padding(4.dp)
    ) {
        AuthTab("Masuk", !isRegister, Modifier.weight(1f)) { onChange(false) }
        AuthTab("Daftar Akun", isRegister, Modifier.weight(1f)) { onChange(true) }
    }
}

@Composable
private fun AuthTab(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(placeholder) },
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Tampilkan kata sandi"
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation()
            else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}