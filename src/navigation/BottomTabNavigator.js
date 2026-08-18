import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

import DashboardScreen from '../screens/DashboardScreen';
import StudentsScreen from '../screens/StudentsScreen';
import AttendanceScreen from '../screens/AttendanceScreen';
import HistoryScreen from '../screens/HistoryScreen';
import ReportsScreen from '../screens/ReportsScreen';
import SettingsScreen from '../screens/SettingsScreen';

const Tab = createBottomTabNavigator();

export default function BottomTabNavigator({ colors, currentTheme, onThemeChange }) {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName;

          if (route.name === 'Home') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Students') {
            iconName = focused ? 'people' : 'people-outline';
          } else if (route.name === 'Attendance') {
            iconName = focused ? 'checkmark-circle' : 'checkmark-circle-outline';
          } else if (route.name === 'History') {
            iconName = focused ? 'time' : 'time-outline';
          } else if (route.name === 'Reports') {
            iconName = focused ? 'stats-chart' : 'stats-chart-outline';
          } else if (route.name === 'Settings') {
            iconName = focused ? 'settings' : 'settings-outline';
          }

          return <Ionicons name={iconName} size={size - 2} color={color} />;
        },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textMuted,
        tabBarStyle: {
          backgroundColor: colors.surface,
          borderTopColor: colors.border,
          height: 60,
          paddingBottom: 8,
          paddingTop: 6,
        },
        tabBarLabelStyle: {
          fontSize: 10,
          fontWeight: '600',
        },
        headerStyle: {
          backgroundColor: colors.surface,
          shadowColor: 'transparent',
          elevation: 0,
          borderBottomWidth: 1,
          borderBottomColor: colors.border,
        },
        headerTitleStyle: {
          fontWeight: 'bold',
          fontSize: 17,
          color: colors.text,
        },
      })}
    >
      <Tab.Screen name="Home" options={{ title: 'Smart Attendance' }}>
        {(props) => (
          <DashboardScreen
            {...props}
            colors={colors}
            onNavigateToTab={(tabName, params) => props.navigation.navigate(tabName, params)}
          />
        )}
      </Tab.Screen>

      <Tab.Screen name="Students" options={{ title: 'Student Management' }}>
        {(props) => <StudentsScreen {...props} colors={colors} />}
      </Tab.Screen>

      <Tab.Screen name="Attendance" options={{ title: 'Mark Attendance' }}>
        {(props) => <AttendanceScreen {...props} colors={colors} />}
      </Tab.Screen>

      <Tab.Screen name="History" options={{ title: 'Attendance History' }}>
        {(props) => (
          <HistoryScreen
            {...props}
            colors={colors}
            onNavigateToTab={(tabName, params) => props.navigation.navigate(tabName, params)}
          />
        )}
      </Tab.Screen>

      <Tab.Screen name="Reports" options={{ title: 'Attendance Reports' }}>
        {(props) => <ReportsScreen {...props} colors={colors} />}
      </Tab.Screen>

      <Tab.Screen name="Settings" options={{ title: 'Settings & More' }}>
        {(props) => (
          <SettingsScreen
            {...props}
            colors={colors}
            currentTheme={currentTheme}
            onThemeChange={onThemeChange}
          />
        )}
      </Tab.Screen>
    </Tab.Navigator>
  );
}
