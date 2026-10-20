import { addDaysISO, todayISO } from '@/lib/dates';
import { Ionicons } from '@expo/vector-icons';
import * as ImagePicker from 'expo-image-picker';
import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import {
  Alert,
  Image,
  Linking,
  Modal,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native';
import { KeyboardAwareScrollViewCompat } from '@/components/KeyboardAwareScrollViewCompat';
import { Badge, FormField, Header } from '@/components/UI';
import { SendWhatsAppButton } from '@/components/SendWhatsAppButton';
import {
  Customer,
  CustomerPaymentStatus,
  formatDate,
  getExpiryInfo,
  MESS_PLANS,
  useMess,
} from '@/context/AppContext';
import { useColors } from '@/hooks/useColors';
import { useTranslation } from '@/hooks/useTranslation';
import { localizedValue } from '@/lib/i18n';

type FilterTab = 'all' | 'active' | 'expiring' | 'expired' | 'unpaid';

export default function CustomersScreen() {
  const colors = useColors();
  const { customers, addCustomer, updateCustomer, deleteCustomer, addPayment } =
    useMess();
  const { language, t } = useTranslation();

  const todayStr = useMemo(() => todayISO(), []);

  // Search & Filter State
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState<FilterTab>('all');

  // Add Customer Modal State
  const [addModalVisible, setAddModalVisible] = useState(false);
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
  const [amountPaid, setAmountPaid] = useState('3000');
  const [imageUri, setImageUri] = useState<string | undefined>(undefined);

  // Customer Details Modal State
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);

  // Renew Modal State
  const [renewModalVisible, setRenewModalVisible] = useState(false);
  const [renewCustomerId, setRenewCustomerId] = useState('');
  const [renewAmount, setRenewAmount] = useState('3000');

  // Post-add WhatsApp prompt state
  const [postAddCustomer, setPostAddCustomer] = useState<{ id: string; name: string; phone: string; expiryDate: string; joiningDate: string; plan: string; amount: string } | null>(null);
  // Post-renew WhatsApp prompt state
  const [postRenewCustomer, setPostRenewCustomer] = useState<{ id: string; name: string; phone: string; expiryDate: string; prevExpiry: string; amount: string; plan: string } | null>(null);

  // Auto calculate expiry & default amount when plan changes
  const handlePlanSelect = (plan: string) => {
    setSelectedPlan(plan);
    const start = joiningDate ? new Date(`${joiningDate}T12:00:00`) : new Date();
    if (isNaN(start.getTime())) return;

    const p = plan.toLowerCase();
    if (p.includes('15-day') || p.includes('15')) {
      const exp = new Date(start);
      exp.setDate(exp.getDate() + 15);
      setExpiryDate(exp.toISOString().slice(0, 10));
      setAmountPaid('1500');
    } else if (p.includes('daily') || p.includes('per meal')) {
      const exp = new Date(start);
      exp.setDate(exp.getDate() + 1);
      setExpiryDate(exp.toISOString().slice(0, 10));
      setAmountPaid('150');
    } else if (p.includes('two') || p.includes('both')) {
      const exp = new Date(start);
      exp.setDate(exp.getDate() + 30);
      setExpiryDate(exp.toISOString().slice(0, 10));
      setAmountPaid('4500');
    } else {
      const exp = new Date(start);
      exp.setDate(exp.getDate() + 30);
      setExpiryDate(exp.toISOString().slice(0, 10));
      setAmountPaid('3000');
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
        Alert.alert(
          t('errorTitle') || 'Permission Required',
          'Permission to access gallery is required to choose a customer photo.'
        );
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
      Alert.alert('Gallery Error', 'Could not open image library. Please check app permissions.');
    }
  };

  const takePhoto = async () => {
    try {
      const permissionResult = await ImagePicker.requestCameraPermissionsAsync();
      if (!permissionResult.granted) {
        Alert.alert(
          t('errorTitle') || 'Permission Required',
          'Permission to access camera is required to take a customer photo.'
        );
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
      Alert.alert('Camera Error', 'Could not open camera. Please check camera permissions.');
    }
  };

  const showPhotoOptions = () => {
    Alert.alert(
      t('addCustomerPhoto'),
      'Choose how you want to add the customer photo:',
      [
        { text: t('takePhoto'), onPress: takePhoto },
        { text: t('chooseFromLibrary'), onPress: pickImage },
        { text: t('cancel'), style: 'cancel' },
      ]
    );
  };

  const openAddCustomer = () => {
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
    setAmountPaid('3000');
    setAddModalVisible(true);
  };

  const handleCreateCustomer = () => {
    const finalPlan = selectedPlan === 'Custom Plan' ? customPlan.trim() : selectedPlan;
    if (!name.trim() || !finalPlan || !joiningDate.trim() || !expiryDate.trim() || !phone.trim()) {
      Alert.alert(t('completeCustomer'), t('customerRequiredMessage'));
      return;
    }
    const newCustomerId = addCustomer({
      name: name.trim(),
      plan: finalPlan,
      joiningDate: joiningDate.trim(),
      expiryDate: expiryDate.trim(),
      phone: phone.trim(),
      paymentStatus,
      imageUri,
      amount: paymentStatus === 'Paid' && amountPaid.trim() !== '' && !isNaN(Number(amountPaid)) ? Number(amountPaid) : undefined,
      subscriptionStatus: 'active',
    });

    if (paymentStatus === 'Paid' && amountPaid.trim() !== '' && !isNaN(Number(amountPaid))) {
      addPayment({
        amount: Number(amountPaid),
        method: 'Cash/UPI',
        note: `Subscription: ${name.trim()} - ${finalPlan}`,
        customerId: newCustomerId,
        date: todayStr,
      });
    }

    setAddModalVisible(false);
    setPostAddCustomer({ id: newCustomerId, name: name.trim(), phone: phone.trim(), expiryDate: expiryDate.trim(), joiningDate: joiningDate.trim(), plan: finalPlan, amount: amountPaid });
  };

  const handleCall = (phoneNumber: string) => {
    if (!phoneNumber) return;
    const cleanPhone = phoneNumber.replace(/[^0-9+]/g, '');
    Linking.openURL(`tel:${cleanPhone}`).catch(() => {
      Alert.alert(t('errorTitle') || 'Error', 'Unable to initiate phone call.');
    });
  };

  const handleRenew = (customer: Customer) => {
    setRenewCustomerId(customer.id);
    let defaultRenew = '3000';
    const p = customer.plan.toLowerCase();
    if (p.includes('15')) defaultRenew = '1500';
    else if (p.includes('daily')) defaultRenew = '150';
    else if (p.includes('two') || p.includes('both')) defaultRenew = '4500';
    setRenewAmount(defaultRenew);
    setRenewModalVisible(true);
  };

  const confirmRenew = () => {
    const customer = customers.find((c) => c.id === renewCustomerId);
    if (!customer) return;

    let duration = 30;
    if (customer.plan.includes('15')) duration = 15;
    if (customer.plan.includes('Daily') || customer.plan.includes('Per Meal')) duration = 1;

    const amt = Number(renewAmount);
    if (isNaN(amt) || amt <= 0) {
      Alert.alert(t('errorTitle') || 'Error', 'Please enter a valid amount.');
      return;
    }

    const info = getExpiryInfo(customer.expiryDate);
    const baseDate = info.status === 'expired' || !customer.expiryDate ? todayISO() : customer.expiryDate;
    const newExpiry = addDaysISO(baseDate, duration);

    updateCustomer(renewCustomerId, {
      expiryDate: newExpiry,
      paymentStatus: 'Paid',
      amount: amt,
      lastRenewedAt: todayISO(),
      subscriptionStatus: 'active',
    });
    addPayment({
      amount: amt,
      method: 'Cash/UPI',
      note: `Plan Renewal: ${customer.name} - ${customer.plan}`,
      customerId: renewCustomerId,
      date: todayISO(),
    });

    setRenewModalVisible(false);
    setRenewAmount('');
    setPostRenewCustomer({ id: customer.id, name: customer.name, phone: customer.phone, expiryDate: newExpiry, prevExpiry: customer.expiryDate, amount: String(amt), plan: customer.plan });

    // If detail modal is open for this customer, update view
    if (selectedCustomer && selectedCustomer.id === renewCustomerId) {
      setSelectedCustomer({
        ...selectedCustomer,
        expiryDate: newExpiry,
        paymentStatus: 'Paid',
      });
    }
  };

  const handleDeleteCustomer = (customerId: string, customerName: string) => {
    Alert.alert(
      t('deleteCustomer'),
      t('deleteCustomerConfirm'),
      [
        { text: t('cancel'), style: 'cancel' },
        {
          text: t('delete'),
          style: 'destructive',
          onPress: () => {
            deleteCustomer(customerId);
            if (selectedCustomer?.id === customerId) {
              setSelectedCustomer(null);
            }
          },
        },
      ]
    );
  };

  // Compute metrics
  const customerStats = useMemo(() => {
    let activeCount = 0;
    let expiringCount = 0;
    let expiredCount = 0;
    let unpaidCount = 0;

    customers.forEach((c) => {
      const exp = getExpiryInfo(c.expiryDate);
      if (exp.isExpired) expiredCount++;
      else if (exp.isExpiringSoon) expiringCount++;
      else activeCount++;

      if (c.paymentStatus === 'Unpaid') {
        unpaidCount++;
      }
    });

    return { activeCount, expiringCount, expiredCount, unpaidCount };
  }, [customers]);

  // Search & Filter computation
  const filteredCustomers = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    return customers.filter((customer) => {
      // 1. Search query match
      if (q) {
        const matchName = customer.name.toLowerCase().includes(q);
        const matchPhone = customer.phone.toLowerCase().includes(q);
        const matchPlan = customer.plan.toLowerCase().includes(q);
        if (!matchName && !matchPhone && !matchPlan) return false;
      }

      // 2. Tab Filter match
      const exp = getExpiryInfo(customer.expiryDate);
      if (activeFilter === 'active') return !exp.isExpired && !exp.isExpiringSoon;
      if (activeFilter === 'expiring') return exp.isExpiringSoon;
      if (activeFilter === 'expired') return exp.isExpired;
      if (activeFilter === 'unpaid') return customer.paymentStatus === 'Unpaid';

      return true;
    });
  }, [customers, searchQuery, activeFilter]);

  return (
    <KeyboardAwareScrollViewCompat
      style={[styles.screen, { backgroundColor: colors.background }]}
      contentContainerStyle={styles.content}
      bottomOffset={24}
      keyboardShouldPersistTaps="handled"
    >
      <Header
        eyebrow={t('ownerTools')}
        title={t('customers')}
        subtitle={t('customerListSubtitle')}
        onPress={() => router.push('/(tabs)/admin')}
      />

      {/* Top Action Bar: Add Customer Button & Stats Summary */}
      <View style={styles.topActionBar}>
        <Pressable
          onPress={() => router.push('/whatsapp-queue' as any)}
          style={({ pressed }) => [styles.queueButton, { backgroundColor: '#E8F5E9', opacity: pressed ? 0.8 : 1 }]}
        >
          <Ionicons name="logo-whatsapp" size={17} color="#2E7D32" />
          <Text style={styles.queueButtonText}>{t('whatsappQueue')}</Text>
        </Pressable>
        <Pressable
          testID="add-new-customer-btn"
          onPress={openAddCustomer}
          style={({ pressed }) => [
            styles.addCustomerHeroButton,
            { backgroundColor: colors.primary, opacity: pressed ? 0.85 : 1 },
          ]}
        >
          <Ionicons name="person-add" size={18} color={colors.primaryForeground} />
          <Text style={[styles.addCustomerHeroText, { color: colors.primaryForeground }]}>
            {t('newCustomer')}
          </Text>
        </Pressable>
      </View>

      {/* Summary Stats Grid */}
      <View style={styles.summaryGrid}>
        <Pressable
          onPress={() => setActiveFilter('all')}
          style={[
            styles.summaryCard,
            {
              backgroundColor: activeFilter === 'all' ? colors.primary : colors.card,
              borderColor: activeFilter === 'all' ? colors.primary : colors.border,
            },
          ]}
        >
          <Ionicons
            name="people"
            size={18}
            color={activeFilter === 'all' ? colors.accent : colors.primary}
          />
          <Text
            style={[
              styles.summaryValue,
              { color: activeFilter === 'all' ? colors.primaryForeground : colors.foreground },
            ]}
          >
            {customers.length}
          </Text>
          <Text
            style={[
              styles.summaryLabel,
              { color: activeFilter === 'all' ? colors.primaryForeground : colors.mutedForeground },
            ]}
          >
            {t('all')}
          </Text>
        </Pressable>

        <Pressable
          onPress={() => setActiveFilter('active')}
          style={[
            styles.summaryCard,
            {
              backgroundColor: activeFilter === 'active' ? colors.secondary : colors.card,
              borderColor: activeFilter === 'active' ? colors.primary : colors.border,
            },
          ]}
        >
          <Ionicons name="shield-checkmark" size={18} color={colors.primary} />
          <Text style={[styles.summaryValue, { color: colors.primary }]}>
            {customerStats.activeCount}
          </Text>
          <Text style={[styles.summaryLabel, { color: colors.primary }]}>{t('active')}</Text>
        </Pressable>

        <Pressable
          onPress={() => setActiveFilter('expiring')}
          style={[
            styles.summaryCard,
            {
              backgroundColor: activeFilter === 'expiring' ? colors.accent : colors.card,
              borderColor: activeFilter === 'expiring' ? colors.accentForeground : colors.border,
            },
          ]}
        >
          <Ionicons
            name="time"
            size={18}
            color={activeFilter === 'expiring' ? colors.accentForeground : colors.mutedForeground}
          />
          <Text
            style={[
              styles.summaryValue,
              {
                color:
                  activeFilter === 'expiring' ? colors.accentForeground : colors.foreground,
              },
            ]}
          >
            {customerStats.expiringCount}
          </Text>
          <Text
            style={[
              styles.summaryLabel,
              {
                color:
                  activeFilter === 'expiring'
                    ? colors.accentForeground
                    : colors.mutedForeground,
              },
            ]}
          >
            {t('expiringSoon')}
          </Text>
        </Pressable>

        <Pressable
          onPress={() => setActiveFilter('expired')}
          style={[
            styles.summaryCard,
            {
              backgroundColor: activeFilter === 'expired' ? colors.destructive : colors.card,
              borderColor: activeFilter === 'expired' ? colors.destructive : colors.border,
            },
          ]}
        >
          <Ionicons
            name="alert-circle"
            size={18}
            color={activeFilter === 'expired' ? colors.destructiveForeground : colors.destructive}
          />
          <Text
            style={[
              styles.summaryValue,
              {
                color:
                  activeFilter === 'expired'
                    ? colors.destructiveForeground
                    : colors.destructive,
              },
            ]}
          >
            {customerStats.expiredCount}
          </Text>
          <Text
            style={[
              styles.summaryLabel,
              {
                color:
                  activeFilter === 'expired'
                    ? colors.destructiveForeground
                    : colors.mutedForeground,
              },
            ]}
          >
            {t('expired')}
          </Text>
        </Pressable>
      </View>

      {/* Expiry Banner */}
      {customerStats.expiringCount > 0 || customerStats.expiredCount > 0 ? (
        <View
          style={[
            styles.alertBanner,
            { backgroundColor: colors.accent, borderColor: colors.accentForeground },
          ]}
        >
          <Ionicons name="notifications" size={20} color={colors.accentForeground} />
          <View style={styles.alertBannerCopy}>
            <Text style={[styles.alertBannerTitle, { color: colors.accentForeground }]}>
              {t('expiryAlerts')}
            </Text>
            <Text style={[styles.alertBannerDetail, { color: colors.accentForeground }]}>
              {t('membersExpiringBanner', {
                count: customerStats.expiringCount + customerStats.expiredCount,
                plural: customerStats.expiringCount + customerStats.expiredCount === 1 ? '' : 's',
              })}
            </Text>
          </View>
        </View>
      ) : null}

      {/* Primary Search Bar */}
      <View
        style={[
          styles.searchBarContainer,
          { backgroundColor: colors.card, borderColor: colors.border },
        ]}
      >
        <Ionicons name="search" size={20} color={colors.mutedForeground} style={styles.searchIcon} />
        <TextInput
          style={[styles.searchInput, { color: colors.foreground }]}
          placeholder={t('searchCustomers')}
          placeholderTextColor={colors.mutedForeground}
          value={searchQuery}
          onChangeText={setSearchQuery}
          autoCapitalize="none"
          autoCorrect={false}
          clearButtonMode="while-editing"
        />
        {searchQuery.length > 0 ? (
          <Pressable onPress={() => setSearchQuery('')} hitSlop={8}>
            <Ionicons name="close-circle" size={18} color={colors.mutedForeground} />
          </Pressable>
        ) : null}
      </View>

      {/* Filter Tabs Row */}
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.tabsRow}
      >
        {(['all', 'active', 'expiring', 'expired', 'unpaid'] as FilterTab[]).map((tab) => {
          const isSelected = activeFilter === tab;
          const count =
            tab === 'all'
              ? customers.length
              : tab === 'active'
              ? customerStats.activeCount
              : tab === 'expiring'
              ? customerStats.expiringCount
              : tab === 'expired'
              ? customerStats.expiredCount
              : customerStats.unpaidCount;

          const label =
            tab === 'all'
              ? t('all')
              : tab === 'active'
              ? t('active')
              : tab === 'expiring'
              ? t('expiringSoon')
              : tab === 'expired'
              ? t('expired')
              : t('unpaid');

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

      {/* Customer Directory List */}
      <View style={styles.listContainer}>
        {filteredCustomers.length ? (
          filteredCustomers.map((customer) => {
            const exp = getExpiryInfo(customer.expiryDate);

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
              <Pressable
                key={customer.id}
                onPress={() => setSelectedCustomer(customer)}
                style={({ pressed }) => [
                  styles.customerCard,
                  {
                    backgroundColor: colors.card,
                    borderColor: colors.border,
                    opacity: pressed ? 0.95 : 1,
                  },
                ]}
              >
                <View style={styles.cardHeaderRow}>
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

                  <View style={styles.customerHeaderInfo}>
                    <View style={styles.nameRow}>
                      <Text
                        style={[styles.customerName, { color: colors.foreground }]}
                        numberOfLines={1}
                      >
                        {customer.name}
                      </Text>
                      <Ionicons name="chevron-forward" size={16} color={colors.mutedForeground} />
                    </View>

                    <View style={styles.badgeRow}>
                      <Badge label={expiryLabel} tone={expiryTone} />
                      <Badge
                        label={localizedValue(language, customer.paymentStatus)}
                        tone={customer.paymentStatus === 'Paid' ? 'green' : 'amber'}
                      />
                    </View>
                  </View>
                </View>

                {/* Meta details */}
                <View style={[styles.cardMetaContainer, { borderTopColor: colors.border }]}>
                  <View style={styles.metaLine}>
                    <Ionicons name="restaurant-outline" size={13} color={colors.mutedForeground} />
                    <Text style={[styles.metaText, { color: colors.foreground }]}>
                      {customer.plan}
                    </Text>
                  </View>

                  <View style={styles.metaLine}>
                    <Ionicons name="calendar-outline" size={13} color={colors.mutedForeground} />
                    <Text style={[styles.metaText, { color: colors.mutedForeground }]}>
                      {formatDate(customer.joiningDate, language)} →{' '}
                      {formatDate(customer.expiryDate, language)}
                    </Text>
                  </View>
                </View>

                {/* Card Quick Action Buttons */}
                <View style={[styles.quickActionsRow, { borderTopColor: colors.border }]}>
                  <Pressable
                    onPress={() => handleCall(customer.phone)}
                    style={({ pressed }) => [
                      styles.quickActionButton,
                      { backgroundColor: colors.secondary, opacity: pressed ? 0.75 : 1 },
                    ]}
                  >
                    <Ionicons name="call" size={14} color={colors.primary} />
                    <Text style={[styles.quickActionText, { color: colors.primary }]}>
                      {t('callCustomer')}
                    </Text>
                  </Pressable>

                  <SendWhatsAppButton customer={customer} compact />

                  <Pressable
                    onPress={() => handleRenew(customer)}
                    style={({ pressed }) => [
                      styles.quickActionButton,
                      { backgroundColor: colors.primary, opacity: pressed ? 0.75 : 1 },
                    ]}
                  >
                    <Ionicons name="refresh-outline" size={14} color={colors.primaryForeground} />
                    <Text style={[styles.quickActionText, { color: colors.primaryForeground }]}>
                      {t('renewPlan')}
                    </Text>
                  </Pressable>
                </View>
              </Pressable>
            );
          })
        ) : searchQuery.length > 0 ? (
          <View style={[styles.emptyCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <Ionicons name="search-outline" size={36} color={colors.mutedForeground} />
            <Text style={[styles.emptyTitle, { color: colors.foreground }]}>
              {t('noSearchResults')}
            </Text>
            <Text style={[styles.emptyDetail, { color: colors.mutedForeground }]}>
              {t('noSearchResultsDetail')}
            </Text>
            <Pressable
              onPress={() => setSearchQuery('')}
              style={[styles.clearSearchBtn, { backgroundColor: colors.secondary }]}
            >
              <Text style={[styles.clearSearchBtnText, { color: colors.primary }]}>
                {t('clearSearch')}
              </Text>
            </Pressable>
          </View>
        ) : (
          <View style={[styles.emptyCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
            <Ionicons name="people-outline" size={36} color={colors.primary} />
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
            <Pressable
              onPress={openAddCustomer}
              style={[styles.addFirstBtn, { backgroundColor: colors.primary }]}
            >
              <Ionicons name="person-add" size={16} color={colors.primaryForeground} />
              <Text style={[styles.addFirstBtnText, { color: colors.primaryForeground }]}>
                {t('addNewCustomer')}
              </Text>
            </Pressable>
          </View>
        )}
      </View>

      {/* ADD CUSTOMER MODAL */}
      <Modal
        visible={addModalVisible}
        animationType="slide"
        transparent
        onRequestClose={() => setAddModalVisible(false)}
      >
        <View style={styles.modalBackdrop}>
          <View
            style={[
              styles.modalSheet,
              { backgroundColor: colors.background, borderColor: colors.border },
            ]}
          >
            <View style={[styles.modalHeader, { borderBottomColor: colors.border }]}>
              <View>
                <Text style={[styles.modalSheetTitle, { color: colors.foreground }]}>
                  {t('addNewCustomer')}
                </Text>
                <Text style={[styles.modalSheetSubtitle, { color: colors.mutedForeground }]}>
                  {t('customerListSubtitle')}
                </Text>
              </View>
              <Pressable
                onPress={() => setAddModalVisible(false)}
                style={[styles.closeCircleBtn, { backgroundColor: colors.secondary }]}
              >
                <Ionicons name="close" size={20} color={colors.foreground} />
              </Pressable>
            </View>

            <KeyboardAwareScrollViewCompat
              style={styles.modalScroll}
              contentContainerStyle={styles.modalScrollContent}
              bottomOffset={20}
              keyboardShouldPersistTaps="handled"
            >
              {/* Photo Picker in Add Modal */}
              <View style={styles.photoPickerContainer}>
                <Pressable
                  onPress={showPhotoOptions}
                  style={[
                    styles.photoAvatarButton,
                    {
                      borderColor: imageUri ? colors.primary : colors.border,
                      backgroundColor: colors.secondary,
                    },
                  ]}
                >
                  {imageUri ? (
                    <Image source={{ uri: imageUri }} style={styles.formAvatarImage} />
                  ) : (
                    <View style={styles.photoPlaceholder}>
                      <Ionicons name="camera-outline" size={32} color={colors.primary} />
                      <Text style={[styles.photoAddPrompt, { color: colors.primary }]}>
                        {t('addCustomerPhoto')}
                      </Text>
                    </View>
                  )}
                  <View style={[styles.cameraBadge, { backgroundColor: colors.primary }]}>
                    <Ionicons
                      name={imageUri ? 'sync-outline' : 'camera'}
                      size={12}
                      color={colors.primaryForeground}
                    />
                  </View>
                </Pressable>

                <View style={styles.photoActionsRow}>
                  <Pressable
                    onPress={takePhoto}
                    style={[
                      styles.photoActionChip,
                      { backgroundColor: colors.secondary, borderColor: colors.border },
                    ]}
                  >
                    <Ionicons name="camera-outline" size={14} color={colors.primary} />
                    <Text style={[styles.photoActionChipText, { color: colors.primary }]}>
                      {t('takePhoto')}
                    </Text>
                  </Pressable>

                  <Pressable
                    onPress={pickImage}
                    style={[
                      styles.photoActionChip,
                      { backgroundColor: colors.secondary, borderColor: colors.border },
                    ]}
                  >
                    <Ionicons name="images-outline" size={14} color={colors.primary} />
                    <Text style={[styles.photoActionChipText, { color: colors.primary }]}>
                      {t('chooseFromLibrary')}
                    </Text>
                  </Pressable>

                  {imageUri ? (
                    <Pressable
                      onPress={() => setImageUri(undefined)}
                      style={[styles.photoActionChip, { backgroundColor: colors.destructive }]}
                    >
                      <Ionicons name="trash-outline" size={14} color={colors.destructiveForeground} />
                      <Text
                        style={[
                          styles.photoActionChipText,
                          { color: colors.destructiveForeground },
                        ]}
                      >
                        {t('removePhoto')}
                      </Text>
                    </Pressable>
                  ) : null}
                </View>
              </View>

              <FormField
                label={t('customerName')}
                value={name}
                onChangeText={setName}
                placeholder={t('enterCustomerName')}
              />

              {/* Plan Selection Dropdown / Chips */}
              <View style={styles.planSection}>
                <Text style={[styles.fieldLabel, { color: colors.foreground }]}>
                  {t('selectPlan')}
                </Text>
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
                            backgroundColor: isSelected ? colors.secondary : colors.card,
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
                <View style={styles.statusOptions}>
                  {(['Paid', 'Unpaid'] as CustomerPaymentStatus[]).map((status) => (
                    <Pressable
                      key={status}
                      onPress={() => setPaymentStatus(status)}
                      style={[
                        styles.statusOption,
                        {
                          borderColor: paymentStatus === status ? colors.primary : colors.border,
                          backgroundColor:
                            paymentStatus === status ? colors.secondary : colors.card,
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

              <View style={styles.modalButtonRow}>
                <Pressable
                  onPress={() => setAddModalVisible(false)}
                  style={[styles.modalCancelBtn, { backgroundColor: colors.muted }]}
                >
                  <Text style={[styles.modalCancelBtnText, { color: colors.foreground }]}>
                    {t('cancel')}
                  </Text>
                </Pressable>

                <Pressable
                  onPress={handleCreateCustomer}
                  style={[styles.modalSubmitBtn, { backgroundColor: colors.primary }]}
                >
                  <Ionicons name="checkmark" size={18} color={colors.primaryForeground} />
                  <Text style={[styles.modalSubmitBtnText, { color: colors.primaryForeground }]}>
                    {t('addToCustomers')}
                  </Text>
                </Pressable>
              </View>
            </KeyboardAwareScrollViewCompat>
          </View>
        </View>
      </Modal>

      {/* POST-ENROLLMENT WHATSAPP PROMPT */}
      {postAddCustomer ? (
        <Modal visible transparent animationType="fade" onRequestClose={() => setPostAddCustomer(null)}>
          <View style={styles.modalBackdrop}>
            <View style={[styles.modalCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
              <Text style={[styles.modalTitle, { color: colors.foreground }]}>{t('sendWelcomeWhatsApp')}</Text>
              <Text style={[styles.modalDetail, { color: colors.mutedForeground }]}>{postAddCustomer.name}</Text>
              <View style={styles.modalActions}>
                <Pressable onPress={() => setPostAddCustomer(null)} style={[styles.modalButton, { backgroundColor: colors.muted }]}><Text style={[styles.modalButtonText, { color: colors.foreground }]}>{t('skipForNow')}</Text></Pressable>
                <View style={{ flex: 1 }}>
                  <SendWhatsAppButton customer={{ id: postAddCustomer.id, name: postAddCustomer.name, phone: postAddCustomer.phone, joiningDate: postAddCustomer.joiningDate, expiryDate: postAddCustomer.expiryDate, plan: postAddCustomer.plan, amount: Number(postAddCustomer.amount) || undefined, paymentStatus: 'Paid' }} explicitType="ENROLLED" enrolledFrom={postAddCustomer.joiningDate} enrolledTo={postAddCustomer.expiryDate} />
                </View>
              </View>
            </View>
          </View>
        </Modal>
      ) : null}

      {/* POST-RENEWAL WHATSAPP PROMPT */}
      {postRenewCustomer ? (
        <Modal visible transparent animationType="fade" onRequestClose={() => setPostRenewCustomer(null)}>
          <View style={styles.modalBackdrop}>
            <View style={[styles.modalCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
              <Text style={[styles.modalTitle, { color: colors.foreground }]}>{t('sendRenewalWhatsApp')}</Text>
              <Text style={[styles.modalDetail, { color: colors.mutedForeground }]}>{postRenewCustomer.name}</Text>
              <View style={styles.modalActions}>
                <Pressable onPress={() => setPostRenewCustomer(null)} style={[styles.modalButton, { backgroundColor: colors.muted }]}><Text style={[styles.modalButtonText, { color: colors.foreground }]}>{t('skipForNow')}</Text></Pressable>
                <View style={{ flex: 1 }}>
                  <SendWhatsAppButton customer={{ id: postRenewCustomer.id, name: postRenewCustomer.name, phone: postRenewCustomer.phone, joiningDate: postRenewCustomer.prevExpiry, expiryDate: postRenewCustomer.expiryDate, plan: postRenewCustomer.plan, amount: Number(postRenewCustomer.amount) || undefined, paymentStatus: 'Paid' }} explicitType="RENEWED" renewedFrom={todayISO()} renewedTo={postRenewCustomer.expiryDate} />
                </View>
              </View>
            </View>
          </View>
        </Modal>
      ) : null}

      {/* CUSTOMER DETAILS MODAL */}
      {selectedCustomer && (
        <Modal
          visible={!!selectedCustomer}
          animationType="fade"
          transparent
          onRequestClose={() => setSelectedCustomer(null)}
        >
          <View style={styles.modalBackdrop}>
            <View
              style={[
                styles.detailCardModal,
                { backgroundColor: colors.card, borderColor: colors.border },
              ]}
            >
              <View style={styles.detailHeader}>
                <Text style={[styles.detailTitle, { color: colors.foreground }]}>
                  {t('customerDetails')}
                </Text>
                <Pressable
                  onPress={() => setSelectedCustomer(null)}
                  style={[styles.closeCircleBtn, { backgroundColor: colors.secondary }]}
                >
                  <Ionicons name="close" size={18} color={colors.foreground} />
                </Pressable>
              </View>

              <ScrollView
                style={styles.detailBody}
                contentContainerStyle={styles.detailBodyContent}
                showsVerticalScrollIndicator={false}
              >
                {/* Large Profile Picture / Monogram */}
                <View style={styles.detailAvatarContainer}>
                  {selectedCustomer.imageUri ? (
                    <Image
                      source={{ uri: selectedCustomer.imageUri }}
                      style={styles.detailAvatarImage}
                    />
                  ) : (
                    <View style={[styles.detailAvatarPlaceholder, { backgroundColor: colors.secondary }]}>
                      <Text style={[styles.detailAvatarText, { color: colors.primary }]}>
                        {selectedCustomer.name
                          .split(' ')
                          .map((word) => word[0])
                          .join('')
                          .slice(0, 2)
                          .toUpperCase()}
                      </Text>
                    </View>
                  )}
                  <Text style={[styles.detailCustomerName, { color: colors.foreground }]}>
                    {selectedCustomer.name}
                  </Text>
                  <Text style={[styles.detailCustomerPhone, { color: colors.mutedForeground }]}>
                    {selectedCustomer.phone}
                  </Text>
                </View>

                {/* Direct Action Chips in Details */}
                <View style={styles.detailActionGrid}>
                  <Pressable
                    onPress={() => handleCall(selectedCustomer.phone)}
                    style={[styles.detailActionBtn, { backgroundColor: colors.secondary }]}
                  >
                    <Ionicons name="call" size={18} color={colors.primary} />
                    <Text style={[styles.detailActionBtnText, { color: colors.primary }]}>
                      {t('callCustomer')}
                    </Text>
                  </Pressable>

                  <View style={styles.detailActionBtn}>
                    <SendWhatsAppButton customer={selectedCustomer} />
                  </View>
                </View>

                {/* Plan & Subscription Card */}
                <View
                  style={[
                    styles.detailSectionBox,
                    { backgroundColor: colors.background, borderColor: colors.border },
                  ]}
                >
                  <Text style={[styles.detailSectionHeading, { color: colors.foreground }]}>
                    {t('subscriptionDetails')}
                  </Text>

                  <View style={styles.detailInfoRow}>
                    <Text style={[styles.detailInfoLabel, { color: colors.mutedForeground }]}>
                      {t('planName')}:
                    </Text>
                    <Text style={[styles.detailInfoValue, { color: colors.foreground }]}>
                      {selectedCustomer.plan}
                    </Text>
                  </View>

                  <View style={styles.detailInfoRow}>
                    <Text style={[styles.detailInfoLabel, { color: colors.mutedForeground }]}>
                      {t('paymentStatus')}:
                    </Text>
                    <Badge
                      label={localizedValue(language, selectedCustomer.paymentStatus)}
                      tone={selectedCustomer.paymentStatus === 'Paid' ? 'green' : 'amber'}
                    />
                  </View>

                  <View style={styles.detailInfoRow}>
                    <Text style={[styles.detailInfoLabel, { color: colors.mutedForeground }]}>
                      {t('joiningDate')}:
                    </Text>
                    <Text style={[styles.detailInfoValue, { color: colors.foreground }]}>
                      {formatDate(selectedCustomer.joiningDate, language)}
                    </Text>
                  </View>

                  <View style={styles.detailInfoRow}>
                    <Text style={[styles.detailInfoLabel, { color: colors.mutedForeground }]}>
                      {t('expiryDate')}:
                    </Text>
                    <Text style={[styles.detailInfoValue, { color: colors.foreground }]}>
                      {formatDate(selectedCustomer.expiryDate, language)}
                    </Text>
                  </View>

                  {/* Expiry Badge */}
                  {(() => {
                    const exp = getExpiryInfo(selectedCustomer.expiryDate);
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
                    }

                    return (
                      <View style={styles.detailInfoRow}>
                        <Text style={[styles.detailInfoLabel, { color: colors.mutedForeground }]}>
                          {t('validity')}:
                        </Text>
                        <Badge label={expiryLabel} tone={expiryTone} />
                      </View>
                    );
                  })()}
                </View>

                {/* Renew & Delete buttons in details */}
                <View style={styles.detailFooterActions}>
                  <Pressable
                    onPress={() => handleRenew(selectedCustomer)}
                    style={[styles.detailRenewBtn, { backgroundColor: colors.primary }]}
                  >
                    <Ionicons name="refresh" size={16} color={colors.primaryForeground} />
                    <Text style={[styles.detailRenewBtnText, { color: colors.primaryForeground }]}>
                      {t('renewPlan')}
                    </Text>
                  </Pressable>

                  <Pressable
                    onPress={() =>
                      handleDeleteCustomer(selectedCustomer.id, selectedCustomer.name)
                    }
                    style={[styles.detailDeleteBtn, { backgroundColor: colors.destructive }]}
                  >
                    <Ionicons name="trash-outline" size={16} color={colors.destructiveForeground} />
                    <Text
                      style={[
                        styles.detailDeleteBtnText,
                        { color: colors.destructiveForeground },
                      ]}
                    >
                      {t('deleteCustomer')}
                    </Text>
                  </Pressable>
                </View>
              </ScrollView>
            </View>
          </View>
        </Modal>
      )}

      {/* RENEW / EXTEND PLAN MODAL */}
      {renewModalVisible && (
        <Modal
          visible={renewModalVisible}
          animationType="fade"
          transparent
          onRequestClose={() => setRenewModalVisible(false)}
        >
          <View style={styles.modalBackdrop}>
            <View
              style={[
                styles.modalCard,
                { backgroundColor: colors.card, borderColor: colors.border },
              ]}
            >
              <Text style={[styles.modalTitle, { color: colors.foreground }]}>
                {t('renewPlan')}
              </Text>
              <Text style={[styles.modalDetail, { color: colors.mutedForeground }]}>
                {t('enterPaymentAmount')}
              </Text>

              <FormField
                label={t('amount')}
                value={renewAmount}
                onChangeText={setRenewAmount}
                keyboardType="numeric"
                placeholder="0"
              />

              <View style={styles.modalActions}>
                <Pressable
                  onPress={() => {
                    setRenewModalVisible(false);
                    setRenewAmount('');
                  }}
                  style={[styles.modalButton, { backgroundColor: colors.muted }]}
                >
                  <Text style={[styles.modalButtonText, { color: colors.foreground }]}>
                    {t('cancel')}
                  </Text>
                </Pressable>
                <Pressable
                  onPress={confirmRenew}
                  style={[styles.modalButton, { backgroundColor: colors.primary }]}
                >
                  <Text style={[styles.modalButtonText, { color: colors.primaryForeground }]}>
                    {t('confirm')}
                  </Text>
                </Pressable>
              </View>
            </View>
          </View>
        </Modal>
      )}
    </KeyboardAwareScrollViewCompat>
  );
}

const styles = StyleSheet.create({
  screen: { flex: 1 },
  content: { paddingHorizontal: 18, paddingTop: 54, paddingBottom: 40, gap: 16 },

  // Top action bar
  topActionBar: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'flex-end',
  },
  addCustomerHeroButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 3,
    elevation: 2,
  },
  addCustomerHeroText: {
    fontSize: 14,
    fontWeight: '700',
  },
  queueButton: { flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 12, paddingVertical: 10, borderRadius: 14 },
  queueButtonText: { color: '#2E7D32', fontSize: 12, fontWeight: '700' },

  // Summary Grid
  summaryGrid: { flexDirection: 'row', gap: 8 },
  summaryCard: {
    flex: 1,
    borderRadius: 14,
    borderWidth: 1,
    padding: 10,
    gap: 2,
    alignItems: 'center',
  },
  summaryValue: { fontSize: 18, fontWeight: '700', marginTop: 2 },
  summaryLabel: { fontSize: 10, fontWeight: '700', textAlign: 'center' },

  // Expiry Alert Banner
  alertBanner: {
    borderRadius: 14,
    borderWidth: 1,
    padding: 12,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  alertBannerCopy: { flex: 1, gap: 2 },
  alertBannerTitle: { fontSize: 13, fontWeight: '700' },
  alertBannerDetail: { fontSize: 11, opacity: 0.85 },

  // Search Bar
  searchBarContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderRadius: 14,
    paddingHorizontal: 12,
    height: 46,
    gap: 8,
  },
  searchIcon: { marginRight: 2 },
  searchInput: {
    flex: 1,
    fontSize: 14,
    height: '100%',
  },

  // Filter Tabs
  tabsRow: { flexDirection: 'row', gap: 8, paddingVertical: 2 },
  filterTab: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 12,
    borderWidth: 1,
  },
  filterTabText: { fontSize: 12, fontWeight: '700' },
  countPill: { paddingHorizontal: 6, paddingVertical: 2, borderRadius: 8 },
  countPillText: { fontSize: 11, fontWeight: '700' },

  // Customer List & Cards
  listContainer: { gap: 12 },
  customerCard: {
    borderWidth: 1,
    borderRadius: 18,
    padding: 14,
    gap: 10,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 2,
    elevation: 1,
  },
  cardHeaderRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarImage: {
    width: 48,
    height: 48,
    borderRadius: 24,
  },
  avatarText: { fontSize: 15, fontWeight: '700' },
  customerHeaderInfo: { flex: 1, gap: 4 },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  customerName: { fontSize: 15, fontWeight: '700', flex: 1, marginRight: 6 },
  badgeRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 5 },

  cardMetaContainer: {
    borderTopWidth: StyleSheet.hairlineWidth,
    paddingTop: 8,
    gap: 4,
  },
  metaLine: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  metaText: { fontSize: 12 },

  // Quick Action Buttons on Card
  quickActionsRow: {
    flexDirection: 'row',
    borderTopWidth: StyleSheet.hairlineWidth,
    paddingTop: 10,
    gap: 8,
  },
  quickActionButton: {
    flex: 1,
    minHeight: 34,
    paddingHorizontal: 8,
    borderRadius: 10,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
  },
  quickActionText: { fontSize: 11, fontWeight: '700' },

  // Empty State Cards
  emptyCard: {
    borderWidth: 1,
    borderRadius: 18,
    padding: 24,
    alignItems: 'center',
    gap: 8,
  },
  emptyTitle: { fontSize: 15, fontWeight: '700', marginTop: 4 },
  emptyDetail: { fontSize: 12, lineHeight: 18, textAlign: 'center' },
  clearSearchBtn: {
    marginTop: 6,
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 10,
  },
  clearSearchBtnText: { fontSize: 12, fontWeight: '700' },
  addFirstBtn: {
    marginTop: 8,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingHorizontal: 18,
    paddingVertical: 10,
    borderRadius: 12,
  },
  addFirstBtnText: { fontSize: 13, fontWeight: '700' },

  // Add Customer Modal
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.55)',
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
  },
  modalSheet: {
    width: '100%',
    maxHeight: '90%',
    borderRadius: 22,
    borderWidth: 1,
    overflow: 'hidden',
  },
  modalHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
    paddingVertical: 14,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  modalSheetTitle: { fontSize: 17, fontWeight: '700' },
  modalSheetSubtitle: { fontSize: 11, marginTop: 1 },
  closeCircleBtn: {
    width: 32,
    height: 32,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  modalScroll: { flexGrow: 0 },
  modalScrollContent: { padding: 18, gap: 14 },

  // Photo Picker
  photoPickerContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
    paddingVertical: 6,
  },
  photoAvatarButton: {
    width: 90,
    height: 90,
    borderRadius: 45,
    borderWidth: 2,
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  formAvatarImage: { width: 86, height: 86, borderRadius: 43 },
  photoPlaceholder: { alignItems: 'center', justifyContent: 'center', gap: 2 },
  photoAddPrompt: { fontSize: 10, fontWeight: '700' },
  cameraBadge: {
    position: 'absolute',
    bottom: -2,
    right: -2,
    width: 26,
    height: 26,
    borderRadius: 13,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 2,
    borderColor: '#ffffff',
  },
  photoActionsRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 8,
  },
  photoActionChip: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 20,
    borderWidth: 1,
  },
  photoActionChipText: { fontSize: 12, fontWeight: '600' },

  // Plan Selection
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

  // Status Field
  statusField: { gap: 7 },
  statusLabel: { fontSize: 13, fontWeight: '700' },
  statusOptions: { flexDirection: 'row', gap: 8 },
  statusOption: {
    flex: 1,
    minHeight: 44,
    borderWidth: 1,
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  statusOptionText: { fontSize: 13, fontWeight: '700' },

  modalButtonRow: {
    flexDirection: 'row',
    gap: 10,
    marginTop: 6,
  },
  modalCancelBtn: {
    flex: 1,
    height: 46,
    borderRadius: 13,
    alignItems: 'center',
    justifyContent: 'center',
  },
  modalCancelBtnText: { fontSize: 14, fontWeight: '700' },
  modalSubmitBtn: {
    flex: 2,
    height: 46,
    borderRadius: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  modalSubmitBtnText: { fontSize: 14, fontWeight: '700' },

  // Detail Modal
  detailCardModal: {
    width: '100%',
    maxHeight: '85%',
    borderRadius: 22,
    borderWidth: 1,
    overflow: 'hidden',
  },
  detailHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 18,
    paddingVertical: 14,
    borderBottomWidth: StyleSheet.hairlineWidth,
    borderBottomColor: '#ccc',
  },
  detailTitle: { fontSize: 16, fontWeight: '700' },
  detailBody: { flexGrow: 0 },
  detailBodyContent: { padding: 18, gap: 16 },

  detailAvatarContainer: {
    alignItems: 'center',
    gap: 6,
  },
  detailAvatarImage: {
    width: 80,
    height: 80,
    borderRadius: 40,
  },
  detailAvatarPlaceholder: {
    width: 80,
    height: 80,
    borderRadius: 40,
    alignItems: 'center',
    justifyContent: 'center',
  },
  detailAvatarText: { fontSize: 24, fontWeight: '700' },
  detailCustomerName: { fontSize: 18, fontWeight: '700', marginTop: 4 },
  detailCustomerPhone: { fontSize: 13 },

  detailActionGrid: {
    flexDirection: 'row',
    gap: 10,
  },
  detailActionBtn: {
    flex: 1,
    height: 42,
    borderRadius: 12,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  detailActionBtnText: { fontSize: 13, fontWeight: '700' },

  detailSectionBox: {
    borderWidth: 1,
    borderRadius: 16,
    padding: 14,
    gap: 10,
  },
  detailSectionHeading: { fontSize: 13, fontWeight: '700', marginBottom: 2 },
  detailInfoRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  detailInfoLabel: { fontSize: 12 },
  detailInfoValue: { fontSize: 12, fontWeight: '600' },

  detailFooterActions: {
    gap: 8,
    marginTop: 4,
  },
  detailRenewBtn: {
    height: 44,
    borderRadius: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  detailRenewBtnText: { fontSize: 13, fontWeight: '700' },
  detailDeleteBtn: {
    height: 44,
    borderRadius: 13,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
  },
  detailDeleteBtnText: { fontSize: 13, fontWeight: '700' },

  // Renew Dialog Modal
  modalCard: { width: '100%', borderWidth: 1, borderRadius: 20, padding: 20, gap: 12 },
  modalTitle: { fontSize: 18, fontWeight: '700' },
  modalDetail: { fontSize: 13, marginBottom: 8 },
  modalActions: { flexDirection: 'row', gap: 10, marginTop: 10 },
  modalButton: { flex: 1, height: 44, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  modalButtonText: { fontSize: 14, fontWeight: '700' },
});
