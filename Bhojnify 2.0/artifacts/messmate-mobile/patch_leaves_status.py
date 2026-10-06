from pathlib import Path
import re

rn_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile")
leave_path = rn_dir / "app" / "leave.tsx"
leave = leave_path.read_text('utf-8')

# Fix pending count/filter
leave = leave.replace("leave.status === 'Pending' || !leave.to", "leave.status === 'Pending'")

# Ongoing condition => 'Approved' && !leave.to
leave = leave.replace("const isOngoing = !leave.to && leave.status !== 'Declined';", "const isOngoing = !leave.to && leave.status === 'Approved';")

leave_path.write_text(leave, 'utf-8')

# Android LeaveScreen.kt
kt_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-android/src/main/kotlin/com/bhojnify/android/screens")
kt_leave = kt_dir / "LeaveScreen.kt"
if kt_leave.exists():
    kt = kt_leave.read_text('utf-8')
    kt = kt.replace("it.status == com.bhojnify.core.model.LeaveStatus.PENDING || it.to == null", "it.status == com.bhojnify.core.model.LeaveStatus.PENDING")
    
    kt = kt.replace("val isOngoing = leave.to == null && leave.status != com.bhojnify.core.model.LeaveStatus.DECLINED", "val isOngoing = leave.to == null && leave.status == com.bhojnify.core.model.LeaveStatus.APPROVED")
    kt_leave.write_text(kt, 'utf-8')


print("Leave workflows patched")
