import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Switch,
  ScrollView,
  StyleSheet,
  Alert,
  Modal,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { StorageService } from '../services/storage';
import ConfirmationModal from '../components/ConfirmationModal';

export default function SettingsScreen({ navigation, colors, currentTheme, onThemeChange }) {
  const [settings, setSettings] = useState({
    schoolName: 'Smart Academy',
    academicYear: '2026-2027',
    defaultClass: 'Class 10',
    defaultSection: 'A',
    allowFutureDates: false,
    themeMode: 'SYSTEM',
  });

  const [schoolName, setSchoolName] = useState('');
  const [academicYear, setAcademicYear] = useState('');
  const [defaultClass, setDefaultClass] = useState('');
  const [defaultSection, setDefaultSection] = useState('');
  const [allowFutureDates, setAllowFutureDates] = useState(false);
  const [themeMode, setThemeMode] = useState('SYSTEM');

  // Modals
  const [exportModalVisible, setExportModalVisible] = useState(false);
  const [exportedJson, setExportedJson] = useState('');
  const [importModalVisible, setImportModalVisible] = useState(false);
  const [importJsonText, setImportJsonText] = useState('');
  const [resetModalVisible, setResetModalVisible] = useState(false);
  const [clearModalVisible, setClearModalVisible] = useState(false);
  const [aboutModalVisible, setAboutModalVisible] = useState(false);
  const [privacyModalVisible, setPrivacyModalVisible] = useState(false);

  const loadSettings = async () => {
    const s = await StorageService.getSettings();
    setSettings(s);
    setSchoolName(s.schoolName || 'Smart Academy');
    setAcademicYear(s.academicYear || '2026-2027');
    setDefaultClass(s.defaultClass || 'Class 10');
    setDefaultSection(s.defaultSection || 'A');
    setAllowFutureDates(!!s.allowFutureDates);
    setThemeMode(s.themeMode || 'SYSTEM');
  };

  useEffect(() => {
    loadSettings();
  }, []);

  const handleSavePreferences = async () => {
    const updated = {
      ...settings,
      schoolName: schoolName.trim() || 'Smart Academy',
      academicYear: academicYear.trim() || '2026-2027',
      defaultClass: defaultClass.trim() || 'Class 10',
      defaultSection: defaultSection.trim() || 'A',
      allowFutureDates,
      themeMode,
    };
    await StorageService.saveSettings(updated);
    setSettings(updated);
    onThemeChange(themeMode);
    Alert.alert('Success', 'Settings saved successfully!');
  };

  const handleExport = async () => {
    const json = await StorageService.exportBackupJSON();
    setExportedJson(json);
    setExportModalVisible(true);
  };

  const handleImport = async () => {
    if (!importJsonText.trim()) {
      Alert.alert('Error', 'Please paste valid backup JSON');
      return;
    }
    const result = await StorageService.importBackupJSON(importJsonText);
    if (result.success) {
      setImportModalVisible(false);
      setImportJsonText('');
      await loadSettings();
      Alert.alert('Success', `Imported ${result.studentCount} students and ${result.attendanceCount} records!`);
    } else {
      Alert.alert('Import Failed', result.message);
    }
  };

  const handleResetSampleConfirm = async () => {
    await StorageService.resetToSampleData();
    setResetModalVisible(false);
    await loadSettings();
    Alert.alert('Reset Complete', 'Database restored to initial demo students.');
  };

  const handleClearAllConfirm = async () => {
    await StorageService.clearAllData();
    setClearModalVisible(false);
    await loadSettings();
    Alert.alert('Cleared', 'All students and attendance records deleted.');
  };

  return (
    <ScrollView style={[styles.container, { backgroundColor: colors.background }]}>
      {/* Institute Info Card */}
      <View style={[styles.card, { backgroundColor: colors.surface }]}>
        <View style={styles.cardHeader}>
          <Ionicons name="school" size={20} color={colors.primary} />
          <Text style={[styles.cardTitle, { color: colors.text }]}>Institute Information</Text>
        </View>

        <Text style={[styles.label, { color: colors.textSecondary }]}>School / Institute Name</Text>
        <TextInput
          style={[styles.input, { borderColor: colors.border, color: colors.text }]}
          value={schoolName}
          onChangeText={setSchoolName}
          placeholder="e.g. Smart Academy"
          placeholderTextColor={colors.textMuted}
        />

        <Text style={[styles.label, { color: colors.textSecondary }]}>Academic Session</Text>
        <TextInput
          style={[styles.input, { borderColor: colors.border, color: colors.text }]}
          value={academicYear}
          onChangeText={setAcademicYear}
          placeholder="e.g. 2026-2027"
          placeholderTextColor={colors.textMuted}
        />

        <View style={styles.row}>
          <View style={styles.halfCol}>
            <Text style={[styles.label, { color: colors.textSecondary }]}>Default Class</Text>
            <TextInput
              style={[styles.input, { borderColor: colors.border, color: colors.text }]}
              value={defaultClass}
              onChangeText={setDefaultClass}
              placeholder="Class 10"
              placeholderTextColor={colors.textMuted}
            />
          </View>
          <View style={styles.halfCol}>
            <Text style={[styles.label, { color: colors.textSecondary }]}>Default Section</Text>
            <TextInput
              style={[styles.input, { borderColor: colors.border, color: colors.text }]}
              value={defaultSection}
              onChangeText={setDefaultSection}
              placeholder="A"
              placeholderTextColor={colors.textMuted}
            />
          </View>
        </View>

        <TouchableOpacity
          onPress={handleSavePreferences}
          style={[styles.saveBtn, { backgroundColor: colors.primary }]}
        >
          <Ionicons name="save-outline" size={16} color="#FFF" />
          <Text style={styles.saveBtnText}>Save Preferences</Text>
        </TouchableOpacity>
      </View>

      {/* App Preferences */}
      <View style={[styles.card, { backgroundColor: colors.surface }]}>
        <View style={styles.cardHeader}>
          <Ionicons name="options" size={20} color={colors.primary} />
          <Text style={[styles.cardTitle, { color: colors.text }]}>App Preferences</Text>
        </View>

        {/* Theme Selector */}
        <Text style={[styles.label, { color: colors.textSecondary }]}>Theme Mode</Text>
        <View style={styles.themeRow}>
          {['SYSTEM', 'LIGHT', 'DARK'].map((mode) => (
            <TouchableOpacity
              key={mode}
              onPress={() => {
                setThemeMode(mode);
                onThemeChange(mode);
              }}
              style={[
                styles.themeChip,
                { backgroundColor: themeMode === mode ? colors.primary : colors.surfaceVariant },
              ]}
            >
              <Text
                style={{
                  color: themeMode === mode ? '#FFF' : colors.textSecondary,
                  fontWeight: 'bold',
                  fontSize: 12,
                }}
              >
                {mode === 'SYSTEM' ? '📱 System' : mode === 'LIGHT' ? '☀️ Light' : '🌙 Dark'}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Future Dates Toggle */}
        <View style={styles.toggleRow}>
          <View style={{ flex: 1 }}>
            <Text style={[styles.toggleTitle, { color: colors.text }]}>Allow Future Dates</Text>
            <Text style={[styles.toggleSub, { color: colors.textSecondary }]}>
              Permit marking attendance for upcoming days
            </Text>
          </View>
          <Switch
            value={allowFutureDates}
            onValueChange={v => {
              setAllowFutureDates(v);
              StorageService.saveSettings({ ...settings, allowFutureDates: v });
            }}
            trackColor={{ false: colors.border, true: colors.primaryContainer }}
            thumbColor={allowFutureDates ? colors.primary : '#f4f3f4'}
          />
        </View>
      </View>

      {/* Data Backup & Restore */}
      <View style={[styles.card, { backgroundColor: colors.surface }]}>
        <View style={styles.cardHeader}>
          <Ionicons name="cloud-upload" size={20} color={colors.primary} />
          <Text style={[styles.cardTitle, { color: colors.text }]}>Data Backup & Restore</Text>
        </View>

        <View style={styles.btnGrid}>
          <TouchableOpacity
            onPress={handleExport}
            style={[styles.actionGridBtn, { backgroundColor: colors.primaryContainer }]}
          >
            <Ionicons name="cloud-download-outline" size={18} color={colors.primary} />
            <Text style={[styles.actionGridText, { color: colors.primary }]}>Export JSON</Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => { setImportJsonText(''); setImportModalVisible(true); }}
            style={[styles.actionGridBtn, { backgroundColor: colors.secondaryContainer }]}
          >
            <Ionicons name="cloud-upload-outline" size={18} color={colors.secondary} />
            <Text style={[styles.actionGridText, { color: colors.secondary }]}>Import JSON</Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => setResetModalVisible(true)}
            style={[styles.actionGridBtn, { backgroundColor: colors.surfaceVariant }]}
          >
            <Ionicons name="refresh" size={18} color={colors.textSecondary} />
            <Text style={[styles.actionGridText, { color: colors.textSecondary }]}>Sample Data</Text>
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => setClearModalVisible(true)}
            style={[styles.actionGridBtn, { backgroundColor: colors.absentBg }]}
          >
            <Ionicons name="trash-outline" size={18} color={colors.absent} />
            <Text style={[styles.actionGridText, { color: colors.absent }]}>Clear All</Text>
          </TouchableOpacity>
        </View>
      </View>

      {/* About & Legal */}
      <View style={[styles.card, { backgroundColor: colors.surface }]}>
        <TouchableOpacity
          onPress={() => setAboutModalVisible(true)}
          style={[styles.navItem, { borderBottomColor: colors.border }]}
        >
          <Ionicons name="information-circle-outline" size={20} color={colors.primary} />
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={[styles.navItemTitle, { color: colors.text }]}>About Smart Attendance</Text>
            <Text style={[styles.navItemSub, { color: colors.textSecondary }]}>Version 1.0.0 • Offline First</Text>
          </View>
          <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
        </TouchableOpacity>

        <TouchableOpacity
          onPress={() => setPrivacyModalVisible(true)}
          style={[styles.navItem, { borderBottomWidth: 0 }]}
        >
          <Ionicons name="shield-checkmark-outline" size={20} color={colors.primary} />
          <View style={{ flex: 1, marginLeft: 12 }}>
            <Text style={[styles.navItemTitle, { color: colors.text }]}>Privacy Policy</Text>
            <Text style={[styles.navItemSub, { color: colors.textSecondary }]}>100% on-device private data</Text>
          </View>
          <Ionicons name="chevron-forward" size={18} color={colors.textMuted} />
        </TouchableOpacity>
      </View>

      <View style={{ height: 30 }} />

      {/* Export JSON Modal */}
      <Modal visible={exportModalVisible} transparent animationType="slide" onRequestClose={() => setExportModalVisible(false)}>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <Text style={[styles.modalTitle, { color: colors.text }]}>Exported Backup (JSON)</Text>
            <Text style={[styles.modalSub, { color: colors.textSecondary }]}>
              Copy this raw JSON payload to restore your database later:
            </Text>
            <TextInput
              style={[styles.jsonBox, { borderColor: colors.border, color: colors.text }]}
              value={exportedJson}
              editable={false}
              multiline
            />
            <TouchableOpacity
              onPress={() => setExportModalVisible(false)}
              style={[styles.modalCloseBtn, { backgroundColor: colors.primary }]}
            >
              <Text style={{ color: '#FFF', fontWeight: 'bold' }}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Import JSON Modal */}
      <Modal visible={importModalVisible} transparent animationType="slide" onRequestClose={() => setImportModalVisible(false)}>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <Text style={[styles.modalTitle, { color: colors.text }]}>Import Backup (JSON)</Text>
            <Text style={[styles.modalSub, { color: colors.textSecondary }]}>
              Paste your exported JSON backup data below:
            </Text>
            <TextInput
              style={[styles.jsonBox, { borderColor: colors.border, color: colors.text }]}
              value={importJsonText}
              onChangeText={setImportJsonText}
              placeholder="Paste JSON here..."
              placeholderTextColor={colors.textMuted}
              multiline
            />
            <View style={{ flexDirection: 'row', justifyContent: 'flex-end', gap: 10, marginTop: 12 }}>
              <TouchableOpacity
                onPress={() => setImportModalVisible(false)}
                style={[styles.modalBtn, { borderWidth: 1, borderColor: colors.border }]}
              >
                <Text style={{ color: colors.textSecondary }}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity
                onPress={handleImport}
                style={[styles.modalBtn, { backgroundColor: colors.primary }]}
              >
                <Text style={{ color: '#FFF', fontWeight: 'bold' }}>Import Data</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>

      {/* About Modal */}
      <Modal visible={aboutModalVisible} transparent animationType="fade" onRequestClose={() => setAboutModalVisible(false)}>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <Text style={[styles.modalTitle, { color: colors.text }]}>About Smart Attendance</Text>
            <Text style={[styles.aboutText, { color: colors.textSecondary }]}>
              Smart Attendance is a modern, high-speed attendance management system designed for schools, colleges, and institutes.{'\n\n'}
              Features:{'\n'}
              • Student Management with comprehensive profiles{'\n'}
              • Today & Back-Date Attendance Tracking{'\n'}
              • Daily, Monthly, Student-wise, Class-wise Analytics{'\n'}
              • JSON Backup & Restore for easy migration{'\n'}
              • 100% Offline-first local storage
            </Text>
            <TouchableOpacity
              onPress={() => setAboutModalVisible(false)}
              style={[styles.modalCloseBtn, { backgroundColor: colors.primary }]}
            >
              <Text style={{ color: '#FFF', fontWeight: 'bold' }}>Close</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Privacy Policy Modal */}
      <Modal visible={privacyModalVisible} transparent animationType="fade" onRequestClose={() => setPrivacyModalVisible(false)}>
        <View style={styles.modalOverlay}>
          <View style={[styles.modalCard, { backgroundColor: colors.surface }]}>
            <Text style={[styles.modalTitle, { color: colors.text }]}>Privacy Policy</Text>
            <Text style={[styles.aboutText, { color: colors.textSecondary }]}>
              Smart Attendance is built with privacy by design. All student records, phone numbers, attendance logs, and settings are saved strictly on your device using local storage.{'\n\n'}
              No personal data is ever collected, transmitted, or sold to third parties.
            </Text>
            <TouchableOpacity
              onPress={() => setPrivacyModalVisible(false)}
              style={[styles.modalCloseBtn, { backgroundColor: colors.primary }]}
            >
              <Text style={{ color: '#FFF', fontWeight: 'bold' }}>Got It</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>

      {/* Reset Confirmation */}
      <ConfirmationModal
        visible={resetModalVisible}
        title="Reset Sample Data"
        message="This will reset the database with standard demo student profiles and sample attendance records."
        confirmText="Reset"
        colors={colors}
        onConfirm={handleResetSampleConfirm}
        onClose={() => setResetModalVisible(false)}
      />

      {/* Clear Confirmation */}
      <ConfirmationModal
        visible={clearModalVisible}
        title="Clear All Records"
        message="Are you sure you want to delete ALL students and attendance history? This cannot be undone."
        confirmText="Clear All"
        isDestructive
        colors={colors}
        onConfirm={handleClearAllConfirm}
        onClose={() => setClearModalVisible(false)}
      />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 16,
  },
  card: {
    borderRadius: 16,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.04,
    shadowRadius: 4,
    elevation: 1,
  },
  cardHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginBottom: 12,
  },
  cardTitle: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  label: {
    fontSize: 12,
    fontWeight: '600',
    marginTop: 8,
    marginBottom: 4,
  },
  input: {
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 8,
    fontSize: 14,
  },
  row: {
    flexDirection: 'row',
    gap: 10,
  },
  halfCol: {
    flex: 1,
  },
  saveBtn: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 10,
    borderRadius: 10,
    marginTop: 14,
    gap: 6,
  },
  saveBtnText: {
    color: '#FFF',
    fontWeight: 'bold',
    fontSize: 13,
  },
  themeRow: {
    flexDirection: 'row',
    gap: 8,
    marginTop: 6,
    marginBottom: 12,
  },
  themeChip: {
    flex: 1,
    paddingVertical: 8,
    borderRadius: 10,
    alignItems: 'center',
  },
  toggleRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 8,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
  },
  toggleTitle: {
    fontSize: 14,
    fontWeight: '600',
  },
  toggleSub: {
    fontSize: 11,
    marginTop: 2,
  },
  btnGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    marginTop: 4,
  },
  actionGridBtn: {
    width: '48%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 10,
    borderRadius: 10,
    gap: 6,
  },
  actionGridText: {
    fontWeight: 'bold',
    fontSize: 12,
  },
  navItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
  },
  navItemTitle: {
    fontSize: 14,
    fontWeight: '600',
  },
  navItemSub: {
    fontSize: 11,
    marginTop: 2,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalCard: {
    width: '100%',
    maxWidth: 360,
    borderRadius: 20,
    padding: 20,
    elevation: 8,
  },
  modalTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 6,
  },
  modalSub: {
    fontSize: 12,
    marginBottom: 10,
  },
  jsonBox: {
    borderWidth: 1,
    borderRadius: 10,
    padding: 10,
    height: 180,
    fontSize: 12,
    fontFamily: 'monospace',
    textAlignVertical: 'top',
  },
  modalCloseBtn: {
    marginTop: 14,
    paddingVertical: 10,
    borderRadius: 10,
    alignItems: 'center',
  },
  modalBtn: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 10,
  },
  aboutText: {
    fontSize: 13,
    lineHeight: 19,
    marginVertical: 10,
  },
});
