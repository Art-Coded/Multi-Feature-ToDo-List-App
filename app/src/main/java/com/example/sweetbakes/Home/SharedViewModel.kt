package com.example.sweetbakes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.sweetbakes.data.AppDatabase
import com.example.sweetbakes.data.CheckedOrderEntity
import com.example.sweetbakes.data.OrderCountEntity
import com.example.sweetbakes.data.OrderStateEntity
import com.example.sweetbakes.model.OrdersItem
import com.example.sweetbakes.utils.OrderNotificationUtils
import com.example.sweetbakes.workers.OrderReminderWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SharedViewModel(context: Context) : ViewModel() {
    private val database = AppDatabase.getDatabase(context)
    private val ordersDao = database.ordersDao()
    private val checkedOrdersDao = database.checkedOrdersDao()
    private val orderStatesDao = database.orderStatesDao()
    private val orderCountsDao = database.orderCountsDao()
    private val workManager = WorkManager.getInstance(context)

    private val _pendingOrderCount = MutableStateFlow(0)
    val pendingOrderCount: StateFlow<Int> = _pendingOrderCount.asStateFlow()

    private val _highPriorityOrderCount = MutableStateFlow(0)
    val highPriorityOrderCount: StateFlow<Int> = _highPriorityOrderCount.asStateFlow()

    private val _checkedOrders = MutableStateFlow<List<OrdersItem>>(emptyList())
    val checkedOrders: StateFlow<List<OrdersItem>> = _checkedOrders.asStateFlow()

    val completedOrderCount: StateFlow<Int> =
        _checkedOrders.map { it.size }
            .stateIn(viewModelScope, SharingStarted.Eagerly, _checkedOrders.value.size)

    private val _checkedStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val checkedStates: StateFlow<Map<String, Boolean>> = _checkedStates.asStateFlow()

    init {
        viewModelScope.launch {
            loadCountsFromDatabase()
            loadCheckedStatesFromDatabase()
            loadCheckedOrdersFromDatabase()
            OrderNotificationUtils.createNotificationChannel(context)
        }
    }

    fun scheduleOrderReminder(order: OrdersItem) {
        if (order.reminders.isEmpty()) {
            println("DEBUG: No reminder set for order ${order.title}")
            return
        }

        val reminderTime = order.getReminderTimestamp()
        if (reminderTime <= 0) {
            println("DEBUG: Failed to parse reminder time for order ${order.title}")
            return
        }

        val debugFormat = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault())
        println("DEBUG: Scheduling reminder for ${order.title} at ${debugFormat.format(Date(reminderTime))}")

        if (reminderTime <= System.currentTimeMillis()) {
            return
        }

        val delay = reminderTime - System.currentTimeMillis()

        val inputData = Data.Builder()
            .putString("title", order.title)
            .putString("description", order.description)
            .putString("orderId", order.id)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<OrderReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(inputData)
            .addTag(order.id)
            .build()

        workManager.enqueue(workRequest)
    }

    fun cancelOrderReminder(order: OrdersItem) {
        try {

            workManager.cancelAllWorkByTag(order.id)

            println("DEBUG: Cancelled reminders for order ${order.title} (ID: ${order.id})")
        } catch (e: Exception) {
            println("ERROR: Failed to cancel reminder for order ${order.id}: ${e.message}")
        }
    }

    private suspend fun loadCountsFromDatabase() {
        val counts = orderCountsDao.getCounts() ?: OrderCountEntity()
        _pendingOrderCount.value = counts.pendingOrderCount
        _highPriorityOrderCount.value = counts.highPriorityOrderCount
    }

    private suspend fun loadCheckedStatesFromDatabase() {
        val states = orderStatesDao.getAllStates().first()
        _checkedStates.value = states.associate { it.orderKey to it.isChecked }
    }

    private suspend fun loadCheckedOrdersFromDatabase() {
        val checkedOrderEntities = checkedOrdersDao.getAllCheckedOrders().first()
        val orders = checkedOrderEntities.mapNotNull { entity ->
            ordersDao.getOrderById(entity.orderId)
        }
        _checkedOrders.value = orders
    }

    fun updatePendingOrderCount(count: Int) {
        viewModelScope.launch {
            _pendingOrderCount.value = count
            orderCountsDao.insertOrUpdateCounts(
                OrderCountEntity(
                    pendingOrderCount = count,
                    highPriorityOrderCount = _highPriorityOrderCount.value,
                    completedOrderCount = _checkedOrders.value.size
                )
            )
        }
    }

    fun updateHighPriorityOrderCount(count: Int) {
        viewModelScope.launch {
            _highPriorityOrderCount.value = count
            orderCountsDao.insertOrUpdateCounts(
                OrderCountEntity(
                    pendingOrderCount = _pendingOrderCount.value,
                    highPriorityOrderCount = count,
                    completedOrderCount = _checkedOrders.value.size
                )
            )
        }
    }

    fun addCheckedOrder(order: OrdersItem) {
        viewModelScope.launch {
            _checkedOrders.value = _checkedOrders.value + order
            _checkedStates.value = _checkedStates.value + (order.getKey() to true)

            checkedOrdersDao.insertCheckedOrder(
                CheckedOrderEntity(
                    orderId = order.id,
                    orderTitle = order.title,
                    orderPriority = order.priority
                )
            )
            orderStatesDao.insertOrUpdateState(
                OrderStateEntity(
                    orderKey = order.getKey(),
                    isChecked = true
                )
            )
            updateCounts()
        }
    }

    fun deleteCheckedOrders() {
        viewModelScope.launch {
            val checkedOrderIds = _checkedOrders.value.map { it.id }

            if (checkedOrderIds.isNotEmpty()) {
                ordersDao.deleteOrdersByIds(checkedOrderIds)

                checkedOrdersDao.clearAllCheckedOrders()
                orderStatesDao.clearAllStates()

                _checkedOrders.value = emptyList()
                _checkedStates.value = emptyMap()

                refreshAllCounts()
            }
        }
    }

    fun removeCheckedOrder(order: OrdersItem) {
        viewModelScope.launch {
            _checkedOrders.value = _checkedOrders.value.filterNot { it.id == order.id }
            _checkedStates.value = _checkedStates.value - order.getKey()

            checkedOrdersDao.deleteCheckedOrder(
                CheckedOrderEntity(
                    orderId = order.id,
                    orderTitle = order.title,
                    orderPriority = order.priority
                )
            )
            orderStatesDao.deleteStateByKey(order.getKey())
            updateCounts()
        }
    }

    fun updateCheckedState(order: OrdersItem, isChecked: Boolean) {
        if (isChecked) {
            addCheckedOrder(order)
        } else {
            removeCheckedOrder(order)
        }
    }

    private fun updateCounts() {
        viewModelScope.launch {
            val pendingCount = _checkedStates.value.count { !it.value }
            val highPriorityCount = _checkedOrders.value.count {
                it.priority == "High" && !(_checkedStates.value[it.getKey()] ?: false)
            }

            _pendingOrderCount.value = pendingCount
            _highPriorityOrderCount.value = highPriorityCount

            orderCountsDao.insertOrUpdateCounts(
                OrderCountEntity(
                    pendingOrderCount = pendingCount,
                    highPriorityOrderCount = highPriorityCount,
                    completedOrderCount = _checkedOrders.value.size
                )
            )
        }
    }

    fun clearAllCheckedOrders() {
        viewModelScope.launch {
            checkedOrdersDao.clearAllCheckedOrders()
            orderStatesDao.clearAllStates()
            _checkedOrders.value = emptyList()
            _checkedStates.value = emptyMap()
            updateCounts()
        }
    }

    fun saveOrder(order: OrdersItem) {
        viewModelScope.launch {
            ordersDao.insertOrder(order)

            if (order.reminders.isNotEmpty()) {
                scheduleOrderReminder(order)
            }
        }
    }

    fun updateOrder(order: OrdersItem) {
        viewModelScope.launch {
            val existingOrder = ordersDao.getOrderById(order.id)
            existingOrder?.let {
                if (it.notificationScheduled) {
                    cancelOrderReminder(it)
                }

                if (order.reminders.isNotEmpty()) {
                    scheduleOrderReminder(order)
                }

                ordersDao.updateOrder(order.copy(
                    notificationScheduled = order.reminders.isNotEmpty()
                ))
            }
        }
    }

    fun deleteOrder(order: OrdersItem) {
        viewModelScope.launch {
            try {
                cancelOrderReminder(order)

                ordersDao.deleteOrder(order)

                if (checkedOrders.value.any { it.id == order.id }) {
                    removeCheckedOrder(order)
                }

                refreshAllCounts()
            } catch (e: Exception) {
                println("ERROR: Failed to delete order ${order.id}: ${e.message}")
            }
        }
    }

    private suspend fun loadOrdersFromDatabase() {
        val orders = ordersDao.getAllOrders().first()
        _checkedOrders.value = orders.filter { order ->
            _checkedStates.value[order.getKey()] ?: false
        }
    }

    init {
        viewModelScope.launch {
            loadCountsFromDatabase()
            loadCheckedStatesFromDatabase()
            loadOrdersFromDatabase()
        }
    }

    val orders: Flow<List<OrdersItem>> = ordersDao.getAllOrders()

    fun getAllOrders(): Flow<List<OrdersItem>> {
        return ordersDao.getAllOrders()
    }

    fun refreshAllCounts() {
        viewModelScope.launch {
            val allOrders = ordersDao.getAllOrders().first()
            val currentStates = _checkedStates.value

            val pending = allOrders.count { !(currentStates[it.id] ?: false) }
            val highPriority = allOrders.count {
                it.priority == "High" && !(currentStates[it.id] ?: false)
            }

            _pendingOrderCount.value = pending
            _highPriorityOrderCount.value = highPriority

            orderCountsDao.insertOrUpdateCounts(
                OrderCountEntity(
                    pendingOrderCount = pending,
                    highPriorityOrderCount = highPriority,
                    completedOrderCount = _checkedOrders.value.size
                )
            )
        }
    }
}