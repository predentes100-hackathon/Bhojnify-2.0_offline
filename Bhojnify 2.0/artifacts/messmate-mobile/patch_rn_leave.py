import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
app_context_path = rn_dir / "context" / "AppContext.tsx"
app_context = app_context_path.read_text('utf-8')

app_context = app_context.replace("import { todayISO } from '@/lib/dates';", "import { todayISO, daysBetween, addDaysISO } from '@/lib/dates';")

old_leave_logic = """
    completeLeave: (requestId, returnDate) => setState((prev) => {
      const leave = prev.leaves.find((l) => l.id === requestId);
      if (!leave || !leave.customerId) return prev;
      const fromMs = new Date(`${leave.from}T12:00:00`).getTime();
      const returnMs = new Date(`${returnDate}T12:00:00`).getTime();
      const daysMissed = Math.max(0, Math.round((returnMs - fromMs) / 86400000) + 1);
      return {
        ...prev,
        leaves: prev.leaves.map((l) => l.id === requestId ? { ...l, to: returnDate, status: 'Approved' } : l),
        customers: prev.customers.map((customer) => {
          if (customer.id !== leave.customerId || daysMissed <= 0) return customer;
          const expiry = new Date(`${customer.expiryDate}T12:00:00`);
          expiry.setDate(expiry.getDate() + daysMissed);
          return { ...customer, expiryDate: expiry.toISOString().slice(0, 10) };
        }),
      };
    }),
""".strip()

new_leave_logic = """
    completeLeave: (requestId, returnDate) => setState((prev) => {
      const leave = prev.leaves.find((l) => l.id === requestId);
      if (!leave || !leave.customerId) return prev;
      const daysMissed = Math.max(0, daysBetween(leave.from, returnDate));
      return {
        ...prev,
        leaves: prev.leaves.map((l) => l.id === requestId ? { ...l, to: returnDate, status: 'Approved' } : l),
        customers: prev.customers.map((customer) => {
          if (customer.id !== leave.customerId || daysMissed <= 0) return customer;
          return { ...customer, expiryDate: addDaysISO(customer.expiryDate, daysMissed) };
        }),
      };
    }),
""".strip()

# find and replace exact block
if "const fromMs = new Date(`${leave.from}T12:00:00`).getTime();" in app_context:
    app_context = re.sub(r'completeLeave: \(requestId, returnDate\) => setState\(\(prev\) => {.*?}\),', new_leave_logic + ",", app_context, flags=re.DOTALL)

app_context_path.write_text(app_context, 'utf-8')
print("RN Leave Logic patched!")
