import re
from pathlib import Path

repo_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/main/kotlin/com/bhojnify/core/repository/JsonFileMessRepository.kt")
content = repo_path.read_text('utf-8')

import_nio = "import java.nio.file.Files\nimport java.nio.file.StandardCopyOption"
if "java.nio.file.Files" not in content:
    content = content.replace("import java.io.File", "import java.io.File\n" + import_nio)

new_load = """
    private fun loadInitial(): MessState {
        return try {
            if (storageFile.exists()) {
                val content = storageFile.readText(Charsets.UTF_8)
                json.decodeFromString<MessState>(content)
            } else {
                MessState.defaultInitialState()
            }
        } catch (e: Exception) {
            System.err.println("State parsing failed! Corrupt file preserved.")
            val backup = File(storageFile.parentFile, "${storageFile.name}.corrupt-${System.currentTimeMillis()}")
            storageFile.renameTo(backup)
            MessState.defaultInitialState()
        }
    }
"""

new_persist = """
    private suspend fun persistToFile(state: MessState) = withContext(Dispatchers.IO) {
        try {
            storageFile.parentFile?.mkdirs()
            val text = json.encodeToString(MessState.serializer(), state)
            val tempFile = File(storageFile.parentFile, "${storageFile.name}.tmp")
            tempFile.writeText(text, Charsets.UTF_8)
            Files.move(tempFile.toPath(), storageFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (e: Exception) {
            System.err.println("Failed to persist state: ${e.message}")
        }
    }
"""

content = re.sub(r'private fun loadInitial\(\): MessState \{.*?\n    \}', new_load.strip(), content, flags=re.DOTALL)
content = re.sub(r'private suspend fun persistToFile\(state: MessState\) = withContext\(Dispatchers\.IO\) \{.*?\n    \}', new_persist.strip(), content, flags=re.DOTALL)

repo_path.write_text(content, 'utf-8')
print("JSON Repository Atomic Patched")
