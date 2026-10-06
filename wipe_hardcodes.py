import re
from pathlib import Path

rn_file = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/Bhojnify 2.0/artifacts/messmate-mobile/lib/i18n.ts")
kt_file = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/main/kotlin/com/bhojnify/core/i18n/I18n.kt")

# For RN File
rn = rn_file.read_text('utf-8')
rn = rn.replace("'vsLastWeek': '+8% vs last week',", "")
rn = rn.replace("'vsLastWeek': 'मागील आठवड्यापेक्षा +8%',", "")
rn = rn.replace("'attendanceRate': '96% attendance',", "")
rn = rn.replace("'attendanceRate': '96% उपस्थिती',", "")
rn = rn.replace("'septemberReport': 'September report',", "'monthlyReport': 'Monthly report',")
rn = rn.replace("'septemberReport': 'सप्टेंबर अहवाल',", "'monthlyReport': 'मासिक अहवाल',")
rn = rn.replace("'septemberSpend': 'SEPTEMBER SPEND',", "'monthlySpend': 'MONTHLY SPEND',")
rn = rn.replace("'septemberSpend': 'सप्टेंबरचा खर्च',", "'monthlySpend': 'मासिक खर्च',")
rn_file.write_text(rn, 'utf-8')

# For Kotlin File
kt = kt_file.read_text('utf-8')
kt = kt.replace('"vsLastWeek" to "+8% vs last week",', '')
kt = kt.replace('"vsLastWeek" to "मागील आठवड्यापेक्षा +8%",', '')
kt = kt.replace('"attendanceRate" to "96% attendance",', '')
kt = kt.replace('"attendanceRate" to "96% उपस्थिती",', '')
kt = kt.replace('"septemberReport" to "September report",', '"monthlyReport" to "Monthly report",')
kt = kt.replace('"septemberReport" to "सप्टेंबर अहवाल",', '"monthlyReport" to "मासिक अहवाल",')
kt = kt.replace('"septemberSpend" to "SEPTEMBER SPEND",', '"monthlySpend" to "MONTHLY SPEND",')
kt = kt.replace('"septemberSpend" to "सप्टेंबरचा खर्च",', '"monthlySpend" to "मासिक खर्च",')
kt_file.write_text(kt, 'utf-8')

print("Hardcodes cleaned!")
