import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  RefreshControl,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { StorageService, getTodayDateString } from '../services/storage';

export default function DashboardScreen({ navigation, colors, onNavigateToTab }) {
  const todayStr = getTodayDateString();
  const [students, setStudents] = useState([]);
  const [todayAttendance, setTodayAttendance] = useState([]);
  const [settings, setSettings] = useState({});
  const [refreshing, setRefreshing] = useState(false);

  const loadData = async () => {
    const stList = await StorageService.getStudents();
    const attList = await StorageService.getAttendanceForDate(todayStr);
    const sett = await StorageService.getSettings();
    setStudents(stList);
    setTodayAttendance(attList);
    setSettings(sett);
  };

  useEffect(() => {
    loadData();
    const unsubscribe = navigation.addListener('focus', () => {
      loadData();
    });
    return unsubscribe;
  }, [navigation]);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadData();
    setRefreshing(false);
  };

  const totalStudents = students.filter(s => s.status !== 'Inactive').length;
  const presentCount = todayAttendance.filter(a => a.status === 'PRESENT').length;
  const absentCount = todayAttendance.filter(a => a.status === 'ABSENT').length;
  const leaveCount = todayAttendance.filter(a => a.status === 'LEAVE').length;
  const markedCount = todayAttendance.length;

  const attendancePercentage = totalStudents > 0 && markedCount > 0
    ? ((presentCount / totalStudents) * 100).toFixed(1)
    : 0;

  // Format readable today's date
  const todayFormatted = new Date().toLocaleDateString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  });

  return (
    <ScrollView
      style={[styles.container, { backgroundColor: colors.background }]}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
    >
      {/* Top Banner with School & Date */}
      <View style={[styles.banner, { backgroundColor: colors.primary }]}>
        <View style={styles.bannerTop}>
          <View>
            <Text style={styles.schoolName}>{settings.schoolName || 'Smart Academy'}</Text>
            <Text style={styles.sessionText}>Academic Session: {settings.academicYear || '2026-2027'}</Text>
          </View>
          <View style={styles.dateBadge}>
            <Ionicons name="calendar-outline" size={14} color="#FFF" />
            <Text style={styles.dateBadgeText}>{todayFormatted}</Text>
          </View>
        </View>

        {/* Big Percentage Card */}
        <View style={styles.attendanceProgressContainer}>
          <View>
            <Text style={styles.progressSub}>Today's Attendance</Text>
            <Text style={styles.progressValue}>{attendancePercentage}%</Text>
            <Text style={styles.progressRatio}>
              {presentCount} Present / {totalStudents} Students
            </Text>
          </View>
          <View style={styles.statusPillsColumn}>
            <View style={[styles.smallPill, { backgroundColor: '#22C55E30' }]}>
              <Text style={{ color: '#86EFAC', fontWeight: 'bold', fontSize: 12 }}>✓ {presentCount} Present</Text>
            </View>
            <View style={[styles.smallPill, { backgroundColor: '#EF444430' }]}>
              <Text style={{ color: '#FCA5A5', fontWeight: 'bold', fontSize: 12 }}>✕ {absentCount} Absent</Text>
            </View>
            <View style={[styles.smallPill, { backgroundColor: '#F59E0B30' }]}>
              <Text style={{ color: '#FDE68A', fontWeight: 'bold', fontSize: 12 }}>🕒 {leaveCount} Leave</Text>
            </View>
          </View>
        </View>
      </View>

      {/* 4 Stat Cards Grid */}
      <View style={styles.statsGrid}>
        <View style={[styles.statCard, { backgroundColor: colors.surface }]}>
          <View style={[styles.iconWrap, { backgroundColor: colors.primaryContainer }]}>
            <Ionicons name="people" size={20} color={colors.primary} />
          </View>
          <Text style={[styles.statNum, { color: colors.text }]}>{totalStudents}</Text>
          <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Total Students</Text>
        </View>

        <View style={[styles.statCard, { backgroundColor: colors.surface }]}>
          <View style={[styles.iconWrap, { backgroundColor: colors.presentBg }]}>
            <Ionicons name="checkmark-circle" size={20} color={colors.present} />
          </View>
          <Text style={[styles.statNum, { color: colors.present }]}>{presentCount}</Text>
          <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Present Today</Text>
        </View>

        <View style={[styles.statCard, { backgroundColor: colors.surface }]}>
          <View style={[styles.iconWrap, { backgroundColor: colors.absentBg }]}>
            <Ionicons name="close-circle" size={20} color={colors.absent} />
          </View>
          <Text style={[styles.statNum, { color: colors.absent }]}>{absentCount}</Text>
          <Text style={[styles.statLabel, { color: colors.textSecondary }]}>Absent Today</Text>
        </View>

        <View style={[styles.statCard, { backgroundColor: colors.surface }]}>
          <View style={[styles.iconWrap, { backgroundColor: colors.leaveBg }]}>
            <Ionicons name="time" size={20} color={colors.leave} />
          </View>
          <Text style={[styles.statNum, { color: colors.leave }]}>{leaveCount}</Text>
          <Text style={[styles.statLabel, { color: colors.textSecondary }]}>On Leave</Text>
        </View>
      </View>

      {/* Quick Action Navigation Buttons */}
      <Text style={[styles.sectionTitle, { color: colors.text }]}>Quick Actions</Text>
      
      <View style={styles.quickGrid}>
        <TouchableOpacity
          onPress={() => onNavigateToTab('Attendance', { date: todayStr })}
          style={[styles.quickCard, { backgroundColor: colors.surface }]}
        >
          <View style={[styles.quickIcon, { backgroundColor: colors.primaryContainer }]}>
            <Ionicons name="create" size={22} color={colors.primary} />
          </View>
          <Text style={[styles.quickTitle, { color: colors.text }]}>Mark Attendance</Text>
          <Text style={[styles.quickDesc, { color: colors.textSecondary }]}>Today's entry</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => onNavigateToTab('Attendance', { openDatePicker: true })}
          style={[styles.quickCard, { backgroundColor: colors.surface }]}
        >
          <View style={[styles.quickIcon, { backgroundColor: colors.secondaryContainer }]}>
            <Ionicons name="calendar" size={22} color={colors.secondary} />
          </View>
          <Text style={[styles.quickTitle, { color: colors.text }]}>Back Date</Text>
          <Text style={[styles.quickDesc, { color: colors.textSecondary }]}>Mark past date</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => onNavigateToTab('Students')}
          style={[styles.quickCard, { backgroundColor: colors.surface }]}
        >
          <View style={[styles.quickIcon, { backgroundColor: '#E0E7FF' }]}>
            <Ionicons name="people" size={22} color="#4338CA" />
          </View>
          <Text style={[styles.quickTitle, { color: colors.text }]}>Students</Text>
          <Text style={[styles.quickDesc, { color: colors.textSecondary }]}>Manage profiles</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => onNavigateToTab('History')}
          style={[styles.quickCard, { backgroundColor: colors.surface }]}
        >
          <View style={[styles.quickIcon, { backgroundColor: '#FCE7F3' }]}>
            <Ionicons name="time" size={22} color="#BE185D" />
          </View>
          <Text style={[styles.quickTitle, { color: colors.text }]}>History</Text>
          <Text style={[styles.quickDesc, { color: colors.textSecondary }]}>Daily log archive</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => onNavigateToTab('Reports')}
          style={[styles.quickCard, { backgroundColor: colors.surface }]}
        >
          <View style={[styles.quickIcon, { backgroundColor: '#FEF3C7' }]}>
            <Ionicons name="stats-chart" size={22} color="#B45309" />
          </View>
          <Text style={[styles.quickTitle, { color: colors.text }]}>Reports</Text>
          <Text style={[styles.quickDesc, { color: colors.textSecondary }]}>Monthly stats</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => onNavigateToTab('Settings')}
          style={[styles.quickCard, { backgroundColor: colors.surface }]}
        >
          <View style={[styles.quickIcon, { backgroundColor: '#F1F5F9' }]}>
            <Ionicons name="settings" size={22} color="#475569" />
          </View>
          <Text style={[styles.quickTitle, { color: colors.text }]}>Settings</Text>
          <Text style={[styles.quickDesc, { color: colors.textSecondary }]}>Backup & config</Text>
        </TouchableOpacity>
      </View>

      <View style={{ height: 30 }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  banner: {
    padding: 20,
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  bannerTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  schoolName: {
    color: '#FFF',
    fontSize: 20,
    fontWeight: 'bold',
  },
  sessionText: {
    color: '#BFDBFE',
    fontSize: 12,
    marginTop: 2,
  },
  dateBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'rgba(255,255,255,0.2)',
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 12,
    gap: 4,
  },
  dateBadgeText: {
    color: '#FFF',
    fontSize: 12,
    fontWeight: '600',
  },
  attendanceProgressContainer: {
    backgroundColor: 'rgba(255,255,255,0.12)',
    borderRadius: 18,
    padding: 16,
    marginTop: 16,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  progressSub: {
    color: '#BFDBFE',
    fontSize: 12,
    fontWeight: '600',
  },
  progressValue: {
    color: '#FFF',
    fontSize: 32,
    fontWeight: 'bold',
    marginVertical: 2,
  },
  progressRatio: {
    color: '#E2E8F0',
    fontSize: 12,
  },
  statusPillsColumn: {
    gap: 6,
  },
  smallPill: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 10,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    padding: 16,
    gap: 12,
  },
  statCard: {
    width: '48%',
    borderRadius: 16,
    padding: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 2,
  },
  iconWrap: {
    width: 36,
    height: 36,
    borderRadius: 10,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  statNum: {
    fontSize: 22,
    fontWeight: 'bold',
  },
  statLabel: {
    fontSize: 12,
    marginTop: 2,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginHorizontal: 16,
    marginTop: 8,
    marginBottom: 10,
  },
  quickGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    paddingHorizontal: 16,
    gap: 12,
  },
  quickCard: {
    width: '31%',
    borderRadius: 14,
    padding: 12,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  quickIcon: {
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  quickTitle: {
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  quickDesc: {
    fontSize: 10,
    marginTop: 2,
    textAlign: 'center',
  },
});
