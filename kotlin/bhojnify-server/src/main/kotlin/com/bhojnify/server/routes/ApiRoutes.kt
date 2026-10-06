package com.bhojnify.server.routes

import com.bhojnify.core.model.*
import com.bhojnify.core.repository.MessRepository
import com.bhojnify.core.service.DateUtils
import com.bhojnify.core.service.ReportCalculator
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CreateCustomerRequest(
    val name: String,
    val plan: String,
    val joiningDate: String,
    val expiryDate: String,
    val phone: String,
    val paymentStatus: CustomerPaymentStatus = CustomerPaymentStatus.PAID,
    val imageUri: String? = null
)

@Serializable
data class CreateLeaveRequest(
    val customerId: String,
    val from: String,
    val to: String? = null,
    val reason: String = ""
)

@Serializable
data class CompleteLeaveResponse(val completed: Boolean, val daysExtended: Long)

@Serializable
data class CompleteLeaveRequestBody(
    val returnDate: String
)

@Serializable
data class UpdateLeaveStatusRequest(
    val status: LeaveStatus
)

@Serializable
data class CreateExpenseRequest(
    val category: String,
    val amount: Double,
    val note: String = "",
    val date: String? = null
)

@Serializable
data class CreatePaymentRequest(
    val amount: Double,
    val method: String = "UPI",
    val note: String = "",
    val date: String? = null
)

@Serializable
data class CreateInventoryRequest(
    val name: String,
    val quantity: Double,
    val unit: String = "kg",
    val minimum: Double,
    val category: String = "Essentials"
)

@Serializable
data class CreateMenuRequest(
    val day: String,
    val meal: Meal,
    val dish: String,
    val note: String = ""
)

