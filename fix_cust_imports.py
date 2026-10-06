import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
cust_path = rn_dir / "app" / "customers.tsx"
cust = cust_path.read_text('utf-8')

cust = cust.replace("import { isValidISODate, daysBetween, addDaysISO, todayISO } from '@/lib/dates';\nimport { todayISO } from '@/lib/dates';", "import { isValidISODate, daysBetween, addDaysISO, todayISO } from '@/lib/dates';")
cust = cust.replace("import { isValidISODate, daysBetween } from '@/lib/dates';\nimport { todayISO } from '@/lib/dates';", "import { isValidISODate, daysBetween, addDaysISO, todayISO } from '@/lib/dates';")
cust_path.write_text(cust, 'utf-8')
