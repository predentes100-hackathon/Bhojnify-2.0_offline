import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
app_context_path = rn_dir / "context" / "AppContext.tsx"
app_context = app_context_path.read_text('utf-8')

app_context = app_context.replace("const MessContext = createContext<(MessState & MessActions) | null>(null);", "const MessContext = createContext<(MessState & MessActions & { hydrated: boolean }) | null>(null);")

app_context_path.write_text(app_context, 'utf-8')
print("Typeched fixed in AppContext")
