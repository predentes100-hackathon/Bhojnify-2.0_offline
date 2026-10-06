import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")

# 1. report.tsx
report_path = rn_dir / "app" / "report.tsx"
content = report_path.read_text('utf-8')
content = content.replace("import { useMess } from '@/context/AppContext';", "import { useMess, getExpiryInfo } from '@/context/AppContext';")
content = content.replace("const { payments, expenses, inventory, attendance } = useMess();", "const { payments, expenses, inventory, attendance, customers, lowStock } = useMess();")
# Change labels to use monthlyReport instead of septemberReport
content = content.replace("t('septemberReport')", "t('monthlyReport')")
report_path.write_text(content, 'utf-8')

# 2. _layout.tsx and onboarding.tsx and AppContext.tsx (Hydration bug)
layout_path = rn_dir / "app" / "(tabs)" / "_layout.tsx"
layout = layout_path.read_text('utf-8')
layout = layout.replace("const { onboardingComplete } = useMess();", "const { onboardingComplete, hydrated } = useMess();")
layout = layout.replace("if (!onboardingComplete) return <Redirect href=\"/onboarding\" />;", "if (!hydrated) return null;\n  if (!onboardingComplete) return <Redirect href=\"/onboarding\" />;")
layout_path.write_text(layout, 'utf-8')

onboarding_path = rn_dir / "app" / "onboarding.tsx"
onboarding = onboarding_path.read_text('utf-8')
onboarding = onboarding.replace("import { OwnerProfile, useMess } from '@/context/AppContext';", "import { OwnerProfile, useMess } from '@/context/AppContext';\nimport { Redirect } from 'expo-router';")
onboarding = onboarding.replace("const { completeOwnerSetup } = useMess();", "const { completeOwnerSetup, onboardingComplete, hydrated } = useMess();")
if "if (hydrated && onboardingComplete) return <Redirect href=\"/(tabs)\" />;" not in onboarding:
    onboarding = onboarding.replace("const [name, setName]", "if (hydrated && onboardingComplete) return <Redirect href=\"/(tabs)\" />;\n\n  const [name, setName]")
onboarding_path.write_text(onboarding, 'utf-8')

app_context_path = rn_dir / "context" / "AppContext.tsx"
app_context = app_context_path.read_text('utf-8')
# Expose hydrated from context
app_context = app_context.replace("interface MessState {", "interface MessState {\n  hydrated: boolean;")
app_context = app_context.replace("role: 'owner',", "role: 'owner',\n  hydrated: false,")
# update the state returning in provider
if "return <MessContext.Provider value={{ ...state, ...actions, hydrated }}>" not in app_context:
    app_context = app_context.replace("return <MessContext.Provider value={{ ...state, ...actions }}>", "return <MessContext.Provider value={{ ...state, ...actions, hydrated }}>")
app_context_path.write_text(app_context, 'utf-8')

print("Phase 1 - First 2 bugs patched.")
