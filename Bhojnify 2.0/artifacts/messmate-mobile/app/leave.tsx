import { isValidISODate, daysBetween } from '@/lib/dates';
import { todayISO } from '@/lib/dates';
import { Ionicons } from '@expo/vector-icons';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { Badge, FormField, Header, PrimaryButton, SectionHeading } from '@/components/UI';
import { formatDate, useMess } from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { localizedValue } from '@/lib/i18n';

export default function LeaveScreen() {
  const colors = useColors();
  const { customers, leaves, addLeave, updateLeaveStatus, updateLeave, deleteLeave, completeLeave } = useMess();
  const { language, t } = useTranslation();

  const todayStr = useMemo(() => todayISO(), []);

  const [showForm, setShowForm] = useState(false);
  const [editingLeaveId, setEditingLeaveId] = useState<string | null>(null);
  const [customerId, setCustomerId] = useState('');
  const [from, setFrom] = useState(todayStr);
  const [to, setTo] = useState('');
  const [note, setNote] = useState('');

  // Complete Leave modal / inline prompt state
  const [completingLeaveId, setCompletingLeaveId] = useState<string | null>(null);
  const [returnDateInput, setReturnDateInput] = useState(todayStr);

  const pendingLeaves = useMemo(
    () => leaves.filter((leave) => leave.status === 'Pending' || !leave.to),
    [leaves]
  );

  const resetForm = () => {
    setCustomerId('');
    setFrom(todayStr);
    setTo('');
    setNote('');
    setEditingLeaveId(null);
    setShowForm(false);
  };

  const handleStartEdit = (leaveId: string) => {
    const target = leaves.find((l) => l.id === leaveId);
    if (!target) return;
    setEditingLeaveId(leaveId);
    setCustomerId(target.customerId || '');
    setFrom(target.from);
    setTo(target.to || '');
    setNote(target.reason);
    setShowForm(true);
  };

  const handleDelete = (leaveId: string) => {
    Alert.alert(t('deleteLeave'), t('deleteLeaveConfirm'), [
      { text: t('cancel'), style: 'cancel' },
      {
        text: t('delete'),
        style: 'destructive',
        onPress: () => deleteLeave(leaveId),
      },
    ]);
  };

  const saveLeave = () => {
    if (!customerId || !from.trim()) {
      Alert.alert(t('completeLeave'), t('leaveRequiredMessage'));
      return;
    }

    if (editingLeaveId) {
      updateLeave(editingLeaveId, {
        customerId,
        from: from.trim(),
        to: to.trim() || undefined,
        reason: note.trim(),
      });
    } else {
      addLeave(customerId, from.trim(), to.trim() || undefined, note.trim());
    }

    resetForm();
  };

  const handleCompleteLeave = (leaveId: string) => {
    if (!returnDateInput.trim()) {
      Alert.alert(t('date'), t('enterDay'));
      return;
    }
    completeLeave(leaveId, returnDateInput.trim());
    setCompletingLeaveId(null);
    Alert.alert(t('holidayCompleted'), t('planExtendedNotice'));
  };

  return (
    <KeyboardAwareScrollViewCompat
      style={[styles.screen, { backgroundColor: colors.background }]}
      contentContainerStyle={styles.content}
      bottomOffset={24}
      keyboardShouldPersistTaps="handled"
    >
      <Header
        eyebrow={t('ownerTools')}
        title={t('leaveTitle')}
        subtitle={t('leaveSubtitle')}
        onPress={() => router.push('/(tabs)/admin')}
      />

      <View style={[styles.summary, { backgroundColor: colors.secondary }]}>
        <Ionicons name="calendar-outline" size={22} color={colors.primary} />
        <View style={styles.summaryCopy}>
          <Text style={[styles.summaryTitle, { color: colors.primary }]}>
            {t('requestWaiting', {
              count: pendingLeaves.length,
              plural: pendingLeaves.length === 1 ? '' : 's',
            })}
          </Text>
          <Text style={[styles.summaryDetail, { color: colors.secondaryForeground }]}>
            {t('approvedLeaveDetail')}
          </Text>
        </View>
      </View>

      <PrimaryButton
        label={showForm ? t('cancel') : t('addLeave')}
        icon={showForm ? 'x' : 'plus'}
        onPress={() => {
          if (showForm) resetForm();
          else {
            resetForm();
            setShowForm(true);
          }
        }}
      />

      {/* Complete Leave Card */}
      {completingLeaveId ? (
        <View style={[styles.completeLeaveCard, { backgroundColor: colors.card, borderColor: colors.primary }]}>
          <View style={styles.completeLeaveHeader}>
            <Ionicons name="checkmark-done-circle" size={22} color={colors.primary} />
            <Text style={[styles.completeLeaveTitle, { color: colors.foreground }]}>
              {t('completeLeave')}
            </Text>
          </View>
          <Text style={[styles.completeLeaveDetail, { color: colors.mutedForeground }]}>
            {t('autoExtendSub')}
          </Text>
          <FormField
            label={t('returnDate')}
            value={returnDateInput}
            onChangeText={setReturnDateInput}
            placeholder="YYYY-MM-DD"
          />
          <View style={styles.completeActionsRow}>
            <Pressable
              onPress={() => setCompletingLeaveId(null)}
              style={[styles.cancelBtn, { borderColor: colors.border }]}
            >
              <Text style={[styles.cancelBtnText, { color: colors.mutedForeground }]}>
                {t('cancel')}
              </Text>
            </Pressable>
            <Pressable
              onPress={() => handleCompleteLeave(completingLeaveId)}
              style={[styles.confirmBtn, { backgroundColor: colors.primary }]}
            >
              <Text style={[styles.confirmBtnText, { color: colors.primaryForeground }]}>
                {t('confirmReturn')}
              </Text>
            </Pressable>
          </View>
        </View>
      ) : null}

      {/* Leave Form Modal/Inline */}
      {showForm ? (
        <View style={[styles.formCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
          <View style={styles.formIntro}>
            <Text style={[styles.formTitle, { color: colors.foreground }]}>
              {editingLeaveId ? t('editLeaveTitle') : t('addLeaveTitle')}
            </Text>
            <Text style={[styles.formSubtitle, { color: colors.mutedForeground }]}>
              {t('addLeaveSubtitle')}
            </Text>
          </View>

          <View style={styles.customerPicker}>
            <Text style={[styles.fieldLabel, { color: colors.foreground }]}>
              {t('selectCustomer')}
            </Text>
            {customers.length ? (
              <View style={styles.customerOptions}>
                {customers.map((customer) => (
                  <Pressable
                    key={customer.id}
                    testID={`select-customer-${customer.id}`}
                    onPress={() => setCustomerId(customer.id)}
                    style={[
                      styles.customerOption,
                      {
                        borderColor: customerId === customer.id ? colors.primary : colors.border,
                        backgroundColor:
                          customerId === customer.id ? colors.secondary : colors.background,
                      },
                    ]}
                  >
                    <View style={styles.customerOptionCopy}>
                      <Text style={[styles.customerOptionName, { color: colors.foreground }]}>
                        {customer.name}
                      </Text>
                      <Text style={[styles.customerOptionMeta, { color: colors.mutedForeground }]}>
                        {customer.plan} · {localizedValue(language, customer.paymentStatus)}
                      </Text>
                    </View>
                    <Ionicons
                      name={customerId === customer.id ? 'checkmark-circle' : 'ellipse-outline'}
                      size={20}
                      color={customerId === customer.id ? colors.primary : colors.mutedForeground}
                    />
                  </Pressable>
                ))}
              </View>
            ) : (
              <Pressable
                onPress={() => router.push('/customers')}
                style={[styles.noCustomers, { backgroundColor: colors.secondary }]}
              >
                <Ionicons name="person-add-outline" size={18} color={colors.primary} />
                <Text style={[styles.noCustomersText, { color: colors.primary }]}>
                  {t('noCustomersForLeave')}
                </Text>
              </Pressable>
            )}
          </View>

          <FormField
            label={t('leaveFrom')}
            value={from}
            onChangeText={setFrom}
            placeholder="YYYY-MM-DD"
          />

          <FormField
            label={t('leaveToOptional')}
            value={to}
            onChangeText={setTo}
            placeholder="YYYY-MM-DD (Leave empty if return date is pending)"
          />

          <FormField
            label={t('leaveNoteOptional')}
            value={note}
            onChangeText={setNote}
            placeholder={t('enterLeaveNote')}
          />

          <PrimaryButton
            label={editingLeaveId ? t('saveChanges') : t('saveLeave')}
            icon="check"
            onPress={saveLeave}
            disabled={!customers.length}
          />
        </View>
      ) : null}

      <SectionHeading title={t('allRequests')} />
      <View style={[styles.list, { backgroundColor: colors.card, borderColor: colors.border }]}>
        {leaves.length ? (
          leaves.map((leave, index) => {
            const customer = customers.find((c) => c.id === leave.customerId);
            const isOngoing = !leave.to;

            return (
              <View
                key={leave.id}
                style={[
                  styles.row,
                  index < leaves.length - 1 && {
                    borderBottomWidth: 1,
                    borderBottomColor: colors.border,
                  },
                ]}
              >
                <View style={styles.copy}>
                  <View style={styles.customerRowTop}>
                    <Text style={[styles.customer, { color: colors.foreground }]}>
                      {customer?.name ?? t('memberLeave')}
                    </Text>
                    {isOngoing ? (
                      <Badge label={t('holidayPending')} tone="amber" />
                    ) : (
                      <Badge
                        label={localizedValue(language, leave.status)}
                        tone={leave.status === 'Approved' ? 'green' : 'red'}
                      />
                    )}
                  </View>

                  <Text style={[styles.dates, { color: colors.foreground }]}>
                    {formatDate(leave.from, language)} –{' '}
                    {leave.to ? formatDate(leave.to, language) : t('returnPending')}
                  </Text>

                  {leave.reason ? (
                    <Text style={[styles.reason, { color: colors.mutedForeground }]}>
                      {leave.reason}
                    </Text>
                  ) : null}

                  {/* Actions Row */}
                  <View style={styles.leaveActionFooter}>
                    {isOngoing ? (
                      <Pressable
                        onPress={() => {
                          setCompletingLeaveId(leave.id);
                          setReturnDateInput(todayStr);
                        }}
                        style={[styles.completeBtn, { backgroundColor: colors.primary }]}
                      >
                        <Ionicons name="checkmark-circle-outline" size={14} color={colors.primaryForeground} />
                        <Text style={[styles.completeBtnText, { color: colors.primaryForeground }]}>
                          {t('completeHoliday')}
                        </Text>
                      </Pressable>
                    ) : leave.status === 'Pending' ? (
                      <View style={styles.approvalActions}>
                        <Pressable
                          testID={`approve-leave-${leave.id}`}
                          onPress={() => updateLeaveStatus(leave.id, 'Approved')}
                          style={[styles.action, { backgroundColor: colors.secondary }]}
                        >
                          <Ionicons name="checkmark" size={17} color={colors.primary} />
                        </Pressable>
                        <Pressable
                          testID={`decline-leave-${leave.id}`}
                          onPress={() => updateLeaveStatus(leave.id, 'Declined')}
                          style={[styles.action, { backgroundColor: colors.muted }]}
                        >
                          <Ionicons name="close" size={17} color={colors.destructive} />
                        </Pressable>
                      </View>
                    ) : null}

                    <View style={styles.editDeleteGroup}>
                      <Pressable
                        onPress={() => handleStartEdit(leave.id)}
                        style={[styles.iconButton, { backgroundColor: colors.secondary }]}
                      >
                        <Ionicons name="pencil-outline" size={14} color={colors.primary} />
                      </Pressable>
                      <Pressable
                        onPress={() => handleDelete(leave.id)}
                        style={[styles.iconButton, { backgroundColor: colors.muted }]}
                      >
                        <Ionicons name="trash-outline" size={14} color={colors.destructive} />
                      </Pressable>
                    </View>
                  </View>
                </View>
              </View>
            );
          })
        ) : (
          <Text style={[styles.empty, { color: colors.mutedForeground }]}>{t('noLeaveRequests')}</Text>
        )}
      </View>
    </KeyboardAwareScrollViewCompat>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 54, paddingBottom: 40, gap: 18 },
  summary: { padding: 16, borderRadius: 18, flexDirection: 'row', gap: 10, alignItems: 'flex-start' },
  summaryCopy: { flex: 1, gap: 4 },
  summaryTitle: { fontSize: 15, fontWeight: '700' },
  summaryDetail: { fontSize: 12, lineHeight: 18 },
  formCard: { borderWidth: 1, borderRadius: 20, padding: 16, gap: 15 },
  formIntro: { gap: 4 },
  formTitle: { fontSize: 17, fontWeight: '700' },
  formSubtitle: { fontSize: 12, lineHeight: 18 },
  customerPicker: { gap: 8 },
  fieldLabel: { fontSize: 13, fontWeight: '700' },
  customerOptions: { gap: 8 },
  customerOption: {
    minHeight: 54,
    borderWidth: 1,
    borderRadius: 13,
    paddingHorizontal: 12,
    paddingVertical: 9,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  customerOptionCopy: { flex: 1, gap: 3 },
  customerOptionName: { fontSize: 13, fontWeight: '700' },
  customerOptionMeta: { fontSize: 10 },
  noCustomers: { minHeight: 48, borderRadius: 13, paddingHorizontal: 12, flexDirection: 'row', alignItems: 'center', gap: 8 },
  noCustomersText: { fontSize: 12, fontWeight: '700' },
  list: { borderWidth: 1, borderRadius: 19, paddingHorizontal: 14 },
  row: { flexDirection: 'row', alignItems: 'flex-start', gap: 10, paddingVertical: 14 },
  copy: { flex: 1, gap: 6 },
  customerRowTop: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  customer: { fontSize: 14, fontWeight: '700' },
  dates: { fontSize: 14, fontWeight: '700' },
  reason: { fontSize: 12 },
  leaveActionFooter: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: 4,
    paddingTop: 8,
    borderTopWidth: 0.5,
    borderTopColor: '#e5e7eb',
  },
  completeBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 10,
  },
  completeBtnText: { fontSize: 12, fontWeight: '700' },
  approvalActions: { flexDirection: 'row', gap: 6 },
  action: { width: 32, height: 32, borderRadius: 11, alignItems: 'center', justifyContent: 'center' },
  editDeleteGroup: { flexDirection: 'row', gap: 6, marginLeft: 'auto' },
  iconButton: { width: 32, height: 32, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  empty: { paddingVertical: 16, fontSize: 12 },
  completeLeaveCard: {
    borderWidth: 1.5,
    borderRadius: 18,
    padding: 16,
    gap: 12,
  },
  completeLeaveHeader: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  completeLeaveTitle: { fontSize: 15, fontWeight: '700' },
  completeLeaveDetail: { fontSize: 12, lineHeight: 17 },
  completeActionsRow: { flexDirection: 'row', gap: 10, justifyContent: 'flex-end', marginTop: 4 },
  cancelBtn: { paddingHorizontal: 14, paddingVertical: 8, borderRadius: 10, borderWidth: 1 },
  cancelBtnText: { fontSize: 12, fontWeight: '600' },
  confirmBtn: { paddingHorizontal: 16, paddingVertical: 8, borderRadius: 10 },
  confirmBtnText: { fontSize: 12, fontWeight: '700' },
});