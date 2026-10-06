import { todayISO } from '@/lib/dates';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { Alert, StyleSheet, Text, View } from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { FormField, Header, PrimaryButton, SectionHeading } from '@/components/UI';
import { useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { localizedValue } from '@/lib/i18n';
import { useState } from 'react';

export default function PaymentsScreen() {
  const colors = useColors();
  const { payments, addPayment } = useMess();
  const { language, t } = useTranslation();
  const [amount, setAmount] = useState('');
  const [method, setMethod] = useState('UPI');
  const [note, setNote] = useState('');
  const add = () => { if (!amount || !note) { Alert.alert(t('completePayment'), t('paymentRequiredMessage')); return; } 
  const val = Number(amount);
  if (isNaN(val) || val <= 0) { Alert.alert(t('error'), t('invalidAmount')); return; }
  addPayment({ amount: val, method, note, date: todayISO() }); setAmount(''); setNote(''); Alert.alert(t('paymentRecorded'), t('paymentRecordedMessage')); };
  return <KeyboardAwareScrollViewCompat style={[styles.screen, { backgroundColor: colors.background }]} contentContainerStyle={styles.content} bottomOffset={24} keyboardShouldPersistTaps="handled"><Header eyebrow={t('ownerTools')} title={t('paymentLedger')} subtitle={t('paymentSubtitle')} onPress={() => router.push('/(tabs)/profile')} /><View style={[styles.activeCard, { backgroundColor: colors.primary }]}><Ionicons name="checkmark-circle-outline" size={30} color={colors.accent} /><View><Text style={[styles.activeTitle, { color: colors.primaryForeground }]}>{t('ledgerUpToDate')}</Text><Text style={[styles.activeDetail, { color: colors.primaryForeground }]}>{t('recordsStayOnDevice')}</Text></View></View><SectionHeading title={t('pastPayments')} /><View style={[styles.list, { backgroundColor: colors.card, borderColor: colors.border }]}>{payments.map((payment, index) => <View key={payment.id} style={[styles.paymentRow, index < payments.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.border }]}><View style={[styles.paymentIcon, { backgroundColor: colors.secondary }]}><Ionicons name="receipt-outline" size={18} color={colors.primary} /></View><View style={styles.paymentCopy}><Text style={[styles.paymentNote, { color: colors.foreground }]}>{payment.note}</Text><Text style={[styles.paymentMeta, { color: colors.mutedForeground }]}>{payment.date} · {localizedValue(language, payment.method)}</Text></View><Text style={[styles.amount, { color: colors.foreground }]}>₹{payment.amount.toLocaleString()}</Text></View>)}</View><SectionHeading title={t('recordPayment')} /><View style={[styles.formCard, { backgroundColor: colors.card, borderColor: colors.border }]}><FormField label={t('amount')} value={amount} onChangeText={setAmount} placeholder={t('enterPaymentAmount')} keyboardType="numeric" /><FormField label={t('method')} value={method} onChangeText={setMethod} placeholder={t('enterPaymentMethod')} /><FormField label={t('note')} value={note} onChangeText={setNote} placeholder={t('enterPaymentNote')} /><PrimaryButton label={t('recordPaymentButton')} icon="check" onPress={add} /></View></KeyboardAwareScrollViewCompat>;
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 54, paddingBottom: 40, gap: 18 },
  activeCard: { borderRadius: 21, padding: 18, flexDirection: 'row', alignItems: 'center', gap: 12 },
  activeTitle: { fontSize: 16, fontWeight: '700' },
  activeDetail: { fontSize: 11, opacity: 0.76, marginTop: 4 },
  list: { borderWidth: 1, borderRadius: 19, paddingHorizontal: 14 },
  paymentRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 14 },
  paymentIcon: { width: 36, height: 36, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  paymentCopy: { flex: 1, gap: 3 },
  paymentNote: { fontSize: 14, fontWeight: '700' },
  paymentMeta: { fontSize: 11 },
  amount: { fontSize: 13, fontWeight: '700' },
  formCard: { borderWidth: 1, borderRadius: 20, padding: 16, gap: 15 },
});