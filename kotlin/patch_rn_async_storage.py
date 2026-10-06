import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
app_context_path = rn_dir / "context" / "AppContext.tsx"
content = app_context_path.read_text('utf-8')

# Debounce save state using useEffect, useRef, and setTimeout
import_stmt = "import React, { createContext, ReactNode, useContext, useEffect, useMemo, useState, useRef } from 'react';"
content = re.sub(r'import React, \{ createContext([^;]+)\} from \'react\';', import_stmt, content)

storage_effect = """
  const saveTimeout = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (!hydrated) return;
    if (saveTimeout.current) clearTimeout(saveTimeout.current);
    saveTimeout.current = setTimeout(() => {
      try {
        const jsonValue = JSON.stringify(state);
        AsyncStorage.setItem(STORAGE_KEY, jsonValue).catch(e => {
          console.error("Failed to persist state: ", e);
        });
      } catch (error) {
        console.error("Failed to serialize state: ", error);
      }
    }, 500);
    return () => {
      if (saveTimeout.current) clearTimeout(saveTimeout.current);
    };
  }, [state, hydrated]);
"""

content = re.sub(
    r'  useEffect\(\(\) => \{\n    if \(hydrated\) AsyncStorage\.setItem\(STORAGE_KEY, JSON\.stringify\(state\)\)\.catch\(\(\) => undefined\);\n  \}, \[state, hydrated\]\);',
    storage_effect.strip(),
    content,
    flags=re.DOTALL
)

app_context_path.write_text(content, 'utf-8')
print("RN AsyncStorage debounce/error reporting added")
