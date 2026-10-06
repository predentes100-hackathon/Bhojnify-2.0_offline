import re
from pathlib import Path

test_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/test/kotlin/com/bhojnify/core/state/MessStateManagerTest.kt")
test_content = test_path.read_text('utf-8')

# Remove duplicate InMemoryMessRepository mapping
test_content = re.sub(r'class InMemoryMessRepository.*?\n}\n\n', '', test_content, flags=re.DOTALL)

# Add var
test_content = test_content.replace('private lateinit classUnderTest: MessStateManager', 'private lateinit var classUnderTest: MessStateManager')

# Add missing import if needed
if 'import com.bhojnify.core.repository.InMemoryMessRepository' not in test_content:
    test_content = test_content.replace('import com.bhojnify.core.repository.MessRepository', 'import com.bhojnify.core.repository.MessRepository\nimport com.bhojnify.core.repository.InMemoryMessRepository')

test_path.write_text(test_content, 'utf-8')
print("MessStateManagerTest patched")
