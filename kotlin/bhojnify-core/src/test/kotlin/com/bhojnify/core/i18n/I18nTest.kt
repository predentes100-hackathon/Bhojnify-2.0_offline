package com.bhojnify.core.i18n

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class I18nTest {

    @Test
    fun `test english translation with variable substitution`() {
        val result = I18n.translate(Language.EN, "goodMorning", mapOf("name" to "Ketan"))
        assertEquals("Good morning, Ketan", result)
    }

    @Test
    fun `test marathi translation with variable substitution`() {
        val result = I18n.translate(Language.MR, "goodMorning", mapOf("name" to "केतन"))
        assertEquals("शुभ सकाळ, केतन", result)
    }

    @Test
    fun `test plan extension notice translation in marathi`() {
        val result = I18n.translate(Language.MR, "planExtendedNotice")
        assertTrue(result.contains("ग्राहकाची प्लॅन मुदत यशस्वीरित्या वाढवण्यात आली आहे"))
    }

    @Test
    fun `test localized values for meals and categories`() {
        assertEquals("दुपारचे जेवण", I18n.localizedValue(Language.MR, "Lunch"))
        assertEquals("रात्रीचे जेवण", I18n.localizedValue(Language.MR, "Dinner"))
        assertEquals("नाश्ता", I18n.localizedValue(Language.MR, "Breakfast"))
        assertEquals("Lunch", I18n.localizedValue(Language.EN, "Lunch"))
    }

    @Test
    fun `test english and marathi maps have identical key sets`() {
        val enKeys = I18n.englishKeys()
        val mrKeys = I18n.marathiKeys()
        val missingInMr = enKeys - mrKeys
        val missingInEn = mrKeys - enKeys
        assertTrue(missingInMr.isEmpty(), "Keys missing in Marathi: $missingInMr")
        assertTrue(missingInEn.isEmpty(), "Keys missing in English: $missingInEn")
        assertEquals(enKeys, mrKeys)
    }
}
