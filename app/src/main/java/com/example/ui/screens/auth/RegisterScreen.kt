package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: (String, String, String, String, String, String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") } // YYYY-MM-DD
    var emergencyName by remember { mutableStateOf("") }
    var emergencyPhone by remember { mutableStateOf("") }

    var showError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        dob = sdf.format(java.util.Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create Account",
            color = ElegantTextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Join MedCare to stay healthy",
            color = ElegantTextSecondary,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Required Fields Section
        Text(
            text = "Personal Information",
            color = ElegantPurple,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", icon = Icons.Default.Person)
        Spacer(modifier = Modifier.height(12.dp))
        
        // DOB Picker Field
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = dob,
                onValueChange = { },
                label = { Text("Date of Birth") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ElegantPurple) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElegantPurple,
                    unfocusedBorderColor = ElegantDarkOutline,
                    focusedLabelColor = ElegantPurple,
                    cursorColor = ElegantPurple,
                    focusedContainerColor = ElegantDarkSurface,
                    unfocusedContainerColor = ElegantDarkSurface
                )
            )
            // Invisible clickable layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.Email)
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number", icon = Icons.Default.Phone)
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock, isPassword = true)
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = "Confirm Password", icon = Icons.Default.Lock, isPassword = true)

        Spacer(modifier = Modifier.height(32.dp))

        // Optional Emergency Contact Section
        Text(
            text = "Emergency Contact (Recommended)",
            color = ElegantPurple,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(16.dp))

        AuthTextField(value = emergencyName, onValueChange = { emergencyName = it }, label = "Contact Name", icon = Icons.Default.ContactPhone)
        Spacer(modifier = Modifier.height(12.dp))
        AuthTextField(value = emergencyPhone, onValueChange = { emergencyPhone = it }, label = "Contact Phone", icon = Icons.Default.Phone)

        if (showError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = showError!!, color = ElegantRed, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                when {
                    fullName.isBlank() || email.isBlank() || password.isBlank() || dob.isBlank() -> showError = "Please fill all required fields"
                    password != confirmPassword -> showError = "Passwords do not match"
                    else -> onRegisterSuccess(fullName, email, password, dob, emergencyName, emergencyPhone)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElegantPurple)
        ) {
            Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateBack) {
            Text(
                "Already have an account? Login",
                color = ElegantTeal,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = { Icon(icon, contentDescription = null, tint = ElegantPurple) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ElegantPurple,
            unfocusedBorderColor = ElegantDarkOutline,
            focusedLabelColor = ElegantPurple,
            cursorColor = ElegantPurple,
            focusedContainerColor = ElegantDarkSurface,
            unfocusedContainerColor = ElegantDarkSurface
        )
    )
}
