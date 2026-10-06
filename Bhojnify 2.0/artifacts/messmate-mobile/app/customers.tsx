import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import {
  Alert,
  Image,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { Badge, FormField, Header, PrimaryButton, SectionHeading } from '@/components/UI';
import {
  CustomerPaymentStatus,
  formatDate,
  getExpiryInfo,
  MESS_PLANS,
  useMess,
} from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { localizedValue } from '@/lib/i18n';

type FilterTab = 'all' | 'active' | 'expiring' | 'expired';

export default function CustomersScreen() {
  const colors = useColors();
  const { customers, addCustomer, markCustomerPaid, updateCustomer, addPayment } = useMess();
  const { language, t } = useTranslation();

  const todayStr = useMemo(() => new Date().toISOString().slice(0, 10), []);

  const [activeFilter, setActiveFilter] = useState<FilterTab>('all');
  const [name, setName] = useState('');
  const [selectedPlan, setSelectedPlan] = useState<string>(MESS_PLANS[0]);
  const [customPlan, setCustomPlan] = useState('');
  const [joiningDate, setJoiningDate] = useState(todayStr);
  const [expiryDate, setExpiryDate] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() + 30);
    return d.toISOString().slice(0, 10);
  });
  const [phone, setPhone] = useState('');
  const [paymentStatus, setPaymentStatus] = useState<CustomerPaymentStatus>('Paid');
  const [amountPaid, setAmountPaid] = useState('');
  const [imageUri, setImageUri] = useState<string | undefined>(undefined);

  // Auto calculate expiry when plan changes
  const handlePlanSelect = (plan: string) => {
    setSelectedPlan(plan);
    const start = joiningDate ? new Date(`${joiningDate}T12:00:00`) : new Date();
    if (isNaN(start.getTime())) return;

    if (plan.includes('Monthly')) {
      const exp = new Date(start);
      exp.setDate(exp.getDate() + 30);
      setExpiryDate(exp.toISOString().slice(0, 10));
    } else if (plan.includes('15-Day')) {
      const exp = new Date(start);
      exp.setDate(exp.getDate() + 15);
      setExpiryDate(exp.toISOString().slice(0, 10));
    } else if (plan.includes('Daily')) {
      const exp = new Date(start);
      exp.setDate(exp.getDate() + 1);
      setExpiryDate(exp.toISOString().slice(0, 10));
    }
  };

  const handleJoiningDateChange = (date: string) => {
    setJoiningDate(date);
    if (/^\d{4}-\d{2}-\d{2}$/.test(date.trim())) {
      const start = new Date(`${date.trim()}T12:00:00`);
      if (!isNaN(start.getTime())) {
        const days = selectedPlan.includes('15-Day') ? 15 : selectedPlan.includes('Daily') ? 1 : 30;
        const exp = new Date(start);
        exp.setDate(exp.getDate() + days);
        setExpiryDate(exp.toISOString().slice(0, 10));
      }
    }
  };

  const pickImage = async () => {
    try {
      const permissionResult = await ImagePicker.requestMediaLibraryPermissionsAsync();
      if (!permissionResult.granted) {
        Alert.alert(t('errorTitle'), 'Permission to access gallery is required to choose a customer photo.');
        return;
      }
      const result = await ImagePicker.launchImageLibraryAsync({
        mediaTypes: ['images'],
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.7,
      });
      if (!result.canceled && result.assets && result.assets[0]?.uri) {
        setImageUri(result.assets[0].uri);
      }
    } catch (err) {
      console.warn('Image picker error:', err);
    }
  };

  const takePhoto = async () => {
    try {
      const permissionResult = await ImagePicker.requestCameraPermissionsAsync();
      if (!permissionResult.granted) {
        Alert.alert(t('errorTitle'), 'Permission to access camera is required to take a photo.');
        return;
      }
      const result = await ImagePicker.launchCameraAsync({
        allowsEditing: true,
        aspect: [1, 1],
        quality: 0.7,
      });
      if (!result.canceled && result.assets && result.assets[0]?.uri) {
        setImageUri(result.assets[0].uri);
      }
    } catch (err) {
      console.warn('Camera error:', err);
    }
  };

  const showPhotoOptions = () => {
    Alert.alert(
      t('addCustomerPhoto'),
      t('addCustomerPhoto'),
      [
        { text: t('chooseFromLibrary'), onPress: pickImage },
        { text: t('takePhoto'), onPress: takePhoto },
        { text: t('cancel'), style: 'cancel' },
      ]
    );
  };

  const add = () => {
    const finalPlan = selectedPlan === 'Custom Plan' ? customPlan.trim() : selectedPlan;
    if (!name.trim() || !finalPlan || !joiningDate.trim() || !expiryDate.trim() || !phone.trim()) {
      Alert.alert(t('completeCustomer'), t('customerRequiredMessage'));
      return;
    }
    addCustomer({
      name: name.trim(),
      plan: finalPlan,
      joiningDate: joiningDate.trim(),
      expiryDate: expiryDate.trim(),
      phone: phone.trim(),
      paymentStatus,
      imageUri,
    });

    if (paymentStatus === 'Paid' && amountPaid.trim() !== '' && !isNaN(Number(amountPaid))) {
      addPayment({
        amount: Number(amountPaid),
        method: 'Cash/UPI',
        note: `Subscription: ${name.trim()} - ${finalPlan}`,
        date: todayStr,
      });
    }

    setName('');
    setSelectedPlan(MESS_PLANS[0]);
    setCustomPlan('');
    setJoiningDate(todayStr);
    const d = new Date();
    d.setDate(d.getDate() + 30);
    setExpiryDate(d.toISOString().slice(0, 10));
    setPhone('');
    setPaymentStatus('Paid');
    setImageUri(undefined);
    setAmountPaid('');
  };

  const handleRenew = (customerId: string) => {
    const d = new Date();
    const newJoining = d.toISOString().slice(0, 10);
    d.setDate(d.getDate() + 30);
    const newExpiry = d.toISOString().slice(0, 10);

    updateCustomer(customerId, {
      joiningDate: newJoining,
      expiryDate: newExpiry,
      paymentStatus: 'Paid',
    });
  };

  // Compute metrics
  const customerStats = useMemo(() => {
    let activeCount = 0;
    let expiringCount = 0;
    let expiredCount = 0;

    customers.forEach((c) => {
      const exp = getExpiryInfo(c.expiryDate);
      if (exp.isExpired) expiredCount++;
      else if (exp.isExpiringSoon) expiringCount++;
      else activeCount++;
    });

    return { activeCount, expiringCount, expiredCount };
  }, [customers]);

  // Filtered customer list
  const filteredCustomers = useMemo(() => {
    return customers.filter((customer) => {
      const exp = getExpiryInfo(customer.expiryDate);
      if (activeFilter === 'active') return !exp.isExpired && !exp.isExpiringSoon;
      if (activeFilter === 'expiring') return exp.isExpiringSoon;
      if (activeFilter === 'expired') return exp.isExpired;
      return true;
    });
  }, [customers, activeFilter]);

  return (
    <KeyboardAwareScrollViewCompat
      style={[styles.screen, { backgroundColor: colors.background }]}
      contentContainerStyle={styles.content}
      bottomOffset={24}
      keyboardShouldPersistTaps="handled"
    >
      <Header
        eyebrow={t('ownerTools')}
        title={t('customerList')}
        subtitle={t('customerListSubtitle')}
        onPress={() => router.push('/(tabs)/admin')}
      />

      {/* Summary Stats */}
      <View style={styles.summaryGrid}>
        <View style={[styles.summaryCard, { backgroundColor: colors.primary }]}>
          <Ionicons name="people-outline" size={20} color={colors.accent} />
          <Text style={[styles.summaryValue, { color: colors.primaryForeground }]}>
            {customers.length}
          </Text>
          <Text style={[styles.summaryLabel, { color: colors.primaryForeground }]}>
            {t('all')}
          </Text>
        </View>
        <View style={[styles.summaryCard, { backgroundColor: colors.secondary }]}>
          <Ionicons name="shield-checkmark-outline" size={20} color={colors.primary} />
          <Text style={[styles.summaryValue, { color: colors.primary }]}>
            {customerStats.activeCount}
          </Text>
          <Text style={[styles.summaryLabel, { color: colors.primary }]}>
            {t('active')}
          </Text>
        </View>
        <View style={[styles.summaryCard, { backgroundColor: colors.accent }]}>
          <Ionicons name="time-outline" size={20} color={colors.accentForeground} />
          <Text style={[styles.summaryValue, { color: colors.accentForeground }]}>
            {customerStats.expiringCount}
          </Text>
          <Text style={[styles.summaryLabel, { color: colors.accentForeground }]}>
            {t('expiringSoon')}
          </Text>
        </View>
        <View style={[styles.summaryCard, { backgroundColor: colors.destructive }]}>
          <Ionicons name="alert-circle-outline" size={20} color={colors.destructiveForeground} />
          <Text style={[styles.summaryValue, { color: colors.destructiveForeground }]}>
            {customerStats.expiredCount}
          </Text>
          <Text style={[styles.summaryLabel, { color: colors.destructiveForeground }]}>
            {t('expired')}
          </Text>
        </View>
      </View>

      {/* Expiry Alerts Banner if any members are expiring today or in 3 days */}
      {customerStats.expiringCount > 0 || customerStats.expiredCount > 0 ? (
        <View style={[styles.alertBanner, { backgroundColor: colors.accent, borderColor: colors.accentForeground }]}>
          <Ionicons name="notifications" size={20} color={colors.accentForeground} />
          <View style={styles.alertBannerCopy}>
            <Text style={[styles.alertBannerTitle, { color: colors.accentForeground }]}>
              {t('expiryAlerts')}
            </Text>
            <Text style={[styles.alertBannerDetail, { color: colors.accentForeground }]}>
              {t('membersExpiringBanner', {
                count: customerStats.expiringCount + customerStats.expiredCount,
                plural: (customerStats.expiringCount + customerStats.expiredCount) === 1 ? '' : 's',
              })}
            </Text>
          </View>
        </View>
      ) : null}

      {/* Filter Tabs */}
      <View style={styles.filterSection}>
        <SectionHeading title={t('currentCustomers')} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.tabsRow}>
          {(['all', 'active', 'expiring', 'expired'] as FilterTab[]).map((tab) => {
            const isSelected = activeFilter === tab;
            const count =
              tab === 'all'
                ? customers.length
                : tab === 'active'
                ? customerStats.activeCount
                : tab === 'expiring'
                ? customerStats.expiringCount
                : customerStats.expiredCount;
            const label =
              tab === 'all'
                ? t('all')
                : tab === 'active'
                ? t('active')
                : tab === 'expiring'
                ? t('expiringSoon')
                : t('expired');

            return (
              <Pressable
                key={tab}
                onPress={() => setActiveFilter(tab)}
                style={[
                  styles.filterTab,
                  {
                    backgroundColor: isSelected ? colors.primary : colors.card,
                    borderColor: isSelected ? colors.primary : colors.border,
                  },
                ]}
              >
                <Text
                  style={[
                    styles.filterTabText,
                    { color: isSelected ? colors.primaryForeground : colors.foreground },
                  ]}
                >
                  {label}
                </Text>
                <View
                  style={[
                    styles.countPill,
                    {
                      backgroundColor: isSelected ? colors.accent : colors.secondary,
                    },
                  ]}
                >
                  <Text
                    style={[
                      styles.countPillText,
                      { color: isSelected ? colors.accentForeground : colors.primary },
                    ]}
                  >
                    {count}
                  </Text>
                </View>
              </Pressable>
            );
          })}
        </ScrollView>
      </View>

      {/* Customer List */}
      <View style={[styles.list, { backgroundColor: colors.card, borderColor: colors.border }]}>
        {filteredCustomers.length ? (
          filteredCustomers.map((customer, index) => {
            const exp = getExpiryInfo(customer.expiryDate);

            // Determine badge configuration for Expiry
            let expiryTone: 'green' | 'amber' | 'red' | 'gray' = 'green';
            let expiryLabel = `${exp.daysRemaining}d left`;

            if (exp.isExpired) {
              expiryTone = 'red';
              expiryLabel = t('expired');
            } else if (exp.isExpiringToday) {
              expiryTone = 'red';
              expiryLabel = t('expiresToday');
            } else if (exp.isExpiringSoon) {
              expiryTone = 'amber';
              expiryLabel = t('expiresInDays', { days: exp.daysRemaining });
            } else {
              expiryTone = 'green';
              expiryLabel = t('daysLeft', { days: exp.daysRemaining });
            }

            return (
              <View
                key={customer.id}
                style={[
                  styles.customerRow,
                  index < filteredCustomers.length - 1 && {
                    borderBottomWidth: 1,
                    borderBottomColor: colors.border,
                  },
                ]}
              >
                {customer.imageUri ? (
                  <Image source={{ uri: customer.imageUri }} style={styles.avatarImage} />
                ) : (
                  <View style={[styles.avatar, { backgroundColor: colors.secondary }]}>
                    <Text style={[styles.avatarText, { color: colors.primary }]}>
                      {customer.name
                        .split(' ')
                        .map((word) => word[0])
                        .join('')
                        .slice(0, 2)
                        .toUpperCase()}
                    </Text>
                  </View>
                )}

                <View style={styles.customerCopy}>
                  <View style={styles.nameLine}>
                    <Text style={[styles.customerName, { color: colors.foreground }]}>
                      {customer.name}
                    </Text>
                    <View style={styles.badgeGroup}>
                      <Badge label={expiryLabel} tone={expiryTone} />
                      <Badge
                        label={localizedValue(language, customer.paymentStatus)}
                        tone={customer.paymentStatus === 'Paid' ? 'green' : 'amber'}
                      />
                    </View>
                  </View>
                  <Text style={[styles.meta, { color: colors.mutedForeground }]}>
                    {customer.plan} · {customer.phone}
                  </Text>
                  <Text style={[styles.meta, { color: colors.mutedForeground }]}>
                    {formatDate(customer.joiningDate, language)} –{' '}
                    {formatDate(customer.expiryDate, language)}
                  </Text>
                </View>

                {/* Actions: Renew if expired / Mark Paid */}
                {exp.isExpired ? (
                  <Pressable
                    testID={`renew-${customer.id}`}
                    onPress={() => handleRenew(customer.id)}
                    style={({ pressed }) => [
                      styles.actionButton,
                      { backgroundColor: colors.primary, opacity: pressed ? 0.72 : 1 },
                    ]}
                  >
                    <Ionicons name="refresh-outline" size={14} color={colors.primaryForeground} />
                    <Text style={[styles.actionButtonText, { color: colors.primaryForeground }]}>
                      {t('renewPlan')}
                    </Text>
                  </Pressable>
                ) : customer.paymentStatus === 'Unpaid' ? (
                  <Pressable
                    testID={`mark-paid-${customer.id}`}
                    onPress={() => markCustomerPaid(customer.id)}
                    style={({ pressed }) => [
                      styles.actionButton,
                      { backgroundColor: colors.secondary, opacity: pressed ? 0.72 : 1 },
                    ]}
                  >
                    <Ionicons name="checkmark-circle-outline" size={14} color={colors.primary} />
                    <Text style={[styles.actionButtonText, { color: colors.primary }]}>
                      {t('markAsPaid')}
                    </Text>
                  </Pressable>
                ) : null}
              </View>
            );
          })
        ) : (
          <View style={styles.empty}>
            <Ionicons name="people-outline" size={28} color={colors.primary} />
            <Text style={[styles.emptyTitle, { color: colors.foreground }]}>
              {activeFilter === 'expired'
                ? t('noExpiredMembers')
                : activeFilter === 'expiring'
                ? t('noExpiringMembers')
                : t('noCustomers')}
            </Text>
            <Text style={[styles.emptyDetail, { color: colors.mutedForeground }]}>
              {t('noCustomersDetail')}
            </Text>
          </View>
        )}
      </View>

      {/* Add Customer Form */}
      <SectionHeading title={t('addCustomer')} />
      <View style={[styles.formCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
        {/* Customer Photo Selector */}
        <View style={styles.photoPickerContainer}>
          <Pressable
            onPress={showPhotoOptions}
            style={[styles.photoButton, { borderColor: colors.border, backgroundColor: colors.background }]}
          >
            {imageUri ? (
              <Image source={{ uri: imageUri }} style={styles.formAvatarImage} />
            ) : (
              <View style={styles.photoPlaceholder}>
                <Ionicons name="camera-outline" size={26} color={colors.primary} />
                <Text style={[styles.photoPlaceholderText, { color: colors.primary }]}>
                  {t('addCustomerPhoto')}
                </Text>
              </View>
            )}
          </Pressable>
          {imageUri ? (
            <Pressable
              onPress={() => setImageUri(undefined)}
              style={[styles.removePhotoButton, { backgroundColor: colors.destructive }]}
            >
              <Ionicons name="close" size={14} color={colors.destructiveForeground} />
              <Text style={[styles.removePhotoText, { color: colors.destructiveForeground }]}>
                {t('removePhoto')}
              </Text>
            </Pressable>
          ) : (
            <Text style={[styles.photoHint, { color: colors.mutedForeground }]}>
              {t('addCustomerPhoto')} (Gallery / Camera)
            </Text>
          )}
        </View>

        <FormField
          label={t('customerName')}
          value={name}
          onChangeText={setName}
          placeholder={t('enterCustomerName')}
        />

        {/* Plan Selection Dropdown / Chips */}
        <View style={styles.planSection}>
          <Text style={[styles.fieldLabel, { color: colors.foreground }]}>{t('selectPlan')}</Text>
          <View style={styles.planChipsContainer}>
            {MESS_PLANS.map((plan) => {
              const isSelected = selectedPlan === plan;
              return (
                <Pressable
                  key={plan}
                  onPress={() => handlePlanSelect(plan)}
                  style={[
                    styles.planChip,
                    {
                      borderColor: isSelected ? colors.primary : colors.border,
                      backgroundColor: isSelected ? colors.secondary : colors.background,
                    },
                  ]}
                >
                  <Ionicons
                    name={isSelected ? 'checkmark-circle' : 'ellipse-outline'}
                    size={16}
                    color={isSelected ? colors.primary : colors.mutedForeground}
                  />
                  <Text
                    style={[
                      styles.planChipText,
                      { color: isSelected ? colors.primary : colors.foreground },
                    ]}
                  >
                    {plan}
                  </Text>
                </Pressable>
              );
            })}
          </View>

          {selectedPlan === 'Custom Plan' ? (
            <FormField
              label={t('planName')}
              value={customPlan}
              onChangeText={setCustomPlan}
              placeholder={t('enterPlanName')}
            />
          ) : null}
        </View>

        <FormField
          label={t('joiningDate')}
          value={joiningDate}
          onChangeText={handleJoiningDateChange}
          placeholder={t('enterJoiningDate')}
        />
        <FormField
          label={t('expiryDate')}
          value={expiryDate}
          onChangeText={setExpiryDate}
          placeholder={t('enterExpiryDate')}
        />
        <FormField
          label={t('customerPhone')}
          value={phone}
          onChangeText={setPhone}
          placeholder={t('enterCustomerPhone')}
          keyboardType="phone-pad"
        />

        <View style={styles.statusField}>
          <Text style={[styles.statusLabel, { color: colors.foreground }]}>
            {t('paymentStatus')}
          </Text>
          <Text style={[styles.statusHint, { color: colors.mutedForeground }]}>
            {t('choosePaymentStatus')}
          </Text>
          <View style={styles.statusOptions}>
            {(['Paid', 'Unpaid'] as CustomerPaymentStatus[]).map((status) => (
              <Pressable
                key={status}
                testID={`payment-status-${status.toLowerCase()}`}
                onPress={() => setPaymentStatus(status)}
                style={[
                  styles.statusOption,
                  {
                    borderColor: paymentStatus === status ? colors.primary : colors.border,
                    backgroundColor:
                      paymentStatus === status ? colors.secondary : colors.background,
                  },
                ]}
              >
                <Ionicons
                  name={paymentStatus === status ? 'radio-button-on' : 'radio-button-off'}
                  size={18}
                  color={paymentStatus === status ? colors.primary : colors.mutedForeground}
                />
                <Text style={[styles.statusOptionText, { color: colors.foreground }]}>
                  {localizedValue(language, status)}
                </Text>
              </Pressable>
            ))}
          </View>
        </View>

        {paymentStatus === 'Paid' ? (
          <FormField
            label={t('amount')}
            value={amountPaid}
            onChangeText={setAmountPaid}
            placeholder={t('enterPaymentAmount')}
            keyboardType="numeric"
          />
        ) : null}

        <PrimaryButton label={t('addToCustomers')} icon="user-plus" onPress={add} />
      </View>
    </KeyboardAwareScrollViewCompat>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 20, paddingTop: 54, paddingBottom: 40, gap: 18 },
  summaryGrid: { flexDirection: 'row', gap: 8 },
  summaryCard: { flex: 1, borderRadius: 16, padding: 12, gap: 3, alignItems: 'center' },
  summaryValue: { fontSize: 20, fontWeight: '700', marginTop: 2 },
  summaryLabel: { fontSize: 10, fontWeight: '700', opacity: 0.85, textAlign: 'center' },
  alertBanner: {
    borderRadius: 16,
    borderWidth: 1,
    padding: 14,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  alertBannerCopy: { flex: 1, gap: 2 },
  alertBannerTitle: { fontSize: 14, fontWeight: '700' },
  alertBannerDetail: { fontSize: 12, opacity: 0.85 },
  filterSection: { gap: 10 },
  tabsRow: { flexDirection: 'row', gap: 8, paddingVertical: 4 },
  filterTab: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 13,
    paddingVertical: 8,
    borderRadius: 12,
    borderWidth: 1,
  },
  filterTabText: { fontSize: 12, fontWeight: '700' },
  countPill: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 8 },
  countPillText: { fontSize: 11, fontWeight: '700' },
  list: { borderWidth: 1, borderRadius: 19, paddingHorizontal: 14 },
  customerRow: { flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 14 },
  avatar: { width: 44, height: 44, borderRadius: 15, alignItems: 'center', justifyContent: 'center' },
  avatarImage: { width: 44, height: 44, borderRadius: 15 },
  avatarText: { fontSize: 13, fontWeight: '700' },
  customerCopy: { flex: 1, gap: 4 },
  nameLine: { flexDirection: 'column', alignItems: 'flex-start', gap: 4 },
  badgeGroup: { flexDirection: 'row', flexWrap: 'wrap', gap: 5 },
  customerName: { fontSize: 14, fontWeight: '700' },
  meta: { fontSize: 11 },
  actionButton: {
    minHeight: 34,
    paddingHorizontal: 10,
    borderRadius: 11,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
  },
  actionButtonText: { fontSize: 11, fontWeight: '700' },
  empty: { alignItems: 'center', gap: 8, paddingVertical: 24 },
  emptyTitle: { fontSize: 15, fontWeight: '700' },
  emptyDetail: { fontSize: 12, lineHeight: 18, textAlign: 'center' },
  formCard: { borderWidth: 1, borderRadius: 20, padding: 16, gap: 15 },
  photoPickerContainer: { alignItems: 'center', gap: 8, paddingVertical: 6 },
  photoButton: {
    width: 80,
    height: 80,
    borderRadius: 24,
    borderWidth: 1.5,
    borderStyle: 'dashed',
    alignItems: 'center',
    justifyContent: 'center',
    overflow: 'hidden',
  },
  formAvatarImage: { width: 80, height: 80, borderRadius: 24 },
  photoPlaceholder: { alignItems: 'center', gap: 4 },
  photoPlaceholderText: { fontSize: 10, fontWeight: '700' },
  photoHint: { fontSize: 11 },
  removePhotoButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 8,
  },
  removePhotoText: { fontSize: 11, fontWeight: '700' },
  fieldLabel: { fontSize: 13, fontWeight: '700' },
  planSection: { gap: 8 },
  planChipsContainer: { flexDirection: 'column', gap: 6 },
  planChip: {
    borderWidth: 1,
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 10,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  planChipText: { fontSize: 13, fontWeight: '600' },
  statusField: { gap: 7 },
  statusLabel: { fontSize: 13, fontWeight: '700' },
  statusHint: { fontSize: 11 },
  statusOptions: { flexDirection: 'row', gap: 8 },
  statusOption: {
    flex: 1,
    minHeight: 45,
    borderWidth: 1,
    borderRadius: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  statusOptionText: { fontSize: 13, fontWeight: '700' },
});