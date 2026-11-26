package com.bhojnify.core.state

import com.bhojnify.core.model.CustomerPaymentStatus
import com.bhojnify.core.model.LeaveStatus
import com.bhojnify.core.model.Meal
import com.bhojnify.core.model.MessState
import com.bhojnify.core.repository.MessRepository
import com.bhojnify.core.repository.InMemoryMessRepository
import com.bhojnify.core.service.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun `attendance is linked to customer without consuming global credits`() {
        val initialCredits = classUnderTest.state.value.credits

        classUnderTest.markAttendance(Meal.LUNCH, customerId = "student-1")

        val record = classUnderTest.state.value.attendance.first()
        assertEquals("student-1", record.customerId)
        assertEquals("Manual", record.method)
        assertEquals(initialCredits, classUnderTest.state.value.credits)
    }

    @Test
    fun `payment can be linked to a customer`() {
        classUnderTest.addPayment(1250.0, "UPI", "Monthly fee", customerId = "student-1")

        val payment = classUnderTest.state.value.payments.first()
        assertEquals(1250.0, payment.amount)
        assertEquals("student-1", payment.customerId)
    }

    @Test
    fun `renewal records a linked payment when amount is provided`() {
        classUnderTest.addCustomer(
            name = "John Doe",
            plan = "Monthly Veg",
            joiningDate = "2026-09-01",
            expiryDate = "2026-10-01",
            phone = "1234567890"
        )
        val customerId = classUnderTest.state.value.customers.first().id

        classUnderTest.renewCustomerPlan(customerId, amount = 1500.0)

        val payment = classUnderTest.state.value.payments.first()
        assertEquals(1500.0, payment.amount)
        assertEquals(customerId, payment.customerId)
    }

    @Test
    fun `payment amount must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            classUnderTest.addPayment(0.0, "UPI", "Invalid")
        }
    }

    @Test
    fun `test resolve reminder`() {
        classUnderTest.addReminder("Check Rice", 3, "Inventory follow-up")
        val reminderId = classUnderTest.state.value.reminders.first().id

        classUnderTest.resolveReminder(reminderId)

        // Ensure reminder is removed from the state
        assertNull(classUnderTest.state.value.reminders.find { it.id == reminderId })
    }

    @Test
    fun `early renewal preserves existing days and retains original joiningDate`() {
        val today = DateUtils.todayString()
        // Customer expiry is 10 days in the future from today
        val futureExpiry = DateUtils.addDays(today, 10)
        val originalJoiningDate = "2026-01-01"

        classUnderTest.addCustomer(
            name = "Alice",
            plan = "Monthly Veg",
            joiningDate = originalJoiningDate,
            expiryDate = futureExpiry,
            phone = "9876543210"
        )
        val customerId = classUnderTest.state.value.customers.first().id

        // Renew for 30 days
        classUnderTest.renewCustomerPlan(customerId, durationDays = 30)

        val updatedCustomer = classUnderTest.state.value.customers.first { it.id == customerId }

        // 1. Joining date must remain preserved (not overwritten with today)
        assertEquals(originalJoiningDate, updatedCustomer.joiningDate)

        // 2. Expiry date must be anchored to existing futureExpiry + 30 days, NOT today + 30 days
        val expectedExpiry = DateUtils.addDays(futureExpiry, 30)
        assertEquals(expectedExpiry, updatedCustomer.expiryDate)
        assertEquals(CustomerPaymentStatus.PAID, updatedCustomer.paymentStatus)
    }

    @Test
    fun `expired customer renewal anchors from today and retains original joiningDate`() {
        val today = DateUtils.todayString()
        // Customer expiry is 10 days in the past
        val pastExpiry = DateUtils.addDays(today, -10)
        val originalJoiningDate = "2025-12-01"

        classUnderTest.addCustomer(
            name = "Bob",
            plan = "Monthly Veg",
            joiningDate = originalJoiningDate,
            expiryDate = pastExpiry,
            phone = "9876543211"
        )
        val customerId = classUnderTest.state.value.customers.first().id

        // Renew for 30 days
        classUnderTest.renewCustomerPlan(customerId, durationDays = 30)

        val updatedCustomer = classUnderTest.state.value.customers.first { it.id == customerId }

        // Joining date preserved
        assertEquals(originalJoiningDate, updatedCustomer.joiningDate)

        // Since expired in the past, new expiry is anchored to today + 30 days
        val expectedExpiry = DateUtils.addDays(today, 30)
        assertEquals(expectedExpiry, updatedCustomer.expiryDate)
    }

    @Test
    fun `approved leave extends customer expiryDate by duration of leave inclusive`() {
        val initialExpiry = "2026-10-31"
        classUnderTest.addCustomer(
            name = "Charlie",
            plan = "Monthly Veg",
            joiningDate = "2026-09-01",
            expiryDate = initialExpiry,
            phone = "9876543212"
        )
        val customerId = classUnderTest.state.value.customers.first().id

        // Leave from 2026-10-01 to 2026-10-05 (5 days inclusive: Oct 1, 2, 3, 4, 5)
        classUnderTest.addLeave(
            customerId = customerId,
            from = "2026-10-01",
            to = "2026-10-05",
            reason = "Festival"
        )
        val leaveId = classUnderTest.state.value.leaves.first().id

        // Approve leave via updateLeaveStatus
        classUnderTest.updateLeaveStatus(leaveId, LeaveStatus.APPROVED)

        val updatedLeave = classUnderTest.state.value.leaves.first { it.id == leaveId }
        assertEquals(LeaveStatus.APPROVED, updatedLeave.status)

        val updatedCustomer = classUnderTest.state.value.customers.first { it.id == customerId }
        // 5 days inclusive added to 2026-10-31 -> 2026-11-05
        val expectedExpiry = DateUtils.addDays(initialExpiry, 5)
        assertEquals("2026-11-05", expectedExpiry)
        assertEquals(expectedExpiry, updatedCustomer.expiryDate)
    }

    @Test
    fun `single day approved leave extends customer expiryDate by 1 day`() {
        val initialExpiry = "2026-10-31"
        classUnderTest.addCustomer(
            name = "Dana",
            plan = "Monthly Veg",
            joiningDate = "2026-09-01",
            expiryDate = initialExpiry,
            phone = "9876543213"
        )
        val customerId = classUnderTest.state.value.customers.first().id

        // Single day leave: from 2026-10-15 to 2026-10-15 (1 day inclusive)
        classUnderTest.addLeave(
            customerId = customerId,
            from = "2026-10-15",
            to = "2026-10-15",
            reason = "Doctor visit"
        )
        val leaveId = classUnderTest.state.value.leaves.first().id

        classUnderTest.updateLeaveStatus(leaveId, LeaveStatus.APPROVED)

        val updatedCustomer = classUnderTest.state.value.customers.first { it.id == customerId }
        assertEquals("2026-11-01", updatedCustomer.expiryDate)
    }

    @Test
    fun `non-approved or pending leave does not extend expiryDate`() {
        val initialExpiry = "2026-10-31"
        classUnderTest.addCustomer(
            name = "Eve",
            plan = "Monthly Veg",
            joiningDate = "2026-09-01",
            expiryDate = initialExpiry,
            phone = "9876543214"
        )
        val customerId = classUnderTest.state.value.customers.first().id

        // 1. Adding pending leave does not change expiryDate
        classUnderTest.addLeave(
            customerId = customerId,
            from = "2026-10-01",
            to = "2026-10-05",
            reason = "Vacation"
        )
        val leaveId = classUnderTest.state.value.leaves.first().id
        assertEquals(LeaveStatus.PENDING, classUnderTest.state.value.leaves.first().status)
        assertEquals(initialExpiry, classUnderTest.state.value.customers.first { it.id == customerId }.expiryDate)

        // 2. Marking leave as DECLINED does not change expiryDate
        classUnderTest.updateLeaveStatus(leaveId, LeaveStatus.DECLINED)
        val declinedLeave = classUnderTest.state.value.leaves.first { it.id == leaveId }
        assertEquals(LeaveStatus.DECLINED, declinedLeave.status)
        assertEquals(initialExpiry, classUnderTest.state.value.customers.first { it.id == customerId }.expiryDate)

        // 3. Re-setting to PENDING does not change expiryDate
        classUnderTest.updateLeaveStatus(leaveId, LeaveStatus.PENDING)
        assertEquals(initialExpiry, classUnderTest.state.value.customers.first { it.id == customerId }.expiryDate)
    }

    @Test
    fun `approving already approved leave does not duplicate extension`() {
        val initialExpiry = "2026-10-31"
        classUnderTest.addCustomer(
            name = "Frank",
            plan = "Monthly Veg",
            joiningDate = "2026-09-01",
            expiryDate = initialExpiry,
            phone = "9876543215"
        )
        val customerId = classUnderTest.state.value.customers.first().id

        classUnderTest.addLeave(
            customerId = customerId,
            from = "2026-10-01",
            to = "2026-10-03", // 3 days
            reason = "Trip"
        )
        val leaveId = classUnderTest.state.value.leaves.first().id

        // First approval: extends by 3 days -> 2026-11-03
        classUnderTest.updateLeaveStatus(leaveId, LeaveStatus.APPROVED)
        val customerAfterFirstApproval = classUnderTest.state.value.customers.first { it.id == customerId }
        assertEquals("2026-11-03", customerAfterFirstApproval.expiryDate)

        // Second approval of same leave: must NOT extend again
        classUnderTest.updateLeaveStatus(leaveId, LeaveStatus.APPROVED)
        val customerAfterSecondApproval = classUnderTest.state.value.customers.first { it.id == customerId }
        assertEquals("2026-11-03", customerAfterSecondApproval.expiryDate)
    }
}
