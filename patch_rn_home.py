import re
from pathlib import Path

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
index_path = rn_dir / "app" / "(tabs)" / "index.tsx"
content = index_path.read_text('utf-8')

# Change attendance onPress
content = content.replace("markAttendance('Lunch', true, 'Manual Entry')", "router.push('/attendance')")

# "Meals served — +8% vs last week" -> compute or remove. The user says "Compute or remove". Let's remove the detail string to avoid fake claims.
content = content.replace("detail={t('vsLastWeek')}", "detail={''}")
content = content.replace("detail={t('attendanceRate')}", "detail={''}")
content = content.replace("detail={t('thisMonth')}", "detail={''}")

# "goodMorning" shown at any hour. Add morning/afternoon/evening logic if possible, or just skip it if it's too much work? "goodMorning" is the only key we have currently without parsing time.
# "Add morning/afternoon/evening keys."
# I'll just change the key dynamically. 

good_morning_logic = """
  const hour = new Date().getHours();
  const greetingKey = hour < 12 ? 'goodMorning' : hour < 17 ? 'goodAfternoon' : 'goodEvening';
  const greeting = t(greetingKey, { name: profile.name || t('owner') }) || t('goodMorning', { name: profile.name || t('owner') });
"""
if "const hour =" not in content:
    content = content.replace("const todayLabel =", good_morning_logic + "\n  const todayLabel =")
    content = content.replace("t('goodMorning', { name: profile.name || t('owner') })", "greeting")

index_path.write_text(content, 'utf-8')
print("RN Home patched")
