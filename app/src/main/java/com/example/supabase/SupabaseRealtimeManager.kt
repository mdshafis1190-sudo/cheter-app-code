package com.example.supabase

import android.content.Context
import android.util.Log
import com.example.model.MenuItem
import com.example.model.RestaurantTable
import com.example.model.TableOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class SupabaseRealtimeEvent {
    data class NewOrderReceived(val order: TableOrder) : SupabaseRealtimeEvent()
    data class OrderUpdated(val order: TableOrder) : SupabaseRealtimeEvent()
    data class OrdersSync(val orders: List<TableOrder>) : SupabaseRealtimeEvent()
    data class TablesSync(val tables: List<RestaurantTable>) : SupabaseRealtimeEvent()
    data class ConnectionStateChanged(val isConnected: Boolean, val message: String) : SupabaseRealtimeEvent()
}

class SupabaseRealtimeManager(
    private val client: SupabaseClient,
    private val scope: CoroutineScope
) {

    private val tag = "SupabaseRealtime"
    private var syncJob: Job? = null

    private val _realtimeEvents = MutableSharedFlow<SupabaseRealtimeEvent>(extraBufferCapacity = 64)
    val realtimeEvents: SharedFlow<SupabaseRealtimeEvent> = _realtimeEvents.asSharedFlow()

    private var knownOrderIds = mutableSetOf<String>()
    private var isFirstSync = true

    fun startListening(
        hotelId: String = "hotel1",
        menuItemsProvider: () -> List<MenuItem>,
        pollIntervalMs: Long = 3000L
    ) {
        syncJob?.cancel()
        isFirstSync = true
        knownOrderIds.clear()

        val cleanHotelId = hotelId.ifBlank { "hotel1" }
        syncJob = scope.launch(Dispatchers.IO) {
            Log.d(tag, "Started Supabase Sync for hotel: $cleanHotelId")
            var lastConnectedState = false

            while (isActive) {
                var currentCycleSuccess = false
                var nextDelayMs = pollIntervalMs

                try {
                    val availableMenuItems = menuItemsProvider()

                    // 1. Sync Live Orders strictly for this hotel from Supabase
                    val ordersResult = client.fetchOrders(cleanHotelId)
                    if (ordersResult.isSuccess) {
                        currentCycleSuccess = true
                        val dtos = ordersResult.getOrDefault(emptyList())
                        val orders = dtos.map { it.toTableOrder(availableMenuItems) }

                        if (isFirstSync) {
                            knownOrderIds.clear()
                            orders.forEach { knownOrderIds.add(it.orderId) }
                            isFirstSync = false
                            _realtimeEvents.emit(SupabaseRealtimeEvent.OrdersSync(orders))
                        } else {
                            // Check for newly arrived customer orders
                            for (order in orders) {
                                if (!knownOrderIds.contains(order.orderId)) {
                                    knownOrderIds.add(order.orderId)
                                    Log.d(tag, "🚨 New incoming customer order detected for hotel $cleanHotelId: ${order.orderId}")
                                    _realtimeEvents.emit(SupabaseRealtimeEvent.NewOrderReceived(order))
                                }
                            }
                            _realtimeEvents.emit(SupabaseRealtimeEvent.OrdersSync(orders))
                        }
                    }

                    // 2. Sync Live Restaurant Tables (T-1 to T-100) strictly for this hotel
                    val tablesResult = client.fetchTables(cleanHotelId)
                    if (tablesResult.isSuccess) {
                        currentCycleSuccess = true
                        val tableDtos = tablesResult.getOrDefault(emptyList())
                        if (tableDtos.isNotEmpty()) {
                            val tables = tableDtos.map { it.toRestaurantTable() }
                            _realtimeEvents.emit(SupabaseRealtimeEvent.TablesSync(tables))
                        }
                    }

                    if (currentCycleSuccess) {
                        if (!lastConnectedState) {
                            lastConnectedState = true
                            _realtimeEvents.emit(SupabaseRealtimeEvent.ConnectionStateChanged(true, "⚡ Supabase Realtime Active ($cleanHotelId)"))
                        }
                        nextDelayMs = pollIntervalMs
                    } else {
                        if (lastConnectedState || isFirstSync) {
                            lastConnectedState = false
                            isFirstSync = false
                            _realtimeEvents.emit(SupabaseRealtimeEvent.ConnectionStateChanged(false, "Offline / Local Engine Active"))
                        }
                        // Adaptive backoff when offline or host unreachable
                        nextDelayMs = 15000L
                    }

                } catch (e: Exception) {
                    if (lastConnectedState) {
                        lastConnectedState = false
                        _realtimeEvents.emit(SupabaseRealtimeEvent.ConnectionStateChanged(false, "Offline / Local Engine Active"))
                    }
                    nextDelayMs = 15000L
                }

                delay(nextDelayMs)
            }
        }
    }

    fun stopListening() {
        syncJob?.cancel()
        syncJob = null
        isFirstSync = true
        Log.d(tag, "Stopped Supabase Realtime Subscription")
    }

    fun notifyLocalNewOrder(order: TableOrder) {
        knownOrderIds.add(order.orderId)
        scope.launch {
            _realtimeEvents.emit(SupabaseRealtimeEvent.NewOrderReceived(order))
        }
    }
}
