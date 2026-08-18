import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  FlatList,
  StyleSheet,
  Alert,
  ScrollView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { StorageService, getTodayDateString } from '../services/storage';
import DatePickerModal from '../components/DatePickerModal';

export default function AttendanceScreen({ navigation, route, colors }) {
  const todayStr = getTodayDateString();
  const [selectedDate, setSelectedDate] = useState(todayStr);
  const [students, setStudents] = useState([]);
  const [selectedClass, setSelectedClass] = useState('Class 10');
  const [selectedSection, setSelectedSection] = useState('A');
  const [settings, setSettings] = useState({});
  const [datePickerVisible, setDatePickerVisible] = useState(false);

  // In-memory status map: { studentId: 'PRESENT' | 'ABSENT' | 'LEAVE' }
  const [attendanceMap, setAttendanceMap] = useState({});
  const [isSavedInDb, setIsSavedInDb] = useState(false);

  // Load initial data and parameters
  const loadData = async (date = selectedDate, sClass = selectedClass, sSec = selectedSection) => {
    const stList = await StorageService.getStudents();
    const sett = await StorageService.getSettings();
    const existingLogs = await StorageService.getAttendanceForDateAndClass(date, sClass, sSec);

    setStudents(stList);
    setSettings(sett);

    // Filter students belonging to this class & section
    const classStudents = stList.filter(
      s => s.studentClass === sClass && s.section === sSec && s.status !== 'Inactive'
    );

    // Map existing attendance or default to PRESENT
    const newMap = {};
    if (existingLogs.length > 0) {
      existingLogs.forEach(log => {
        newMap[log.studentId] = log.status;
      });
      // In case any newly added students are missing in existing logs
      classStudents.forEach(st => {
        if (!newMap[st.id]) newMap[st.id] = 'PRESENT';
      });
      setIsSavedInDb(true);
    } else {
      classStudents.forEach(st => {
        newMap[st.id] = 'PRESENT';
      });
      setIsSavedInDb(false);
    }
    setAttendanceMap(newMap);
  };

  useEffect(() => {
    loadData();
    const unsubscribe = navigation.addListener('focus', () => {
      // Check if route params passed a date or openDatePicker flag
      if (route?.params?.date) {
        setSelectedDate(route.params.date);
        loadData(route.params.date, selectedClass, selectedSection);
      } else if (route?.params?.openDatePicker) {
        setDatePickerVisible(true);
      } else {
        loadData();
      }
    });
    return unsubscribe;
  }, [navigation, route?.params]);

  // Handle Date, Class, Section change
  const handleDateChange = (newDate) => {
    setSelectedDate(newDate);
    loadData(newDate, selectedClass, selectedSection);
  };

  const handleClassChange = (newClass) => {
    setSelectedClass(newClass);
    loadData(selectedDate, newClass, selectedSection);
  };

  const handleSectionChange = (newSec) => {
    setSelectedSection(newSec);
    loadData(selectedDate, selectedClass, newSec);
  };

  // Quick Marks
  const handleMarkAll = (status) => {
    const updated = { ...attendanceMap };
    classStudents.forEach(st => {
      updated[st.id] = status;
    });
    setAttendanceMap(updated);
  };

  const setStudentStatus = (studentId, status) => {
    setAttendanceMap(prev => ({ ...prev, [studentId]: status }));
  };

  // Save Attendance to AsyncStorage
  const handleSaveAttendance = async () => {
    if (classStudents.length === 0) {
      Alert.alert('Notice', 'No students in this class to save attendance for.');
      return;
    }

    const result = await StorageService.saveBatchAttendance(
      selectedDate,
      selectedClass,
      selectedSection,
      attendanceMap
    );

    if (result.success) {
      setIsSavedInDb(true);
      Alert.alert(
        'Success',
        `Attendance saved for ${result.count} students on ${selectedDate}`
      );
    } else {
      Alert.alert('Error', result.message || 'Failed to save attendance');
    }
  };

  // Extract unique classes and sections from students
  const availableClasses = [...new Set(students.map(s => s.studentClass).filter(Boolean))];
  if (availableClasses.length === 0) availableClasses.push('Class 10');

  const availableSections = [...new Set(students.map(s => s.section).filter(Boolean))];
  if (availableSections.length === 0) availableSections.push('A');

  const classStudents = students.filter(
    s => s.studentClass === selectedClass && s.section === selectedSection && s.status !== 'Inactive'
  ).sort((a, b) => (parseInt(a.rollNumber) || 999) - (parseInt(b.rollNumber) || 999));

  // Count summaries
  const presentCount = Object.values(attendanceMap).filter(v => v === 'PRESENT').length;
  const absentCount = Object.values(attendanceMap).filter(v => v === 'ABSENT').length;
  const leaveCount = Object.values(attendanceMap).filter(v => v === 'LEAVE').length;

  const isToday = selectedDate === todayStr;

  // Format readable display date
  let displayDateStr = selectedDate;
  try {
    const [y, m, d] = selectedDate.split('-');
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
      {/* Date Header Card with Back Date Button */}
      <View style={[styles.dateCard, { backgroundColor: colors.surface }]}>
        <View style={styles.dateCardTop}>
          <View>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
              <Ionicons
                name="calendar"
                size={18}
                color={isToday ? colors.primary : colors.secondary}
              />
              <Text style={[styles.dateTypeLabel, { color: isToday ? colors.primary : colors.secondary }]}>
                {isToday ? "Today's Attendance" : "Back-Date Attendance"}
              </Text>
            </View>
            <Text style={[styles.dateText, { color: colors.text }]}>{displayDateStr}</Text>
          </View>

          {/* Saved Status Badge */}
          <View style={[styles.savedBadge, { backgroundColor: isSavedInDb ? colors.presentBg : colors.absentBg }]}>
            <Text style={{ color: isSavedInDb ? colors.present : colors.absent, fontSize: 11, fontWeight: 'bold' }}>
              {isSavedInDb ? "✓ Saved" : "● Pending"}
            </Text>
          </View>
        </View>

        {/* Date Selector Row */}
        <View style={styles.dateSelectorRow}>
          <TouchableOpacity
            onPress={() => handleDateChange(todayStr)}
            style={[
              styles.datePill,
              { backgroundColor: isToday ? colors.primary : colors.surfaceVariant },
            ]}
          >
            <Text style={{ color: isToday ? '#FFF' : colors.textSecondary, fontSize: 12, fontWeight: '600' }}>
              Today
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => {
              const d = new Date();
              d.setDate(d.getDate() - 1);
              const yDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
              handleDateChange(yDate);
            }}
            style={[
              styles.datePill,
              { backgroundColor: selectedDate !== todayStr && selectedDate ? colors.surfaceVariant : colors.surfaceVariant },
            ]}
          >
            <Text style={{ color: colors.textSecondary, fontSize: 12, fontWeight: '600' }}>
              Yesterday
            </Text>
          </TouchableOpacity>

          {/* Prominent Back Date Button */}
          <TouchableOpacity
            onPress={() => setDatePickerVisible(true)}
            style={[styles.backDateBtn, { backgroundColor: colors.primaryContainer }]}
          >
            <Ionicons name="calendar-outline" size={14} color={colors.primary} />
            <Text style={[styles.backDateBtnText, { color: colors.primary }]}>
              📅 Pick Date
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* Class & Section Selectors */}
      <View style={styles.chipsContainer}>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: 6 }}>
          <Text style={[styles.filterLabel, { color: colors.textSecondary }]}>Class: </Text>
          {availableClasses.map(cls => (
            <TouchableOpacity
              key={cls}
              onPress={() => handleClassChange(cls)}
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
        </ScrollView>

        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
          <Text style={[styles.filterLabel, { color: colors.textSecondary }]}>Section: </Text>
          {availableSections.map(sec => (
            <TouchableOpacity
              key={sec}
              onPress={() => handleSectionChange(sec)}
              style={[
                styles.chip,
                { backgroundColor: selectedSection === sec ? colors.secondary : colors.surface },
              ]}
            >
              <Text style={{ color: selectedSection === sec ? '#FFF' : colors.text, fontSize: 12, fontWeight: '600' }}>
                Sec {sec}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>
      </View>

      {/* Quick Mark All Row */}
      <View style={styles.quickMarkRow}>
        <TouchableOpacity
          onPress={() => handleMarkAll('PRESENT')}
          style={[styles.quickMarkBtn, { backgroundColor: colors.presentBg }]}
        >
          <Text style={[styles.quickMarkText, { color: colors.present }]}>All Present</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => handleMarkAll('ABSENT')}
          style={[styles.quickMarkBtn, { backgroundColor: colors.absentBg }]}
        >
          <Text style={[styles.quickMarkText, { color: colors.absent }]}>All Absent</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => handleMarkAll('LEAVE')}
          style={[styles.quickMarkBtn, { backgroundColor: colors.leaveBg }]}
        >
          <Text style={[styles.quickMarkText, { color: colors.leave }]}>All Leave</Text>
        </TouchableOpacity>
      </View>

      {/* Student List */}
      <FlatList
        data={classStudents}
        keyExtractor={item => item.id}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 90 }}
        renderItem={({ item }) => {
          const currentStatus = attendanceMap[item.id] || 'PRESENT';

          return (
            <View style={[styles.studentRow, { backgroundColor: colors.surface }]}>
              {/* Avatar */}
              <View style={[styles.avatar, { backgroundColor: item.avatarColor || colors.primary }]}>
                <Text style={styles.avatarText}>{item.name ? item.name[0].toUpperCase() : 'S'}</Text>
              </View>

              {/* Info */}
              <View style={styles.studentInfo}>
                <Text style={[styles.nameText, { color: colors.text }]}>{item.name}</Text>
                <Text style={[styles.rollText, { color: colors.textSecondary }]}>
                  Roll No: {item.rollNumber}
                </Text>
              </View>

              {/* 3-State Segmented Control */}
              <View style={[styles.segmentedWrap, { backgroundColor: colors.surfaceVariant }]}>
                <TouchableOpacity
                  onPress={() => setStudentStatus(item.id, 'PRESENT')}
                  style={[
                    styles.segmentBtn,
                    currentStatus === 'PRESENT' && { backgroundColor: colors.present },
                  ]}
                >
                  <Text style={[styles.segmentText, currentStatus === 'PRESENT' && styles.segmentTextActive]}>
                    P
                  </Text>
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => setStudentStatus(item.id, 'ABSENT')}
                  style={[
                    styles.segmentBtn,
                    currentStatus === 'ABSENT' && { backgroundColor: colors.absent },
                  ]}
                >
                  <Text style={[styles.segmentText, currentStatus === 'ABSENT' && styles.segmentTextActive]}>
                    A
                  </Text>
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => setStudentStatus(item.id, 'LEAVE')}
                  style={[
                    styles.segmentBtn,
                    currentStatus === 'LEAVE' && { backgroundColor: colors.leave },
                  ]}
                >
                  <Text style={[styles.segmentText, currentStatus === 'LEAVE' && styles.segmentTextActive]}>
                    L
                  </Text>
                </TouchableOpacity>
              </View>
            </View>
          );
        }}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Ionicons name="alert-circle-outline" size={48} color={colors.textMuted} />
            <Text style={[styles.emptyTitle, { color: colors.text }]}>
              No Students in {selectedClass} - Sec {selectedSection}
            </Text>
            <Text style={[styles.emptySub, { color: colors.textSecondary }]}>
              Add students to this class in the Students tab.
            </Text>
          </View>
        }
      />

      {/* Floating Bottom Save Bar */}
      <View style={[styles.bottomBar, { backgroundColor: colors.surface, borderTopColor: colors.border }]}>
        <View>
          <Text style={[styles.bottomCount, { color: colors.text }]}>
            {presentCount} P  •  {absentCount} A  •  {leaveCount} L
          </Text>
          <Text style={[styles.bottomSub, { color: isSavedInDb ? colors.present : colors.absent }]}>
            {isSavedInDb ? 'Saved in database' : 'Unsaved changes'}
          </Text>
        </View>

        <TouchableOpacity
          onPress={handleSaveAttendance}
          style={[styles.saveButton, { backgroundColor: colors.primary }]}
        >
          <Ionicons name={isSavedInDb ? "checkmark-circle" : "save"} size={18} color="#FFF" />
          <Text style={styles.saveButtonText}>
            {isSavedInDb ? "Update" : "Save"}
          </Text>
        </TouchableOpacity>
      </View>

      {/* Date Picker Modal */}
      <DatePickerModal
        visible={datePickerVisible}
        currentDate={selectedDate}
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
  dateCard: {
    margin: 16,
    borderRadius: 16,
    padding: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.05,
    shadowRadius: 6,
    elevation: 2,
  },
  dateCardTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  dateTypeLabel: {
    fontSize: 12,
    fontWeight: 'bold',
  },
  dateText: {
    fontSize: 16,
    fontWeight: 'bold',
    marginTop: 2,
  },
  savedBadge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
  },
  dateSelectorRow: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 12,
    alignItems: 'center',
  },
  datePill: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
  },
  backDateBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 12,
    gap: 4,
    flex: 1,
    justifyContent: 'center',
  },
  backDateBtnText: {
    fontWeight: 'bold',
    fontSize: 12,
  },
  chipsContainer: {
    paddingHorizontal: 16,
    marginBottom: 8,
  },
  filterLabel: {
    fontSize: 12,
    fontWeight: '600',
    alignSelf: 'center',
    marginRight: 6,
  },
  chip: {
    paddingHorizontal: 12,
    paddingVertical: 5,
    borderRadius: 12,
    marginRight: 6,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  quickMarkRow: {
    flexDirection: 'row',
    paddingHorizontal: 16,
    gap: 8,
    marginBottom: 10,
  },
  quickMarkBtn: {
    flex: 1,
    paddingVertical: 7,
    borderRadius: 10,
    alignItems: 'center',
  },
  quickMarkText: {
    fontSize: 12,
    fontWeight: 'bold',
  },
  studentRow: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 14,
    padding: 10,
    marginBottom: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.04,
    shadowRadius: 3,
    elevation: 1,
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
  studentInfo: {
    flex: 1,
  },
  nameText: {
    fontSize: 14,
    fontWeight: 'bold',
  },
  rollText: {
    fontSize: 11,
    marginTop: 1,
  },
  segmentedWrap: {
    flexDirection: 'row',
    borderRadius: 10,
    padding: 2,
  },
  segmentBtn: {
    width: 32,
    height: 30,
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  segmentText: {
    fontSize: 13,
    fontWeight: 'bold',
    color: '#64748B',
  },
  segmentTextActive: {
    color: '#FFFFFF',
  },
  emptyState: {
    alignItems: 'center',
    marginTop: 40,
    paddingHorizontal: 20,
  },
  emptyTitle: {
    fontSize: 15,
    fontWeight: 'bold',
    marginTop: 10,
  },
  emptySub: {
    fontSize: 12,
    marginTop: 4,
  },
  bottomBar: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderTopWidth: 1,
    elevation: 8,
  },
  bottomCount: {
    fontSize: 13,
    fontWeight: 'bold',
  },
  bottomSub: {
    fontSize: 11,
    fontWeight: '600',
    marginTop: 1,
  },
  saveButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 18,
    paddingVertical: 10,
    borderRadius: 12,
    gap: 6,
  },
  saveButtonText: {
    color: '#FFF',
    fontWeight: 'bold',
    fontSize: 14,
  },
});
