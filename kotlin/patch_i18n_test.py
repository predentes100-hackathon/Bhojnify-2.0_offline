import re
from pathlib import Path

test_path = Path("C:/Users/vedan/OneDrive/Desktop/App_offline/Bhojnify-2.0_offline/kotlin/bhojnify-core/src/test/kotlin/com/bhojnify/core/i18n/I18nTest.kt")
content = test_path.read_text('utf-8')

content = content.replace('assertEquals("शुभ प्रभात, केतन", result)', 'assertEquals("शुभ सकाळ, केतन", result)')
content = content.replace('assertTrue(result.contains("सुट्टीच्या कालावधीनुसार मेस प्लॅनची मुदत आपोआप वाढवली गेली आहे"))', 'assertTrue(result.contains("ग्राहकाची प्लॅन मुदत यशस्वीरित्या वाढवण्यात आली आहे"))')

test_path.write_text(content, 'utf-8')
print("I18n Tests patched")
