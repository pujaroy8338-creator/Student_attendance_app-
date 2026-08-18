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

export default function ReportsScreen({ navigation, colors }) {
  const [activeTab, setActiveTab] = useState(0); // 0: Monthly, 1: Daily, 2: Student-wise, 3: Class-wise
  const [students, setStudents] = useState([]);
  const [allAttendance, setAllAttendance] = useState([]);

  // Filters
  const [selectedClass, setSelectedClass] = useState('Class 10');
  const [selectedSection, setSelectedSection] = useState('A');
  const [selectedStudentId, setSelectedStudentId] = useState(null);

  const loadData = async () => {
    const stList = await StorageService.getStudents();
    const attList = await StorageService.getAllAttendance();
    setStudents(stList);
    setAllAttendance(attList);
    if (stList.length > 0 && !selectedStudentId) {
      setSelectedStudentId(stList[0].id);
    }
  };

  useEffect(() => {
    loadData();
    const unsubscribe = navigation.addListener('focus', () => {
      loadData();
    });
    return unsubscribe;
  }, [navigation]);

  const availableClasses = [...new Set(students.map(s => s.studentClass).filter(Boolean))];
  if (availableClasses.length === 0) availableClasses.push('Class 10');

  const availableSections = [...new Set(students.map(s => s.section).filter(Boolean))];
  if (availableSections.length === 0) availableSections.push('A');

  const tabs = ['Monthly', 'Daily', 'Student-Wise', 'Class-Wise'];

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Tab Header */}
      <View style={[styles.tabBar, { backgroundColor: colors.surface }]}>
        {tabs.map((t, idx) => (
          <TouchableOpacity
            key={t}
            onPress={() => setActiveTab(idx)}
            style={[
              styles.tabItem,
              activeTab === idx && [styles.tabItemActive, { borderBottomColor: colors.primary }],
            ]}
          >
            <Text
              style={[
                styles.tabText,
                { color: activeTab === idx ? colors.primary : colors.textSecondary },
                activeTab === idx && { fontWeight: 'bold' },
              ]}
            >
              {t}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Render Active Report */}
      {activeTab === 0 && (
        <MonthlyReportView
          students={students}
          attendance={allAttendance}
          selectedClass={selectedClass}
          setSelectedClass={setSelectedClass}
          selectedSection={selectedSection}
          setSelectedSection={setSelectedSection}
          availableClasses={availableClasses}
          availableSections={availableSections}
          colors={colors}
        />
      )}

      {activeTab === 1 && (
        <DailyReportView
          students={students}
          attendance={allAttendance}
          colors={colors}
        />
      )}

      {activeTab === 2 && (
        <StudentWiseReportView
          students={students}
          attendance={allAttendance}
          selectedStudentId={selectedStudentId}
          setSelectedStudentId={setSelectedStudentId}
          colors={colors}
        />
      )}

      {activeTab === 3 && (
        <ClassWiseReportView
          students={students}
          attendance={allAttendance}
          colors={colors}
        />
      )}
    </View>
  );
}

