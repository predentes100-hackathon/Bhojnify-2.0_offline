from pathlib import Path
import re

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
app_context_path = rn_dir / "context" / "AppContext.tsx"
content = app_context_path.read_text('utf-8')

content = content.replace("'Monthly Unlimited (2 Meals)'", "'Monthly Two-Meal'")
app_context_path.write_text(content, 'utf-8')

cust_path = rn_dir / "app" / "customers.tsx"
cust = cust_path.read_text('utf-8')
cust = cust.replace("'Monthly Unlimited (2 Meals)'", "'Monthly Two-Meal'")

# handleJoiningDateChange overwrites Custom Plan
handle_joining = """  const handleJoiningDateChange = (val: string) => {
    setJoiningDate(val);
    if (!plan || plan === 'Custom Plan') return;
    if (isValidISODate(val)) {
      let duration = 30;
      if (plan.includes('15')) duration = 15;
      if (plan.includes('Daily') || plan.includes('Meal')) duration = 1;
      setExpiryDate(addDaysISO(val, duration));
    }
  };
"""
cust = re.sub(r'const handleJoiningDateChange = \(val: string\) => \{.*?\n  \};', handle_joining.strip(), cust, flags=re.DOTALL)
cust_path.write_text(cust, 'utf-8')
print("RN Plans patched")
