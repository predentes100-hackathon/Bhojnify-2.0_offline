import re

file_path = "C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile/app/customers.tsx"
with open(file_path, "r", encoding="utf-8") as f:
    code = f.read()

# 1. Add addPayment to useMess hook
code = re.sub(
    r"const { customers, addCustomer, markCustomerPaid, updateCustomer } = useMess\(\);",
    r"const { customers, addCustomer, markCustomerPaid, updateCustomer, addPayment } = useMess();",
    code
)

# 2. Add amountPaid state
code = re.sub(
    r"const \[paymentStatus, setPaymentStatus\] = useState<CustomerPaymentStatus>\('Paid'\);",
    r"const [paymentStatus, setPaymentStatus] = useState<CustomerPaymentStatus>('Paid');\n  const [amountPaid, setAmountPaid] = useState('');",
    code
)

# 3. Inside add(), process addPayment
replacement_add = """    addCustomer({
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

    setName('');"""
    
code = re.sub(
    r"    addCustomer\(\{[\s\S]*?imageUri,\s*\}\);\s*setName\(''\);",
    replacement_add,
    code
)

# 4. Reset setAmountPaid inside add()
code = re.sub(
    r"setImageUri\(undefined\);\n  \};",
    r"setImageUri(undefined);\n    setAmountPaid('');\n  };",
    code
)

# 5. Add UI field
# We will insert FormField for Amount right after statusOptions
ui_insertion = """          </View>
        </View>

        {paymentStatus === 'Paid' ? (
          <FormField
            label={t('amountPaid', { defaultValue: 'Amount Paid (INR)' })}
            value={amountPaid}
            onChangeText={setAmountPaid}
            placeholder={t('enterPaymentAmount', { defaultValue: 'e.g. 4200' })}
            keyboardType="numeric"
          />
        ) : null}

        <PrimaryButton"""

code = code.replace("          </View>\n        </View>\n\n        <PrimaryButton", ui_insertion)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(code)

print("Patch applied to customers.tsx")
