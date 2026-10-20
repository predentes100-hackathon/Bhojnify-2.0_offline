import { Ionicons } from '@expo/vector-icons';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { Customer, getExpiryInfo, useMess } from '@/context/AppContext';
import { buildWhatsAppLink, formatWhatsAppDate, getDefaultMessageType, MessageType, renderTemplate } from '@/lib/whatsapp';
import { todayISO } from '@/lib/dates';
import { useColors } from '@/hooks/useColors';
import { MessagePreviewDialog } from './MessagePreviewDialog';

interface SendWhatsAppButtonProps {
  customer: Customer;
  explicitType?: MessageType;
  /** ISO dates for ENROLLED/RENEWED context */
  enrolledFrom?: string;
  enrolledTo?: string;
  renewedFrom?: string;
  renewedTo?: string;
  compact?: boolean;
}

export function SendWhatsAppButton({ customer, explicitType, enrolledFrom, enrolledTo, renewedFrom, renewedTo, compact }: SendWhatsAppButtonProps) {
  const colors = useColors();
  const { profile, messSettings, messageTemplates, whatsappMessageLogs, logWhatsAppOpen } = useMess();
  const [dialogVisible, setDialogVisible] = useState(false);

  const exp = getExpiryInfo(customer.expiryDate);
  const todayStr = todayISO();

  // Choose the message type
  const resolvedType: MessageType | null = explicitType ?? getDefaultMessageType(exp.daysRemaining);

  // Don't show for paused/cancelled plans unless an explicit type was provided
  if (!resolvedType) return null;
  if (!explicitType && customer.subscriptionStatus && customer.subscriptionStatus !== 'active') return null;

  // Build the template data using what we know
  const templateData = {
    name: customer.name,
    days_left: String(exp.daysRemaining),
    end_date: formatWhatsAppDate(customer.expiryDate),
    start_date: formatWhatsAppDate(enrolledFrom ?? renewedFrom ?? customer.joiningDate),
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

  if (explicitType === 'RENEWED' && renewedTo) {
    templateData.end_date = formatWhatsAppDate(renewedTo);
    if (renewedFrom) templateData.start_date = formatWhatsAppDate(renewedFrom);
  }
  if (explicitType === 'ENROLLED' && enrolledTo) {
    templateData.end_date = formatWhatsAppDate(enrolledTo);
    if (enrolledFrom) templateData.start_date = formatWhatsAppDate(enrolledFrom);
  }

  const renderedMessage = renderTemplate(messageTemplates[resolvedType], templateData);
  const normalizedLink = buildWhatsAppLink(customer.phone, renderedMessage);
  const phoneInvalid = !normalizedLink;

  // Advisory "last messaged" hint
  const logsForCustomer = whatsappMessageLogs.filter((l) => l.studentId === customer.id);
  const lastLog = logsForCustomer[0];
  const msgToday = lastLog?.openedAt?.startsWith(todayStr);

  const handleOpened = () => {
    logWhatsAppOpen(customer.id, customer.subscriptionId, resolvedType);
  };

  if (compact) {
    return (
      <>
        <Pressable
          onPress={() => setDialogVisible(true)}
          style={({ pressed }) => [styles.compactBtn, { backgroundColor: '#E8F5E9', opacity: pressed ? 0.75 : 1 }]}
        >
          <Ionicons name="logo-whatsapp" size={15} color="#2E7D32" />
          <Text style={styles.compactBtnText}>WhatsApp</Text>
        </Pressable>
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
      </>
    );
  }

  return (
    <>
      <View style={styles.container}>
        <Pressable
          onPress={() => setDialogVisible(true)}
          style={({ pressed }) => [styles.button, { backgroundColor: '#E8F5E9', opacity: pressed ? 0.82 : 1 }]}
        >
          <Ionicons name="logo-whatsapp" size={18} color="#2E7D32" />
          <View style={styles.btnInner}>
            <Text style={styles.btnLabel}>Send on WhatsApp</Text>
            {msgToday ? <Text style={styles.todayHint}>Already opened today</Text> : lastLog ? <Text style={styles.lastHint}>Last: {lastLog.openedAt.slice(0, 10)}</Text> : null}
          </View>
          {phoneInvalid ? <Ionicons name="warning-outline" size={16} color={colors.destructive} /> : null}
        </Pressable>
      </View>
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
    </>
  );
}

const styles = StyleSheet.create({
  container: {},
  button: { flexDirection: 'row', alignItems: 'center', gap: 10, borderRadius: 14, paddingVertical: 11, paddingHorizontal: 15 },
  btnInner: { flex: 1 },
  btnLabel: { color: '#2E7D32', fontWeight: '700', fontSize: 14 },
  todayHint: { color: '#388E3C', fontSize: 11, opacity: 0.8 },
  lastHint: { color: '#388E3C', fontSize: 11, opacity: 0.7 },
  compactBtn: { flex: 1, minHeight: 34, paddingHorizontal: 8, borderRadius: 10, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 4 },
  compactBtnText: { fontSize: 11, fontWeight: '700', color: '#2E7D32' },
});
