import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Badge, Header, IconTile, RowItem, Screen, SectionHeading, StatCard } from '@/components/UI';
import { formatDate, useMess } from '@/context/AppContext';
import { localizedValue } from '@/lib/i18n';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';

export default function AdminScreen() {
  const colors = useColors();
  const { profile, inventory, attendance, customers, leaves, payments, expenses, feedback, updateLeaveStatus } = useMess();
  const { language, t } = useTranslation();
  const pendingLeaves = leaves.filter((leave) => leave.status === 'Pending');
  const lowStock = inventory.filter((item) => item.quantity <= item.minimum);
  const revenue = payments.reduce((sum, payment) => sum + payment.amount, 0);
  const spend = expenses.reduce((sum, expense) => sum + expense.amount, 0);
  return <Screen><Header eyebrow={t('controlRoom')} title={t('operationsPanel')} subtitle={t('importantThings')} onPress={() => router.push('/(tabs)/profile')} /><View style={[styles.hero, { backgroundColor: colors.foreground }]}><View style={styles.heroCopy}><Text style={[styles.heroKicker, { color: colors.accent }]}>{profile.messName || t('yourMess').toUpperCase()}</Text><Text style={[styles.heroTitle, { color: colors.card }]}>{t('calmerKitchen')}</Text><Text style={[styles.heroDetail, { color: colors.card }]}>{t('monitorService')}</Text></View><Ionicons name="grid-outline" size={42} color={colors.accent} /></View><View style={styles.statsRow}><StatCard label={t('attendance').toUpperCase()} value={`${attendance.length}`} detail={t('todayRecords')} /><StatCard label={t('pending').toUpperCase()} value={`${pendingLeaves.length}`} detail={t('leaveApprovals')} tone="amber" /></View><View style={styles.statsRow}><StatCard label={t('lowStock').toUpperCase()} value={`${lowStock.length}`} detail={t('itemsToReorder')} tone="red" /><StatCard label={t('surplus').toUpperCase()} value={`₹${((revenue - spend) / 1000).toFixed(1)}k`} detail={t('thisMonth')} /></View><SectionHeading title={t('manageOperations')} /><View style={styles.actionGrid}><IconTile icon="cube-outline" label={t('inventory')} onPress={() => router.push('/inventory')} /><IconTile icon="restaurant-outline" label={t('menu')} onPress={() => router.push('/menu')} /><IconTile icon="people-outline" label={t('staff')} onPress={() => router.push('/staff')} /><IconTile icon="cash-outline" label={t('expenses')} onPress={() => router.push('/expenses')} /></View><View style={styles.actionGrid}><IconTile icon="people-circle-outline" label={t('customers')} onPress={() => router.push('/customers')} /><IconTile icon="bar-chart-outline" label={t('reports')} onPress={() => router.push('/report')} /><IconTile icon="card-outline" label={t('payments')} onPress={() => router.push('/payments')} /><IconTile icon="calendar-outline" label={t('leaveLog')} onPress={() => router.push('/leave')} /></View><SectionHeading title={t('leaveApprovals')} action={pendingLeaves.length ? t('waiting', { count: pendingLeaves.length }) : undefined} /><View style={[styles.approvalCard, { backgroundColor: colors.card, borderColor: colors.border }]}>{pendingLeaves.length ? pendingLeaves.slice(0, 3).map((leave) => <View key={leave.id} style={styles.approvalRow}><View style={[styles.approvalIcon, { backgroundColor: colors.secondary }]}><Ionicons name="person-outline" size={17} color={colors.primary} /></View><View style={styles.approvalCopy}><Text style={[styles.approvalTitle, { color: colors.foreground }]}>{customers.find((customer) => customer.id === leave.customerId)?.name ?? t('memberLeave')}</Text><Text style={[styles.approvalDetail, { color: colors.mutedForeground }]}>{formatDate(leave.from, language)} – {leave.to ? formatDate(leave.to, language) : t('returnPending')}{leave.reason ? ` · ${leave.reason}` : ''}</Text></View><View style={styles.approvalActions}><Pressable testID={`approve-${leave.id}`} onPress={() => updateLeaveStatus(leave.id, 'Approved')} style={[styles.smallButton, { backgroundColor: colors.secondary }]}><Ionicons name="checkmark" size={17} color={colors.primary} /></Pressable><Pressable testID={`decline-${leave.id}`} onPress={() => updateLeaveStatus(leave.id, 'Declined')} style={[styles.smallButton, { backgroundColor: colors.muted }]}><Ionicons name="close" size={17} color={colors.destructive} /></Pressable></View></View>) : <RowItem icon="check-circle" title={t('nothingWaiting')} detail={t('caughtUpLeave')} right={<Badge label={t('clear')} />} />}</View><SectionHeading title={t('latestFeedback')} action={t('viewAll')} onPress={() => router.push('/feedback')} /><View style={[styles.feedbackCard, { backgroundColor: colors.card, borderColor: colors.border }]}>{feedback.slice(0, 2).map((item) => <View key={item.id} style={styles.feedbackRow}><View style={styles.stars}>{Array.from({ length: item.rating }).map((_, index) => <Ionicons key={index} name="star" size={14} color={colors.accent} />)}</View><Text style={[styles.feedbackText, { color: colors.foreground }]}>{item.note || t('rated', { dish: item.dish })}</Text><Text style={[styles.feedbackDish, { color: colors.mutedForeground }]}>{item.dish}</Text></View>)}</View></Screen>;
}

const styles = StyleSheet.create({
  lockCard: { borderWidth: 1, borderRadius: 22, padding: 22, alignItems: 'center', gap: 11, marginTop: 12 },
  lockIcon: { width: 58, height: 58, borderRadius: 20, alignItems: 'center', justifyContent: 'center', marginBottom: 3 },
  lockTitle: { fontSize: 19, fontWeight: '700' },
  lockDetail: { textAlign: 'center', fontSize: 13, lineHeight: 19, marginBottom: 8 },
  hero: { padding: 20, borderRadius: 23, flexDirection: 'row', alignItems: 'center', gap: 12 },
  heroCopy: { flex: 1, gap: 6 },
  heroKicker: { fontSize: 10, fontWeight: '700', letterSpacing: 1.3 },
  heroTitle: { fontSize: 22, fontWeight: '700', letterSpacing: -0.4 },
  heroDetail: { fontSize: 12, lineHeight: 17, opacity: 0.76 },
  statsRow: { flexDirection: 'row', gap: 10 },
  actionGrid: { flexDirection: 'row', gap: 8 },
  approvalCard: { borderWidth: 1, borderRadius: 19, paddingHorizontal: 14 },
  approvalRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 13 },
  approvalIcon: { width: 36, height: 36, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  approvalCopy: { flex: 1, gap: 3 },
  approvalTitle: { fontSize: 13, fontWeight: '700' },
  approvalDetail: { fontSize: 11 },
  approvalActions: { flexDirection: 'row', gap: 6 },
  smallButton: { width: 32, height: 32, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
  feedbackCard: { borderWidth: 1, borderRadius: 19, padding: 15, gap: 15 },
  feedbackRow: { gap: 5 },
  stars: { flexDirection: 'row', gap: 2 },
  feedbackText: { fontSize: 13, fontWeight: '600' },
  feedbackDish: { fontSize: 11 },
});