// 1. Monthly Report
function MonthlyReportView({
  students,
  attendance,
  selectedClass,
  setSelectedClass,
  selectedSection,
  setSelectedSection,
  availableClasses,
  availableSections,
  colors,
}) {
  const classStudents = students.filter(
    s => s.studentClass === selectedClass && s.section === selectedSection
  ).sort((a, b) => (parseInt(a.rollNumber) || 999) - (parseInt(b.rollNumber) || 999));

  // Find unique working days logged for this class
  const classLogs = attendance.filter(
    a => (a.class === selectedClass || a.studentClass === selectedClass) && a.section === selectedSection
  );
  const uniqueDates = [...new Set(classLogs.map(a => a.date))];
  const totalWorkingDays = uniqueDates.length;

  return (
    <View style={{ flex: 1, padding: 16 }}>
      {/* Class & Section Filters */}
      <View style={styles.filterCard}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: 6 }}>
          {availableClasses.map(cls => (
            <TouchableOpacity
              key={cls}
              onPress={() => setSelectedClass(cls)}
              style={[
                styles.chip,
                { backgroundColor: selectedClass === cls ? colors.primary : colors.surface },
              ]}
            >
              <Text style={{ color: selectedClass === cls ? '#FFF' : colors.text, fontSize: 12, fontWeight: '600' }}>
                {cls}
              </Text>
            </TouchableOpacity>
          ))}
          {availableSections.map(sec => (
            <TouchableOpacity
              key={sec}
              onPress={() => setSelectedSection(sec)}
              style={[
                styles.chip,
                { backgroundColor: selectedSection === sec ? colors.secondary : colors.surfaceVariant },
              ]}
            >
              <Text style={{ color: selectedSection === sec ? '#FFF' : colors.textSecondary, fontSize: 12, fontWeight: '600' }}>
                Sec {sec}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
        <Text style={[styles.workingDaysInfo, { color: colors.textSecondary }]}>
          Total Working Days Logged: <Text style={{ fontWeight: 'bold', color: colors.primary }}>{totalWorkingDays}</Text>
        </Text>
      </View>

      {/* Student Monthly Summary List */}
      <FlatList
        data={classStudents}
        keyExtractor={item => item.id}
        contentContainerStyle={{ paddingBottom: 20 }}
        renderItem={({ item }) => {
          const stLogs = classLogs.filter(a => a.studentId === item.id);
          const presentDays = stLogs.filter(a => a.status === 'PRESENT').length;
          const absentDays = stLogs.filter(a => a.status === 'ABSENT').length;
          const leaveDays = stLogs.filter(a => a.status === 'LEAVE').length;
          const pct = totalWorkingDays > 0 ? ((presentDays / totalWorkingDays) * 100).toFixed(1) : 0;

          return (
            <View style={[styles.monthlyCard, { backgroundColor: colors.surface }]}>
              <View style={styles.monthlyCardTop}>
                <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                  <View style={[styles.miniAvatar, { backgroundColor: item.avatarColor || colors.primary }]}>
                    <Text style={{ color: '#FFF', fontWeight: 'bold', fontSize: 13 }}>{item.name ? item.name[0] : 'S'}</Text>
                  </View>
                  <View style={{ marginLeft: 8 }}>
                    <Text style={[styles.monthlyStudentName, { color: colors.text }]}>{item.name}</Text>
                    <Text style={[styles.monthlyRoll, { color: colors.textSecondary }]}>Roll No: {item.rollNumber}</Text>
                  </View>
                </View>

                <Text style={[styles.monthlyPct, { color: pct >= 75 ? colors.present : pct >= 50 ? colors.leave : colors.absent }]}>
                  {pct}%
                </Text>
              </View>

              {/* Progress Bar */}
              <View style={[styles.progressBarBg, { backgroundColor: colors.surfaceVariant }]}>
                <View
                  style={[
                    styles.progressBarFill,
                    {
                      width: `${Math.min(100, Math.max(0, pct))}%`,
                      backgroundColor: pct >= 75 ? colors.present : pct >= 50 ? colors.leave : colors.absent,
                    },
                  ]}
                />
              </View>

              {/* Counts */}
              <View style={styles.monthlyCountsRow}>
                <Text style={{ color: colors.present, fontSize: 12, fontWeight: 'bold' }}>✓ {presentDays} Present</Text>
                <Text style={{ color: colors.absent, fontSize: 12, fontWeight: 'bold' }}>✕ {absentDays} Absent</Text>
                <Text style={{ color: colors.leave, fontSize: 12, fontWeight: 'bold' }}>🕒 {leaveDays} Leave</Text>
              </View>
            </View>
          );
        }}
        ListEmptyComponent={
          <Text style={{ textAlign: 'center', marginTop: 30, color: colors.textMuted }}>No students found</Text>
        }
      />
    </View>
  );
}

// 2. Daily Report
function DailyReportView({ students, attendance, colors }) {
  const todayStr = getTodayDateString();
  const todayLogs = attendance.filter(a => a.date === todayStr);

  const totalStudents = students.filter(s => s.status !== 'Inactive').length;
  const presentCount = todayLogs.filter(a => a.status === 'PRESENT').length;
  const absentCount = todayLogs.filter(a => a.status === 'ABSENT').length;
  const leaveCount = todayLogs.filter(a => a.status === 'LEAVE').length;
  const pct = todayLogs.length > 0 ? ((presentCount / todayLogs.length) * 100).toFixed(1) : 0;

  // Breakdown by Class
  const classGroups = {};
  students.forEach(s => {
    const key = `${s.studentClass} - Sec ${s.section}`;
    if (!classGroups[key]) classGroups[key] = [];
    classGroups[key].push(s);
  });

  return (
    <ScrollView style={{ flex: 1, padding: 16 }}>
      {/* Overall Daily Banner */}
      <View style={[styles.dailyOverviewCard, { backgroundColor: colors.primaryContainer }]}>
        <Text style={[styles.dailyOverviewTitle, { color: colors.text }]}>Today's Overall Performance</Text>
        <Text style={[styles.dailyBigPct, { color: colors.primary }]}>{pct}%</Text>
        <View style={styles.dailyRowStats}>
          <Text style={{ color: colors.present, fontWeight: 'bold' }}>{presentCount} Present</Text>
          <Text style={{ color: colors.absent, fontWeight: 'bold' }}>{absentCount} Absent</Text>
          <Text style={{ color: colors.leave, fontWeight: 'bold' }}>{leaveCount} Leave</Text>
          <Text style={{ color: colors.text, fontWeight: 'bold' }}>{totalStudents} Enrolled</Text>
        </View>
      </View>

      <Text style={[styles.subSectionTitle, { color: colors.text }]}>Class-Wise Daily Performance</Text>

      {Object.entries(classGroups).map(([groupName, stList]) => {
        const groupLogs = todayLogs.filter(a => stList.some(s => s.id === a.studentId));
        const gPresent = groupLogs.filter(a => a.status === 'PRESENT').length;
        const gAbsent = groupLogs.filter(a => a.status === 'ABSENT').length;
        const gLeave = groupLogs.filter(a => a.status === 'LEAVE').length;
        const gPct = groupLogs.length > 0 ? ((gPresent / groupLogs.length) * 100).toFixed(1) : 0;
        const isMarked = groupLogs.length > 0;

        return (
          <View key={groupName} style={[styles.classDailyCard, { backgroundColor: colors.surface }]}>
            <View style={styles.classDailyHeader}>
              <Text style={[styles.classDailyName, { color: colors.text }]}>{groupName}</Text>
              {isMarked ? (
                <Text style={[styles.classDailyPct, { color: colors.primary }]}>{gPct}%</Text>
              ) : (
                <View style={[styles.unmarkedBadge, { backgroundColor: colors.surfaceVariant }]}>
                  <Text style={{ color: colors.textMuted, fontSize: 11, fontWeight: '600' }}>Not Marked</Text>
                </View>
              )}
            </View>

            {isMarked && (
              <View style={styles.classDailyBottom}>
                <Text style={{ color: colors.present, fontSize: 12 }}>Present: {gPresent}</Text>
                <Text style={{ color: colors.absent, fontSize: 12 }}>Absent: {gAbsent}</Text>
                <Text style={{ color: colors.leave, fontSize: 12 }}>Leave: {gLeave}</Text>
                <Text style={{ color: colors.textSecondary, fontSize: 12 }}>Total: {stList.length}</Text>
              </View>
            )}
          </View>
        );
      })}

      <View style={{ height: 20 }} />
    </ScrollView>
  );
}

// 3. Student-Wise Report
function StudentWiseReportView({ students, attendance, selectedStudentId, setSelectedStudentId, colors }) {
  const selectedStudent = students.find(s => s.id === selectedStudentId) || students[0];
  const stLogs = attendance.filter(a => selectedStudent && a.studentId === selectedStudent.id);

  const total = stLogs.length;
  const present = stLogs.filter(a => a.status === 'PRESENT').length;
  const absent = stLogs.filter(a => a.status === 'ABSENT').length;
  const leave = stLogs.filter(a => a.status === 'LEAVE').length;
  const pct = total > 0 ? ((present / total) * 100).toFixed(1) : 0;

  return (
    <View style={{ flex: 1, padding: 16 }}>
      {/* Student Picker Chips */}
      <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ maxHeight: 42, marginBottom: 12 }}>
        {students.map(st => (
          <TouchableOpacity
            key={st.id}
            onPress={() => setSelectedStudentId(st.id)}
            style={[
              styles.chip,
              { backgroundColor: selectedStudent?.id === st.id ? colors.primary : colors.surface },
            ]}
          >
            <Text style={{ color: selectedStudent?.id === st.id ? '#FFF' : colors.text, fontSize: 12, fontWeight: '600' }}>
              {st.rollNumber}. {st.name}
            </Text>
          </TouchableOpacity>
        ))}
      </ScrollView>

      {selectedStudent && (
        <>
          {/* Student Profile Overview Card */}
          <View style={[styles.studentCardHeader, { backgroundColor: colors.surface }]}>
            <View style={{ flexDirection: 'row', alignItems: 'center' }}>
              <View style={[styles.avatar, { backgroundColor: selectedStudent.avatarColor || colors.primary }]}>
                <Text style={styles.avatarText}>{selectedStudent.name ? selectedStudent.name[0] : 'S'}</Text>
              </View>
              <View style={{ marginLeft: 10, flex: 1 }}>
                <Text style={[styles.stName, { color: colors.text }]}>{selectedStudent.name}</Text>
                <Text style={[styles.stMeta, { color: colors.textSecondary }]}>
                  Roll: {selectedStudent.rollNumber}  •  {selectedStudent.studentClass} - Sec {selectedStudent.section}
                </Text>
              </View>
              <Text style={[styles.stPct, { color: colors.primary }]}>{pct}%</Text>
            </View>

            <View style={[styles.statsRow, { marginTop: 12 }]}>
              <Text style={{ color: colors.present, fontWeight: 'bold' }}>✓ {present} Present</Text>
              <Text style={{ color: colors.absent, fontWeight: 'bold' }}>✕ {absent} Absent</Text>
              <Text style={{ color: colors.leave, fontWeight: 'bold' }}>🕒 {leave} Leave</Text>
              <Text style={{ color: colors.text, fontWeight: 'bold' }}>Total: {total}</Text>
            </View>
          </View>

          <Text style={[styles.subSectionTitle, { color: colors.text }]}>Attendance History Logs ({total})</Text>

          <FlatList
            data={stLogs}
            keyExtractor={item => item.id}
            contentContainerStyle={{ paddingBottom: 20 }}
            renderItem={({ item }) => (
              <View style={[styles.logItemRow, { backgroundColor: colors.surface }]}>
                <Text style={{ color: colors.text, fontWeight: 'bold', fontSize: 13 }}>{item.date}</Text>
                <View style={[
                  styles.badge,
                  { backgroundColor: item.status === 'PRESENT' ? colors.presentBg : item.status === 'ABSENT' ? colors.absentBg : colors.leaveBg }
                ]}>
                  <Text style={{
                    color: item.status === 'PRESENT' ? colors.present : item.status === 'ABSENT' ? colors.absent : colors.leave,
                    fontWeight: 'bold',
                    fontSize: 11
                  }}>
                    {item.status}
                  </Text>
                </View>
              </View>
            )}
            ListEmptyComponent={
              <Text style={{ textAlign: 'center', marginTop: 20, color: colors.textMuted }}>No logs recorded yet</Text>
            }
          />
        </>
      )}
    </View>
  );
}

