package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SosEmergencyDialog
import com.example.ui.screens.adherence.AdherenceAnalyticsScreen
import com.example.ui.screens.appointments.AppointmentManagementScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.caregiver.CaregiverManagementScreen
import com.example.ui.screens.dashboard.PatientDashboardScreen
import com.example.ui.screens.emergency.SosEmergencyScreen
import com.example.ui.screens.help.HelpSystemScreen
import com.example.ui.screens.history.MedicationHistoryScreen
import com.example.ui.screens.medications.MedicationListScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.prescriptions.PrescriptionManagementScreen
import com.example.ui.screens.profile.ProfileSettingsScreen
import com.example.ui.screens.scanner.MedicineScannerScreen
import com.example.ui.screens.stock.StockRefillScreen
import com.example.ui.screens.sustainability.SustainabilityImpactScreen
import com.example.ui.theme.ElegantAmber
import com.example.ui.theme.ElegantBlue
import com.example.ui.theme.ElegantDarkBackground
import com.example.ui.theme.ElegantDarkOutline
import com.example.ui.theme.ElegantDarkSurface
import com.example.ui.theme.ElegantDarkSurfaceElevated
import com.example.ui.theme.ElegantDarkSurfaceVariant
import com.example.ui.theme.ElegantEmergencyRed
import com.example.ui.theme.ElegantGreen
import com.example.ui.theme.ElegantOnPrimary
import com.example.ui.theme.ElegantOnPurpleContainer
import com.example.ui.theme.ElegantPurple
import com.example.ui.theme.ElegantPurpleContainer
import com.example.ui.theme.ElegantRed
import com.example.ui.theme.ElegantTeal
import com.example.ui.theme.ElegantTextMuted
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import com.example.viewmodel.MedCareViewModel
import kotlinx.coroutines.flow.collectLatest

enum class ScreenNav(val title: String, val icon: ImageVector) {
    DASHBOARD("Today", Icons.Default.Home),
    MEDICATIONS("Medicines", Icons.Default.LocalPharmacy),
    MEDICINE_SCANNER("Scan Meds", Icons.Default.CameraAlt),
    ADHERENCE("Adherence", Icons.Default.Insights),
    AI_ASSISTANT("AI Health", Icons.Default.AutoAwesome),
    CARE_NETWORK("Care", Icons.Default.People),
    HISTORY("History", Icons.Default.History),
    PRESCRIPTIONS("Prescriptions", Icons.Default.Description),
    APPOINTMENTS("Appointments", Icons.Default.CalendarMonth),
    STOCK_REFILLS("Stock & Refill", Icons.Default.LocalPharmacy),
    SUSTAINABILITY("Sustainability", Icons.Default.Eco),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    SOS_EMERGENCY("SOS Emergency", Icons.Default.Warning),
    PROFILE("Profile", Icons.Default.Person),
    HELP("Help", Icons.Default.Info)
}

