import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
app_context_path = rn_dir / "context" / "AppContext.tsx"
content = app_context_path.read_text('utf-8')

# Empty out all arrays
content = re.sub(r'attendance: \[\s*\{.*?\}\s*\],', 'attendance: [],', content, flags=re.DOTALL)
content = re.sub(r'customers: \[\s*\{.*?\}\s*\],', 'customers: [],', content, flags=re.DOTALL)
content = re.sub(r'leaves: \[\s*\{.*?\}\s*\],', 'leaves: [],', content, flags=re.DOTALL)
content = re.sub(r'payments: \[\s*\{.*?\}\s*\],', 'payments: [],', content, flags=re.DOTALL)
content = re.sub(r'inventory: \[\s*\{.*?\}\s*\],', 'inventory: [],', content, flags=re.DOTALL)
content = re.sub(r'menus: \[\s*\{.*?\}\s*\],', 'menus: [],', content, flags=re.DOTALL)
content = re.sub(r'staff: \[\s*\{.*?\}\s*\],', 'staff: [],', content, flags=re.DOTALL)
content = re.sub(r'expenses: \[\s*\{.*?\}\s*\],', 'expenses: [],', content, flags=re.DOTALL)
content = re.sub(r'feedback: \[\s*\{.*?\}\s*\],', 'feedback: [],', content, flags=re.DOTALL)
content = re.sub(r'reminders: \[\s*\{.*?\}\s*\],', 'reminders: [],', content, flags=re.DOTALL)

app_context_path.write_text(content, 'utf-8')
print("RN Demo Data Emptied")
