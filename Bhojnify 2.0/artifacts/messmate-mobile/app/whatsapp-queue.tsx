import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { Header, Screen } from '@/components/UI';
import { useMess, getExpiryInfo, Customer } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { formatWhatsAppDate, buildWhatsAppLink, renderTemplate } from '@/lib/whatsapp';
import { MessagePreviewDialog } from '@/components/MessagePreviewDialog';

export default function WhatsAppQueueScreen() {
  const colors = useColors();
  const { customers, profile, messSettings, messageTemplates, whatsappMessageLogs, logWhatsAppOpen } = useMess();
  const [currentIndex, setCurrentIndex] = useState(0);
  const [dialogVisible, setDialogVisible] = useState(false);

  // Filter: active subscriptions, expiring within 5 days, sorted by days left
  const queue = useMemo<Customer[]>(() => {
    return customers
      .filter((c) => {
        if (c.subscriptionStatus && c.subscriptionStatus !== 'active') return false;
        const exp = getExpiryInfo(c.expiryDate);
        return exp.isExpiringSoon || exp.isExpiringToday;
      })
      .sort((a, b) => {
        const aExp = getExpiryInfo(a.expiryDate);
        const bExp = getExpiryInfo(b.expiryDate);
        return aExp.daysRemaining - bExp.daysRemaining;
      });
  }, [customers]);

  const customer = queue[currentIndex] ?? null;

  const advance = () => {
    if (currentIndex < queue.length - 1) {
      setCurrentIndex((i) => i + 1);
    } else {
      router.back();
    }
  };

  if (!customer) {
    return (
      <Screen>
        <Header eyebrow="Owner tools" title="Expiry Queue" subtitle="Send reminders one member at a time" onPress={() => router.back()} />
        <View style={[styles.emptyCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <Ionicons name="checkmark-circle-outline" size={36} color={colors.primary} />
          <Text style={[styles.emptyTitle, { color: colors.foreground }]}>All done!</Text>
          <Text style={[styles.emptyDetail, { color: colors.mutedForeground }]}>No members with plans expiring in the next 5 days.</Text>
        </View>
      </Screen>
    );
  }

  const exp = getExpiryInfo(customer.expiryDate);
  const resolvedType = exp.isExpiringToday ? ('EXPIRY_TODAY' as const) : ('EXPIRY_REMINDER' as const);

  const templateData = {
    name: customer.name,
    days_left: String(exp.daysRemaining),
    end_date: formatWhatsAppDate(customer.expiryDate),
    start_date: formatWhatsAppDate(customer.joiningDate),
    plan: customer.plan,
    amount: customer.amount != null ? String(customer.amount) : '—',
    meals_left: customer.mealsLeft != null ? String(customer.mealsLeft) : '—',
    upi_id: messSettings.upiId || profile.phone || '—',
    mess_name: profile.messName || 'Mess',
    phone: messSettings.messPhone || profile.phone || '—',
    breakfast_time: messSettings.breakfastTime || '—',
    lunch_time: messSettings.lunchTime || '—',
    dinner_time: messSettings.dinnerTime || '—',
  };

  const renderedMessage = renderTemplate(messageTemplates[resolvedType], templateData);
  const phoneInvalid = !buildWhatsAppLink(customer.phone, renderedMessage);

  const lastLog = whatsappMessageLogs.filter((l) => l.studentId === customer.id)[0];
  const msgToday = lastLog?.openedAt?.startsWith(new Date().toISOString().slice(0, 10));

  const handleOpened = () => {
    logWhatsAppOpen(customer.id, customer.subscriptionId, resolvedType);
    advance();
  };

  return (
    <Screen scroll={false}>
      <ScrollView contentContainerStyle={styles.content}>
        <Header eyebrow="Owner tools" title="Expiry Queue" subtitle={`${currentIndex + 1} of ${queue.length} · expiring soon`} onPress={() => router.back()} />

        {/* Progress */}
        <View style={[styles.progressBar, { backgroundColor: colors.muted }]}>
          <View style={[styles.progressFill, { backgroundColor: colors.primary, width: `${((currentIndex + 1) / queue.length) * 100}%` as any }]} />
        </View>

        {/* Customer card */}
        <View style={[styles.card, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <View style={styles.cardHeader}>
            <View style={[styles.avatar, { backgroundColor: colors.secondary }]}>
              <Text style={[styles.avatarText, { color: colors.primary }]}>{customer.name.slice(0, 2).toUpperCase()}</Text>
            </View>
            <View style={styles.cardMeta}>
              <Text style={[styles.cardName, { color: colors.foreground }]}>{customer.name}</Text>
              <Text style={[styles.cardPhone, { color: colors.mutedForeground }]}>{customer.phone}</Text>
            </View>
            <View style={[styles.daysBadge, { backgroundColor: exp.isExpiringToday ? colors.destructive : colors.accent }]}>
              <Text style={[styles.daysBadgeText, { color: exp.isExpiringToday ? colors.destructiveForeground : colors.accentForeground }]}>
                {exp.isExpiringToday ? 'Today' : `${exp.daysRemaining}d left`}
              </Text>
            </View>
          </View>
          <Text style={[styles.cardPlan, { color: colors.mutedForeground }]}>{customer.plan} · expires {formatWhatsAppDate(customer.expiryDate)}</Text>
          {msgToday ? <Text style={[styles.msgHint, { color: colors.primary }]}>ℹ Already opened in WhatsApp today</Text> : null}
        </View>

        {/* Action buttons */}
        {phoneInvalid ? (
          <View style={[styles.warningCard, { backgroundColor: colors.accent, borderColor: colors.accentForeground }]}>
            <Ionicons name="warning-outline" size={18} color={colors.accentForeground} />
            <Text style={[styles.warningText, { color: colors.accentForeground }]}>Invalid phone number — cannot open WhatsApp</Text>
          </View>
        ) : (
          <Pressable onPress={() => setDialogVisible(true)} style={styles.openBtn}>
            <Ionicons name="logo-whatsapp" size={20} color="#fff" />
            <Text style={styles.openBtnText}>Preview &amp; Open in WhatsApp</Text>
          </Pressable>
        )}

        <Pressable onPress={advance} style={[styles.skipBtn, { borderColor: colors.border }]}>
          <Text style={[styles.skipBtnText, { color: colors.mutedForeground }]}>
            {currentIndex < queue.length - 1 ? 'Skip / Next →' : 'Done'}
          </Text>
        </Pressable>
      </ScrollView>

      <MessagePreviewDialog
        visible={dialogVisible}
        customerName={customer.name}
        phone={customer.phone}
        messageType={resolvedType}
        initialMessage={renderedMessage}
        invalidPhone={phoneInvalid}
        onClose={() => setDialogVisible(false)}
        onOpened={handleOpened}
      />
    </Screen>
  );
}

const styles = StyleSheet.create({
  content: { padding: 18, paddingTop: 54, gap: 16, paddingBottom: 40 },
  progressBar: { height: 5, borderRadius: 5, overflow: 'hidden' },
  progressFill: { height: 5, borderRadius: 5 },
  card: { borderWidth: 1, borderRadius: 18, padding: 16, gap: 10 },
  cardHeader: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatar: { width: 48, height: 48, borderRadius: 24, alignItems: 'center', justifyContent: 'center' },
  avatarText: { fontSize: 15, fontWeight: '700' },
  cardMeta: { flex: 1 },
  cardName: { fontSize: 16, fontWeight: '700' },
  cardPhone: { fontSize: 13 },
  daysBadge: { paddingHorizontal: 10, paddingVertical: 5, borderRadius: 99 },
  daysBadgeText: { fontSize: 12, fontWeight: '700' },
  cardPlan: { fontSize: 13 },
  msgHint: { fontSize: 12, fontWeight: '600' },
  warningCard: { borderWidth: 1, borderRadius: 14, padding: 12, flexDirection: 'row', alignItems: 'center', gap: 8 },
  warningText: { fontSize: 13, fontWeight: '600', flex: 1 },
  openBtn: { backgroundColor: '#2E7D32', borderRadius: 16, minHeight: 52, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 10 },
  openBtnText: { color: '#fff', fontSize: 16, fontWeight: '700' },
  skipBtn: { borderWidth: 1, borderRadius: 16, minHeight: 46, alignItems: 'center', justifyContent: 'center' },
  skipBtnText: { fontSize: 15, fontWeight: '600' },
  emptyCard: { borderWidth: 1, borderRadius: 18, padding: 28, alignItems: 'center', gap: 10 },
  emptyTitle: { fontSize: 17, fontWeight: '700' },
  emptyDetail: { fontSize: 13, textAlign: 'center', lineHeight: 19 },
});
