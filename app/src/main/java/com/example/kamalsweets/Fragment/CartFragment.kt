package com.example.kamalsweets.Fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kamalsweets.Activity.AddressActivity
import com.example.kamalsweets.Adapter.CartAdapter
import com.example.kamalsweets.Roomdb.AppDatabase
import com.example.kamalsweets.Roomdb.ProductModel
import com.example.kamalsweets.databinding.FragmentCartBinding


class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding
    private lateinit var list:ArrayList<String>
    private lateinit var list1:ArrayList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        
        val context = context ?: return binding?.root
        val preference = context.getSharedPreferences("info", AppCompatActivity.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putBoolean("isCart", false)
        editor.apply()

        val dao = AppDatabase.getInstance(context).productDao()
        list = ArrayList()
        list1 = ArrayList()
        
        dao.getAllProducts().observe(viewLifecycleOwner) {
            if (_binding == null) return@observe
            if (it.isNotEmpty()) {
                binding?.noCart?.visibility = View.GONE
                binding?.materialCardView2?.visibility = View.VISIBLE
            } else {
                binding?.noCart?.visibility = View.VISIBLE
                binding?.materialCardView2?.visibility = View.GONE
            }
            binding?.cartRecycler?.adapter = CartAdapter(requireContext(), it)
            list.clear()
            list1.clear()
            for (data in it) {
                list.add(data.productId)
                list1.add(data.productQuantity.toString())
            }
            totalCost(it)
        }

        return binding?.root
    }

    private fun totalCost(data: List<ProductModel>?) {
        val context = context ?: return
        val paidPreference = context.getSharedPreferences("online_payment", Context.MODE_PRIVATE)
        val paidEditor = paidPreference.edit()
        var total = 0.0
        for (item in data!!) {
            total += item.productSp!!.toDouble()
        }

        val preference = activity?.getSharedPreferences("totalAmount", Context.MODE_PRIVATE)
        val editor = preference?.edit()
        editor?.putFloat("total", total.toFloat())
        editor?.apply()

        binding?.totalItemsCount?.text = data.size.toString()
        binding?.textView6?.text = "₹ $total"

        binding?.checkout?.setOnClickListener {
            if (data.isNotEmpty()) {
                binding?.checkout?.visibility = View.GONE
                binding?.paymentButtons?.visibility = View.VISIBLE
            } else {
                binding?.checkout?.visibility = View.VISIBLE
                binding?.paymentButtons?.visibility = View.GONE
                Toast.makeText(requireContext(), "Please Add Item To Cart", Toast.LENGTH_LONG).show()
            }
        }
        
        val intent = Intent(context, AddressActivity::class.java)
        val b = Bundle()
        b.putStringArrayList("productIds", list)
        b.putStringArrayList("productQuantity", list1)
        b.putString("totalCost", total.toString())
        intent.putExtras(b)
        
        binding?.onlinePayment?.setOnClickListener {
            paidEditor.putBoolean("paid", true)
            paidEditor.apply()
            startActivity(intent)
        }
        
        binding?.codPayment?.setOnClickListener {
            paidEditor.putBoolean("paid", false)
            paidEditor.apply()
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}