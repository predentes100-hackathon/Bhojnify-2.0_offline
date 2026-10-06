import re

with open('bhojnify-android/src/main/kotlin/com/bhojnify/android/screens/ProfileScreen.kt', 'r') as f:
    orig = f.read()

# 1. Add imports
imports_to_add = """import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
"""

if "import androidx.activity.compose.rememberLauncherForActivityResult" not in orig:
    orig = orig.replace("import androidx.compose.foundation.background", imports_to_add + "import androidx.compose.foundation.background")

# 2. Add Export / Import UI block at the very end
ui_block = """
        SectionHeading(title = "Data Management")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                .background(colors.card)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "Backup and restore your local data", fontSize = 12.sp, color = colors.mutedForeground)
            
            val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
                uri?.let {
                    if (com.bhojnify.android.utils.BackupManager.exportData(context, it, state)) {
                        Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to export data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                uri?.let { fileUri ->
                    val importedState = com.bhojnify.android.utils.BackupManager.importData(context, fileUri)
                    if (importedState != null) {
                        viewModel.replaceState(importedState)
                        Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to import data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.secondary)
                        .clickable { exportLauncher.launch("messmate_backup_${System.currentTimeMillis()}.json") }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Upload, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Text(text = "Export Backup", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.secondary)
                        .clickable { importLauncher.launch(arrayOf("application/json", "*/*")) }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                        Text(text = "Import Backup", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.primary)
                    }
                }
            }
        }
    }
}"""

orig = orig.replace("    }\n}", ui_block)

with open('bhojnify-android/src/main/kotlin/com/bhojnify/android/screens/ProfileScreen.kt', 'w') as f:
    f.write(orig)
