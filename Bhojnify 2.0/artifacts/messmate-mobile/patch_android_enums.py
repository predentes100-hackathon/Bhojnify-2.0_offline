from pathlib import Path
import re

android_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-android/src/main/kotlin/com/bhojnify/android/screens")

for f in android_dir.glob("*.kt"):
    text = f.read_text('utf-8')
    text = re.sub(r'viewModel\.localized\(([^)]+)\.name\)', r'viewModel.localized(\1.displayName)', text)
    # also handle `viewModel.localized(leave.status.name)` -> `viewModel.localized(leave.status.displayName)`
    text = re.sub(r'viewModel\.localized\(([^)]+)\.status\.name\)', r'viewModel.localized(\1.status.displayName)', text)
    # for enums that don't have displayName (ExpiryStatus, Role), it might fail to compile here, but ExpiryStatus is not used in localized(name) usually. Let's check.
    f.write_text(text, 'utf-8')

print("Enums patched to use displayName")
