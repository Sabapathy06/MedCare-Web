package com.example.ui.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HelpSystemScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "How to Use MedCare",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = ElegantTextPrimary
        )
        Text(
            text = "Simple guides to help you manage your medications.",
            fontSize = 14.sp,
            color = ElegantTextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        HelpSection(
            title = "How to Scan Medicine",
            description = "Tap 'Scan' on the dashboard or medicines list. Point your camera at the medicine box or strip so the name and expiry are visible. Tap 'Capture'.",
            icon = Icons.Default.CameraAlt,
            color = ElegantTeal
        )

        HelpSection(
            title = "Add Medicine Manually",
            description = "Go to 'Medicines' tab, tap '+ Add Medicine'. Fill in the name, dosage, and schedule. Don't forget to set an expiry date!",
            icon = Icons.Default.Edit,
            color = ElegantPurple
        )

        HelpSection(
            title = "Marking a Dose",
            description = "On the 'Today' screen, you will see your scheduled doses. Tap 'TAKE' when you consume the medicine, or 'SKIP' if you miss it.",
            icon = Icons.Default.CheckCircle,
            color = ElegantGreen
        )

        HelpSection(
            title = "Understanding Expiry Colors",
            description = "🟢 SAFE: Medicine is valid.\n🟡 EXPIRING SOON: Expires within 30 days.\n🔴 EXPIRED: Do not use this medicine.",
            icon = Icons.Default.Warning,
            color = ElegantAmber
        )

        HelpSection(
            title = "Using SOS",
            description = "In an emergency, tap the red SOS button. It will immediately notify your emergency contact and show your last known location.",
            icon = Icons.Default.Warning,
            color = ElegantRed
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Surface(
            color = ElegantDarkSurfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Need more help? Ask the AI Health Assistant in the 'AI Health' tab.",
                modifier = Modifier.padding(16.dp),
                fontSize = 14.sp,
                color = ElegantTextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun HelpSection(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = ElegantTextSecondary,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
