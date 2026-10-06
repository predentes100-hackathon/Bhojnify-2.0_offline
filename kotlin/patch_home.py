from pathlib import Path
home_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-android/src/main/kotlin/com/bhojnify/android/screens/HomeScreen.kt")
home = home_path.read_text('utf-8')
home = home.replace("    }.filter { it.second.isExpiringSoon || it.second.isExpired }\n}.filter { it.isExpiringSoon || it.isExpired }\n}", "    }.filter { it.second.isExpiringSoon || it.second.isExpired }\n}")
home_path.write_text(home, 'utf-8')
