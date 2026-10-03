package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Elegant Dark Design Theme Colors
// ==========================================

// Core Backgrounds & Surfaces (M3 Dark Palette)
val ElegantDarkBackground = Color(0xFF1C1B1F)         // Base canvas background
val ElegantDarkSurface = Color(0xFF2B2930)            // Card & container surface
val ElegantDarkSurfaceVariant = Color(0xFF313033)     // Secondary / elevated tile surface
val ElegantDarkSurfaceElevated = Color(0xFF38343D)    // Popups, modals, dropdowns
val ElegantDarkOutline = Color(0xFF4A4458)             // Card borders & dividers
val ElegantDarkOutlineVariant = Color(0xFF3F3B48)      // Subtle separators

// Typography & Text
val ElegantTextPrimary = Color(0xFFE6E1E5)             // High emphasis text
val ElegantTextSecondary = Color(0xFFCAC4D0)           // Medium emphasis text
val ElegantTextMuted = Color(0xFF938F99)               // Low emphasis / timestamps / captions

// Primary Purple / Lilac Highlights (From Design HTML #D0BCFF / #4F378B)
val ElegantPurple = Color(0xFFD0BCFF)                  // Vibrant Lilac / Primary Accent
val ElegantOnPrimary = Color(0xFF381E72)               // Deep Purple text on primary
val ElegantPurpleContainer = Color(0xFF4F378B)         // Hero card container
val ElegantOnPurpleContainer = Color(0xFFEADDFF)       // Soft lavender text on hero container
val ElegantPurpleLight = Color(0xFFE8DEF8)             // Subtle indicator tint
val ElegantPurpleDark = Color(0xFF211047)

// Status: Success / High Adherence / Taken (From Design HTML #B4E197)
val ElegantGreen = Color(0xFFB4E197)                   // Soft Pastel Lime / Green
val ElegantOnGreen = Color(0xFF213611)
val ElegantGreenContainer = Color(0xFF294619)          // Dark Green Badge BG
val ElegantOnGreenContainer = Color(0xFFD3F8BC)        // Dark Green Badge Text

// Status: Warning / Skipped / Low Stock / Refills
val ElegantAmber = Color(0xFFFFD8A8)                   // Warm Amber / Peach
val ElegantOnAmber = Color(0xFF462A00)
val ElegantAmberContainer = Color(0xFF48330A)          // Dark Amber Badge BG
val ElegantOnAmberContainer = Color(0xFFFFE8C8)

// Status: Danger / Missed / SOS / Expiry (From Design HTML #F2B8B5)
val ElegantRed = Color(0xFFF2B8B5)                     // Soft Red Accent
val ElegantOnRed = Color(0xFF601410)
val ElegantRedContainer = Color(0xFF5A1D1A)            // Dark Red Badge BG
val ElegantOnRedContainer = Color(0xFFFCE8E6)
val ElegantEmergencyRed = Color(0xFFCF2C27)            // SOS Button Highlight

// Secondary Accents: Blue & Teal
val ElegantBlue = Color(0xFFA5D8FF)                    // Light Sky Blue
val ElegantOnBlue = Color(0xFF003355)
val ElegantBlueContainer = Color(0xFF143B5C)
val ElegantOnBlueContainer = Color(0xFFCEEAFF)

val ElegantTeal = Color(0xFF80DEEA)                    // Soft Cyan / Teal
val ElegantOnTeal = Color(0xFF00373A)
val ElegantTealContainer = Color(0xFF123D40)
val ElegantOnTealContainer = Color(0xFFBCEFF5)

// ==========================================
// Semantic MedCare Colors Mapped to Elegant Dark
// ==========================================
val MedCareNavy = ElegantPurpleContainer
val MedCareBlue = ElegantPurple
val MedCareLightBlue = ElegantOnPurpleContainer
val MedCareDarkBlue = ElegantOnPrimary

val MedCareTeal = ElegantTeal
val MedCareGreen = ElegantGreen
val MedCareLightGreen = ElegantGreenContainer
val MedCareAmber = ElegantAmber
val MedCareLightAmber = ElegantAmberContainer
val MedCareRed = ElegantRed
val MedCareLightRed = ElegantRedContainer

val MedCareSurfaceLight = ElegantDarkSurface
val MedCareBackgroundLight = ElegantDarkBackground
val MedCareTextPrimary = ElegantTextPrimary
val MedCareTextSecondary = ElegantTextSecondary
val MedCareBorder = ElegantDarkOutline

val MedCareDarkBackground = ElegantDarkBackground
val MedCareDarkSurface = ElegantDarkSurface
val MedCareDarkSurfaceVariant = ElegantDarkSurfaceVariant
val MedCareDarkTextPrimary = ElegantTextPrimary
val MedCareDarkTextSecondary = ElegantTextSecondary
