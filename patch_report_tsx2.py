import re

file_path = "C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile/app/report.tsx"
with open(file_path, "r", encoding="utf-8") as f:
    code = f.read()

# We need to make sure getExpiryInfo is imported in report.tsx
if "getExpiryInfo" not in code:
    code = code.replace("import { useMess } from '@/context/AppContext';", "import { useMess, getExpiryInfo } from '@/context/AppContext';")

with open(file_path, "w", encoding="utf-8") as f:
    f.write(code)

print("Imported getExpiryInfo in report.tsx")
