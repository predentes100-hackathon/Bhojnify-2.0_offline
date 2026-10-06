import re
from pathlib import Path

kotlin_file = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/main/kotlin/com/bhojnify/core/i18n/I18n.kt")
rn_file = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile/lib/i18n.ts")

# Define additional keys missing from EN/MR
# #19 Missing translations: deleteLeaveConfirm, delete, holidayCompleted, planExtendedNotice, autoExtendSub, confirmReturn, editLeaveTitle, leaveToOptional, saveChanges, holidayPending, returnPending, completeHoliday, enter
additional_en = {
    "deleteLeaveConfirm": "Are you sure you want to delete this leave request?",
    "delete": "Delete",
    "holidayCompleted": "Leave Completed",
    "planExtendedNotice": "Customer plan expiry date has been extended successfully.",
    "autoExtendSub": "Automatically extend subscription based on the leave period",
    "confirmReturn": "Confirm Return",
    "editLeaveTitle": "Edit Leave Request",
    "leaveToOptional": "Return Date (Optional if pending)",
    "saveChanges": "Save Changes",
    "holidayPending": "Leave Pending",
    "returnPending": "Return Pending",
    "completeHoliday": "Complete Leave",
    "enter": "Enter",
    # Add new ones for Phase 2 
    "invalidAmount": "Invalid amount",
    "expiryBeforeJoining": "Expiry date cannot be before joining date",
    "returnDateBeforeFrom": "Return date cannot be before leaving date",
    "breakfast": "Breakfast",
    "lunch": "Lunch",
    "dinner": "Dinner",
    "success": "Success",
    "attendanceLogged": "Attendance logged",
    "invalidDate": "Invalid ISO Date (YYYY-MM-DD)",
    "selectMeal": "Select Meal",
    "markAttendance": "Mark Attendance",
    # Enums
    "Groceries": "Groceries",
    "Vegetables": "Vegetables",
    "Gas": "Gas",
    "Rent/Maintenance": "Rent/Maintenance",
    "Other": "Other",
    "Cash": "Cash",
    "UPI": "UPI",
    "Card": "Card",
    "Monthly Two-Meal": "Monthly Two-Meal",
    "Monthly Lunch Only": "Monthly Lunch Only",
    "Monthly Dinner Only": "Monthly Dinner Only",
    "15-Day Flexi Plan": "15-Day Flexi Plan",
    "Daily / Per Meal": "Daily / Per Meal",
    "Custom Plan": "Custom Plan",
    "error": "Error"
}

additional_mr = {
    "deleteLeaveConfirm": "तुम्हाला खात्री आहे का की तुम्हाला ही सुट्टीची विनंती हटवायची आहे?",
    "delete": "हटवा",
    "holidayCompleted": "सुट्टी पूर्ण झाली",
    "planExtendedNotice": "ग्राहकाची प्लॅन मुदत यशस्वीरित्या वाढवण्यात आली आहे.",
    "autoExtendSub": "सुट्टीच्या कालावधीनुसार सबस्क्रिप्शन आपोआप वाढवा",
    "confirmReturn": "परतण्याची खात्री करा",
    "editLeaveTitle": "सुट्टीची विनंती संपादित करा",
    "leaveToOptional": "परतण्याचा दिवस (पर्यायी)",
    "saveChanges": "बदल जतन करा",
    "holidayPending": "सुट्टी प्रलंबित",
    "returnPending": "परतण्याची तारीख प्रलंबित",
    "completeHoliday": "सुट्टी पूर्ण करा",
    "enter": "प्रविष्ट करा",
    "invalidAmount": "अवैध रक्कम",
    "expiryBeforeJoining": "मुदत संपण्याची तारीख सुरुवातीच्या तारखेआधी असू शकत नाही",
    "returnDateBeforeFrom": "परतण्याची तारीख सुट्टीच्या तारखेआधी असू शकत नाही",
    "breakfast": "नाश्ता",
    "lunch": "दुपारचे जेवण",
    "dinner": "रात्रीचे जेवण",
    "success": "यशस्वी",
    "attendanceLogged": "उपस्थिती नोंदवली",
    "invalidDate": "अवैध तारीख",
    "selectMeal": "जेवण निवडा",
    "markAttendance": "उपस्थिती नोंदवा",
    "Groceries": "किराणा",
    "Vegetables": "भाज्या",
    "Gas": "गॅस",
    "Rent/Maintenance": "भाडे/देखभाल",
    "Other": "इतर",
    "Cash": "रोख",
    "UPI": "UPI",
    "Card": "कार्ड",
    "Monthly Two-Meal": "मासिक (दोन वेळेस)",
    "Monthly Lunch Only": "मासिक दुपारचे जेवण",
    "Monthly Dinner Only": "मासिक रात्रीचे जेवण",
    "15-Day Flexi Plan": "१५-दिवसांचा लवचिक प्लॅन",
    "Daily / Per Meal": "दैनिक / प्रति जेवण",
    "Custom Plan": "तुमचा प्लॅन",
    "error": "त्रुटी"
}

