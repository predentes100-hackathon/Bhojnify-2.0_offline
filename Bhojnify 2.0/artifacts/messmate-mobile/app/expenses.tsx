import { todayISO } from '@/lib/dates';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { Alert, StyleSheet, Text, View, ScrollView, Pressable } from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { FormField, Header, PrimaryButton, SectionHeading } from '@/components/UI';
import { useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { localizedValue } from '@/lib/i18n';
import { useState } from 'react';

export default function ExpensesScreen() {
  const colors = useColors();
  const { expenses, addExpense } = useMess();
  const { language, t } = useTranslation();
  const [category, setCategory] = useState('Groceries');
  const [amount, setAmount] = useState('');
  const [note, setNote] = useState('');
  const add = () => { if (!amount || !note) { Alert.alert(t('completeExpense'), t('expenseRequiredMessage')); return; } addExpense({ category, amount: Number(amount), note, date: todayISO() }); setAmount(''); setNote(''); };
  return <KeyboardAwareScrollViewCompat style={[styles.screen, { backgroundColor: colors.background }]} contentContainerStyle={styles.content} bottomOffset={24} keyboardShouldPersistTaps="handled"><Header eyebrow={t('ownerTools')} title={t('expensesTitle')} subtitle={t('expensesSubtitle')} onPress={() => router.push('/(tabs)/profile')} /><View style={[styles.totalCard, { backgroundColor: colors.foreground }]}><View><Text style={[styles.totalLabel, { color: colors.card }]}>{t('monthlySpend').toUpperCase()}</Text><Text style={[styles.totalValue, { color: colors.card }]}>₹{expenses.filter(e => e.date.startsWith(todayISO().slice(0, 7))).reduce((sum, item) => sum + item.amount, 0).toLocaleString()}</Text></View><Ionicons name="trending-down-outline" size={30} color={colors.accent} /></View><SectionHeading title={t('recentExpenses')} /><View style={[styles.list, { backgroundColor: colors.card, borderColor: colors.border }]}>{expenses.map((expense, index) => <View key={expense.id} style={[styles.expenseRow, index < expenses.length - 1 && { borderBottomWidth: 1, borderBottomColor: colors.border }]}><View style={[styles.expenseIcon, { backgroundColor: colors.secondary }]}><Ionicons name={expense.category === 'Gas' ? 'flame-outline' : expense.category === 'Maintenance' ? 'construct-outline' : 'cart-outline'} size={18} color={colors.primary} /></View><View style={styles.expenseCopy}><Text style={[styles.expenseTitle, { color: colors.foreground }]}>{localizedValue(language, expense.category)}</Text><Text style={[styles.expenseNote, { color: colors.mutedForeground }]}>{expense.note}</Text></View><Text style={[styles.expenseAmount, { color: colors.foreground }]}>₹{expense.amount.toLocaleString()}</Text></View>)}</View><SectionHeading title={t('logExpense')} /><View style={[styles.formCard, { backgroundColor: colors.card, borderColor: colors.border }]}><View style={{ gap: 8 }}>  <Text style={{ fontSize: 13, fontWeight: '600', color: colors.foreground }}>{t('category')}</Text>  <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8 }}>    {['Groceries', 'Vegetables', 'Gas', 'Staff', 'Rent/Maintenance', 'Other'].map(cat => (      <Pressable key={cat} onPress={() => setCategory(cat)} style={{ paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20, backgroundColor: category === cat ? colors.primary : colors.muted }}>        <Text style={{ color: category === cat ? colors.primaryForeground : colors.foreground, fontWeight: category === cat ? '700' : '500' }}>{t(cat) || cat}</Text>      </Pressable>    ))}  </ScrollView></View><FormField label={t('amount')} value={amount} onChangeText={setAmount} placeholder={t('enterExpenseAmount')} keyboardType="numeric" /><FormField label={t('note')} value={note} onChangeText={setNote} placeholder={t('enterExpenseNote')} /><PrimaryButton label={t('saveExpense')} icon="plus" onPress={add} /></View></KeyboardAwareScrollViewCompat>;
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 54, paddingBottom: 40, gap: 18 },
  totalCard: { borderRadius: 21, padding: 20, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  totalLabel: { fontSize: 10, letterSpacing: 1.3, fontWeight: '700', opacity: 0.68 },
  totalValue: { fontSize: 30, fontWeight: '700', marginTop: 5 },
  list: { borderWidth: 1, borderRadius: 19, paddingHorizontal: 14 },
  expenseRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 13 },
  expenseIcon: { width: 36, height: 36, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  expenseCopy: { flex: 1, gap: 3 },
  expenseTitle: { fontSize: 14, fontWeight: '700' },
  expenseNote: { fontSize: 11 },
  expenseAmount: { fontSize: 13, fontWeight: '700' },
  formCard: { borderWidth: 1, borderRadius: 20, padding: 16, gap: 15 },
});