fun Route.bhojnifyApiRoutes(repository: MessRepository) {
    route("/api") {
        healthRoutes()

        // Full Mess State
        get("/state") {
            val state = repository.getState()
            call.respond(HttpStatusCode.OK, state)
        }

        // Customers
        route("/customers") {
            get {
                val state = repository.getState()
                call.respond(HttpStatusCode.OK, state.customers)
            }

            post {
                val req = call.receive<CreateCustomerRequest>()
                val newCustomer = Customer(
                    id = UUID.randomUUID().toString().take(8),
                    name = req.name.trim(),
                    plan = req.plan.trim(),
                    joiningDate = req.joiningDate.trim(),
                    expiryDate = req.expiryDate.trim(),
                    paymentStatus = req.paymentStatus,
                    phone = req.phone.trim(),
                    imageUri = req.imageUri
                )
                repository.updateState { it.copy(customers = listOf(newCustomer) + it.customers) }
                call.respond(HttpStatusCode.Created, newCustomer)
            }

            patch("/{id}/paid") {
                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val updated = repository.updateState { state ->
                    state.copy(customers = state.customers.map {
                        if (it.id == id) it.copy(paymentStatus = CustomerPaymentStatus.PAID) else it
                    })
                }
                val customer = updated.customers.find { it.id == id }
                if (customer != null) call.respond(HttpStatusCode.OK, customer)
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/{id}/renew") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val today = DateUtils.todayString()
                val newExpiry = DateUtils.addDays(today, 30)
                val updated = repository.updateState { state ->
                    state.copy(customers = state.customers.map {
                        if (it.id == id) it.copy(joiningDate = today, expiryDate = newExpiry, paymentStatus = CustomerPaymentStatus.PAID) else it
                    })
                }
                val customer = updated.customers.find { it.id == id }
                if (customer != null) call.respond(HttpStatusCode.OK, customer)
                else call.respond(HttpStatusCode.NotFound)
            }
        }

        // Leaves
        route("/leave") {
            get {
                val state = repository.getState()
                call.respond(HttpStatusCode.OK, state.leaves)
            }

            post {
                val req = call.receive<CreateLeaveRequest>()
                val newLeave = LeaveRequest(
                    id = UUID.randomUUID().toString().take(8),
                    customerId = req.customerId,
                    from = req.from.trim(),
                    to = req.to?.trim(),
                    reason = req.reason.trim(),
                    status = LeaveStatus.PENDING
                )
                repository.updateState { it.copy(leaves = listOf(newLeave) + it.leaves) }
                call.respond(HttpStatusCode.Created, newLeave)
            }

            patch("/{id}/status") {
                val id = call.parameters["id"] ?: return@patch call.respond(HttpStatusCode.BadRequest)
                val req = call.receive<UpdateLeaveStatusRequest>()
                val updated = repository.updateState { state ->
                    state.copy(leaves = state.leaves.map {
                        if (it.id == id) it.copy(status = req.status) else it
                    })
                }
                val leave = updated.leaves.find { it.id == id }
                if (leave != null) call.respond(HttpStatusCode.OK, leave)
                else call.respond(HttpStatusCode.NotFound)
            }

            post("/{id}/complete") {
                val id = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val body = call.receive<CompleteLeaveRequestBody>()
                val state = repository.getState()
                val leave = state.leaves.find { it.id == id } ?: return@post call.respond(HttpStatusCode.NotFound)
                val customerId = leave.customerId ?: return@post call.respond(HttpStatusCode.BadRequest)

                val daysMissed = maxOf(0L, DateUtils.daysBetween(leave.from, body.returnDate))

                repository.updateState { current ->
                    val updatedLeaves = current.leaves.map {
                        if (it.id == id) it.copy(to = body.returnDate, status = LeaveStatus.APPROVED) else it
                    }
                    val updatedCustomers = current.customers.map { customer ->
                        if (customer.id == customerId && daysMissed > 0) {
                            customer.copy(expiryDate = DateUtils.addDays(customer.expiryDate, daysMissed))
                        } else customer
                    }
                    current.copy(leaves = updatedLeaves, customers = updatedCustomers)
                }

                call.respond(HttpStatusCode.OK, CompleteLeaveResponse(completed = true, daysExtended = daysMissed))
            }
        }

        // Inventory
        route("/inventory") {
            get {
                val state = repository.getState()
                call.respond(HttpStatusCode.OK, state.inventory)
            }

            post {
                val req = call.receive<CreateInventoryRequest>()
                val newItem = InventoryItem(
                    id = UUID.randomUUID().toString().take(8),
                    name = req.name.trim(),
                    quantity = req.quantity,
                    unit = req.unit.trim(),
                    minimum = req.minimum,
                    category = req.category.trim()
                )
                repository.updateState { it.copy(inventory = listOf(newItem) + it.inventory) }
                call.respond(HttpStatusCode.Created, newItem)
            }

            delete("/{id}") {
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                repository.updateState { it.copy(inventory = it.inventory.filter { item -> item.id != id }) }
                call.respond(HttpStatusCode.NoContent)
            }
        }

        // Menus
        route("/menu") {
            get {
                val state = repository.getState()
                call.respond(HttpStatusCode.OK, state.menus)
            }

            post {
                val req = call.receive<CreateMenuRequest>()
                val newItem = MenuItem(
                    id = UUID.randomUUID().toString().take(8),
                    day = req.day.trim(),
                    meal = req.meal,
                    dish = req.dish.trim(),
                    note = req.note.trim()
                )
                repository.updateState { it.copy(menus = listOf(newItem) + it.menus) }
                call.respond(HttpStatusCode.Created, newItem)
            }
        }

        // Expenses
        route("/expenses") {
            get {
                val state = repository.getState()
                call.respond(HttpStatusCode.OK, state.expenses)
            }

            post {
                val req = call.receive<CreateExpenseRequest>()
                val expense = Expense(
                    id = UUID.randomUUID().toString().take(8),
                    date = req.date ?: DateUtils.todayString(),
                    category = req.category.trim(),
                    amount = req.amount,
                    note = req.note.trim()
                )
                repository.updateState { it.copy(expenses = listOf(expense) + it.expenses) }
                call.respond(HttpStatusCode.Created, expense)
            }
        }

        // Payments
        route("/payments") {
            get {
                val state = repository.getState()
                call.respond(HttpStatusCode.OK, state.payments)
            }

            post {
                val req = call.receive<CreatePaymentRequest>()
                val payment = Payment(
                    id = UUID.randomUUID().toString().take(8),
                    date = req.date ?: DateUtils.todayString(),
                    amount = req.amount,
                    method = req.method.trim(),
                    note = req.note.trim()
                )
                repository.updateState { it.copy(payments = listOf(payment) + it.payments) }
                call.respond(HttpStatusCode.Created, payment)
            }
        }

        // Report
        get("/report") {
            val state = repository.getState()
            val report = ReportCalculator.calculate(
                payments = state.payments,
                expenses = state.expenses,
                inventory = state.inventory,
                attendanceCount = state.attendance.size
            )
            call.respond(HttpStatusCode.OK, report)
        }
    }
}
