package com.bhojnify.core.state

import com.bhojnify.core.i18n.Language
import com.bhojnify.core.model.*
import com.bhojnify.core.repository.MessRepository
import com.bhojnify.core.service.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MessStateManager(
    private val repository: MessRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    val state: StateFlow<MessState> = repository.stateFlow

    private fun generateId(): String = UUID.randomUUID().toString().take(8)

    fun setLanguage(language: Language) {
        scope.launch {
            repository.updateState { it.copy(language = language) }
        }
    }

    fun completeOwnerSetup(profile: OwnerProfile) {
        scope.launch {
            repository.updateState {
                it.copy(
                    profile = profile,
                    onboardingComplete = true,
                    role = Role.OWNER
                )
            }
        }
    }

    fun updatePolicies(policies: OwnerPolicies) {
        scope.launch {
            repository.updateState { it.copy(policies = policies) }
        }
    }

    fun markAttendance(meal: Meal, verified: Boolean = true, method: String = "Biometric + GPS") {
        scope.launch {
            val record = AttendanceRecord(
                id = generateId(),
                date = DateUtils.todayString(),
                meal = meal,
                time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")),
                verified = verified,
                method = method
            )
            repository.updateState {
                it.copy(
                    credits = maxOf(0, it.credits - 1),
                    attendance = listOf(record) + it.attendance
                )
            }
        }
    }

    fun addCustomer(
        name: String,
        plan: String,
        joiningDate: String,
        expiryDate: String,
        phone: String,
        paymentStatus: CustomerPaymentStatus = CustomerPaymentStatus.PAID,
        imageUri: String? = null
    ) {
        scope.launch {
            val newCustomer = Customer(
                id = generateId(),
                name = name,
                plan = plan,
                joiningDate = joiningDate,
                expiryDate = expiryDate,
                paymentStatus = paymentStatus,
                phone = phone,
                imageUri = imageUri
            )
            repository.updateState {
                it.copy(customers = listOf(newCustomer) + it.customers)
            }
        }
    }

    fun markCustomerPaid(customerId: String) {
        scope.launch {
            repository.updateState { current ->
                current.copy(
                    customers = current.customers.map {
                        if (it.id == customerId) it.copy(paymentStatus = CustomerPaymentStatus.PAID) else it
                    }
                )
            }
        }
    }

    fun updateCustomer(customerId: String, transform: (Customer) -> Customer) {
        scope.launch {
            repository.updateState { current ->
                current.copy(
                    customers = current.customers.map {
                        if (it.id == customerId) transform(it) else it
                    }
                )
            }
        }
    }

    fun renewCustomerPlan(customerId: String, durationDays: Long = 30) {
        scope.launch {
            val today = DateUtils.todayString()
            val newExpiry = DateUtils.addDays(today, durationDays)
            repository.updateState { current ->
                current.copy(
                    customers = current.customers.map {
                        if (it.id == customerId) {
                            it.copy(
                                joiningDate = today,
                                expiryDate = newExpiry,
                                paymentStatus = CustomerPaymentStatus.PAID
                            )
                        } else it
                    }
                )
            }
        }
    }

    fun addLeave(customerId: String, from: String, to: String?, reason: String) {
        scope.launch {
            val newLeave = LeaveRequest(
                id = generateId(),
                customerId = customerId,
                from = from,
                to = to,
                reason = reason,
                status = LeaveStatus.PENDING
            )
            repository.updateState {
                it.copy(leaves = listOf(newLeave) + it.leaves)
            }
        }
    }

    fun updateLeaveStatus(requestId: String, status: LeaveStatus) {
        scope.launch {
            repository.updateState { current ->
                current.copy(
                    leaves = current.leaves.map {
                        if (it.id == requestId) it.copy(status = status) else it
                    }
                )
            }
        }
    }

    fun updateLeave(requestId: String, from: String, to: String?, reason: String, customerId: String? = null) {
        scope.launch {
            repository.updateState { current ->
                current.copy(
                    leaves = current.leaves.map {
                        if (it.id == requestId) {
                            it.copy(
                                from = from,
                                to = to,
                                reason = reason,
                                customerId = customerId ?: it.customerId
                            )
                        } else it
                    }
                )
            }
        }
    }

    fun deleteLeave(requestId: String) {
        scope.launch {
            repository.updateState { current ->
                current.copy(leaves = current.leaves.filter { it.id != requestId })
            }
        }
    }

    fun completeLeave(requestId: String, returnDate: String) {
        scope.launch {
            repository.updateState { current ->
                val leave = current.leaves.find { it.id == requestId } ?: return@updateState current
                val customerId = leave.customerId ?: return@updateState current

                val daysMissed = maxOf(0L, DateUtils.daysBetween(leave.from, returnDate))

                val updatedLeaves = current.leaves.map {
                    if (it.id == requestId) it.copy(to = returnDate, status = LeaveStatus.APPROVED) else it
                }

                val updatedCustomers = current.customers.map { customer ->
                    if (customer.id == customerId && daysMissed > 0) {
                        val newExpiry = DateUtils.addDays(customer.expiryDate, daysMissed)
                        customer.copy(expiryDate = newExpiry)
                    } else {
                        customer
                    }
                }

                current.copy(
                    leaves = updatedLeaves,
                    customers = updatedCustomers
                )
            }
        }
    }

    fun addFeedback(dish: String, rating: Int, note: String) {
        scope.launch {
            val newFeedback = Feedback(
                id = generateId(),
                dish = dish,
                rating = rating,
                note = note,
                date = DateUtils.todayString()
            )
            repository.updateState {
                it.copy(feedback = listOf(newFeedback) + it.feedback)
            }
        }
    }

    fun addInventory(name: String, quantity: Double, unit: String, minimum: Double, category: String = "Essentials") {
        scope.launch {
            val item = InventoryItem(
                id = generateId(),
                name = name,
                quantity = quantity,
                unit = unit,
                minimum = minimum,
                category = category
            )
            repository.updateState {
                it.copy(inventory = listOf(item) + it.inventory)
            }
        }
    }

    fun removeInventory(itemId: String) {
        scope.launch {
            repository.updateState { current ->
                current.copy(inventory = current.inventory.filter { it.id != itemId })
            }
        }
    }

    fun addMenu(day: String, meal: Meal, dish: String, note: String) {
        scope.launch {
            val item = MenuItem(
                id = generateId(),
                day = day,
                meal = meal,
                dish = dish,
                note = note
            )
            repository.updateState {
                it.copy(menus = listOf(item) + it.menus)
            }
        }
    }

    fun addStaff(name: String, role: String, phone: String, salary: Double) {
        scope.launch {
            val member = StaffMember(
                id = generateId(),
                name = name,
                role = role,
                phone = phone,
                salary = salary
            )
            repository.updateState {
                it.copy(staff = listOf(member) + it.staff)
            }
        }
    }

    fun addExpense(category: String, amount: Double, note: String) {
        scope.launch {
            val expense = Expense(
                id = generateId(),
                date = DateUtils.todayString(),
                category = category,
                amount = amount,
                note = note
            )
            repository.updateState {
                it.copy(expenses = listOf(expense) + it.expenses)
            }
        }
    }

    fun addPayment(amount: Double, method: String, note: String) {
        scope.launch {
            val payment = Payment(
                id = generateId(),
                date = DateUtils.todayString(),
                amount = amount,
                method = method,
                note = note
            )
            repository.updateState {
                it.copy(payments = listOf(payment) + it.payments)
            }
        }
    }

    fun addReminder(title: String, daysFromNow: Long, detail: String = "Inventory follow-up") {
        scope.launch {
            val today = DateUtils.todayString()
            val dueDate = DateUtils.addDays(today, maxOf(0L, daysFromNow))
            val reminder = Reminder(
                id = generateId(),
                title = title,
                detail = detail,
                dueDate = dueDate,
                createdAt = today
            )
            repository.updateState {
                it.copy(reminders = listOf(reminder) + it.reminders)
            }
        }
    }

    fun resolveReminder(reminderId: String) {
        scope.launch {
            repository.updateState { current ->
                current.copy(reminders = current.reminders.filter { it.id != reminderId })
            }
        }
    }
    fun replaceState(newState: MessState) {
        scope.launch {
            repository.updateState { newState }
        }
    }
}
