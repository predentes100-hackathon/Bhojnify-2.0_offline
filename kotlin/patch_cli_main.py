from pathlib import Path

cli_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-cli/src/main/kotlin/com/bhojnify/cli/Main.kt")
c = cli_path.read_text('utf-8')
if "report.revenue" in c:
    c = c.replace("report.revenue", "report.totalRevenue")
    c = c.replace("report.costs", "report.totalExpenses")
    c = c.replace("report.margin", "report.operatingSurplus")
    c = c.replace("report.stockValue", "report.stockValueEstimate")
    c = c.replace("report.mealsServed", "report.mealsServedCount")

import re
c = re.sub(
r'val today = LocalDate\.now\(\)\.toString\(\)\n\s*state\.customers\.forEach \{ customer ->\n\s*val days = DateUtils\.daysBetween\(today, customer\.expiryDate\)\n\s*val status = when \{\n\s*days < 0 -> "\[EXPIRED\]"\n\s*days in 0\.\.3 -> "\[EXPIRING SOON - \$\{days\}d left\]"\n\s*else -> "\[ACTIVE - \$\{days\}d left\]"\n\s*\}',
r'''val today = DateUtils.todayString()
    state.customers.forEach { customer ->
        val info = DateUtils.getExpiryInfo(customer.expiryDate, today)
        val days = info.daysRemaining
        val status = when {
            info.isExpired -> "[EXPIRED]"
            info.isExpiringSoon -> "[EXPIRING SOON - ${days}d left]"
            else -> "[ACTIVE - ${days}d left]"
        }''', c)

cli_path.write_text(c, 'utf-8')
print("CLI Main patched")
