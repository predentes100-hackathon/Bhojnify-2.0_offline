from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")

index_path = rn_dir / "app" / "(tabs)" / "index.tsx"
index = index_path.read_text('utf-8')
index = index.replace("const greeting = t(greetingKey, { name: profile.name || t('owner') }) || greeting;", "const greeting = t(greetingKey, { name: profile.name || t('owner') }) || 'Good Morning';")
index_path.write_text(index, 'utf-8')

atten_path = rn_dir / "app" / "attendance.tsx"
atten = atten_path.read_text('utf-8')
atten = atten.replace("icon=\"checkmark-circle\"", "icon=\"check-circle\"")
atten_path.write_text(atten, 'utf-8')

app_context_path = rn_dir / "context" / "AppContext.tsx"
app = app_context_path.read_text('utf-8')
app = app.replace("paymentStatus: 'Paid'", "paymentStatus: 'Paid' as const")
app_context_path.write_text(app, 'utf-8')

cust_path = rn_dir / "app" / "customers.tsx"
cust = cust_path.read_text('utf-8')
if "import { addDaysISO" not in cust: # Double check because we did add it via patch_customers_actions.py but maybe it was overridden
    cust = cust.replace("import { isValidISODate, daysBetween } from '@/lib/dates';", "import { isValidISODate, daysBetween, addDaysISO, todayISO } from '@/lib/dates';")
    cust = cust.replace("import { addDaysISO, todayISO } from '@/lib/dates';", "") # cleanup duplicates if any
cust_path.write_text(cust, 'utf-8')
