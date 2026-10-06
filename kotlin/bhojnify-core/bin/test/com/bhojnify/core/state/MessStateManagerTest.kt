package com.bhojnify.core.state

import com.bhojnify.core.model.CustomerPaymentStatus
import com.bhojnify.core.model.LeaveStatus
import com.bhojnify.core.model.MessState
import com.bhojnify.core.repository.MessRepository
import com.bhojnify.core.repository.InMemoryMessRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MessStateManagerTest {

    private lateinit var classUnderTest: MessStateManager
    private val standardDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(standardDispatcher)

    @BeforeEach
    fun setup() {
        val repo = InMemoryMessRepository()
        classUnderTest = MessStateManager(repo, testScope)
    }

    @Test
    fun `test add and complete leave extending plan correctly`() {
        // Add customer
        classUnderTest.addCustomer(
            name = "John Doe",
            plan = "Monthly Veg",
            joiningDate = "2026-09-01",
            expiryDate = "2026-10-01",
            phone = "1234567890",
            paymentStatus = CustomerPaymentStatus.PAID
        )

        val customerId = classUnderTest.state.value.customers.first().id

        // Add Leave
        classUnderTest.addLeave(
            customerId = customerId,
            from = "2026-09-10",
            to = null,
            reason = "Going home"
        )

        val leaveId = classUnderTest.state.value.leaves.first().id
        assertEquals(LeaveStatus.PENDING, classUnderTest.state.value.leaves.first().status)

        // Complete Leave -> Return Date "2026-09-15" (5 days total)
        classUnderTest.completeLeave(leaveId, "2026-09-15")

        val completedLeave = classUnderTest.state.value.leaves.first()
        assertEquals(LeaveStatus.APPROVED, completedLeave.status)
        assertEquals("2026-09-15", completedLeave.to)

        val updatedCustomer = classUnderTest.state.value.customers.first()
        // Plan should have been extended by 5 days: 2026-10-01 + 5 = 2026-10-06
        assertEquals("2026-10-06", updatedCustomer.expiryDate)
    }

    @Test
    fun `test resolve reminder`() {
        classUnderTest.addReminder("Check Rice", 3, "Inventory follow-up")
        val reminderId = classUnderTest.state.value.reminders.first().id

        classUnderTest.resolveReminder(reminderId)

        // Ensure reminder is removed from the state
        assertNull(classUnderTest.state.value.reminders.find { it.id == reminderId })
    }
}
