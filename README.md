# 📱 Smart Attendance - Android Attendance Management App

A complete, production-ready, offline-first Attendance Management Android application built with **Expo React Native** and **AsyncStorage**. Designed for schools, colleges, coaching centers, and educational institutes.

---

## ✨ Features

### 1. 🏠 Home Dashboard
- **Live Summary**: Total Students, Present Today, Absent Today, On Leave Today, and Real-time Attendance Percentage.
- **Academic Header**: Displays Institute Name, Academic Session, and today's formatted date.
- **Quick Action Grid**: 1-tap navigation to Mark Attendance, Back Date Attendance, Student Directory, History, Reports, and Settings.

### 2. 👥 Student Management
- **Complete Profiles**: Roll Number, Full Name, Father's Name, Mother's Name, Class, Section, Mobile Number (with 1-tap dialer), Date of Birth, Admission Number, Address, and Status (Active / Inactive).
- **CRUD Operations**: Add new students, Edit existing profiles (with all previously saved fields pre-populated), and Delete students.
- **Search & Filtering**: Search in real-time by name, roll number, or admission number. Filter by Class and Section.
- **Sorting**: Instant sorting by Roll Number or Alphabetical Name.
- **Conflict Prevention**: Validates duplicate Roll Numbers within the same class and section.

### 3. 📋 Attendance System (with Back Date Support)
- **📅 Back Date Attendance Picker**: Select any previous calendar date to mark or update past attendance.
- **Quick Date Selector**: Instant toggles for "Today", "Yesterday", and "📅 Pick Date".
- **Class & Section Switcher**: Fast chip selectors to navigate between grades and sections.
- **3-State Segmented Control**: `[P] Present` (Green), `[A] Absent` (Red), `[L] Leave` (Yellow).
- **Batch Actions**: "Mark All Present", "Mark All Absent", "Mark All Leave" with a single tap.
- **Duplicate Prevention**: Composite key logic prevents duplicate logs for the same student on the same date.
- **Auto-Load Existing Logs**: Automatically loads previously saved attendance records for the selected date.

### 4. 🕒 Attendance History
- **Date Archive**: Browse historical records date-by-date with Next/Previous navigation and calendar picker.
- **Status Badges & Stats**: Full summary of Present, Absent, and Leave count for that day.
- **Direct Edit Mode**: "Edit Attendance For This Date" button takes you directly into the editing view with pre-loaded values.

### 5. 📊 Reports & Analytics
- **Monthly Attendance Report**: Class/Section-wise table showing Total Working Days, Present Days, Absent Days, Leave Days, and Attendance Percentage Progress Bar for every student.
- **Daily Attendance Report**: School-wide performance rate and class-by-class comparison.
- **Student-Wise Report**: In-depth individual profile report with attendance percentage and full chronological history logs.
- **Class-Wise Overview**: Side-by-side comparison across all classes in the institute.

### 6. ⚙️ Settings, Preferences & Backup
- **Institute Details**: Customize School Name, Academic Session, Default Class, and Section.
- **Theme Modes**: System Default, Light Mode, and Dark Mode.
- **Future Dates Lock**: Safety toggle to permit or forbid marking upcoming calendar dates.
- **JSON Backup & Export**: 1-click JSON backup generation to copy/save.
- **JSON Restore**: Import and restore your database from backup text.
- **Demo Data Reset**: Instant reset button to populate sample student records.

---

## 📂 Project Structure

```
SmartAttendance/
├── App.js                         # Root application entry point & Theme provider
├── app.json                       # Expo configuration & Android package details
├── eas.json                       # EAS Build profiles (APK & AAB)
├── package.json                   # Dependencies and scripts
├── README.md                      # Project documentation
└── src/
    ├── constants/
    │   └── theme.js               # Light & Dark color tokens & design styles
    ├── services/
    │   └── storage.js             # Local persistence (AsyncStorage) & Backup logic
    ├── navigation/
    │   └── BottomTabNavigator.js  # 5-tab bottom navigation with vector icons
    ├── components/
    │   ├── DatePickerModal.js     # Custom calendar picker for past/back dates
    │   ├── StudentFormModal.js    # Add / Edit student modal form
    │   ├── StudentProfileModal.js # Comprehensive student profile & logs
    │   └── ConfirmationModal.js   # Delete & reset confirmation dialogs
    └── screens/
        ├── DashboardScreen.js     # Home dashboard & statistics
        ├── StudentsScreen.js      # Student directory & management
        ├── AttendanceScreen.js    # Mark today & back-date attendance
        ├── HistoryScreen.js       # Daily archive and edit view
        ├── ReportsScreen.js       # Monthly, Daily, Student-wise, Class-wise reports
        └── SettingsScreen.js      # Configuration, themes, and backup/restore
```

---

## 🚀 Getting Started

### 1. Installation
Clone the repository and install dependencies:
```bash
npm install
```

### 2. Run Locally with Expo
Start the development server:
```bash
npm start
```
- Press `a` in the terminal to run on Android Emulator / Device.
- Scan the QR code using the **Expo Go** app on your phone.

---

## 📦 Building Android APK & AAB (Expo EAS Build)

You can build an Android APK or Google Play Store App Bundle (AAB) in the cloud **without needing Android Studio installed**.

### Prerequisites
Install EAS CLI globally and log in:
```bash
npm install -g eas-cli
eas login
```

### 1. Build Standalone Android APK (for direct install / distribution)
```bash
eas build -p android --profile preview
```
*This uses the `preview` profile defined in `eas.json` with `"buildType": "apk"`.*

### 2. Build Android App Bundle (AAB for Google Play Store)
```bash
eas build -p android --profile production
```
*This uses the `production` profile defined in `eas.json` with `"buildType": "app-bundle"`.*

---

## 🔒 Privacy & Offline Storage
All student information, phone numbers, and attendance records are stored 100% locally on the device using SQLite/AsyncStorage. No internet connection is required to take attendance or generate reports.
