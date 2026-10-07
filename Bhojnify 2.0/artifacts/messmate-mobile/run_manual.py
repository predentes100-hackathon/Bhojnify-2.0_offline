import re

file_path = "app/customers.tsx"
with open(file_path, "r", encoding="utf-8") as f:
    code = f.read()

# 1. Update `addCustomer` to capture newCustomerId
code = code.replace(
    "addCustomer({", 
    "const newCustomerId = addCustomer({"
)
# 2. Add customerId to addPayment inside `add()`
code = code.replace(
    "note: `Subscription: ${name.trim()} - ${finalPlan}`,\n        date: todayStr,",
    "note: `Subscription: ${name.trim()} - ${finalPlan}`,\n        customerId: newCustomerId,\n        date: todayStr,"
)

modal_jsx = """
      {/* Renew / Mark Paid Modal */}
      {renewModalVisible && (
        <View style={StyleSheet.absoluteFill}>
          <View style={[styles.modalOverlay, { backgroundColor: 'rgba(0,0,0,0.5)' }]} />
          <View style={styles.modalContentWrapper}>
            <View style={[styles.modalCard, { backgroundColor: colors.card, borderColor: colors.border }]}>
              <Text style={[styles.modalTitle, { color: colors.foreground }]}>{t('markAsPaid')} / {t('renewPlan')}</Text>
              <Text style={[styles.modalDetail, { color: colors.mutedForeground }]}>{t('enterPaymentAmount')}</Text>
              
              <FormField 
                label={t('amount')} 
                value={renewAmount} 
                onChangeText={setRenewAmount} 
                keyboardType="numeric" 
                placeholder="0"
              />
              
              <View style={styles.modalActions}>
                <Pressable onPress={() => { setRenewModalVisible(false); setRenewAmount(''); }} style={[styles.modalButton, { backgroundColor: colors.muted }]}>
                  <Text style={[styles.modalButtonText, { color: colors.foreground }]}>{t('cancel')}</Text>
                </Pressable>
                <Pressable onPress={confirmRenew} style={[styles.modalButton, { backgroundColor: colors.primary }]}>
                  <Text style={[styles.modalButtonText, { color: colors.primaryForeground }]}>{t('confirm')}</Text>
                </Pressable>
              </View>
            </View>
          </View>
        </View>
      )}
    </KeyboardAwareScrollViewCompat>
"""
code = code.replace("    </KeyboardAwareScrollViewCompat>", modal_jsx)

# 4. Add the modal styles
style_append = """
  statusOptionText: { fontSize: 13, fontWeight: '700' },
  modalOverlay: { ...StyleSheet.absoluteFillObject },
  modalContentWrapper: { flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20 },
  modalCard: { width: '100%', borderWidth: 1, borderRadius: 20, padding: 20, gap: 12 },
  modalTitle: { fontSize: 18, fontWeight: '700' },
  modalDetail: { fontSize: 13, marginBottom: 8 },
  modalActions: { flexDirection: 'row', gap: 10, marginTop: 10 },
  modalButton: { flex: 1, height: 44, borderRadius: 12, alignItems: 'center', justifyContent: 'center' },
  modalButtonText: { fontSize: 14, fontWeight: '700' },
});
"""

code = code.replace("  statusOptionText: { fontSize: 13, fontWeight: '700' },\n});", style_append)

with open(file_path, "w", encoding="utf-8") as f:
    f.write(code)

ctx_path = "context/AppContext.tsx"
with open(ctx_path, "r", encoding="utf-8") as f:
    ctx = f.read()

ctx = ctx.replace("addCustomer: (customer: Omit<Customer, 'id'>) => void;", "addCustomer: (customer: Omit<Customer, 'id'>) => string;")

old_add = "addCustomer: (customer) => setState((prev) => ({ ...prev, customers: [{ ...customer, id: id() }, ...prev.customers] })),"
new_add = """addCustomer: (customer) => {
      const newId = id();
      setState((prev) => ({ ...prev, customers: [{ ...customer, id: newId }, ...prev.customers] }));
      return newId;
    },"""
ctx = ctx.replace(old_add, new_add)

with open(ctx_path, "w", encoding="utf-8") as f:
    f.write(ctx)
