import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useState } from 'react';
import { Alert, StyleSheet, Text, View, Pressable } from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { Header, PrimaryButton, SectionHeading, FormField } from '@/components/UI';
import { useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';

export default function AttendanceScreen() {
  const colors = useColors();
  const { markAttendance, customers } = useMess();
  const { t } = useTranslation();
  
  const [meal, setMeal] = useState<'Breakfast' | 'Lunch' | 'Dinner'>('Lunch');
  const [method] = useState('Manual Entry');
  const [marked, setMarked] = useState(false);

  const activeCustomers = customers.filter(c => c.paymentStatus === 'Paid');

  const add = () => {
    markAttendance(meal, true, method);
    setMarked(true);
    Alert.alert(t('success'), t('attendanceLogged'));
  };

  return (
    <KeyboardAwareScrollViewCompat style={[styles.screen, { backgroundColor: colors.background }]} contentContainerStyle={styles.content}>
      <Header eyebrow={t('ownerTools')} title={t('attendance')} subtitle={t('logTodayMeals')} onPress={() => router.back()} />
      <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
        <SectionHeading title={t('selectMeal')} />
        <View style={styles.chips}>
          {(['Breakfast', 'Lunch', 'Dinner'] as const).map(m => (
            <Pressable
              key={m}
              style={[
                styles.chip,
                { backgroundColor: meal === m ? colors.primary : colors.muted }
              ]}
              onPress={() => setMeal(m)}
            >
              <Text style={{ color: meal === m ? colors.primaryForeground : colors.foreground, fontWeight: '600' }}>
                {t(m.toLowerCase()) || m}
              </Text>
            </Pressable>
          ))}
        </View>
        
        <PrimaryButton label={t('markAttendance')} icon="check-circle" onPress={add} />
      </View>
    </KeyboardAwareScrollViewCompat>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 54, paddingBottom: 40, gap: 18 },
  card: { borderWidth: 1, borderRadius: 16, padding: 16, gap: 16 },
  chips: { flexDirection: 'row', gap: 8, flexWrap: 'wrap' },
  chip: { paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20 },
});