// 4. Class-Wise Comparison
function ClassWiseReportView({ students, attendance, colors }) {
  const classGroups = {};
  students.forEach(s => {
    if (!classGroups[s.studentClass]) classGroups[s.studentClass] = [];
    classGroups[s.studentClass].push(s);
  });

  return (
    <ScrollView style={{ flex: 1, padding: 16 }}>
      <Text style={[styles.subSectionTitle, { color: colors.text }]}>Class Comparison Overview</Text>

      {Object.entries(classGroups).map(([className, stList]) => {
        const classLogs = attendance.filter(a => stList.some(s => s.id === a.studentId));
        const cPresent = classLogs.filter(a => a.status === 'PRESENT').length;
        const pct = classLogs.length > 0 ? ((cPresent / classLogs.length) * 100).toFixed(1) : 0;
        const sections = [...new Set(stList.map(s => s.section))];

        return (
          <View key={className} style={[styles.classCompCard, { backgroundColor: colors.surface }]}>
            <View style={styles.classCompTop}>
              <View>
                <Text style={[styles.classCompTitle, { color: colors.text }]}>{className}</Text>
                <Text style={[styles.classCompSub, { color: colors.textSecondary }]}>
                  {stList.length} Students  •  {sections.length} Sections ({sections.join(', ')})
                </Text>
              </View>
              <Text style={[styles.classCompPct, { color: colors.primary }]}>{pct}%</Text>
            </View>

            <View style={[styles.progressBarBg, { backgroundColor: colors.surfaceVariant, marginTop: 8 }]}>
              <View
                style={[
                  styles.progressBarFill,
                  { width: `${Math.min(100, Math.max(0, pct))}%`, backgroundColor: colors.primary },
                ]}
              />
            </View>
          </View>
        );
      })}

      <View style={{ height: 20 }} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  tabBar: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
  },
  tabItem: {
    flex: 1,
    paddingVertical: 12,
    alignItems: 'center',
  },
  tabItemActive: {
    borderBottomWidth: 2,
  },
  tabText: {
    fontSize: 12,
  },
  filterCard: {
    marginBottom: 12,
  },
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 12,
    marginRight: 6,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  workingDaysInfo: {
    fontSize: 12,
    marginTop: 4,
  },
  monthlyCard: {
    borderRadius: 14,
    padding: 12,
    marginBottom: 8,
  },
  monthlyCardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  miniAvatar: {
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  monthlyStudentName: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  monthlyRoll: {
    fontSize: 11,
  },
  monthlyPct: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  progressBarBg: {
    height: 6,
    borderRadius: 3,
    marginVertical: 8,
    overflow: 'hidden',
  },
  progressBarFill: {
    height: '100%',
    borderRadius: 3,
  },
  monthlyCountsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  dailyOverviewCard: {
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
    marginBottom: 16,
  },
  dailyOverviewTitle: {
    fontSize: 14,
    fontWeight: '600',
  },
  dailyBigPct: {
    fontSize: 36,
    fontWeight: 'bold',
    marginVertical: 4,
  },
  dailyRowStats: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    marginTop: 8,
  },
  subSectionTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    marginVertical: 8,
  },
  classDailyCard: {
    borderRadius: 12,
    padding: 12,
    marginBottom: 8,
  },
  classDailyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  classDailyName: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  classDailyPct: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  unmarkedBadge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  classDailyBottom: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 8,
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
  },
  studentCardHeader: {
    borderRadius: 14,
    padding: 12,
    marginBottom: 12,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
  },
  avatarText: {
    color: '#FFF',
    fontWeight: 'bold',
    fontSize: 18,
  },
  stName: {
    fontSize: 15,
    fontWeight: 'bold',
  },
  stMeta: {
    fontSize: 12,
    marginTop: 2,
  },
  stPct: {
    fontSize: 20,
    fontWeight: 'bold',
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  logItemRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 10,
    borderRadius: 10,
    marginBottom: 6,
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
  classCompCard: {
    borderRadius: 14,
    padding: 14,
    marginBottom: 10,
  },
  classCompTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  classCompTitle: {
    fontSize: 15,
    fontWeight: 'bold',
  },
  classCompSub: {
    fontSize: 12,
    marginTop: 2,
  },
  classCompPct: {
    fontSize: 18,
    fontWeight: 'bold',
  },
});
