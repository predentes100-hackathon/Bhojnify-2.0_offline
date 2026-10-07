import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { StyleSheet, Text, View, DimensionValue } from 'react-native';
import { Badge, Header, PrimaryButton, Screen, SectionHeading } from '@/components/UI';
import { getExpiryInfo, useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';

export default function ReportScreen() {
  const colors = useColors();
  const { payments, expenses, inventory, attendance, customers } = useMess();
  const { t } = useTranslation();
  const revenue = payments.reduce((sum, item) => sum + item.amount, 0);
  const costs = expenses.reduce((sum, item) => sum + item.amount, 0);
  const margin = revenue - costs;
  const stockValue = inventory.reduce((sum, item) => sum + item.quantity * 100, 0);

  const lowStock = inventory.filter((item) => item.quantity <= item.minimum);

  // Compute real metrics
  const activeCount = customers.filter(c => getExpiryInfo(c.expiryDate).daysRemaining > 0).length;
  const renewalRate = customers.length ? Math.round((activeCount / customers.length) * 100) : 0;
  
  // Progress ratios
  const renewalWidth = `${Math.min(100, renewalRate)}%` as DimensionValue;
  const mealsWidth = `${Math.min(100, attendance.length > 0 ? (attendance.length / 50) * 100 : 0)}%` as DimensionValue; // 50 cap benchmark
  const stockWidth = `${Math.min(100, stockValue > 0 ? (lowStock.length / inventory.length) * 100 : 0)}%` as DimensionValue; // Not perfect, wait

  return <Screen><Header eyebrow={t('ownerTools')} title={t('monthlyReport')} subtitle={t('reportSubtitle')} onPress={() => router.push('/(tabs)/profile')} /><View style={[styles.profitCard, { backgroundColor: colors.primary }]}><View><Text style={[styles.profitLabel, { color: colors.primaryForeground }]}>{t('operatingSurplus').toUpperCase()}</Text><Text style={[styles.profitValue, { color: colors.primaryForeground }]}>₹{margin.toLocaleString()}</Text><Text style={[styles.profitDetail, { color: colors.primaryForeground }]}>{t('revenueLessExpenses')}</Text></View><Ionicons name="trending-up-outline" size={34} color={colors.accent} /></View><View style={styles.statsRow}><View style={[styles.smallStat, { backgroundColor: colors.card, borderColor: colors.border }]}><Text style={[styles.smallLabel, { color: colors.mutedForeground }]}>{t('revenue').toUpperCase()}</Text><Text style={[styles.smallValue, { color: colors.foreground }]}>₹{revenue.toLocaleString()}</Text></View><View style={[styles.smallStat, { backgroundColor: colors.card, borderColor: colors.border }]}><Text style={[styles.smallLabel, { color: colors.mutedForeground }]}>{t('expenses').toUpperCase()}</Text><Text style={[styles.smallValue, { color: colors.foreground }]}>₹{costs.toLocaleString()}</Text><Badge label={t('tracked')} tone="gray" /></View></View><PrimaryButton label={t('manageExpenses')} icon="edit-3" secondary onPress={() => router.push('/expenses')} /></Screen>;
}

const styles = StyleSheet.create({
  profitCard: { borderRadius: 24, padding: 20, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14 },
  profitLabel: { fontSize: 10, fontWeight: '700', letterSpacing: 1.2, opacity: 0.9, marginBottom: 4 },
  profitValue: { fontSize: 32, fontWeight: '700', marginBottom: 2 },
  profitDetail: { fontSize: 13, opacity: 0.9 },
  statsRow: { flexDirection: 'row', gap: 10, marginBottom: 20 },
  smallStat: { flex: 1, borderWidth: 1, borderRadius: 16, padding: 14 },
  smallLabel: { fontSize: 10, fontWeight: '700', marginBottom: 4 },
  smallValue: { fontSize: 22, fontWeight: '700', marginBottom: 8 },
  healthCard: { borderWidth: 1, borderRadius: 16, paddingHorizontal: 16, paddingBottom: 16, marginBottom: 14 },
  healthRow: { flexDirection: 'row', justifyContent: 'space-between', marginTop: 16, marginBottom: 8 },
  healthLabel: { fontSize: 14, fontWeight: '600' },
  healthValue: { fontSize: 14, fontWeight: '700' },
  progressTrack: { height: 6, borderRadius: 3, overflow: 'hidden' },
  progressFill: { height: '100%', borderRadius: 3 },
});
