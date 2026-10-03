package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

import com.example.ui.components.FloatingPillsBackground
import androidx.compose.material.icons.filled.MedicalServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onQaBypass: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(ElegantDarkBackground)) {
        // Subtle animated background
        FloatingPillsBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ElegantPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "MedCare",
                color = ElegantTextPrimary,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Text(
                text = "Intelligent Medicine Manager",
                color = ElegantPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email or Phone") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = ElegantPurple) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantPurple,
                    unfocusedBorderColor = ElegantDarkOutline,
                    focusedLabelColor = ElegantPurple,
                    cursorColor = ElegantPurple,
                    focusedContainerColor = ElegantDarkSurface.copy(alpha = 0.8f),
                    unfocusedContainerColor = ElegantDarkSurface.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = ElegantPurple) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = ElegantTextSecondary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantPurple,
                    unfocusedBorderColor = ElegantDarkOutline,
                    focusedLabelColor = ElegantPurple,
                    cursorColor = ElegantPurple,
                    focusedContainerColor = ElegantDarkSurface.copy(alpha = 0.8f),
                    unfocusedContainerColor = ElegantDarkSurface.copy(alpha = 0.8f)
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { if (email.isNotBlank() && password.isNotBlank()) onLoginSuccess(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple)
            ) {
                Text("LOGIN", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("New to MedCare?", color = ElegantTextSecondary)
                TextButton(onClick = onNavigateToRegister) {
                    Text("Create Account", color = ElegantTeal, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Subtle QA Bypass for testing
            OutlinedButton(
                onClick = onQaBypass,
                border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElegantPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text("QA Bypass Login", fontSize = 12.sp, color = ElegantTextSecondary)
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
