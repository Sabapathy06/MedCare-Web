package com.example.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DoseRecordEntity
import com.example.ui.components.DoseStatusBadge
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MedicationHistoryScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    val doseHistory by viewModel.allDoseHistory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val statusFilters = listOf("All", "Taken", "Skipped", "Snoozed", "Upcoming", "Missed")

    val filteredHistory = doseHistory.filter { record ->
        val matchesSearch = record.medicineName.contains(searchQuery, ignoreCase = true) ||
                record.scheduledDate.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (selectedStatusFilter) {
            "All" -> true
            else -> record.status.equals(selectedStatusFilter, ignoreCase = true)
        }
        matchesSearch && matchesStatus
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElegantDarkBackground)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by medicine or date (YYYY-MM-DD)...", color = ElegantTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ElegantPurple) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElegantPurple,
                unfocusedBorderColor = ElegantDarkOutline,
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedContainerColor = ElegantDarkSurface,
                unfocusedContainerColor = ElegantDarkSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(statusFilters) { status ->
                val isSelected = selectedStatusFilter == status
                Surface(
                    color = if (isSelected) ElegantPurple else ElegantDarkSurfaceVariant,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) ElegantPurple else ElegantDarkOutline),
                    modifier = Modifier.clickable { selectedStatusFilter = status }
                ) {
                    Text(
                        text = status,
                        color = if (isSelected) ElegantOnPrimary else ElegantTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredHistory.isEmpty()) {
            EmptyStateView(
                title = "No dose history found",
                subtitle = "Logged doses, taken events, and skipped alerts will appear here.",
                icon = Icons.Default.History
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredHistory, key = { it.id }) { record ->
                    HistoryItemCard(record = record)
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(record: DoseRecordEntity) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElegantDarkOutline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = record.medicineName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ElegantTextPrimary
                    )
                    Text(
                        text = "${record.dosage} ${record.dosageUnit} • ${record.scheduledDate} at ${record.scheduledTime}",
                        fontSize = 12.sp,
                        color = ElegantTextSecondary
                    )
                }
                DoseStatusBadge(status = record.status)
            }

            if (record.actionTimeMillis != null) {
                Spacer(modifier = Modifier.height(6.dp))
                val actionFormatted = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(record.actionTimeMillis))
                Text(
                    text = "Action recorded: $actionFormatted",
                    fontSize = 11.sp,
                    color = ElegantTeal,
                    fontWeight = FontWeight.Medium
                )
            }

            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${record.notes}",
                    fontSize = 11.sp,
                    color = ElegantTextMuted
                )
            }
        }
    }
}
