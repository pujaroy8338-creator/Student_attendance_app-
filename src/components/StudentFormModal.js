import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  Modal,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';

const AVATAR_COLORS = [
  '#4CAF50', '#2196F3', '#9C27B0', '#E91E63',
  '#FF9800', '#009688', '#3F51B5', '#00BCD4',
];

export default function StudentFormModal({
  visible,
  student, // if null -> Add mode, if present -> Edit mode
  colors,
  onClose,
  onSave,
}) {
  const isEdit = !!student;

  const [name, setName] = useState('');
  const [rollNumber, setRollNumber] = useState('');
  const [studentClass, setStudentClass] = useState('Class 10');
  const [section, setSection] = useState('A');
  const [fatherName, setFatherName] = useState('');
  const [motherName, setMotherName] = useState('');
  const [mobileNumber, setMobileNumber] = useState('');
  const [address, setAddress] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [admissionNumber, setAdmissionNumber] = useState('');
  const [status, setStatus] = useState('Active');
  const [avatarColor, setAvatarColor] = useState('#4CAF50');
  const [error, setError] = useState('');

  // IMPORTANT: Populate all previously saved fields in Edit mode
  useEffect(() => {
    if (student) {
      setName(student.name || '');
      setRollNumber(student.rollNumber || '');
      setStudentClass(student.studentClass || 'Class 10');
      setSection(student.section || 'A');
      setFatherName(student.fatherName || '');
      setMotherName(student.motherName || '');
      setMobileNumber(student.mobileNumber || '');
      setAddress(student.address || '');
      setDateOfBirth(student.dateOfBirth || '');
      setAdmissionNumber(student.admissionNumber || '');
      setStatus(student.status || 'Active');
      setAvatarColor(student.avatarColor || AVATAR_COLORS[0]);
    } else {
      setName('');
      setRollNumber('');
      setStudentClass('Class 10');
      setSection('A');
      setFatherName('');
      setMotherName('');
      setMobileNumber('');
      setAddress('');
      setDateOfBirth('');
      setAdmissionNumber('');
      setStatus('Active');
      setAvatarColor(AVATAR_COLORS[Math.floor(Math.random() * AVATAR_COLORS.length)]);
    }
    setError('');
  }, [student, visible]);

  const handleSave = () => {
    if (!name.trim()) {
      setError('Student name is required');
      return;
    }
    if (!rollNumber.trim()) {
      setError('Roll number is required');
      return;
    }
    if (!studentClass.trim()) {
      setError('Class is required');
      return;
    }
    if (!section.trim()) {
      setError('Section is required');
      return;
    }

    const payload = {
      ...(student || {}),
      name: name.trim(),
      rollNumber: rollNumber.trim(),
      studentClass: studentClass.trim(),
      section: section.trim(),
      fatherName: fatherName.trim(),
      motherName: motherName.trim(),
      mobileNumber: mobileNumber.trim(),
      address: address.trim(),
      dateOfBirth: dateOfBirth.trim(),
      admissionNumber: admissionNumber.trim(),
      status: status.trim() || 'Active',
      avatarColor,
    };

    onSave(payload, isEdit);
  };

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <View style={styles.overlay}>
        <View style={[styles.container, { backgroundColor: colors.surface }]}>
          {/* Header */}
          <View style={styles.header}>
            <Text style={[styles.title, { color: colors.text }]}>
              {isEdit ? 'Edit Student Details' : 'Add New Student'}
            </Text>
            <TouchableOpacity onPress={onClose}>
              <Ionicons name="close" size={24} color={colors.textSecondary} />
            </TouchableOpacity>
          </View>

          {error ? (
            <View style={[styles.errorBox, { backgroundColor: colors.absentBg }]}>
              <Text style={[styles.errorText, { color: colors.absent }]}>{error}</Text>
            </View>
          ) : null}

          <ScrollView style={styles.body} showsVerticalScrollIndicator={false}>
            {/* Student Name */}
            <Text style={[styles.label, { color: colors.textSecondary }]}>Student Name *</Text>
            <TextInput
              style={[styles.input, { borderColor: colors.border, color: colors.text }]}
              value={name}
              onChangeText={(t) => { setName(t); setError(''); }}
              placeholder="e.g. Aarav Sharma"
              placeholderTextColor={colors.textMuted}
            />

            {/* Roll Number & Admission No */}
            <View style={styles.row}>
              <View style={styles.halfCol}>
                <Text style={[styles.label, { color: colors.textSecondary }]}>Roll No *</Text>
                <TextInput
                  style={[styles.input, { borderColor: colors.border, color: colors.text }]}
                  value={rollNumber}
                  onChangeText={(t) => { setRollNumber(t); setError(''); }}
                  placeholder="e.g. 101"
                  placeholderTextColor={colors.textMuted}
                />
              </View>
              <View style={styles.halfCol}>
                <Text style={[styles.label, { color: colors.textSecondary }]}>Admission No</Text>
                <TextInput
                  style={[styles.input, { borderColor: colors.border, color: colors.text }]}
                  value={admissionNumber}
                  onChangeText={setAdmissionNumber}
                  placeholder="ADM-2022-001"
                  placeholderTextColor={colors.textMuted}
                />
              </View>
            </View>

            {/* Class & Section */}
            <View style={styles.row}>
              <View style={styles.halfCol}>
                <Text style={[styles.label, { color: colors.textSecondary }]}>Class *</Text>
                <TextInput
                  style={[styles.input, { borderColor: colors.border, color: colors.text }]}
                  value={studentClass}
                  onChangeText={(t) => { setStudentClass(t); setError(''); }}
                  placeholder="Class 10"
                  placeholderTextColor={colors.textMuted}
                />
              </View>
              <View style={styles.halfCol}>
                <Text style={[styles.label, { color: colors.textSecondary }]}>Section *</Text>
                <TextInput
                  style={[styles.input, { borderColor: colors.border, color: colors.text }]}
                  value={section}
                  onChangeText={(t) => { setSection(t); setError(''); }}
                  placeholder="A"
                  placeholderTextColor={colors.textMuted}
                />
              </View>
            </View>

            {/* Father's Name */}
            <Text style={[styles.label, { color: colors.textSecondary }]}>Father's Name</Text>
            <TextInput
              style={[styles.input, { borderColor: colors.border, color: colors.text }]}
              value={fatherName}
              onChangeText={setFatherName}
              placeholder="e.g. Rajesh Sharma"
              placeholderTextColor={colors.textMuted}
            />

            {/* Mother's Name */}
            <Text style={[styles.label, { color: colors.textSecondary }]}>Mother's Name</Text>
            <TextInput
              style={[styles.input, { borderColor: colors.border, color: colors.text }]}
              value={motherName}
              onChangeText={setMotherName}
              placeholder="e.g. Sunita Sharma"
              placeholderTextColor={colors.textMuted}
            />

            {/* Mobile Number */}
            <Text style={[styles.label, { color: colors.textSecondary }]}>Mobile Number</Text>
            <TextInput
              style={[styles.input, { borderColor: colors.border, color: colors.text }]}
              value={mobileNumber}
              onChangeText={setMobileNumber}
              placeholder="+91 98765 43210"
              keyboardType="phone-pad"
              placeholderTextColor={colors.textMuted}
            />

            {/* DOB & Status */}
            <View style={styles.row}>
              <View style={styles.halfCol}>
                <Text style={[styles.label, { color: colors.textSecondary }]}>Date of Birth</Text>
                <TextInput
                  style={[styles.input, { borderColor: colors.border, color: colors.text }]}
                  value={dateOfBirth}
                  onChangeText={setDateOfBirth}
                  placeholder="YYYY-MM-DD"
                  placeholderTextColor={colors.textMuted}
                />
              </View>
              <View style={styles.halfCol}>
                <Text style={[styles.label, { color: colors.textSecondary }]}>Status</Text>
                <TextInput
                  style={[styles.input, { borderColor: colors.border, color: colors.text }]}
                  value={status}
                  onChangeText={setStatus}
                  placeholder="Active / Inactive"
                  placeholderTextColor={colors.textMuted}
                />
              </View>
            </View>

            {/* Address */}
            <Text style={[styles.label, { color: colors.textSecondary }]}>Residential Address</Text>
            <TextInput
              style={[styles.input, { borderColor: colors.border, color: colors.text, height: 60 }]}
              value={address}
              onChangeText={setAddress}
              placeholder="Full home address..."
              multiline
              placeholderTextColor={colors.textMuted}
            />

            {/* Avatar Color Picker */}
            <Text style={[styles.label, { color: colors.textSecondary }]}>Avatar Theme</Text>
            <View style={styles.colorRow}>
              {AVATAR_COLORS.map((c) => (
                <TouchableOpacity
                  key={c}
                  onPress={() => setAvatarColor(c)}
                  style={[
                    styles.colorCircle,
                    { backgroundColor: c },
                    avatarColor === c && styles.colorCircleSelected,
                  ]}
                >
                  {avatarColor === c && <Ionicons name="checkmark" size={14} color="#FFF" />}
                </TouchableOpacity>
              ))}
            </View>

            <View style={{ height: 20 }} />
          </ScrollView>

          {/* Footer Buttons */}
          <View style={styles.footer}>
            <TouchableOpacity onPress={onClose} style={[styles.button, styles.cancelBtn, { borderColor: colors.border }]}>
              <Text style={{ color: colors.textSecondary }}>Cancel</Text>
            </TouchableOpacity>

            <TouchableOpacity
              onPress={handleSave}
              style={[styles.button, styles.saveBtn, { backgroundColor: colors.primary }]}
            >
              <Text style={styles.saveBtnText}>{isEdit ? 'Update Student' : 'Save Student'}</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'flex-end',
  },
  container: {
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    maxHeight: '90%',
    padding: 20,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  errorBox: {
    padding: 8,
    borderRadius: 8,
    marginBottom: 10,
  },
  errorText: {
    fontSize: 12,
    fontWeight: '600',
  },
  body: {
    maxHeight: 480,
  },
  label: {
    fontSize: 12,
    fontWeight: '600',
    marginBottom: 4,
    marginTop: 8,
  },
  input: {
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 9,
    fontSize: 14,
  },
  row: {
    flexDirection: 'row',
    gap: 10,
  },
  halfCol: {
    flex: 1,
  },
  colorRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 6,
    marginBottom: 10,
  },
  colorCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    justifyContent: 'center',
    alignItems: 'center',
  },
  colorCircleSelected: {
    borderWidth: 2,
    borderColor: '#000',
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 12,
    marginTop: 16,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
  },
  button: {
    paddingHorizontal: 18,
    paddingVertical: 10,
    borderRadius: 12,
  },
  cancelBtn: {
    borderWidth: 1,
  },
  saveBtn: {},
  saveBtnText: {
    color: '#FFFFFF',
    fontWeight: 'bold',
    fontSize: 14,
  },
});
