package com.bhojnify.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a baseline profile to optimize cold startup and critical user interactions.
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() {
        rule.collect(
            packageName = "com.bhojnify.android",
            includeInStartupProfile = true
        ) {
            // Cold startup
            pressHome()
            startActivityAndWait()

            // Wait for Home content
            device.wait(Until.hasObject(By.text("MessMate")), 5_000)

            // Navigate to Customers tab
            val customersTab = device.findObject(By.text("Customers")) ?: device.findObject(By.desc("Customers"))
            customersTab?.click()
            device.waitForIdle()

            // Fling scroll through customers LazyColumn
            val customerList = device.findObject(By.scrollable(true))
            customerList?.setGestureMargin(device.displayWidth / 5)
            customerList?.fling(Direction.DOWN)
            device.waitForIdle()
            customerList?.fling(Direction.UP)
            device.waitForIdle()

            // Navigate to Admin tab
            val adminTab = device.findObject(By.text("Admin")) ?: device.findObject(By.desc("Admin"))
            adminTab?.click()
            device.waitForIdle()

            // Navigate to Profile tab
            val profileTab = device.findObject(By.text("Profile")) ?: device.findObject(By.desc("Profile"))
            profileTab?.click()
            device.waitForIdle()

            // Return to Home tab
            val homeTab = device.findObject(By.text("Home")) ?: device.findObject(By.desc("Home"))
            homeTab?.click()
            device.waitForIdle()
        }
    }
}
