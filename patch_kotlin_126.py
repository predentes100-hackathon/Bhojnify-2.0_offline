import re

# 1. AdminScreen.kt
admin_file = "C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-android/src/main/kotlin/com/bhojnify/android/screens/AdminScreen.kt"
with open(admin_file, "r", encoding="utf-8") as f:
    admin_code = f.read()
admin_code = admin_code.replace('value = "${attendance.size + 126}"', 'value = "${attendance.size}"')
with open(admin_file, "w", encoding="utf-8") as f:
    f.write(admin_code)

# 2. HomeScreen.kt
home_file = "C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-android/src/main/kotlin/com/bhojnify/android/screens/HomeScreen.kt"
with open(home_file, "r", encoding="utf-8") as f:
    home_code = f.read()
home_code = home_code.replace('value = "${attendance.size + 126}"', 'value = "${attendance.size}"')

# Also in HomeScreen.kt, the action button grid should have "attendance"
# The current is Leaves. Let's see what IconTiles exist there.