# Parse current RN keys
rn_content = rn_file.read_text('utf-8')
en_match = re.search(r'const english: Dictionary = \{(.*?)\};', rn_content, re.DOTALL)
mr_match = re.search(r'const marathi: Dictionary = \{(.*?)\};', rn_content, re.DOTALL)

def parse_dict(body):
    out = {}
    for line in body.split('\n'):
        if ':' in line:
            parts = line.split(':', 1)
            k = parts[0].strip().strip("'")
            v = parts[1].strip().strip("',")
            out[k] = v
    return out

en_dict = parse_dict(en_match.group(1)) if en_match else {}
mr_dict = parse_dict(mr_match.group(1)) if mr_match else {}

kt_content = kotlin_file.read_text('utf-8')
kt_en_match = re.search(r'private val english: Map<String, String> = mapOf\((.*?)\)', kt_content, re.DOTALL)
kt_mr_match = re.search(r'private val marathi: Map<String, String> = mapOf\((.*?)\)', kt_content, re.DOTALL)

def parse_kt_dict(body):
    out = {}
    for line in body.split('\n'):
        if ' to ' in line:
            parts = line.split(' to ', 1)
            k = parts[0].strip().strip('",')
            v = parts[1].strip().strip('",')
            out[k] = v
    return out

kt_en_dict = parse_kt_dict(kt_en_match.group(1)) if kt_en_match else {}
kt_mr_dict = parse_kt_dict(kt_mr_match.group(1)) if kt_mr_match else {}

# Merge
master_en = {**en_dict, **kt_en_dict, **additional_en}
master_mr = {**mr_dict, **kt_mr_dict, **additional_mr}

# We need parity! Any key in en must be in mr
for k, v in master_en.items():
    if k not in master_mr: master_mr[k] = f"[MR] {v}"
for k, v in master_mr.items():
    if k not in master_en: master_en[k] = f"[EN] {v}"

def rn_format(d):
    lines = []
    for k, v in sorted(d.items()):
        v = v.replace("'", "\'")
        lines.append(f"  '{k}': '{v}',")
    return "\n".join(lines)

def kt_format(d):
    lines = []
    for k, v in sorted(d.items()):
        val = v.replace('"', '\\"').replace('\n', '\n')
        lines.append(f'        "{k}" to "{val}",')
    return "\n".join(lines)

new_rn = re.sub(r'const english: Dictionary = \{.*?\};', f"const english: Dictionary = {{\n{rn_format(master_en)}\n}};", rn_content, flags=re.DOTALL)
new_rn = re.sub(r'const marathi: Dictionary = \{.*?\};', f"const marathi: Dictionary = {{\n{rn_format(master_mr)}\n}};", new_rn, flags=re.DOTALL)
rn_file.write_text(new_rn, 'utf-8')

new_kt = re.sub(r'private val english: Map<String, String> = mapOf\(.*?\)', f"private val english: Map<String, String> = mapOf(\n{kt_format(master_en)}\n    )", kt_content, flags=re.DOTALL)
new_kt = re.sub(r'private val marathi: Map<String, String> = mapOf\(.*?\)', f"private val marathi: Map<String, String> = mapOf(\n{kt_format(master_mr)}\n    )", new_kt, flags=re.DOTALL)
kotlin_file.write_text(new_kt, 'utf-8')

print("i18n synced")
