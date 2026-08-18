import React from 'react';
import {
  View,
  Text,
  Modal,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
  Linking,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';

export default function StudentProfileModal({
  visible,
  student,
  attendanceLogs = [],
  colors,
  onClose,
  onEdit,
}) {
  if (!student) return null;

  const totalLogs = attendanceLogs.length;
  const presentCount = attendanceLogs.filter(a => a.status === 'PRESENT').length;
  const absentCount = attendanceLogs.filter(a => a.status === 'ABSENT').length;
  const leaveCount = attendanceLogs.filter(a => a.status === 'LEAVE').length;
  const percentage = totalLogs > 0 ? ((presentCount / totalLogs) * 100).toFixed(1) : 0;

  const handleCall = () => {
    if (student.mobileNumber) {
      Linking.openURL(`tel:${student.mobileNumber}`);
    }
  };

  return (
    <Modal visible={visible} transparent animationType="slide" onRequestClose={onClose}>
      <View style={styles.overlay}>
        <View style={[styles.container, { backgroundColor: colors.surface }]}>
          {/* Header */}
          <View style={styles.header}>
            <Text style={[styles.title, { color: colors.text }]}>Student Profile</Text>
            <View style={{ flexDirection: 'row', gap: 8 }}>
              <TouchableOpacity onPress={() => { onClose(); onEdit(student); }} style={styles.iconBtn}>
                <Ionicons name="create-outline" size={20} color={colors.primary} />
              </TouchableOpacity>
              <TouchableOpacity onPress={onClose} style={styles.iconBtn}>
                <Ionicons name="close" size={22} color={colors.textSecondary} />
              </TouchableOpacity>
            </View>
          </View>

          <ScrollView style={styles.body} showsVerticalScrollIndicator={false}>
            {/* Avatar & Core Info Card */}
            <View style={[styles.heroCard, { backgroundColor: colors.primaryContainer }]}>
              <View style={[styles.avatar, { backgroundColor: student.avatarColor || colors.primary }]}>
                <Text style={styles.avatarText}>{student.name ? student.name[0].toUpperCase() : 'S'}</Text>
              </View>
              <Text style={[styles.heroName, { color: colors.text }]}>{student.name}</Text>
              <Text style={[styles.heroSub, { color: colors.textSecondary }]}>
                Roll No: {student.rollNumber}  •  {student.studentClass} - Sec {student.section}
              </Text>
              <View style={[styles.statusPill, { backgroundColor: student.status === 'Active' ? colors.presentBg : colors.absentBg }]}>
                <Text style={{ color: student.status === 'Active' ? colors.present : colors.absent, fontSize: 11, fontWeight: 'bold' }}>
                  {student.status || 'Active'}
                </Text>
              </View>
            </View>

            {/* Attendance Stat Summary */}
            <View style={[styles.statRow, { backgroundColor: colors.surfaceVariant }]}>
              <View style={styles.statBox}>
                <Text style={[styles.statVal, { color: colors.primary }]}>{percentage}%</Text>
                <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Attendance</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statBox}>
                <Text style={[styles.statVal, { color: colors.present }]}>{presentCount}</Text>
                <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Present</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statBox}>
                <Text style={[styles.statVal, { color: colors.absent }]}>{absentCount}</Text>
                <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Absent</Text>
              </View>
              <View style={styles.statDivider} />
              <View style={styles.statBox}>
                <Text style={[styles.statVal, { color: colors.leave }]}>{leaveCount}</Text>
                <Text style={[styles.statLbl, { color: colors.textSecondary }]}>Leave</Text>
              </View>
            </View>

            {/* Detail Fields */}
            <Text style={[styles.sectionTitle, { color: colors.text }]}>Personal & Contact Details</Text>
            
            <View style={[styles.detailCard, { borderColor: colors.border }]}>
              <DetailRow label="Admission No" value={student.admissionNumber || '-'} colors={colors} />
              <DetailRow label="Father's Name" value={student.fatherName || '-'} colors={colors} />
              <DetailRow label="Mother's Name" value={student.motherName || '-'} colors={colors} />
              <DetailRow label="Date of Birth" value={student.dateOfBirth || '-'} colors={colors} />
              
              <View style={styles.detailRowBetween}>
                <View>
                  <Text style={[styles.detailLabel, { color: colors.textSecondary }]}>Mobile Number</Text>
                  <Text style={[styles.detailValue, { color: colors.text }]}>{student.mobileNumber || '-'}</Text>
                </View>
                {student.mobileNumber ? (
                  <TouchableOpacity onPress={handleCall} style={[styles.callBtn, { backgroundColor: colors.presentBg }]}>
                    <Ionicons name="call" size={16} color={colors.present} />
                    <Text style={{ color: colors.present, fontWeight: 'bold', fontSize: 12, marginLeft: 4 }}>Call</Text>
                  </TouchableOpacity>
                ) : null}
              </View>

              <DetailRow label="Address" value={student.address || '-'} colors={colors} isLast />
            </View>

            {/* Recent Attendance Logs */}
            <Text style={[styles.sectionTitle, { color: colors.text }]}>Recent Attendance Records ({totalLogs})</Text>
            {attendanceLogs.length === 0 ? (
              <Text style={{ color: colors.textMuted, fontSize: 13, marginVertical: 8 }}>No attendance recorded yet</Text>
            ) : (
              attendanceLogs.slice(0, 10).map((log, index) => (
                <View key={index} style={[styles.logRow, { borderBottomColor: colors.border }]}>
                  <Text style={{ color: colors.text, fontWeight: '600', fontSize: 13 }}>{log.date}</Text>
                  <View style={[
                    styles.logBadge,
                    { backgroundColor: log.status === 'PRESENT' ? colors.presentBg : log.status === 'ABSENT' ? colors.absentBg : colors.leaveBg }
                  ]}>
                    <Text style={{
                      color: log.status === 'PRESENT' ? colors.present : log.status === 'ABSENT' ? colors.absent : colors.leave,
                      fontSize: 11,
                      fontWeight: 'bold',
                    }}>
                      {log.status}
                    </Text>
                  </View>
                </View>
              ))
            )}

            <View style={{ height: 20 }} />
          </ScrollView>
        </View>
      </View>
    </Modal>
  );
}

