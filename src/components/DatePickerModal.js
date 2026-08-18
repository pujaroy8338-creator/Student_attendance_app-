import React, { useState } from 'react';
import {
  View,
  Text,
  Modal,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { getTodayDateString } from '../services/storage';

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

export default function DatePickerModal({
  visible,
  currentDate,
  allowFutureDates = false,
  colors,
  onSelectDate,
  onClose,
}) {
  const todayStr = getTodayDateString();
  const [currentYear, setCurrentYear] = useState(() => {
    return currentDate ? parseInt(currentDate.split('-')[0]) : new Date().getFullYear();
  });
  const [currentMonth, setCurrentMonth] = useState(() => {
    return currentDate ? parseInt(currentDate.split('-')[1]) - 1 : new Date().getMonth();
  });

  // Calculate days in month
  const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
  const firstDayIndex = new Date(currentYear, currentMonth, 1).getDay(); // 0 = Sunday

  const handlePrevMonth = () => {
    if (currentMonth === 0) {
      setCurrentMonth(11);
      setCurrentYear(currentYear - 1);
    } else {
      setCurrentMonth(currentMonth - 1);
    }
  };

  const handleNextMonth = () => {
    const isCurrentOrFuture = (currentYear > new Date().getFullYear()) ||
      (currentYear === new Date().getFullYear() && currentMonth >= new Date().getMonth());

    if (!allowFutureDates && isCurrentOrFuture) {
      return;
    }

    if (currentMonth === 11) {
      setCurrentMonth(0);
      setCurrentYear(currentYear + 1);
    } else {
      setCurrentMonth(currentMonth + 1);
    }
  };

  const handleSelectDay = (day) => {
    const formattedMonth = String(currentMonth + 1).padStart(2, '0');
    const formattedDay = String(day).padStart(2, '0');
    const selected = `${currentYear}-${formattedMonth}-${formattedDay}`;

    if (!allowFutureDates && selected > todayStr) {
      return;
    }

    onSelectDate(selected);
    onClose();
  };

  // Generate calendar grid
  const days = [];
  for (let i = 0; i < firstDayIndex; i++) {
    days.push(null);
  }
  for (let d = 1; d <= daysInMonth; d++) {
    days.push(d);
  }

  return (
    <Modal
      visible={visible}
      transparent
      animationType="fade"
      onRequestClose={onClose}
    >
      <View style={styles.overlay}>
        <View style={[styles.container, { backgroundColor: colors.surface }]}>
          {/* Header */}
          <View style={styles.header}>
            <TouchableOpacity onPress={handlePrevMonth} style={styles.navButton}>
              <Ionicons name="chevron-back" size={22} color={colors.primary} />
            </TouchableOpacity>

            <View style={styles.titleContainer}>
              <Text style={[styles.headerTitle, { color: colors.text }]}>
                {MONTH_NAMES[currentMonth]} {currentYear}
              </Text>
              <Text style={[styles.headerSubtitle, { color: colors.textSecondary }]}>
                Select past date for attendance
              </Text>
            </View>

            <TouchableOpacity onPress={handleNextMonth} style={styles.navButton}>
              <Ionicons name="chevron-forward" size={22} color={colors.primary} />
            </TouchableOpacity>
          </View>

          {/* Day of Week Headers */}
          <View style={styles.weekHeader}>
            {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map((w, idx) => (
              <Text key={idx} style={[styles.weekDayText, { color: colors.textMuted }]}>
                {w}
              </Text>
            ))}
          </View>

          {/* Days Grid */}
          <View style={styles.grid}>
            {days.map((day, idx) => {
              if (day === null) {
                return <View key={idx} style={styles.dayCell} />;
              }

              const formattedMonth = String(currentMonth + 1).padStart(2, '0');
              const formattedDay = String(day).padStart(2, '0');
              const dateStr = `${currentYear}-${formattedMonth}-${formattedDay}`;

              const isSelected = dateStr === currentDate;
              const isToday = dateStr === todayStr;
              const isFuture = dateStr > todayStr;
              const isDisabled = !allowFutureDates && isFuture;

              return (
                <TouchableOpacity
                  key={idx}
                  disabled={isDisabled}
                  onPress={() => handleSelectDay(day)}
                  style={[
                    styles.dayCell,
                    isSelected && { backgroundColor: colors.primary, borderRadius: 20 },
                    isToday && !isSelected && { borderColor: colors.primary, borderWidth: 1.5, borderRadius: 20 },
                    isDisabled && { opacity: 0.25 },
                  ]}
                >
                  <Text
                    style={[
                      styles.dayText,
                      { color: colors.text },
                      isSelected && { color: '#FFFFFF', fontWeight: 'bold' },
                      isToday && !isSelected && { color: colors.primary, fontWeight: 'bold' },
                    ]}
                  >
                    {day}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Footer Quick Selectors */}
          <View style={styles.footer}>
            <TouchableOpacity
              onPress={() => {
                onSelectDate(todayStr);
                onClose();
              }}
              style={[styles.quickButton, { backgroundColor: colors.primaryContainer }]}
            >
              <Text style={[styles.quickButtonText, { color: colors.primary }]}>
                Today ({todayStr.slice(8, 10)}/{todayStr.slice(5, 7)})
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              onPress={() => {
                const d = new Date();
                d.setDate(d.getDate() - 1);
                const yDate = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
                onSelectDate(yDate);
                onClose();
              }}
              style={[styles.quickButton, { backgroundColor: colors.surfaceVariant }]}
            >
              <Text style={[styles.quickButtonText, { color: colors.textSecondary }]}>
                Yesterday
              </Text>
            </TouchableOpacity>

            <TouchableOpacity onPress={onClose} style={styles.cancelButton}>
              <Text style={[styles.cancelText, { color: colors.textMuted }]}>Cancel</Text>
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
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  container: {
    width: '100%',
    maxWidth: 360,
    borderRadius: 20,
    padding: 18,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 10,
    elevation: 8,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 12,
  },
  navButton: {
    padding: 8,
  },
  titleContainer: {
    alignItems: 'center',
  },
  headerTitle: {
    fontSize: 17,
    fontWeight: 'bold',
  },
  headerSubtitle: {
    fontSize: 11,
    marginTop: 2,
  },
  weekHeader: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#E2E8F0',
    paddingBottom: 6,
  },
  weekDayText: {
    width: 36,
    textAlign: 'center',
    fontWeight: '600',
    fontSize: 12,
  },
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'flex-start',
  },
  dayCell: {
    width: `${100 / 7}%`,
    height: 38,
    justifyContent: 'center',
    alignItems: 'center',
    marginVertical: 2,
  },
  dayText: {
    fontSize: 14,
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 16,
    paddingTop: 12,
    borderTopWidth: 1,
    borderTopColor: '#E2E8F0',
  },
  quickButton: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 14,
  },
  quickButtonText: {
    fontSize: 12,
    fontWeight: '600',
  },
  cancelButton: {
    padding: 8,
  },
  cancelText: {
    fontSize: 13,
  },
});
