package com.example.kamalsweets.Fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kamalsweets.Adapter.AllOrderAdapter
import com.example.kamalsweets.Model.AllOrderModel
import com.example.kamalsweets.Roomdb.AppDatabase
import com.example.kamalsweets.Roomdb.OrderHistoryModel
import com.example.kamalsweets.databinding.FragmentAboutBinding
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MoreFragment : Fragment() {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding
    private lateinit var list: ArrayList<AllOrderModel>
    private var adapter: AllOrderAdapter? = null
    private var orderListener: ListenerRegistration? = null
    private lateinit var userId: String

    private val liveFirestoreOrders = HashMap<String, AllOrderModel>()
    private val cachedRoomOrders = HashMap<String, AllOrderModel>()
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list = ArrayList()
        val context = context ?: return
        val preference = context.getSharedPreferences("otpActivityUser", AppCompatActivity.MODE_PRIVATE)
        userId = preference.getString("number", "") ?: ""

        if (userId.isEmpty()) {
            binding?.shimmerViewContainer?.stopShimmer()
            binding?.shimmerViewContainer?.visibility = View.GONE
            binding?.noOrder?.visibility = View.VISIBLE
            return
        }

        adapter = AllOrderAdapter(list, requireContext())
        binding?.recyclerView?.adapter = adapter

        // Start shimmer
        binding?.shimmerViewContainer?.startShimmer()

        // 1. Observe Local History (baseline/fallback)
        observeLocalHistory()
        
        // 2. Setup Real-time Listener (live tracking source)
        setupOrderListener(userId)
    }

    private fun observeLocalHistory() {
        val db = AppDatabase.getInstance(requireContext())
        db.orderHistoryDao().getAllOrders(userId).observe(viewLifecycleOwner) { historyList ->
            cachedRoomOrders.clear()
            historyList.forEach {
                val order = AllOrderModel(
                    name = it.name,
                    orderId = it.orderId,
                    userId = it.userId,
                    status = it.status,
                    productId = it.productId,
                    price = it.price,
                    userName = it.userName,
                    userAddress = it.userAddress,
                    productQuantity = it.productQuantity,
                    timestamp = it.timestamp,
                    paymentStatus = it.paymentStatus,
                    cancelReason = it.cancelReason,
                    deliveryPersonName = it.deliveryPersonName,
                    deliveryPersonNumber = it.deliveryPersonNumber
                )
                cachedRoomOrders[it.orderId] = order
            }
            rebuildUIList()
        }
    }

    private fun setupOrderListener(userId: String) {
        orderListener?.remove()
        orderListener = Firebase.firestore.collection("allOrders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("MoreFragment", "Firestore error: ${error.message}")
                    // On error, still try to show cached data and stop shimmer
                    rebuildUIList()
                    return@addSnapshotListener
                }

                if (!isAdded || _binding == null) return@addSnapshotListener

                liveFirestoreOrders.clear()
                if (snapshots != null) {
                    for (doc in snapshots) {
                        try {
                            val order = doc.toObject(AllOrderModel::class.java).copy(orderId = doc.id)
                            liveFirestoreOrders[doc.id] = order
                            
                            // Synchronize with local storage whenever live data updates
                            cacheOrderLocally(order)
                        } catch (e: Exception) {
                            Log.e("MoreFragment", "Error parsing order: ${e.message}")
                        }
                    }
                }
                rebuildUIList()
            }
    }

    private fun cacheOrderLocally(order: AllOrderModel) {
        val applicationContext = context?.applicationContext ?: return
        val orderId = order.orderId ?: return
        if (orderId.isEmpty()) return

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getInstance(applicationContext)
            db.orderHistoryDao().insertOrder(
                OrderHistoryModel(
                    orderId = orderId,
                    name = order.name,
                    userId = order.userId,
                    status = order.status,
                    productId = order.productId,
                    price = order.price,
                    userName = order.userName,
                    userAddress = order.userAddress,
                    productQuantity = order.productQuantity,
                    timestamp = order.timestamp,
                    paymentStatus = order.paymentStatus,
                    cancelReason = order.cancelReason,
                    deliveryPersonName = order.deliveryPersonName ?: "",
                    deliveryPersonNumber = order.deliveryPersonNumber ?: ""
                )
            )
        }
    }

    @Synchronized
    private fun rebuildUIList() {
        // Merge Room and Firestore data. Firestore overwrites Room to ensure live tracking.
        val masterMap = HashMap<String, AllOrderModel>()
        masterMap.putAll(cachedRoomOrders)
        masterMap.putAll(liveFirestoreOrders)

        val groupedItemsList = ArrayList<AllOrderModel>()
        
        // Grouping logic: Bundle items by delivery partner or order session
        val groups = masterMap.values.groupBy {
            val deliveryPartner = it.deliveryPersonNumber?.trim() ?: ""
            if (deliveryPartner.isNotEmpty()) {
                val day = it.timestamp / (1000 * 60 * 60 * 24)
                "delivery_${deliveryPartner}_$day"
            } else {
                val window = it.timestamp / (1000 * 60 * 15) // 15 min window
                "session_${it.userId}_$window"
            }
        }

        for (items in groups.values) {
            if (items.isEmpty()) continue
            
            // AGGREGATE STATUS: Implementation of Requirement 1, 2, and 3
            // 1. Case-insensitive/trim (Requirement 1)
            val normalizedStatuses = items.map { it.status?.lowercase()?.trim() ?: "" }
            
            val aggregatedStatus = when {
                // 2. Priority for 'Delivered' (Requirement 2) 
                // 3. Support for synonyms 'success' and 'completed' (Requirement 3)
                normalizedStatuses.any { it == "delivered" || it == "success" || it == "completed" } -> "Delivered"
                
                // Other statuses
                normalizedStatuses.any { it == "dispatched" || it == "shipped" || it == "out for delivery" } -> "Out for Delivery"
                normalizedStatuses.any { it == "confirmed" || it == "accepted" } -> "Confirmed"
                normalizedStatuses.any { it == "canceled" || it == "cancelled" } -> "Canceled"
                else -> "Ordered"
            }
            
            val sortedByTime = items.sortedBy { it.timestamp }
            val firstItem = sortedByTime[0]
            val representative = items.find { !it.deliveryPersonNumber.isNullOrBlank() } ?: firstItem

            if (items.size > 1) {
                val combinedNames = items.joinToString("\n") {
                    val qty = it.productQuantity?.trim() ?: ""
                    if (qty.isNotEmpty()) "${it.name}  x  $qty" else it.name ?: ""
                }
                val totalPrice = items.sumOf { it.price?.toDoubleOrNull() ?: 0.0 }
                val combinedIds = items.mapNotNull { it.orderId }.joinToString(",")

                groupedItemsList.add(representative.copy(
                    name = combinedNames,
                    price = String.format("%.1f", totalPrice),
                    productQuantity = "", // Adapter uses this to know it's combined
                    status = aggregatedStatus,
                    orderId = combinedIds
                ))
            } else {
                groupedItemsList.add(items[0].copy(status = aggregatedStatus))
            }
        }

        val finalSortedList = groupedItemsList.sortedByDescending { it.timestamp }

        // Update UI on Main thread
        lifecycleScope.launch(Dispatchers.Main) {
            if (_binding == null) return@launch
            
            // Stop and hide shimmer on first data load
            if (isFirstLoad) {
                binding?.shimmerViewContainer?.stopShimmer()
                binding?.shimmerViewContainer?.visibility = View.GONE
                isFirstLoad = false
            }
            
            list.clear()
            list.addAll(finalSortedList)
            
            if (list.isEmpty()) {
                binding?.noOrder?.visibility = View.VISIBLE
                binding?.recyclerView?.visibility = View.GONE
            } else {
                binding?.noOrder?.visibility = View.GONE
                binding?.recyclerView?.visibility = View.VISIBLE
            }
            
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFirstLoad) {
            binding?.shimmerViewContainer?.startShimmer()
        }
    }

    override fun onPause() {
        binding?.shimmerViewContainer?.stopShimmer()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderListener?.remove()
        _binding = null
    }
}