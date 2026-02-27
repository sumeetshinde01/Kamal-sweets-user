package com.example.kamalsweets.Fragment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import com.example.kamalsweets.Activity.CategorySeeAllActivity
import com.example.kamalsweets.Activity.EditProfileActivity
import com.example.kamalsweets.Activity.LoginActivity
import com.example.kamalsweets.Activity.ProductSeeAllActivity
import com.example.kamalsweets.Adapter.CategoryAdapter
import com.example.kamalsweets.Adapter.ProductAdapter
import com.example.kamalsweets.Model.AddProductModel
import com.example.kamalsweets.Model.CategoryModel
import com.example.kamalsweets.R
import com.example.kamalsweets.databinding.FragmentHomeBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private var productAdapter: ProductAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader()
        
        // Start shimmers
        binding?.categoryShimmerLayout?.startShimmer()
        binding?.productShimmerLayout?.startShimmer()
        
        getCategories()
        getProducts()
        setupSearchView()
        setupThemeToggle()
        setupOptionsMenu()

        binding?.productSeeAll?.setOnClickListener {
            val intent = Intent(requireContext(), ProductSeeAllActivity::class.java)
            startActivity(intent)
        }
        binding?.categorySeeAll?.setOnClickListener {
            val intent = Intent(requireContext(), CategorySeeAllActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupOptionsMenu() {
        binding?.optionsMenuButton?.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.option_menu, popupMenu.menu)

            val preference = requireContext().getSharedPreferences("otpActivityUser", Context.MODE_PRIVATE)
            val userName = preference.getString("name", "Default2")
            val phoneNumber = preference.getString("number", null)

            popupMenu.menu.findItem(R.id.user_N)?.title = userName
            popupMenu.menu.findItem(R.id.user_Number)?.title = phoneNumber

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.user_N -> {
                        Toast.makeText(requireContext(), "Welcome $userName", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.edit_profile -> {
                        startActivity(Intent(requireContext(), EditProfileActivity::class.java))
                        true
                    }
                    R.id.logout -> {
                        val editor = preference.edit()
                        editor.putBoolean("flag", false)
                        editor.apply()
                        startActivity(Intent(requireContext(), LoginActivity::class.java))
                        activity?.finish()
                        true
                    }
                    R.id.about -> {
                        // Handle about
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }

    private fun setupThemeToggle() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        // Set initial icon
        binding?.themeToggleButton?.setImageResource(if (isDarkMode) R.drawable.ic_sun else R.drawable.ic_moon)

        binding?.themeToggleButton?.setOnClickListener {
            // Simple rotation animation for the button
            it.animate().rotationBy(360f).setDuration(500).start()

            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    private fun setupHeader() {
        val preference = requireContext().getSharedPreferences("otpActivityUser", Context.MODE_PRIVATE)
        val userName = preference.getString("name", "Guest")
        binding?.locationText?.text = "Deliver to $userName's Home"
    }

    private fun setupSearchView() {
        binding?.searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productAdapter?.filter?.filter(newText)
                return true
            }
        })
    }

    private fun getProducts() {
        val list = ArrayList<AddProductModel>()
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {
                if (view == null) return@addOnSuccessListener
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(AddProductModel::class.java)
                    if (data != null) {
                        list.add(data)
                    }
                }
                list.shuffle()

                // Hide shimmer and show recyclerview
                binding?.productShimmerLayout?.stopShimmer()
                binding?.productShimmerLayout?.visibility = View.GONE
                binding?.productRecyclerView?.visibility = View.VISIBLE

                productAdapter = ProductAdapter(requireContext(), list)
                binding?.productRecyclerView?.adapter = productAdapter

            }.addOnFailureListener { error->
                if (view != null) {
                    binding?.productShimmerLayout?.stopShimmer()
                    binding?.productShimmerLayout?.visibility = View.GONE
                    Toast.makeText(requireContext(), "$error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getCategories() {
        val list = ArrayList<CategoryModel>()
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {
                if (view == null) return@addOnSuccessListener
                list.clear()
                for(doc in it.documents){
                    val data = doc.toObject(CategoryModel::class.java)
                    if (data != null) {
                        list.add(data)
                    }
                }
                list.shuffle()
                
                // Hide shimmer and show recyclerview
                binding?.categoryShimmerLayout?.stopShimmer()
                binding?.categoryShimmerLayout?.visibility = View.GONE
                binding?.categoryRecyclerView?.visibility = View.VISIBLE

                val adapter = CategoryAdapter(requireContext())
                binding?.categoryRecyclerView?.adapter = adapter
                adapter.submitList(list)
            }.addOnFailureListener {error->
                if (view != null) {
                    binding?.categoryShimmerLayout?.stopShimmer()
                    binding?.categoryShimmerLayout?.visibility = View.GONE
                    Toast.makeText(requireContext(), "$error", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
