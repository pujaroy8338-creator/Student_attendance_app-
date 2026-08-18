import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  FlatList,
  StyleSheet,
  ScrollView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { StorageService, getTodayDateString } from '../services/storage';
import DatePickerModal from '../components/DatePickerModal';

export default function HistoryScreen({ navigation, colors, onNavigateToTab }) {
  const todayStr = getTodayDateString();
  const [historyDate, setHistoryDate] = useState(todayStr);
  const [historyClass, setHistoryClass] = useState('Class 10');
  const [historySection, setHistorySection] = useState('A');
  const [students, setStudents] = useState([]);
  const [attendanceRecords, setAttendanceRecords] = useState([]);
  const [settings, setSettings] = useState({});
  const [datePickerVisible, setDatePickerVisible] = useState(false);

  const loadHistory = async (date = historyDate) => {
    const stList = await StorageService.getStudents();
    const attList = await StorageService.getAttendanceForDate(date);
    const sett = await StorageService.getSettings();
    setStudents(stList);
    setAttendanceRecords(attList);
    setSettings(sett);
  };

  useEffect(() => {
    loadHistory();
    const unsubscribe = navigation.addListener('focus', () => {
      loadHistory();
    });
    return unsubscribe;
  }, [navigation]);

  const handleDateChange = (newDate) => {
    setHistoryDate(newDate);
    loadHistory(newDate);
  };

  const handlePrevDay = () => {
    const [y, m, d] = historyDate.split('-');
    const dt = new Date(parseInt(y), parseInt(m) - 1, parseInt(d));
    dt.setDate(dt.getDate() - 1);
    const prevDateStr = `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
    handleDateChange(prevDateStr);
  };

  const handleNextDay = () => {
    const [y, m, d] = historyDate.split('-');
    const dt = new Date(parseInt(y), parseInt(m) - 1, parseInt(d));
    dt.setDate(dt.getDate() + 1);
    const nextDateStr = `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}-${String(dt.getDate()).padStart(2, '0')}`;
    if (settings.allowFutureDates || nextDateStr <= todayStr) {
      handleDateChange(nextDateStr);
    }
  };

  const availableClasses = [...new Set(students.map(s => s.studentClass).filter(Boolean))];
  if (availableClasses.length === 0) availableClasses.push('Class 10');

  const availableSections = [...new Set(students.map(s => s.section).filter(Boolean))];
  if (availableSections.length === 0) availableSections.push('A');

  const filteredLogs = attendanceRecords.filter(
    a => (a.class === historyClass || a.studentClass === historyClass) && a.section === historySection
  );

  const studentsMap = students.reduce((acc, st) => {
    acc[st.id] = st;
    return acc;
  }, {});

  const presentCount = filteredLogs.filter(a => a.status === 'PRESENT').length;
  const absentCount = filteredLogs.filter(a => a.status === 'ABSENT').length;
  const leaveCount = filteredLogs.filter(a => a.status === 'LEAVE').length;
  const totalCount = filteredLogs.length;
  const percentage = totalCount > 0 ? ((presentCount / totalCount) * 100).toFixed(1) : 0;

  // Format readable display date
  let displayDateStr = historyDate;
  try {
    const [y, m, d] = historyDate.split('-');
    const dt = new Date(parseInt(y), parseInt(m) - 1, parseInt(d));
    displayDateStr = dt.toLocaleDateString('en-GB', {
      weekday: 'short',
      day: 'numeric',
      month: 'short',
      year: 'numeric'
    });
  } catch (e) {}

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Date Navigation Banner */}
      <View style={[styles.navCard, { backgroundColor: colors.surface }]}>
        <TouchableOpacity onPress={handlePrevDay} style={styles.arrowBtn}>
          <Ionicons name="chevron-back" size={22} color={colors.primary} />
        </TouchableOpacity>

        <TouchableOpacity onPress={() => setDatePickerVisible(true)} style={styles.dateCenter}>
          <Text style={[styles.navDateText, { color: colors.text }]}>{displayDateStr}</Text>
          <Text style={[styles.tapHint, { color: colors.primary }]}>📅 Tap to pick date</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={handleNextDay}
          disabled={!settings.allowFutureDates && historyDate >= todayStr}
          style={[styles.arrowBtn, !settings.allowFutureDates && historyDate >= todayStr && { opacity: 0.3 }]}
        >
          <Ionicons name="chevron-forward" size={22} color={colors.primary} />
        </TouchableOpacity>
      </View>

      {/* Class & Section Filter */}
      <View style={styles.filterRow}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          {availableClasses.map(cls => (
            <TouchableOpacity
              key={cls}
              onPress={() => setHistoryClass(cls)}
              style={[
                styles.chip,
                { backgroundColor: historyClass === cls ? colors.primary : colors.surface },
              ]}
            >
              <Text style={{ color: historyClass === cls ? '#FFF' : colors.text, fontSize: 12, fontWeight: '600' }}>
                {cls}
              </Text>
            </TouchableOpacity>
          ))}
          {availableSections.map(sec => (
            <TouchableOpacity
              key={sec}
              onPress={() => setHistorySection(sec)}
              style={[
                styles.chip,
                { backgroundColor: historySection === sec ? colors.secondary : colors.surfaceVariant },
              ]}
            >
              <Text style={{ color: historySection === sec ? '#FFF' : colors.textSecondary, fontSize: 12, fontWeight: '600' }}>
                Sec {sec}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* Day Overview Stat Card */}
      <View style={[styles.summaryCard, { backgroundColor: colors.surface }]}>
        <View style={styles.summaryTop}>
          <Text style={[styles.summaryTitle, { color: colors.text }]}>
            {historyClass} - Sec {historySection}
          </Text>
          {totalCount > 0 && (
            <Text style={[styles.pctText, { color: colors.primary }]}>{percentage}% Attendance</Text>
          )}
        </View>

        <View style={styles.statsRow}>
          <View style={styles.miniStat}>
            <Text style={[styles.statNum, { color: colors.present }]}>{presentCount}</Text>
            <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Present</Text>
          </View>
          <View style={styles.miniStat}>
            <Text style={[styles.statNum, { color: colors.absent }]}>{absentCount}</Text>
            <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Absent</Text>
          </View>
          <View style={styles.miniStat}>
            <Text style={[styles.statNum, { color: colors.leave }]}>{leaveCount}</Text>
            <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Leave</Text>
          </View>
          <View style={styles.miniStat}>
            <Text style={[styles.statNum, { color: colors.text }]}>{totalCount}</Text>
            <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Total</Text>
          </View>
        </View>

        {totalCount > 0 ? (
          <TouchableOpacity
            onPress={() => onNavigateToTab('Attendance', { date: historyDate })}
            style={[styles.editDateBtn, { backgroundColor: colors.primaryContainer }]}
          >
            <Ionicons name="pencil" size={15} color={colors.primary} />
            <Text style={[styles.editDateBtnText, { color: colors.primary }]}>
              Edit Attendance For This Date
            </Text>
          </TouchableOpacity>
        ) : null}
      </View>

      {/* Record list */}
      <FlatList
        data={filteredLogs}
        keyExtractor={item => item.id}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 30 }}
        renderItem={({ item }) => {
          const st = studentsMap[item.studentId];
          const isPresent = item.status === 'PRESENT';
          const isAbsent = item.status === 'ABSENT';

          return (
            <View style={[styles.logCard, { backgroundColor: colors.surface }]}>
              <View style={[styles.avatar, { backgroundColor: st?.avatarColor || colors.primary }]}>
                <Text style={styles.avatarText}>{st?.name ? st.name[0].toUpperCase() : 'S'}</Text>
              </View>

              <View style={styles.info}>
                <Text style={[styles.stName, { color: colors.text }]}>{st?.name || 'Unknown'}</Text>
                <Text style={[styles.stMeta, { color: colors.textSecondary }]}>
                  Roll: {st?.rollNumber || '-'}  •  Adm: {st?.admissionNumber || '-'}
                </Text>
              </View>

              <View style={[
                styles.badge,
                { backgroundColor: isPresent ? colors.presentBg : isAbsent ? colors.absentBg : colors.leaveBg }
              ]}>
                <Text style={{
                  color: isPresent ? colors.present : isAbsent ? colors.absent : colors.leave,
                  fontWeight: 'bold',
                  fontSize: 12
                }}>
                  {item.status}
                </Text>
              </View>
            </View>
          );
        }}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Ionicons name="time-outline" size={48} color={colors.textMuted} />
            <Text style={[styles.emptyTitle, { color: colors.text }]}>No Attendance Logged</Text>
            <Text style={[styles.emptySub, { color: colors.textSecondary }]}>
              No attendance records found for {historyDate} in this class.
            </Text>
            <TouchableOpacity
              onPress={() => onNavigateToTab('Attendance', { date: historyDate })}
              style={[styles.markNowBtn, { backgroundColor: colors.primary }]}
            >
              <Text style={styles.markNowBtnText}>Mark Attendance Now</Text>
            </TouchableOpacity>
          </View>
        }
      />

      <DatePickerModal
        visible={datePickerVisible}
        currentDate={historyDate}
        allowFutureDates={settings.allowFutureDates}
        colors={colors}
        onSelectDate={handleDateChange}
        onClose={() => setDatePickerVisible(false)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  navCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    margin: 16,
    borderRadius: 16,
    padding: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  arrowBtn: {
    padding: 8,
  },
  dateCenter: {
    alignItems: 'center',
  },
  navDateText: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  tapHint: {
    fontSize: 11,
    fontWeight: '600',
    marginTop: 2,
  },
  filterRow: {
    paddingHorizontal: 16,
    marginBottom: 8,
  },
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 12,
    marginRight: 6,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  summaryCard: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 14,
    padding: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 3,
    elevation: 1,
  },
  summaryTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 10,
  },
  summaryTitle: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  pctText: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  miniStat: {
    alignItems: 'center',
  },
  statNum: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  statLbl: {
    fontSize: 11,
    marginTop: 2,
  },
  editDateBtn: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 8,
    borderRadius: 10,
    marginTop: 12,
    gap: 6,
  },
  editDateBtnText: {
    fontWeight: 'bold',
    fontSize: 13,
  },
  logCard: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 12,
    padding: 10,
    marginBottom: 8,
  },
  avatar: {
    width: 38,
    height: 38,
    borderRadius: 19,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 10,
  },
  avatarText: {
    color: '#FFF',
    fontWeight: 'bold',
    fontSize: 15,
  },
  info: {
    flex: 1,
  },
  stName: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  stMeta: {
    fontSize: 11,
    marginTop: 1,
  },
  badge: {
    paddingHorizontal: 10,
    paddingVertical: 4,
    borderRadius: 8,
  },
  emptyState: {
    alignItems: 'center',
    marginTop: 40,
    paddingHorizontal: 30,
  },
  emptyTitle: {
    fontSize: 15,
    fontWeight: 'bold',
    marginTop: 10,
  },
  emptySub: {
    fontSize: 12,
    textAlign: 'center',
    marginTop: 4,
    marginBottom: 16,
  },
  markNowBtn: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 12,
  },
  markNowBtnText: {
    color: '#FFF',
    fontWeight: 'bold',
    fontSize: 13,
  },
});
