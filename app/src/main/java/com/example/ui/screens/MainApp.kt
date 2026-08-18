package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.AttendanceViewModel

enum class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    STUDENTS("Students", Icons.Filled.People, Icons.Outlined.People, "nav_students"),
    ATTENDANCE("Attendance", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "nav_attendance"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "nav_history"),
    REPORTS("Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment, "nav_reports"),
    SETTINGS("More", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_more")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val currentTab = NavigationItem.values()[selectedIndex]

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            NavigationItem.HOME -> "Smart Attendance"
                            NavigationItem.STUDENTS -> "Student Directory"
                            NavigationItem.ATTENDANCE -> "Mark Attendance"
                            NavigationItem.HISTORY -> "Attendance History"
                            NavigationItem.REPORTS -> "Attendance Reports"
                            NavigationItem.SETTINGS -> "Settings & More"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationItem.values().forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationItem.HOME -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAttendance = { date ->
                        if (date != null) {
                            viewModel.selectDate(date)
                        }
                        selectedIndex = NavigationItem.ATTENDANCE.ordinal
                    },
                    onNavigateToStudents = { selectedIndex = NavigationItem.STUDENTS.ordinal },
                    onNavigateToHistory = { selectedIndex = NavigationItem.HISTORY.ordinal },
                    onNavigateToReports = { selectedIndex = NavigationItem.REPORTS.ordinal }
                )
                NavigationItem.STUDENTS -> StudentsScreen(
                    viewModel = viewModel
                )
                NavigationItem.ATTENDANCE -> AttendanceScreen(
                    viewModel = viewModel
                )
                NavigationItem.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onEditAttendance = { date, sClass, sSection ->
                        viewModel.selectDate(date)
                        viewModel.selectClass(sClass)
                        viewModel.selectSection(sSection)
                        selectedIndex = NavigationItem.ATTENDANCE.ordinal
                    }
                )
                NavigationItem.REPORTS -> ReportsScreen(
                    viewModel = viewModel
                )
                NavigationItem.SETTINGS -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
