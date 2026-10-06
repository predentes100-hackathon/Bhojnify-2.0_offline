from pathlib import Path
import re

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
app_context_path = rn_dir / "context" / "AppContext.tsx"
content = app_context_path.read_text('utf-8')

# 1. Add customerId to Payment interface
content = content.replace("export interface Payment {\n  id: string;", "export interface Payment {\n  id: string;\n  customerId?: string;")

# 2. Update markCustomerPaid in Actions to take AMOUNT and create payment
# First add to MessActions
content = content.replace("markCustomerPaid: (id: string) => void;", "markCustomerPaid: (id: string, amount: number) => void;")

mark_paid_old = "markCustomerPaid: (customerId) => setState((prev) => ({ ...prev, customers: prev.customers.map((customer) => customer.id === customerId ? { ...customer, paymentStatus: 'Paid' } : customer) })),"
mark_paid_new = """
    markCustomerPaid: (customerId, amount) => setState((prev) => {
      const customers = prev.customers.map((c) => c.id === customerId ? { ...c, paymentStatus: 'Paid' } : c);
      const payment: Payment = { id: id(), date: todayISO(), amount, method: 'Cash', note: 'Marked as paid', customerId };
      return { ...prev, customers, payments: [payment, ...prev.payments] };
    }),
""".strip()
content = content.replace(mark_paid_old, mark_paid_new)
app_context_path.write_text(content, 'utf-8')

print("RN Payment interface patched")