function DetailRow({ label, value, colors, isLast = false }) {
  return (
    <View style={[styles.detailRow, !isLast && { borderBottomWidth: 1, borderBottomColor: colors.border }]}>
      <Text style={[styles.detailLabel, { color: colors.textSecondary }]}>{label}</Text>
      <Text style={[styles.detailValue, { color: colors.text }]}>{value}</Text>
    </View>
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
  iconBtn: {
    padding: 6,
  },
  body: {
    maxHeight: 500,
  },
  heroCard: {
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
    marginBottom: 12,
  },
  avatar: {
    width: 60,
    height: 60,
    borderRadius: 30,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  avatarText: {
    color: '#FFF',
    fontSize: 24,
    fontWeight: 'bold',
  },
  heroName: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  heroSub: {
    fontSize: 13,
    marginTop: 2,
  },
  statusPill: {
    paddingHorizontal: 10,
    paddingVertical: 3,
    borderRadius: 12,
    marginTop: 6,
  },
  statRow: {
    flexDirection: 'row',
    borderRadius: 14,
    paddingVertical: 10,
    justifyContent: 'space-around',
    alignItems: 'center',
    marginBottom: 16,
  },
  statBox: {
    alignItems: 'center',
  },
  statVal: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  statLbl: {
    fontSize: 11,
    marginTop: 2,
  },
  statDivider: {
    width: 1,
    height: 24,
    backgroundColor: '#CBD5E1',
  },
  sectionTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    marginVertical: 8,
  },
  detailCard: {
    borderWidth: 1,
    borderRadius: 14,
    paddingHorizontal: 12,
    marginBottom: 12,
  },
  detailRow: {
    paddingVertical: 8,
  },
  detailRowBetween: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
  },
  detailLabel: {
    fontSize: 11,
  },
  detailValue: {
    fontSize: 14,
    fontWeight: '600',
    marginTop: 1,
  },
  callBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 12,
  },
  logRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
  },
  logBadge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 6,
  },
});
