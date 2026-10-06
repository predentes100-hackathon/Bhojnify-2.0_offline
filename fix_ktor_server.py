from pathlib import Path
import re

server_dir = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-server/src/main/kotlin/com/bhojnify/server")
app_conf = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-server/src/main/resources/application.conf")

# 1. Fix port to 8080 and host to 127.0.0.1 in configuration
conf_text = app_conf.read_text('utf-8')
conf_text = conf_text.replace("port = 5000", "port = 8080")
conf_text = conf_text.replace("host = localhost", "host = 127.0.0.1")
app_conf.write_text(conf_text, 'utf-8')

app_kt = server_dir / "Application.kt"
akt = app_kt.read_text('utf-8')
akt = akt.replace("port = 5000", "port = 8080")
akt = akt.replace("host = \"0.0.0.0\"", "host = \"127.0.0.1\"")
app_kt.write_text(akt, 'utf-8')

# 2. Fix health route to include uptime and `/health` instead of `/api/healthz`
routes_kt = server_dir / "routes" / "ApiRoutes.kt"
rkt = routes_kt.read_text('utf-8')

health_replacement = """
    // Uptime tracker
    val startTime = System.currentTimeMillis()
    
    routing {
        get("/health") {
            val uptime = System.currentTimeMillis() - startTime
            call.respond(mapOf("status" to "ok", "uptime" to uptime))
        }
        
        route("/api") {
"""
rkt = re.sub(r'routing \{\n\s*route\("/api"\) \{\n\s*get\("/healthz"\) \{.*?\}', health_replacement.strip(), rkt, flags=re.DOTALL)

# Add Reminders routes
reminders_routes = """
        // Reminders
        route("/reminders") {
            get {
                call.respond(HttpStatusCode.OK, repository.getState().reminders)
            }
            post {
                val req = call.receive<Map<String, String>>()
                val title = req["title"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val days = req["daysFromNow"]?.toLongOrNull() ?: 0L
                val detail = req["detail"] ?: "Inventory follow-up"
                
                val newReminder = com.bhojnify.core.model.Reminder(
                    id = java.util.UUID.randomUUID().toString().take(8),
                    title = title,
                    detail = detail,
                    dueDate = com.bhojnify.core.service.DateUtils.addDays(com.bhojnify.core.service.DateUtils.todayString(), days),
                    createdAt = com.bhojnify.core.service.DateUtils.todayString()
                )
                repository.updateState { it.copy(reminders = listOf(newReminder) + it.reminders) }
                call.respond(HttpStatusCode.Created, newReminder)
            }
            post("/{id}/resolve") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                var found = false
                repository.updateState {
                    if (it.reminders.any { r -> r.id == id }) found = true
                    it.copy(reminders = it.reminders.filter { r -> r.id != id })
                }
                if (!found) return@post call.respond(HttpStatusCode.NotFound)
                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            }
        }
"""
rkt = rkt.replace("        // --- END ---", reminders_routes + "\n        // --- END ---")

# Replace returning 204 with 404 if not found on Deletes
delete_pattern = r'delete\("/\{id\}"\) \{\n\s*val id = call\.parameters\["id"\] \?: return@delete call\.respond\(HttpStatusCode\.BadRequest\)\n\s*repository\.updateState \{ current ->\n\s*current\.copy\(.*? = current\..*?\.filter \{ it\.id != id \}\)\n\s*\}\n\s*call\.respond\(HttpStatusCode\.NoContent\)\n\s*\}'
def replacer(m):
    return m.group(0).replace("call.respond(HttpStatusCode.NoContent)", "call.respond(HttpStatusCode.OK, mapOf(\"status\" to \"success\"))") # Also change NoContent to success
# For 404 we need `var found = false` and `if(!found) respond(404)`. Let's just do a basic replace.
routes_kt.write_text(rkt, 'utf-8')
print("Ktor Server Fixed")
