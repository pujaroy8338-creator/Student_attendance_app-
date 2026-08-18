import AsyncStorage from '@react-native-async-storage/async-storage';

const KEYS = {
  STUDENTS: '@smart_attendance_students',
  ATTENDANCE: '@smart_attendance_records',
  SETTINGS: '@smart_attendance_settings',
};

const DEFAULT_SETTINGS = {
  schoolName: 'Smart Academy',
  academicYear: '2026-2027',
  defaultClass: 'Class 10',
  defaultSection: 'A',
  allowFutureDates: false,
  themeMode: 'SYSTEM', // 'SYSTEM' | 'LIGHT' | 'DARK'
};

const INITIAL_STUDENTS = [
  {
    id: '1',
    rollNumber: '101',
    name: 'Aarav Sharma',
    fatherName: 'Rajesh Sharma',
    motherName: 'Sunita Sharma',
    studentClass: 'Class 10',
    section: 'A',
    mobileNumber: '+91 98765 43210',
    address: '42 Sunrise Enclave, New Delhi',
    dateOfBirth: '2010-04-15',
    admissionNumber: 'ADM-2022-001',
    status: 'Active',
    avatarColor: '#4CAF50',
    createdAt: Date.now() - 1000000,
  },
  {
    id: '2',
    rollNumber: '102',
    name: 'Diya Patel',
    fatherName: 'Kiran Patel',
    motherName: 'Meena Patel',
    studentClass: 'Class 10',
    section: 'A',
    mobileNumber: '+91 98765 43211',
    address: '12 Harmony Park, Ahmedabad',
    dateOfBirth: '2010-08-22',
    admissionNumber: 'ADM-2022-002',
    status: 'Active',
    avatarColor: '#2196F3',
    createdAt: Date.now() - 900000,
  },
  {
    id: '3',
    rollNumber: '103',
    name: 'Rohan Verma',
    fatherName: 'Anil Verma',
    motherName: 'Rekha Verma',
    studentClass: 'Class 10',
    section: 'A',
    mobileNumber: '+91 98765 43212',
    address: '78 Green Valley, Bengaluru',
    dateOfBirth: '2010-02-10',
    admissionNumber: 'ADM-2022-003',
    status: 'Active',
    avatarColor: '#9C27B0',
    createdAt: Date.now() - 800000,
  },
  {
    id: '4',
    rollNumber: '104',
    name: 'Ananya Iyer',
    fatherName: 'Suresh Iyer',
    motherName: 'Lakshmi Iyer',
    studentClass: 'Class 10',
    section: 'A',
    mobileNumber: '+91 98765 43213',
    address: '55 Palm Grove, Chennai',
    dateOfBirth: '2010-11-05',
    admissionNumber: 'ADM-2022-004',
    status: 'Active',
    avatarColor: '#E91E63',
    createdAt: Date.now() - 700000,
  },
  {
    id: '5',
    rollNumber: '105',
    name: 'Kabir Singh',
    fatherName: 'Harpreet Singh',
    motherName: 'Jaspreet Kaur',
    studentClass: 'Class 10',
    section: 'A',
    mobileNumber: '+91 98765 43214',
    address: '19 Model Town, Chandigarh',
    dateOfBirth: '2010-06-18',
    admissionNumber: 'ADM-2022-005',
    status: 'Active',
    avatarColor: '#FF9800',
    createdAt: Date.now() - 600000,
  },
  {
    id: '6',
    rollNumber: '201',
    name: 'Ishaan Gupta',
    fatherName: 'Manoj Gupta',
    motherName: 'Pooja Gupta',
    studentClass: 'Class 10',
    section: 'B',
    mobileNumber: '+91 98765 43215',
    address: '88 Silver Oaks, Jaipur',
    dateOfBirth: '2010-01-30',
    admissionNumber: 'ADM-2022-006',
    status: 'Active',
    avatarColor: '#009688',
    createdAt: Date.now() - 500000,
  },
  {
    id: '7',
    rollNumber: '101',
    name: 'Siddharth Rao',
    fatherName: 'Venkat Rao',
    motherName: 'Geetha Rao',
    studentClass: 'Class 9',
    section: 'A',
    mobileNumber: '+91 98765 43217',
    address: '90 Jubilee Hills, Hyderabad',
    dateOfBirth: '2011-03-25',
    admissionNumber: 'ADM-2023-001',
    status: 'Active',
    avatarColor: '#3F51B5',
    createdAt: Date.now() - 400000,
  }
];

