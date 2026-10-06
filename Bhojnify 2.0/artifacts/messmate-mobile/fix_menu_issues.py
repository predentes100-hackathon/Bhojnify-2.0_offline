from pathlib import Path
import re

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
menu_path = rn_dir / "app" / "menu.tsx"
menu = menu_path.read_text('utf-8')

if "isValidISODate" not in menu:
    menu = menu.replace("import { todayISO } from '@/lib/dates';", "import { todayISO, isValidISODate } from '@/lib/dates';")

# 1. Date validation in saveMenu
menu_logic = """
  const save = () => {
    if (!dish || !day) { Alert.alert(t('error'), t('menuRequiredMessage')); return; }
    if (!isValidISODate(day)) { Alert.alert(t('error'), t('invalidDate')); return; }
"""
menu = re.sub(r'const saveMenu = \(\) => \{\n\s*if \(!dish\) \{ Alert.*?\}', menu_logic.strip(), menu, flags=re.DOTALL)
# if the submit function is called 'add' (like in expenses)
menu_logic2 = """
  const add = () => {
    if (!dish || !day) { Alert.alert(t('error'), t('menuRequiredMessage')); return; }
    if (!isValidISODate(day)) { Alert.alert(t('error'), t('invalidDate')); return; }
"""
menu = re.sub(r'const add = \(\) => \{\n\s*if \(!dish.*?return; \}', menu_logic2.strip(), menu, flags=re.DOTALL)
# One-liner version:
# const add = () => { if (!dish) { Alert.alert(t('error'), t('menuRequiredMessage')); return; } addMenu({ day: todayISO(), meal, dish, note }); setDish(''); setNote(''); }; 
if "const add = () => { if (!dish" in menu:
    menu = menu.replace("const add = () => { if (!dish) { Alert.alert(t('error'), t('menuRequiredMessage')); return; }", "const add = () => { if (!dish || !day) { Alert.alert(t('error'), t('menuRequiredMessage')); return; } if (!isValidISODate(day)) { Alert.alert(t('error'), t('invalidDate')); return; }")

# Meals sorting and truncation. Instead of slicing, use full label.
# <Text style={[styles.mealSlot, { color: colors.foreground }]}>{t(item.meal).slice(0, 4)}</Text>
menu = re.sub(r"t\((?:item\.)?meal\)\.slice\(0,\s*4\)", r"t(item.meal)", menu)
# The sorting of meals: we can sort menus array before tracking them. 
# `menus.filter(...).map(...)` -> `menus.filter(...).sort((a,b) => { const order = { Breakfast: 1, Lunch: 2, Dinner: 3 }; return order[a.meal] - order[b.meal]; }).map(...)`
menu = re.sub(r"menus\.filter\(\(m\) => m\.day === date\)\.map\(", r"menus.filter((m) => m.day === date).sort((a,b) => { const O: any = { Breakfast:1, Lunch:2, Dinner:3 }; return O[a.meal] - O[b.meal]; }).map(", menu)

# Drop "Published" badge since there is no publish state
menu = re.sub(r"<Badge label=\{t\('published'\)\} tone=\"green\" />", "", menu)

menu_path.write_text(menu, 'utf-8')
print("Menu logic patched!")
