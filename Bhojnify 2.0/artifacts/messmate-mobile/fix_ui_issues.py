from pathlib import Path
import re

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")

# --- EXPENSES ---
exp_path = rn_dir / "app" / "expenses.tsx"
exp = exp_path.read_text('utf-8')
if "import { ScrollView" not in exp:
    exp = exp.replace("import { Alert, StyleSheet, Text, View } from 'react-native';", "import { Alert, StyleSheet, Text, View, ScrollView, Pressable } from 'react-native';")

exp_form_old = r"<FormField label=\{t\('category'\)\} value=\{localizedValue\(language, category\)\} onChangeText=\{setCategory\} placeholder=\{t\('enterExpenseCategory'\)\} />"
exp_form_new = """
<View style={{ gap: 8 }}>
  <Text style={{ fontSize: 13, fontWeight: '600', color: colors.foreground }}>{t('category')}</Text>
  <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8 }}>
    {['Groceries', 'Vegetables', 'Gas', 'Staff', 'Rent/Maintenance', 'Other'].map(cat => (
      <Pressable key={cat} onPress={() => setCategory(cat)} style={{ paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20, backgroundColor: category === cat ? colors.primary : colors.muted }}>
        <Text style={{ color: category === cat ? colors.primaryForeground : colors.foreground, fontWeight: category === cat ? '700' : '500' }}>{t(cat) || cat}</Text>
      </Pressable>
    ))}
  </ScrollView>
</View>
"""
exp = re.sub(exp_form_old, exp_form_new.strip().replace('\n', ''), exp)
# Change initial category from 'Inventory' to 'Groceries'
exp = exp.replace("useState('Inventory')", "useState('Groceries')")

# Change September Spend to Monthly Spend
exp = exp.replace("t('septemberSpend')", "t('monthlySpend')")
# Filter expense amounts by current month (Phase 2, #12 & #13)
exp = exp.replace("expenses.reduce((sum, item) => sum + item.amount, 0)", "expenses.filter(e => e.date.startsWith(todayISO().slice(0, 7))).reduce((sum, item) => sum + item.amount, 0)")
exp_path.write_text(exp, 'utf-8')

# --- PAYMENTS ---
pay_path = rn_dir / "app" / "payments.tsx"
pay = pay_path.read_text('utf-8')
if "import { ScrollView" not in pay:
    pay = pay.replace("import { Alert, StyleSheet, Text, View } from 'react-native';", "import { Alert, StyleSheet, Text, View, ScrollView, Pressable } from 'react-native';")

pay_form_old = r"<FormField label=\{t\('method'\)\} value=\{method\} onChangeText=\{setMethod\} placeholder=\{t\('enterPaymentMethod'\)\} />"
pay_form_new = """
<View style={{ gap: 8 }}>
  <Text style={{ fontSize: 13, fontWeight: '600', color: colors.foreground }}>{t('method')}</Text>
  <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8 }}>
    {['UPI', 'Cash', 'Card'].map(m => (
      <Pressable key={m} onPress={() => setMethod(m)} style={{ paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20, backgroundColor: method === m ? colors.primary : colors.muted }}>
        <Text style={{ color: method === m ? colors.primaryForeground : colors.foreground, fontWeight: method === m ? '700' : '500' }}>{t(m) || m}</Text>
      </Pressable>
    ))}
  </ScrollView>
</View>
"""
pay = re.sub(pay_form_old, pay_form_new.strip().replace('\n', ''), pay)

# Make Payments ledger sorted chronologically (Phase 2, #27). Also remove "Ledger up to date" static banner claim.
pay = pay.replace("payments.map(", "payments.sort((a,b) => b.date.localeCompare(a.date)).map(")
# Remove activeCard entirely
pay = re.sub(r"<View style=\{\[styles\.activeCard, \{ backgroundColor: colors\.primary \}\]\}>.*?</View></View>", "", pay)

pay_path.write_text(pay, 'utf-8')

# --- STAFF ---
# staff.tsx stores t('notAdded') as phone number
staff_path = rn_dir / "app" / "staff.tsx"
staff = staff_path.read_text('utf-8')
staff = staff.replace("phone: phone || t('notAdded')", "phone: phone || ''")
# display not added if empty
staff = re.sub(r"\{member\.phone\}</Text>", r"{member.phone || t('notAdded')}</Text>", staff)
staff_path.write_text(staff, 'utf-8')

print("UI components patched!")