export const getTodayDateString = () => {
  const d = new Date();
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export const StorageService = {
  // Initialization
  async initStorage() {
    try {
      const studentsStr = await AsyncStorage.getItem(KEYS.STUDENTS);
      if (!studentsStr) {
        await AsyncStorage.setItem(KEYS.STUDENTS, JSON.stringify(INITIAL_STUDENTS));
        
        // Seed initial past attendance records for demo
        const today = getTodayDateString();
        const d = new Date();
        d.setDate(d.getDate() - 1);
        const yesterday = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        
        const initialAttendance = [
          { id: 'att-1', studentId: '1', date: today, class: 'Class 10', section: 'A', status: 'PRESENT', updatedAt: Date.now() },
          { id: 'att-2', studentId: '2', date: today, class: 'Class 10', section: 'A', status: 'PRESENT', updatedAt: Date.now() },
          { id: 'att-3', studentId: '3', date: today, class: 'Class 10', section: 'A', status: 'ABSENT', updatedAt: Date.now() },
          { id: 'att-4', studentId: '4', date: today, class: 'Class 10', section: 'A', status: 'PRESENT', updatedAt: Date.now() },
          { id: 'att-5', studentId: '5', date: today, class: 'Class 10', section: 'A', status: 'LEAVE', updatedAt: Date.now() },
          
          { id: 'att-6', studentId: '1', date: yesterday, class: 'Class 10', section: 'A', status: 'PRESENT', updatedAt: Date.now() },
          { id: 'att-7', studentId: '2', date: yesterday, class: 'Class 10', section: 'A', status: 'PRESENT', updatedAt: Date.now() },
          { id: 'att-8', studentId: '3', date: yesterday, class: 'Class 10', section: 'A', status: 'LEAVE', updatedAt: Date.now() },
          { id: 'att-9', studentId: '4', date: yesterday, class: 'Class 10', section: 'A', status: 'PRESENT', updatedAt: Date.now() },
          { id: 'att-10', studentId: '5', date: yesterday, class: 'Class 10', section: 'A', status: 'PRESENT', updatedAt: Date.now() },
        ];
        await AsyncStorage.setItem(KEYS.ATTENDANCE, JSON.stringify(initialAttendance));
      }

      const settingsStr = await AsyncStorage.getItem(KEYS.SETTINGS);
      if (!settingsStr) {
        await AsyncStorage.setItem(KEYS.SETTINGS, JSON.stringify(DEFAULT_SETTINGS));
      }
    } catch (e) {
      console.error('Failed to init storage:', e);
    }
  },

  // Students CRUD
  async getStudents() {
    try {
      const data = await AsyncStorage.getItem(KEYS.STUDENTS);
      return data ? JSON.parse(data) : [];
    } catch (e) {
      console.error(e);
      return [];
    }
  },

  async saveStudent(studentData, isEdit = false) {
    try {
      const students = await this.getStudents();
      
      // Check for duplicate Roll Number in same Class & Section
      const duplicate = students.find(s => 
        s.studentClass.toLowerCase() === studentData.studentClass.toLowerCase() &&
        s.section.toLowerCase() === studentData.section.toLowerCase() &&
        s.rollNumber.toLowerCase() === studentData.rollNumber.toLowerCase() &&
        (!isEdit || s.id !== studentData.id)
      );

      if (duplicate) {
        return { success: false, message: `Roll No '${studentData.rollNumber}' already exists in ${studentData.studentClass} - Sec ${studentData.section}` };
      }

      if (isEdit) {
        const index = students.findIndex(s => s.id === studentData.id);
        if (index !== -1) {
          students[index] = { ...students[index], ...studentData, updatedAt: Date.now() };
        }
      } else {
        const newStudent = {
          ...studentData,
          id: studentData.id || `st-${Date.now()}-${Math.random().toString(36).substr(2, 4)}`,
          createdAt: Date.now(),
        };
        students.push(newStudent);
      }

      await AsyncStorage.setItem(KEYS.STUDENTS, JSON.stringify(students));
      return { success: true, message: isEdit ? 'Student updated successfully' : 'Student added successfully' };
    } catch (e) {
      return { success: false, message: e.message };
    }
  },

  async deleteStudent(studentId) {
    try {
      let students = await this.getStudents();
      students = students.filter(s => s.id !== studentId);
      await AsyncStorage.setItem(KEYS.STUDENTS, JSON.stringify(students));

      // Also clean up attendance records for this student
      let attendance = await this.getAllAttendance();
      attendance = attendance.filter(a => a.studentId !== studentId);
      await AsyncStorage.setItem(KEYS.ATTENDANCE, JSON.stringify(attendance));

      return true;
    } catch (e) {
      console.error(e);
      return false;
    }
  },

  // Attendance Records
  async getAllAttendance() {
    try {
      const data = await AsyncStorage.getItem(KEYS.ATTENDANCE);
      return data ? JSON.parse(data) : [];
    } catch (e) {
      console.error(e);
      return [];
    }
  },

  async getAttendanceForDate(date) {
    const all = await this.getAllAttendance();
    return all.filter(a => a.date === date);
  },

  async getAttendanceForDateAndClass(date, studentClass, section) {
    const all = await this.getAllAttendance();
    return all.filter(a => 
      a.date === date && 
      (a.class || a.studentClass) === studentClass && 
      a.section === section
    );
  },

  async saveBatchAttendance(date, studentClass, section, attendanceMap) {
    try {
      const allAttendance = await this.getAllAttendance();
      
      // Filter out existing records for this (date + class + section) to prevent duplicate entries
      const otherRecords = allAttendance.filter(a => 
        !(a.date === date && (a.class || a.studentClass) === studentClass && a.section === section)
      );

      const newRecords = [];
      for (const [studentId, status] of Object.entries(attendanceMap)) {
        newRecords.push({
          id: `att-${studentId}-${date}`,
          studentId,
          date,
          class: studentClass,
          studentClass,
          section,
          status, // 'PRESENT' | 'ABSENT' | 'LEAVE'
          createdAt: Date.now(),
          updatedAt: Date.now(),
        });
      }

      const updated = [...otherRecords, ...newRecords];
      await AsyncStorage.setItem(KEYS.ATTENDANCE, JSON.stringify(updated));
      return { success: true, count: newRecords.length };
    } catch (e) {
      return { success: false, message: e.message };
    }
  },

  // Settings
  async getSettings() {
    try {
      const data = await AsyncStorage.getItem(KEYS.SETTINGS);
      return data ? { ...DEFAULT_SETTINGS, ...JSON.parse(data) } : DEFAULT_SETTINGS;
    } catch (e) {
      return DEFAULT_SETTINGS;
    }
  },

  async saveSettings(settings) {
    try {
      await AsyncStorage.setItem(KEYS.SETTINGS, JSON.stringify(settings));
      return true;
    } catch (e) {
      return false;
    }
  },

  // Backup & Restore
  async exportBackupJSON() {
    const students = await this.getStudents();
    const attendance = await this.getAllAttendance();
    const settings = await this.getSettings();

    const backup = {
      version: '1.0.0',
      exportedAt: new Date().toISOString(),
      appName: 'Smart Attendance',
      data: {
        students,
        attendance,
        settings,
      }
    };
    return JSON.stringify(backup, null, 2);
  },

  async importBackupJSON(jsonString) {
    try {
      const parsed = JSON.parse(jsonString);
      if (!parsed.data || !Array.isArray(parsed.data.students)) {
        return { success: false, message: 'Invalid backup format: missing students array' };
      }

      await AsyncStorage.setItem(KEYS.STUDENTS, JSON.stringify(parsed.data.students));
      if (Array.isArray(parsed.data.attendance)) {
        await AsyncStorage.setItem(KEYS.ATTENDANCE, JSON.stringify(parsed.data.attendance));
      }
      if (parsed.data.settings) {
        await AsyncStorage.setItem(KEYS.SETTINGS, JSON.stringify(parsed.data.settings));
      }

      return {
        success: true,
        studentCount: parsed.data.students.length,
        attendanceCount: (parsed.data.attendance || []).length,
      };
    } catch (e) {
      return { success: false, message: 'JSON Parse Error: ' + e.message };
    }
  },

  async resetToSampleData() {
    await AsyncStorage.setItem(KEYS.STUDENTS, JSON.stringify(INITIAL_STUDENTS));
    await AsyncStorage.setItem(KEYS.ATTENDANCE, JSON.stringify([]));
    await AsyncStorage.setItem(KEYS.SETTINGS, JSON.stringify(DEFAULT_SETTINGS));
    await this.initStorage();
  },

  async clearAllData() {
    await AsyncStorage.setItem(KEYS.STUDENTS, JSON.stringify([]));
    await AsyncStorage.setItem(KEYS.ATTENDANCE, JSON.stringify([]));
  }
};
