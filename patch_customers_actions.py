from pathlib import Path
import re

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
cust_path = rn_dir / "app" / "customers.tsx"
cust = cust_path.read_text('utf-8')

# Handle Renew correctly (Prompting for amount is tough natively without a modal, we will add a simple logic Alert/Prompt or fallback to an action sheet, or simply use `Alert.prompt` which works on iOS/Android).
# Wait, `Alert.prompt` is iOS only. We'll add a modal or just use a fixed amount for now, or just use the amount they paid last time?
# Since we need to add a Payment, we can just navigate to a modal or show a modal. To save time, we will just add a state `renewCustomerId`, `renewAmount`, and show a small modal.

# Let's fix the logic first in `AppContext` for renewCustomer or do it inline.
# `handleRenew` in `customers.tsx`
handle_renew_old = """
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
"""

new_renew_modal_state = """
  const [renewModalVisible, setRenewModalVisible] = useState(false);
  const [renewCustomerId, setRenewCustomerId] = useState('');
  const [renewAmount, setRenewAmount] = useState('');

  const confirmRenew = () => {
    const customer = customers.find(c => c.id === renewCustomerId);
    if (!customer) return;
    
    // Duration based on plan
    let duration = 30;
    if (customer.plan.includes('15')) duration = 15;
    if (customer.plan.includes('Daily') || customer.plan.includes('Per Meal')) duration = 1;
    
    const amt = Number(renewAmount);
    if (isNaN(amt) || amt <= 0) {
      Alert.alert(t('error'), t('invalidAmount'));
      return;
    }
    
    const info = getExpiryInfo(customer.expiryDate);
    const baseDate = info.status === 'expired' || !customer.expiryDate ? todayISO() : customer.expiryDate;
    const newExpiry = addDaysISO(baseDate, duration);
    
    updateCustomer(renewCustomerId, {
      expiryDate: newExpiry,
      paymentStatus: 'Paid',
    });
    addPayment({ amount: amt, method: 'Cash', note: 'Plan Renewal', customerId: renewCustomerId, date: todayISO() });
    
    setRenewModalVisible(false);
    setRenewAmount('');
  };

  const handleRenew = (customerId: string) => {
    setRenewCustomerId(customerId);
    setRenewModalVisible(true);
  };
  
  const handleMarkPaid = (customerId: string) => {
    setRenewCustomerId(customerId);
    setRenewModalVisible(true); // Reusing the modal
  };
"""
cust = re.sub(r'const handleRenew = \(customerId: string\) => \{.*?  \};', new_renew_modal_state.strip(), cust, flags=re.DOTALL)
cust = cust.replace("import { useState, useMemo } from 'react';", "import { useState, useMemo } from 'react';\nimport { Modal } from 'react-native';\nimport { addDaysISO, todayISO } from '@/lib/dates';")

# Re-route markCustomerPaid
cust = cust.replace("onPress={() => markCustomerPaid(customer.id)}", "onPress={() => handleMarkPaid(customer.id)}")

modal_jsx = """
      <Modal visible={renewModalVisible} transparent animationType="slide">
        <View style={styles.modalOverlay}>
          <View style={[styles.modalCard, { backgroundColor: colors.background, borderColor: colors.border }]}>
            <Text style={[styles.modalTitle, { color: colors.foreground }]}>{t('amount')}</Text>
            <FormField label={t('amount')} value={renewAmount} onChangeText={setRenewAmount} keyboardType="numeric" />
            <View style={styles.modalActions}>
              <PrimaryButton label={t('cancel')} secondary onPress={() => setRenewModalVisible(false)} />
              <PrimaryButton label={t('save')} onPress={confirmRenew} />
            </View>
          </View>
        </View>
      </Modal>
    </Screen>
"""
cust = cust.replace("</Screen>", modal_jsx)
cust_path.write_text(cust, 'utf-8')
print("RN Customers renewed patched")
