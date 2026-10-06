import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")

def add_import(content, import_stmt):
    if import_stmt in content: return content
    parts = content.split('\n')
    for i, line in enumerate(parts):
        if line.startswith('import '):
            parts.insert(i, import_stmt)
            return '\n'.join(parts)
    return import_stmt + '\n' + content

# 1. leave.tsx completeLeave validation
leave_path = rn_dir / "app" / "leave.tsx"
leave = leave_path.read_text('utf-8')
leave = add_import(leave, "import { isValidISODate, daysBetween } from '@/lib/dates';")
leave_logic = """
  const handleCompleteLeave = () => {
    if (!selectedLeave || !completeReturnDate) return;
    if (!isValidISODate(completeReturnDate)) {
      Alert.alert(t('error'), t('invalidDate'));
      return;
    }
    if (daysBetween(selectedLeave.from, completeReturnDate) < 0) {
      Alert.alert(t('error'), t('returnDateBeforeFrom'));
      return;
    }
    completeLeave(selectedLeave.id, completeReturnDate);
    setCompleteReturnDate('');
    setSelectedLeave(null);
    setCompleteModalVisible(false);
  };
"""
leave = re.sub(r'const handleCompleteLeave = \(\) => \{.*?\n  \};', leave_logic.strip(), leave, flags=re.DOTALL)
leave_path.write_text(leave, 'utf-8')

# 2. payments.tsx amount validation
pay_path = rn_dir / "app" / "payments.tsx"
pay = pay_path.read_text('utf-8')
pay_logic = """  const add = () => { if (!amount || !note) { Alert.alert(t('completePayment'), t('paymentRequiredMessage')); return; } 
  const val = Number(amount);
  if (isNaN(val) || val <= 0) { Alert.alert(t('error'), t('invalidAmount')); return; }
  addPayment({ amount: val, method, note, date: todayISO() }); setAmount(''); setNote(''); Alert.alert(t('paymentRecorded'), t('paymentRecordedMessage')); };"""
pay = re.sub(r'const add = \(\) => \{ if \(\!amount.*?\}', pay_logic.strip(), pay, flags=re.DOTALL)
pay_path.write_text(pay, 'utf-8')

# 3. customers.tsx expiry vs joining expiry >= joining
cust_path = rn_dir / "app" / "customers.tsx"
cust = cust_path.read_text('utf-8')
cust = add_import(cust, "import { isValidISODate, daysBetween } from '@/lib/dates';")
cust_logic = """
  const submitCustomer = () => {
    if (!name || !plan) {
      Alert.alert(t('completeProfile'), t('profileRequiredMessage'));
      return;
    }
    if (!isValidISODate(joiningDate) || !isValidISODate(expiryDate)) {
      Alert.alert(t('error'), t('invalidDate'));
      return;
    }
    if (daysBetween(joiningDate, expiryDate) < 0) {
      Alert.alert(t('error'), t('expiryBeforeJoining'));
      return;
    }
"""
cust = re.sub(r'const submitCustomer = \(\) => \{\n\s*if \(!name \|\| !plan\) \{\n\s*Alert\.alert.*?return;\n\s*\}', cust_logic.strip(), cust, flags=re.DOTALL)
# Also apply Number(amountPaid) validation inside submitCustomer if unpaid/paid...
cust_path.write_text(cust, 'utf-8')
print("RN Validators patched")