enum class AuthState {
    LOADING, LOGIN, REGISTER, ONBOARDING, MAIN_APP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MedCareViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(ScreenNav.DASHBOARD) }
    var authState by remember { mutableStateOf(AuthState.LOADING) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showQuickSosDialog by remember { mutableStateOf(false) }

    val unreadNotifCount by viewModel.unreadNotifCount.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentUser, allUsers) {
        // Wait for database check
        if (allUsers == null) return@LaunchedEffect 
        
        if (currentUser == null) {
            authState = AuthState.LOGIN
        } else if (!currentUser!!.isOnboardingCompleted) {
            authState = AuthState.ONBOARDING
        } else {
            authState = AuthState.MAIN_APP
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messageFlow.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    when (authState) {
        AuthState.LOADING -> {
            Box(modifier = Modifier.fillMaxSize().background(ElegantDarkBackground), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElegantPurple)
            }
        }
        AuthState.LOGIN -> LoginScreen(
            onLoginSuccess = { email, pass -> viewModel.login(email, pass) },
            onNavigateToRegister = { authState = AuthState.REGISTER },
            onQaBypass = { viewModel.qaBypassLogin() }
        )
        AuthState.REGISTER -> RegisterScreen(
            onRegisterSuccess = { name, email, pass, dob, eName, ePhone ->
                viewModel.registerNewUser(name, email, pass, dob, eName, ePhone)
            },
            onNavigateBack = { authState = AuthState.LOGIN }
        )
        AuthState.ONBOARDING -> OnboardingScreen(
            onComplete = { viewModel.completeOnboarding() }
        )
        AuthState.MAIN_APP -> {
            MainScaffold(
                currentScreen = currentScreen,
                onScreenChange = { currentScreen = it },
                showMoreMenu = showMoreMenu,
                onMoreMenuToggle = { showMoreMenu = it },
                showQuickSosDialog = showQuickSosDialog,
                onSosDialogToggle = { showQuickSosDialog = it },
                viewModel = viewModel,
                unreadNotifCount = unreadNotifCount,
                currentUser = currentUser,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentScreen: ScreenNav,
    onScreenChange: (ScreenNav) -> Unit,
    showMoreMenu: Boolean,
    onMoreMenuToggle: (Boolean) -> Unit,
    showQuickSosDialog: Boolean,
    onSosDialogToggle: (Boolean) -> Unit,
    viewModel: MedCareViewModel,
    unreadNotifCount: Int,
    currentUser: com.example.data.local.entity.UserEntity?,
    snackbarHostState: SnackbarHostState
) {
    if (showQuickSosDialog) {
        SosEmergencyDialog(
            onConfirm = {
                onSosDialogToggle(false)
                viewModel.triggerSosEmergency()
            },
            onDismiss = { onSosDialogToggle(false) }
        )
    }

    Scaffold(
        containerColor = ElegantDarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(ElegantDarkBackground)) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = ElegantDarkBackground,
                        titleContentColor = ElegantTextPrimary
                    ),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ElegantPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("M", color = ElegantOnPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "MedCare",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = ElegantTextPrimary
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { onScreenChange(ScreenNav.PROFILE) },
                            modifier = Modifier.size(56.dp).testTag("top_profile_btn")
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(28.dp),
                                tint = if (currentScreen == ScreenNav.PROFILE) ElegantPurple else ElegantTextSecondary
                            )
                        }
                    },
                    actions = {
                        // Notifications
                        IconButton(
                            onClick = { onScreenChange(ScreenNav.NOTIFICATIONS) },
                            modifier = Modifier.size(56.dp).testTag("top_notifications_btn")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifCount > 0) {
                                        Badge(containerColor = ElegantRed, contentColor = Color.White) {
                                            Text("$unreadNotifCount", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(28.dp),
                                    tint = ElegantTextPrimary
                                )
                            }
                        }

                        // Emergency SOS Top Button
                        IconButton(
                            onClick = { onSosDialogToggle(true) },
                            modifier = Modifier.size(56.dp).testTag("top_sos_btn")
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Emergency SOS",
                                modifier = Modifier.size(28.dp),
                                tint = ElegantEmergencyRed
                            )
                        }
                    }
                )
                HorizontalDivider(color = ElegantDarkOutline.copy(alpha = 0.4f), thickness = 1.dp)
            }
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth().background(ElegantDarkSurface)) {
                HorizontalDivider(color = ElegantDarkOutline, thickness = 1.dp)
                NavigationBar(
                    containerColor = ElegantDarkSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(80.dp) // Taller for elderly users
                ) {
                    // 1. Dashboard / Today
                    NavigationBarItem(
                        selected = currentScreen == ScreenNav.DASHBOARD,
                        onClick = { onScreenChange(ScreenNav.DASHBOARD) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Today", modifier = Modifier.size(26.dp)) },
                        label = { Text("Today", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantOnPrimary,
                            selectedTextColor = ElegantPurple,
                            indicatorColor = ElegantPurple,
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    // 2. Medications
                    NavigationBarItem(
                        selected = currentScreen == ScreenNav.MEDICATIONS,
                        onClick = { onScreenChange(ScreenNav.MEDICATIONS) },
                        icon = { Icon(Icons.Default.LocalPharmacy, contentDescription = "Medicines", modifier = Modifier.size(26.dp)) },
                        label = { Text("Meds", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantOnPrimary,
                            selectedTextColor = ElegantPurple,
                            indicatorColor = ElegantPurple,
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    // 3. Adherence & Analytics
                    NavigationBarItem(
                        selected = currentScreen == ScreenNav.ADHERENCE,
                        onClick = { onScreenChange(ScreenNav.ADHERENCE) },
                        icon = { Icon(Icons.Default.Insights, contentDescription = "Adherence", modifier = Modifier.size(26.dp)) },
                        label = { Text("Adherence", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantOnPrimary,
                            selectedTextColor = ElegantPurple,
                            indicatorColor = ElegantPurple,
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    // 4. Gemini AI
                    NavigationBarItem(
                        selected = currentScreen == ScreenNav.AI_ASSISTANT,
                        onClick = { onScreenChange(ScreenNav.AI_ASSISTANT) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Health", modifier = Modifier.size(26.dp)) },
                        label = { Text("AI Health", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantOnPrimary,
                            selectedTextColor = ElegantPurple,
                            indicatorColor = ElegantPurple,
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )

                    // 5. More menu
                    NavigationBarItem(
                        selected = currentScreen in listOf(
                            ScreenNav.CARE_NETWORK,
                            ScreenNav.HISTORY,
                            ScreenNav.PRESCRIPTIONS,
                            ScreenNav.APPOINTMENTS,
                            ScreenNav.STOCK_REFILLS,
                            ScreenNav.SUSTAINABILITY,
                            ScreenNav.SOS_EMERGENCY
                        ),
                        onClick = { onMoreMenuToggle(true) },
                        icon = {
                            Box {
                                Icon(Icons.Default.MoreHoriz, contentDescription = "More", modifier = Modifier.size(26.dp))
                                DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { onMoreMenuToggle(false) },
                                    modifier = Modifier.background(ElegantDarkSurfaceElevated).width(240.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Scan Medicine Package", color = ElegantTeal, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                        leadingIcon = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ElegantTeal, modifier = Modifier.size(24.dp)) },
                                        onClick = {
                                            onScreenChange(ScreenNav.MEDICINE_SCANNER)
                                            onMoreMenuToggle(false)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Dose History Log", color = ElegantTextPrimary, fontSize = 16.sp) },
                                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = ElegantPurple, modifier = Modifier.size(24.dp)) },
                                        onClick = {
                                            onScreenChange(ScreenNav.HISTORY)
                                            onMoreMenuToggle(false)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Stock Manager", color = ElegantTextPrimary, fontSize = 16.sp) },
                                        leadingIcon = { Icon(Icons.Default.LocalPharmacy, contentDescription = null, tint = ElegantTeal, modifier = Modifier.size(24.dp)) },
                                        onClick = {
                                            onScreenChange(ScreenNav.STOCK_REFILLS)
                                            onMoreMenuToggle(false)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Caregiver Network", color = ElegantTextPrimary, fontSize = 16.sp) },
                                        leadingIcon = { Icon(Icons.Default.People, contentDescription = null, tint = ElegantGreen, modifier = Modifier.size(24.dp)) },
                                        onClick = {
                                            onScreenChange(ScreenNav.CARE_NETWORK)
                                            onMoreMenuToggle(false)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("SOS Emergency Center", color = ElegantRed, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                                        leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = ElegantRed, modifier = Modifier.size(24.dp)) },
                                        onClick = {
                                            onScreenChange(ScreenNav.SOS_EMERGENCY)
                                            onMoreMenuToggle(false)
                                        }
                                    )
                                }
                            }
                        },
                        label = { Text("More", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElegantOnPrimary,
                            selectedTextColor = ElegantPurple,
                            indicatorColor = ElegantPurple,
                            unselectedIconColor = ElegantTextSecondary,
                            unselectedTextColor = ElegantTextSecondary
                        )
                    )
                }
            }
        }
    )
{ innerPadding ->
        val currentScreenRef = currentScreen
        when (currentScreenRef) {
            ScreenNav.DASHBOARD -> PatientDashboardScreen(
                viewModel = viewModel,
                onNavigateToMedications = { onScreenChange(ScreenNav.MEDICATIONS) },
                onNavigateToHistory = { onScreenChange(ScreenNav.HISTORY) },
                onNavigateToAdherence = { onScreenChange(ScreenNav.ADHERENCE) },
                onNavigateToAppointments = { onScreenChange(ScreenNav.APPOINTMENTS) },
                onNavigateToAi = { onScreenChange(ScreenNav.AI_ASSISTANT) },
                onNavigateToSos = { onScreenChange(ScreenNav.SOS_EMERGENCY) },
                onNavigateToCaregiver = { onScreenChange(ScreenNav.CARE_NETWORK) },
                onNavigateToProfile = { onScreenChange(ScreenNav.PROFILE) },
                onNavigateToScanner = { onScreenChange(ScreenNav.MEDICINE_SCANNER) },
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.MEDICATIONS -> MedicationListScreen(
                viewModel = viewModel,
                onNavigateToScanner = { onScreenChange(ScreenNav.MEDICINE_SCANNER) },
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.MEDICINE_SCANNER -> MedicineScannerScreen(
                viewModel = viewModel,
                onNavigateBack = { onScreenChange(ScreenNav.MEDICATIONS) },
                onNavigateToManualAdd = { onScreenChange(ScreenNav.MEDICATIONS) },
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.ADHERENCE -> AdherenceAnalyticsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.AI_ASSISTANT -> com.example.ui.screens.ai.GeminiAiAssistantScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.CARE_NETWORK -> CaregiverManagementScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.HISTORY -> MedicationHistoryScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.PRESCRIPTIONS -> PrescriptionManagementScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.APPOINTMENTS -> AppointmentManagementScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.STOCK_REFILLS -> StockRefillScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.SUSTAINABILITY -> SustainabilityImpactScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.NOTIFICATIONS -> NotificationsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.SOS_EMERGENCY -> SosEmergencyScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.PROFILE -> ProfileSettingsScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            ScreenNav.HELP -> HelpSystemScreen()
        }
    }
}
