import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")

def add_import(content, import_stmt):
    if import_stmt in content: return content
    # add after the first import or at top
    parts = content.split('\n')
    for i, line in enumerate(parts):
        if line.startswith('import '):
            parts.insert(i, import_stmt)
            return '\n'.join(parts)
    return import_stmt + '\n' + content

# Map of replacements
files_to_update = [
    "app/(tabs)/index.tsx",
    "app/customers.tsx",
    "app/expenses.tsx",
    "app/inventory.tsx",
    "app/leave.tsx",
    "app/menu.tsx",
    "app/payments.tsx",
    "context/AppContext.tsx"
]

for file in files_to_update:
    path = rn_dir / file
    content = path.read_text('utf-8')
    if "new Date().toISOString().slice(0, 10)" in content:
        content = add_import(content, "import { todayISO } from '@/lib/dates';")
        content = content.replace("new Date().toISOString().slice(0, 10)", "todayISO()")
        path.write_text(content, 'utf-8')

# Also, in AppContext.tsx we need to remove the stale `daysUntil` entirely.
app_context_path = rn_dir / "context" / "AppContext.tsx"
app_context = app_context_path.read_text('utf-8')
if "export function daysUntil" in app_context:
    app_context = re.sub(r'export function daysUntil\([\s\S]*?\}\n', '', app_context)
app_context_path.write_text(app_context, 'utf-8')

print("Replaced Date logic!")
