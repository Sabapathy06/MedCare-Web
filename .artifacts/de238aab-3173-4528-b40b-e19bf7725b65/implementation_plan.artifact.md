# MedCare Production-Ready Overhaul & Verification Plan

Transforming the MedCare prototype into a stable, elderly-friendly, functional application.

## Priority 0: Critical Stability & Core Flows (The Foundation)

### 1. Authentication & Onboarding
- **Goal**: Deterministic startup and complete user collection.
- **Fix Startup Jump**: Ensure the splash/loading state is shown until the authentication status is fully determined. Remove the 1-second Login-to-Home auto-jump.
- **User Collection**: Update `RegisterScreen` to collect **Date of Birth** and **Accessibility Preferences**.
- **Age Logic**: Implement automatic age calculation from DOB in `MedCareViewModel`. Use age to recommend "Simple Mode".
- **Account Security**: Ensure no default/QA users are automatically logged in for production builds.

### 2. Guided Medicine Add Flow
- **Goal**: Replace the complex single form with a user-friendly multi-step wizard.
- **Steps**:
    1. **Identity**: Name, Brand, Generic, Strength, Form.
    2. **Safety**: Manufacturer, Composition, Dates (MFG/EXP), Batch.
    3. **Schedule**: Frequency, Times, Food relation, Instructions.
    4. **Stock**: Current count, Alert level.
    5. **Review**: Summary and confirmation.

### 3. OCR Pipeline Overhaul
- **Goal**: Structured, reliable extraction without hallucinations.
- **Multi-Image Support**: Implement a flow to capture "Front" and "Back/Side" of packaging.
- **Field Classification**: Improve extraction of Batch, MFG Date, and Manufacturer.
- **Confidence Scoring**: Display visual indicators (🟢/🟡/🔴) for each extracted field.
- **Safety Gate**: Force manual verification for low-confidence scans.

## Priority 1: Elderly-First UX & Accessibility

### 4. Simple Mode Implementation
- **Goal**: A distraction-free UI for seniors.
- **Features**: Large buttons, high contrast, prioritization of "Today's Meds", "Scan", "Caregiver", and "SOS".
- **Toggle**: Allow switching between Standard and Simple modes in Settings/Profile.

### 5. Login Redesign & Branding
- **Goal**: Professional, trustworthy first impression.
- **Visuals**: Modern logo, slow/subtle animated background (floating pills).
- **Reduced Motion**: Option to disable animations for accessibility.

### 6. Reliable Reminders & Adherence
- **Goal**: Reminders that work even when the app is closed.
- **Scheduling**: Verify/Fix `AlarmManager` integration.
- **Actionable Notifications**: [TAKEN], [SKIP], [REMIND LATER] buttons in the notification tray.
- **Real Stats**: Adherence calculations based ONLY on confirmed actions (no mocks).

## Priority 2: Care Network, AI & Data Polish

### 7. Caregiver & SOS Escalation
- **Goal**: Real connectivity and emergency response.
- **Missed Dose Flow**: Escalation to caregiver if multiple reminders are ignored.
- **SOS Confirmation**: Prevent accidental triggers with a clear confirmation dialog.

### 8. AI Health "Real Data" Integration
- **Goal**: No hardcoded answers.
- **Context Injection**: Pass actual medication names and instructions to Gemini prompts.
- **Safety Phrases**: Ensure AI always refers to doctors for critical medical decisions.

### 9. 16 KB Page-Size & Performance
- **16 KB Compatibility**: Audit all `.so` native libraries (ML Kit, Image processing) for 16 KB page-size compatibility.
- **Rendering**: Optimize `MedicationListScreen` for 50+ items using `LazyColumn` and stable keys.

## Verification Plan

### Automated Tests
- `gradlew assembleDebug` to verify compilation.
- APK check for 16 KB compatibility.

### Manual Verification on Samsung SM-E156B
1. **End-to-End Auth**: Fresh install -> Register (DOB) -> Verify Age -> Home.
2. **Multi-Step Add**: Add medicine manually through the new 5-step wizard.
3. **OCR Stress Test**: Scan real medicine packages (Front + Back). Verify MFG/EXP extraction.
4. **Reminder Verification**: Schedule a dose for +2 mins. Close app. Verify notification and [TAKEN] action.
5. **Simple Mode**: Toggle Simple Mode and verify UI transformation.
6. **Data Persistence**: Force stop app, restart, verify all data remains.
7. **Performance**: Add 20 "Test Meds" and verify smooth scrolling.
