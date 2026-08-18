import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  FlatList,
  StyleSheet,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { StorageService } from '../services/storage';
import StudentFormModal from '../components/StudentFormModal';
import StudentProfileModal from '../components/StudentProfileModal';
import ConfirmationModal from '../components/ConfirmationModal';

export default function StudentsScreen({ navigation, colors }) {
  const [students, setStudents] = useState([]);
  const [attendanceRecords, setAttendanceRecords] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedClass, setSelectedClass] = useState('All');
  const [selectedSection, setSelectedSection] = useState('All');
  const [sortBy, setSortBy] = useState('roll'); // 'roll' | 'name'

  // Modals
  const [formModalVisible, setFormModalVisible] = useState(false);
  const [editingStudent, setEditingStudent] = useState(null);
  const [profileModalVisible, setProfileModalVisible] = useState(false);
  const [viewingStudent, setViewingStudent] = useState(null);
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);
  const [deletingStudentId, setDeletingStudentId] = useState(null);

  const loadStudents = async () => {
    const list = await StorageService.getStudents();
    const attList = await StorageService.getAllAttendance();
    setStudents(list);
    setAttendanceRecords(attList);
  };

  useEffect(() => {
    loadStudents();
    const unsubscribe = navigation.addListener('focus', () => {
      loadStudents();
    });
    return unsubscribe;
  }, [navigation]);

  // Extract unique classes and sections
  const availableClasses = ['All', ...new Set(students.map(s => s.studentClass).filter(Boolean))];
  const availableSections = ['All', ...new Set(students.map(s => s.section).filter(Boolean))];

  // Filter & Sort
  const filteredStudents = students.filter(s => {
    const matchesSearch =
      s.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.rollNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (s.admissionNumber && s.admissionNumber.toLowerCase().includes(searchQuery.toLowerCase()));

    const matchesClass = selectedClass === 'All' || s.studentClass === selectedClass;
    const matchesSection = selectedSection === 'All' || s.section === selectedSection;

    return matchesSearch && matchesClass && matchesSection;
  }).sort((a, b) => {
    if (sortBy === 'roll') {
      const aRoll = parseInt(a.rollNumber) || 9999;
      const bRoll = parseInt(b.rollNumber) || 9999;
      return aRoll - bRoll;
    } else {
      return a.name.localeCompare(b.name);
    }
  });

  const handleSaveStudent = async (studentData, isEdit) => {
    const result = await StorageService.saveStudent(studentData, isEdit);
    if (result.success) {
      setFormModalVisible(false);
      setEditingStudent(null);
      await loadStudents();
    } else {
      Alert.alert('Error', result.message);
    }
  };

  const handleDeleteConfirm = async () => {
    if (deletingStudentId) {
      await StorageService.deleteStudent(deletingStudentId);
      setDeleteModalVisible(false);
      setDeletingStudentId(null);
      await loadStudents();
    }
  };

  return (
    <View style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Search Bar & Add Button */}
      <View style={styles.searchHeader}>
        <View style={[styles.searchBox, { backgroundColor: colors.surface, borderColor: colors.border }]}>
          <Ionicons name="search" size={18} color={colors.textSecondary} />
          <TextInput
            style={[styles.searchInput, { color: colors.text }]}
            placeholder="Search by name, roll no, adm no..."
            placeholderTextColor={colors.textMuted}
            value={searchQuery}
            onChangeText={setSearchQuery}
          />
          {searchQuery ? (
            <TouchableOpacity onPress={() => setSearchQuery('')}>
              <Ionicons name="close-circle" size={18} color={colors.textMuted} />
            </TouchableOpacity>
          ) : null}
        </View>

        <TouchableOpacity
          onPress={() => { setEditingStudent(null); setFormModalVisible(true); }}
          style={[styles.addBtn, { backgroundColor: colors.primary }]}
        >
          <Ionicons name="person-add" size={18} color="#FFF" />
          <Text style={styles.addBtnText}>Add</Text>
        </TouchableOpacity>
      </View>

      {/* Class & Section Filter Pills */}
      <View style={styles.filterRow}>
        <FlatList
          horizontal
          showsHorizontalScrollIndicator={false}
          data={availableClasses}
          keyExtractor={item => 'cls_' + item}
          renderItem={({ item }) => (
            <TouchableOpacity
              onPress={() => setSelectedClass(item)}
              style={[
                styles.filterChip,
                { backgroundColor: selectedClass === item ? colors.primary : colors.surface },
              ]}
            >
              <Text style={{ color: selectedClass === item ? '#FFF' : colors.textSecondary, fontSize: 12, fontWeight: '600' }}>
                {item === 'All' ? 'All Classes' : item}
              </Text>
            </TouchableOpacity>
          )}
        />
      </View>

      {/* Section Chips */}
      {selectedClass !== 'All' && (
        <View style={[styles.filterRow, { paddingTop: 0 }]}>
          <FlatList
            horizontal
            showsHorizontalScrollIndicator={false}
            data={availableSections}
            keyExtractor={item => 'sec_' + item}
            renderItem={({ item }) => (
              <TouchableOpacity
                onPress={() => setSelectedSection(item)}
                style={[
                  styles.filterChip,
                  { backgroundColor: selectedSection === item ? colors.secondary : colors.surfaceVariant },
                ]}
              >
                <Text style={{ color: selectedSection === item ? '#FFF' : colors.textSecondary, fontSize: 11, fontWeight: '600' }}>
                  {item === 'All' ? 'All Sec' : `Sec ${item}`}
                </Text>
              </TouchableOpacity>
            )}
          />
        </View>
      )}

      {/* Student List */}
      <View style={styles.countRow}>
        <Text style={[styles.countText, { color: colors.textSecondary }]}>
          Showing {filteredStudents.length} Students
        </Text>
        <TouchableOpacity
          onPress={() => setSortBy(sortBy === 'roll' ? 'name' : 'roll')}
          style={styles.sortBtn}
        >
          <Ionicons name="swap-vertical" size={14} color={colors.primary} />
          <Text style={{ color: colors.primary, fontSize: 12, fontWeight: '600', marginLeft: 2 }}>
            Sort: {sortBy === 'roll' ? 'Roll No' : 'Name'}
          </Text>
        </TouchableOpacity>
      </View>

      <FlatList
        data={filteredStudents}
        keyExtractor={item => item.id}
        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 30 }}
        renderItem={({ item }) => (
          <TouchableOpacity
            onPress={() => {
              setViewingStudent(item);
              setProfileModalVisible(true);
            }}
            style={[styles.studentCard, { backgroundColor: colors.surface }]}
          >
            {/* Avatar */}
            <View style={[styles.avatar, { backgroundColor: item.avatarColor || colors.primary }]}>
              <Text style={styles.avatarText}>{item.name ? item.name[0].toUpperCase() : 'S'}</Text>
            </View>

            {/* Info */}
            <View style={styles.infoCol}>
              <Text style={[styles.studentName, { color: colors.text }]}>{item.name}</Text>
              <Text style={[styles.studentMeta, { color: colors.textSecondary }]}>
                Roll No: {item.rollNumber}  •  {item.studentClass} - Sec {item.section}
              </Text>
              {item.mobileNumber ? (
                <Text style={[styles.studentMeta, { color: colors.textMuted }]}>
                  📞 {item.mobileNumber}
                </Text>
              ) : null}
            </View>

            {/* Action Buttons */}
            <View style={styles.actionRow}>
              <TouchableOpacity
                onPress={() => {
                  setEditingStudent(item);
                  setFormModalVisible(true);
                }}
                style={[styles.actionBtn, { backgroundColor: colors.primaryContainer }]}
              >
                <Ionicons name="pencil" size={16} color={colors.primary} />
              </TouchableOpacity>

              <TouchableOpacity
                onPress={() => {
                  setDeletingStudentId(item.id);
                  setDeleteModalVisible(true);
                }}
                style={[styles.actionBtn, { backgroundColor: colors.absentBg }]}
              >
                <Ionicons name="trash" size={16} color={colors.absent} />
              </TouchableOpacity>
            </View>
          </TouchableOpacity>
        )}
        ListEmptyComponent={
          <View style={styles.emptyState}>
            <Ionicons name="people-outline" size={48} color={colors.textMuted} />
            <Text style={[styles.emptyTitle, { color: colors.text }]}>No Students Found</Text>
            <Text style={[styles.emptySub, { color: colors.textSecondary }]}>
              Tap "Add" button above to add a student to this class.
            </Text>
          </View>
        }
      />

      {/* Form Modal (Add / Edit) */}
      <StudentFormModal
        visible={formModalVisible}
        student={editingStudent}
        colors={colors}
        onClose={() => { setFormModalVisible(false); setEditingStudent(null); }}
        onSave={handleSaveStudent}
      />

      {/* Profile Modal */}
      <StudentProfileModal
        visible={profileModalVisible}
        student={viewingStudent}
        attendanceLogs={attendanceRecords.filter(a => viewingStudent && a.studentId === viewingStudent.id)}
        colors={colors}
        onClose={() => { setProfileModalVisible(false); setViewingStudent(null); }}
        onEdit={(st) => {
          setViewingStudent(null);
          setEditingStudent(st);
          setFormModalVisible(true);
        }}
      />

      {/* Delete Confirmation Modal */}
      <ConfirmationModal
        visible={deleteModalVisible}
        title="Delete Student"
        message="Are you sure you want to delete this student and their associated attendance history?"
        confirmText="Delete"
        isDestructive
        colors={colors}
        onConfirm={handleDeleteConfirm}
        onClose={() => setDeleteModalVisible(false)}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  searchHeader: {
    flexDirection: 'row',
    padding: 16,
    gap: 10,
    alignItems: 'center',
  },
  searchBox: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 12,
    height: 44,
    gap: 8,
  },
  searchInput: {
    flex: 1,
    fontSize: 14,
  },
  addBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    height: 44,
    borderRadius: 12,
    gap: 4,
  },
  addBtnText: {
    color: '#FFF',
    fontWeight: 'bold',
    fontSize: 13,
  },
  filterRow: {
    paddingHorizontal: 16,
    paddingBottom: 8,
  },
  filterChip: {
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 14,
    marginRight: 8,
    borderWidth: 1,
    borderColor: '#E2E8F0',
  },
  countRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 6,
  },
  countText: {
    fontSize: 12,
    fontWeight: '600',
  },
  sortBtn: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  studentCard: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 14,
    padding: 12,
    marginBottom: 8,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 1,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 12,
  },
  avatarText: {
    color: '#FFF',
    fontSize: 18,
    fontWeight: 'bold',
  },
  infoCol: {
    flex: 1,
  },
  studentName: {
    fontSize: 15,
    fontWeight: 'bold',
  },
  studentMeta: {
    fontSize: 12,
    marginTop: 2,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 6,
  },
  actionBtn: {
    width: 32,
    height: 32,
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyState: {
    alignItems: 'center',
    marginTop: 60,
    paddingHorizontal: 30,
  },
  emptyTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginTop: 12,
  },
  emptySub: {
    fontSize: 13,
    textAlign: 'center',
    marginTop: 4,
  },
});
