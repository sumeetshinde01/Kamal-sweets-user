package com.example.kamalsweets.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kamalsweets.Model.AllOrderModel
import com.example.kamalsweets.databinding.AllOrderItemLayoutBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AllOrderAdapter(val list: ArrayList<AllOrderModel>, val context: Context) : RecyclerView.Adapter<AllOrderAdapter.viewHolder>() {

    inner class viewHolder(val binding: AllOrderItemLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = AllOrderItemLayoutBinding.inflate((LayoutInflater.from(context)), parent, false)
        return viewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val order = list[position]
        
        // Handle combined product names/quantities
        if (order.productQuantity.isEmpty()) {
            holder.binding.productTitle.text = order.name
        } else {
            holder.binding.productTitle.text = "${order.name}  x  ${order.productQuantity}"
        }
        
        holder.binding.productPrice.text = "₹" + order.price

        // Use lowercase for comparison to avoid case-sensitivity issues
        val status = order.status?.lowercase() ?: ""

        // Handle Status and Colors
        when (status) {
            "ordered", "placed" -> {
                holder.binding.statusIndicator.setCardBackgroundColor(Color.parseColor("#FFA500"))
                holder.binding.productStatus.text = "Order Placed"
                holder.binding.cancelButton.visibility = View.VISIBLE
                holder.binding.deliveryLayout.visibility = View.GONE
            }
            "confirmed", "accepted" -> {
                holder.binding.statusIndicator.setCardBackgroundColor(Color.parseColor("#16BD09"))
                holder.binding.productStatus.text = "Preparing Order..."
                holder.binding.cancelButton.visibility = View.GONE
                holder.binding.deliveryLayout.visibility = View.GONE
            }
            "dispatched", "shipped", "out for delivery" -> {
                holder.binding.statusIndicator.setCardBackgroundColor(Color.parseColor("#10A5E7"))
                holder.binding.productStatus.text = "Out for Delivery"
                holder.binding.cancelButton.visibility = View.GONE
                showDeliveryDetails(holder, order)
            }
            "delivered", "success", "completed" -> {
                holder.binding.statusIndicator.setCardBackgroundColor(Color.parseColor("#16BD09"))
                holder.binding.productStatus.text = "Delivered"
                holder.binding.cancelButton.visibility = View.GONE
                showDeliveryDetails(holder, order)
            }
            "canceled", "cancelled" -> {
                holder.binding.statusIndicator.setCardBackgroundColor(Color.parseColor("#FF0000"))
                holder.binding.productStatus.text = "Canceled"
                holder.binding.cancelButton.visibility = View.GONE
                holder.binding.deliveryLayout.visibility = View.GONE
            }
            else -> {
                holder.binding.statusIndicator.setCardBackgroundColor(Color.GRAY)
                holder.binding.productStatus.text = order.status ?: "Pending"
                holder.binding.cancelButton.visibility = View.GONE

                if (!order.deliveryPersonName.isNullOrEmpty()) {
                    showDeliveryDetails(holder, order)
                } else {
                    holder.binding.deliveryLayout.visibility = View.GONE
                }
            }
        }

        holder.binding.cancelButton.setOnClickListener {
            if (holder.binding.cancelReason.visibility == View.GONE) {
                holder.binding.cancelReason.visibility = View.VISIBLE
                holder.binding.cancelReason.requestFocus()
                Toast.makeText(context, "Please provide a reason for cancellation", Toast.LENGTH_SHORT).show()
            } else {
                val reason = holder.binding.cancelReason.text.toString()
                if (reason.isEmpty()) {
                    Toast.makeText(context, "Please provide a reason", Toast.LENGTH_SHORT).show()
                } else {
                    updateStatus("Canceled", order.orderId!!, reason)
                }
            }
        }
    }

    private fun showDeliveryDetails(holder: viewHolder, order: AllOrderModel) {
        if (!order.deliveryPersonName.isNullOrEmpty() || !order.deliveryPersonNumber.isNullOrEmpty()) {
            holder.binding.deliveryLayout.visibility = View.VISIBLE
            holder.binding.DeliveryBoyName.text = order.deliveryPersonName ?: "Assigned"
            holder.binding.DeliveryBoyNumber.text = if (!order.deliveryPersonNumber.isNullOrEmpty()) "+91 ${order.deliveryPersonNumber}" else "N/A"
        } else {
            // For combined orders, we don't try to fetch a single doc ID as it might represent multiple
            // But usually the name/number is already in the model if it's dispatched/delivered.
            holder.binding.deliveryLayout.visibility = View.GONE
        }
    }

    private fun updateStatus(str: String, doc: String, cancelReason: String) {
        val docIds = doc.split(",")
        var successCount = 0
        
        for (id in docIds) {
            if (id.isEmpty()) continue
            val data = hashMapOf<String, Any>()
            data["status"] = str
            data["cancelReason"] = cancelReason
            Firebase.firestore.collection("allOrders").document(id).update(data)
                .addOnSuccessListener {
                    successCount++
                    if (successCount == docIds.filter { it.isNotEmpty() }.size) {
                        Toast.makeText(context, "Order(s) Canceled Successfully", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
