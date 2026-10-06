from pathlib import Path
import re

api_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-server/src/main/kotlin/com/bhojnify/server/routes/ApiRoutes.kt")
content = api_path.read_text('utf-8')

if "data class CompleteLeaveResponse(" not in content:
    idx = content.find("@Serializable\ndata class CompleteLeaveRequestBody")
    dt = "@Serializable\ndata class CompleteLeaveResponse(val completed: Boolean, val daysExtended: Long)\n\n"
    content = content[:idx] + dt + content[idx:]
    
content = content.replace('mapOf("completed" to true, "daysExtended" to daysMissed)', 'CompleteLeaveResponse(completed = true, daysExtended = daysMissed)')

api_path.write_text(content, 'utf-8')
print("Ktor Leave Patched")
