import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
app_context_path = rn_dir / "context" / "AppContext.tsx"
content = app_context_path.read_text('utf-8')

def add_import(content, import_stmt):
    if import_stmt in content: return content
    parts = content.split('\n')
    for i, line in enumerate(parts):
        if line.startswith('import '):
            parts.insert(i, import_stmt)
            return '\n'.join(parts)
    return import_stmt + '\n' + content

content = add_import(content, "import { todayISO, daysBetween, addDaysISO } from '@/lib/dates';")

# 1. Type
content = content.replace("interface MessState {", "interface MessState {\n  hydrated: boolean;")
content = content.replace("role: 'owner',", "role: 'owner',\n  hydrated: false,")
if "hydrated }>" not in content:
    content = content.replace("return <MessContext.Provider value={{ ...state, ...actions }}>", "return <MessContext.Provider value={{ ...state, ...actions, hydrated }}>")

# 2. Date ISO replacements
content = content.replace("new Date().toISOString().slice(0, 10)", "todayISO()")
if "export function daysUntil" in content:
    content = re.sub(r'export function daysUntil\([\s\S]*?\}\n', '', content)

# 3. Leave logic
old_leave = r'Math\.max\(0,\s*Math\.round\(\(returnMs\s*-\s*fromMs\)\s*/\s*86400000\)\s*\+\s*1\)'
content = re.sub(old_leave, 'Math.max(0, daysBetween(leave.from, returnDate))', content)

content = content.replace("const fromMs = new Date(`${leave.from}T12:00:00`).getTime();", "")
content = content.replace("const returnMs = new Date(`${returnDate}T12:00:00`).getTime();", "")

content = content.replace("const expiry = new Date(`${customer.expiryDate}T12:00:00`);\n          expiry.setDate(expiry.getDate() + daysMissed);\n          return { ...customer, expiryDate: expiry.toISOString().slice(0, 10) };", "return { ...customer, expiryDate: addDaysISO(customer.expiryDate, daysMissed) };")

app_context_path.write_text(content, 'utf-8')
