import re
from pathlib import Path

android_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-android/src/main/kotlin/com/bhojnify/android/screens")
cust_path = android_dir / "CustomersScreen.kt"
home_path = android_dir / "HomeScreen.kt"

# HomeScreen replace reactToCustomers
home = home_path.read_text('utf-8')
home = re.sub(
    r'val todayLabel = LocalDate\.now\(\)\.toString\(\)',
    r'val todayLabel = DateUtils.todayString()',
    home
)
home = re.sub(
    r'data class ExpiryInfo\(.*?\}',
    r'''private fun reactToCustomers(customers: List<com.bhojnify.core.model.Customer>): List<Pair<com.bhojnify.core.model.Customer, com.bhojnify.core.model.ExpiryInfo>> {
    val today = DateUtils.todayString()
    return customers.map {
        it to DateUtils.getExpiryInfo(it.expiryDate, today)
    }.filter { it.second.isExpiringSoon || it.second.isExpired }
}''',
    home, flags=re.DOTALL
)

home = home.replace("val expiringCustomers = reactToCustomers(customers)", "val expiringCustomers = reactToCustomers(customers)")
# expiringCustomers is now list of Pairs. Change usages:
home = home.replace("expiringCustomers.size", "expiringCustomers.size") # this is same
# check if there are others
home_path.write_text(home, 'utf-8')

cust = cust_path.read_text('utf-8')
cust = cust.replace("val todayStr = LocalDate.now().toString()", "val todayStr = DateUtils.todayString()")

cust = re.sub(
r'''val activeCount = customers\.count \{\s*val days = DateUtils\.daysBetween\(todayStr, it\.expiryDate\)\s*days > 3\s*\}\s*val expiringCount = customers\.count \{\s*val days = DateUtils\.daysBetween\(todayStr, it\.expiryDate\)\s*days in 0\.\.3\s*\}\s*val expiredCount = customers\.count \{\s*val days = DateUtils\.daysBetween\(todayStr, it\.expiryDate\)\s*days < 0\s*\}''',
r'''val activeCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).status == com.bhojnify.core.model.ExpiryStatus.ACTIVE }
    val expiringCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).isExpiringSoon }
    val expiredCount = customers.count { DateUtils.getExpiryInfo(it.expiryDate, todayStr).isExpired }''',
cust)

cust = re.sub(
r'''val filteredCustomers = customers\.filter \{\s*val days = DateUtils\.daysBetween\(todayStr, it\.expiryDate\)\s*when \(activeFilter\) \{\s*CustomerFilter\.ACTIVE -> days > 3\s*CustomerFilter\.EXPIRING -> days in 0\.\.3\s*CustomerFilter\.EXPIRED -> days < 0\s*CustomerFilter\.ALL -> true\s*\}\s*\}''',
r'''val filteredCustomers = customers.filter {
        val info = DateUtils.getExpiryInfo(it.expiryDate, todayStr)
        when (activeFilter) {
            CustomerFilter.ACTIVE -> info.status == com.bhojnify.core.model.ExpiryStatus.ACTIVE
            CustomerFilter.EXPIRING -> info.isExpiringSoon
            CustomerFilter.EXPIRED -> info.isExpired
            CustomerFilter.ALL -> true
        }
    }''',
cust)

cust = re.sub(
r'''val daysRemaining = DateUtils\.daysBetween\(todayStr, customer\.expiryDate\)\s*val isExpired = daysRemaining < 0\s*val isExpiringSoon = daysRemaining in 0\.\.3''',
r'''val info = DateUtils.getExpiryInfo(customer.expiryDate, todayStr)
                    val daysRemaining = info.daysRemaining
                    val isExpired = info.isExpired
                    val isExpiringSoon = info.isExpiringSoon''',
cust)

cust_path.write_text(cust, 'utf-8')
print("Customers and Home Kotlin screens patched")
