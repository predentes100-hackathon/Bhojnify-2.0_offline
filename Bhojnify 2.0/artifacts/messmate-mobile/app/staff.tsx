import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { Alert, StyleSheet, Text, View } from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { FormField, Header, PrimaryButton, SectionHeading } from '@/components/UI';
import { useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { useState } from 'react';

export default function StaffScreen() {
  const colors = useColors();
  const { staff, addStaff } = useMess();
  const { t } = useTranslation();
  const [name, setName] = useState('');
  const [role, setRole] = useState('');
  const [phone, setPhone] = useState('');
  const [salary, setSalary] = useState('');
  const add = () => { if (!name || !role || !salary) { Alert.alert(t('completeStaffProfile'), t('staffRequiredMessage')); return; } addStaff({ name, role, phone: phone || '', salary: Number(salary) }); setName(''); setRole(''); setPhone(''); setSalary(''); };
  return <KeyboardAwareScrollViewCompat style={[styles.screen, { backgroundColor: colors.background }]} contentContainerStyle={styles.content} bottomOffset={24} keyboardShouldPersistTaps="handled"><Header eyebrow={t('ownerTools')} title={t('staffRoster')} subtitle={t('staffSubtitle')} onPress={() => router.push('/(tabs)/profile')} /><View style={styles.peopleRow}><View style={[styles.peopleCard, { backgroundColor: colors.primary }]}><Ionicons name="people-outline" size={24} color={colors.accent} /><Text style={[styles.peopleValue, { color: colors.primaryForeground }]}>{staff.length}</Text><Text style={[styles.peopleLabel, { color: colors.primaryForeground }]}>{t('teamMembers')}</Text></View><View style={[styles.peopleCard, { backgroundColor: colors.accent }]}><Ionicons name="cash-outline" size={24} color={colors.accentForeground} /><Text style={[styles.peopleValue, { color: colors.accentForeground }]}>₹{(staff.reduce((sum, item) => sum + item.salary, 0) / 1000).toFixed(1)}k</Text><Text style={[styles.peopleLabel, { color: colors.accentForeground }]}>{t('monthlyPayroll')}</Text></View></View><SectionHeading title={t('currentTeam')} /><View style={[styles.list, { backgroundColor: colors.card, borderColor: colors.border }]}>{staff.map((member, index) => <View key={member.id} style={[styles.staffRow, index < staff.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.border }]}><View style={[styles.avatar, { backgroundColor: colors.secondary }]}><Text style={[styles.avatarText, { color: colors.primary }]}>{member.name.split(' ').map((word) => word[0]).join('').slice(0, 2)}</Text></View><View style={styles.staffCopy}><Text style={[styles.staffName, { color: colors.foreground }]}>{member.name}</Text><Text style={[styles.staffMeta, { color: colors.mutedForeground }]}>{member.role} · {member.phone || t('notAdded')}</Text></View><Text style={[styles.salary, { color: colors.foreground }]}>₹{member.salary.toLocaleString()}</Text></View>)}</View><SectionHeading title={t('addTeamMember')} /><View style={[styles.formCard, { backgroundColor: colors.card, borderColor: colors.border }]}><FormField label={t('fullName')} value={name} onChangeText={setName} placeholder={t('enterFullName')} /><FormField label={t('role')} value={role} onChangeText={setRole} placeholder={t('enterRole')} /><FormField label={t('phoneNumber')} value={phone} onChangeText={setPhone} placeholder={t('enterPhoneNumber')} /><FormField label={t('monthlySalary')} value={salary} onChangeText={setSalary} placeholder={t('enterMonthlySalary')} keyboardType="numeric" /><PrimaryButton label={t('addToRoster')} icon="user-plus" onPress={add} /></View></KeyboardAwareScrollViewCompat>;
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 54, paddingBottom: 40, gap: 18 },
  peopleRow: { flexDirection: 'row', gap: 10 },
  peopleCard: { flex: 1, borderRadius: 18, padding: 15, gap: 5 },
  peopleValue: { fontSize: 23, fontWeight: '700', marginTop: 5 },
  peopleLabel: { fontSize: 11, fontWeight: '600', opacity: 0.76 },
  list: { borderWidth: 1, borderRadius: 19, paddingHorizontal: 14 },
  staffRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 14 },
  avatar: { width: 40, height: 40, borderRadius: 14, alignItems: 'center', justifyContent: 'center' },
  avatarText: { fontSize: 13, fontWeight: '700' },
  staffCopy: { flex: 1, gap: 3 },
  staffName: { fontSize: 14, fontWeight: '700' },
  staffMeta: { fontSize: 10 },
  salary: { fontSize: 12, fontWeight: '700' },
  formCard: { borderWidth: 1, borderRadius: 20, padding: 16, gap: 15 },
});