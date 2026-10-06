from pathlib import Path
import re

utils_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/main/kotlin/com/bhojnify/core/service/DateUtils.kt")
content = utils_path.read_text('utf-8')

# Fix daysBetween
content = content.replace("    fun daysBetween(fromStr: String, toStr: String): Long {\n        val from = parseDate(fromStr)\n        val to = parseDate(toStr)\n        return ChronoUnit.DAYS.between(from, to) + 1L\n    }", "    fun daysBetween(fromStr: String, toStr: String): Long {\n        val from = parseDate(fromStr)\n        val to = parseDate(toStr)\n        return ChronoUnit.DAYS.between(from, to)\n    }")

# Fix parseDate: return Exception instead of LocalDate.now()
# Wait, this might break tests right now. I'll throw an Exception for now or just throw IllegalArgumentException.
content = content.replace("        } catch (e: Exception) {\n            LocalDate.now()\n        }", "        } catch (e: Exception) {\n            throw IllegalArgumentException(\"Invalid date format: $dateStr\")\n        }")

# Fix formatDate and formatFullDate to catch IllegalArgumentException
content = content.replace("        } catch (e: Exception) {\n            value\n        }", "        } catch (e: Exception) {\n            value.toString()\n        }")

utils_path.write_text(content, 'utf-8')
print("DateUtils patched")
