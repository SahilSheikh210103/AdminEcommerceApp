package com.example.adminkabirstore.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.adminkabirstore.R
import com.example.adminkabirstore.adapter.AllItemAdapter
import com.example.adminkabirstore.databinding.ActivityAllItemBinding
import com.example.adminkabirstore.model.AllMenu
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllItemActivity : BaseActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private val menuItems: ArrayList<AllMenu> = ArrayList()
    private lateinit var allItemAdapter: AllItemAdapter
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    private lateinit var binding: ActivityAllItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoader(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor =
                resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
        }

        databaseReference = FirebaseDatabase.getInstance().reference

        binding.swipeToRefresh.setOnRefreshListener {
            checkInternetAndRetrieveMenuItem()
        }

        checkInternetAndRetrieveMenuItem()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun checkInternetAndRetrieveMenuItem() {

        if (isInternetOn(this)) {
            retrieveMenuItem()
        } else {
            dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText(
                getString(R.string.title_internet)
            ).setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->
                // Explicit type
                retrieveMenuItem()
                sweetAlertDialog.dismissWithAnimation()
            }
            dialog.show()

            confirmBtn =
                dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
            confirmBtn.setTextColor(Color.BLACK)
            confirmBtn.textSize = 14f


        }

    }

    private fun showLoader(isLoading: Boolean) {
        if (isLoading) {

            binding.progressBar.visibility = View.VISIBLE
            binding.overlayView.visibility = View.VISIBLE
            binding.totalItemCountTv.visibility = View.GONE
            binding.overlayView.isClickable = true

        } else {

            binding.progressBar.visibility = View.GONE
            binding.overlayView.visibility = View.GONE
            binding.totalItemCountTv.visibility = View.VISIBLE
            binding.overlayView.isClickable = false
        }
    }

    private fun retrieveMenuItem() {
        database = FirebaseDatabase.getInstance()

        val productRef = database.reference.child("menu")

        var itemCount = 0
        productRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                menuItems.clear()

                for (productSnapshot in snapshot.children) {
                    val menuItem = productSnapshot.getValue(AllMenu::class.java)

                    menuItem?.let {
                        menuItems.add(it)
                    }

                }
                itemCount = snapshot.childrenCount.toInt()
                binding.totalItemCountTv.text = itemCount.toString()
                menuItems.reverse()
                if (menuItems.isEmpty()) {
                    binding.emptyAllItemOrderTextView.visibility = View.VISIBLE
                    binding.allItemRv.visibility = View.GONE

                } else {
                    setAdapter()
                    binding.emptyAllItemOrderTextView.visibility = View.GONE
                    binding.allItemRv.visibility = View.VISIBLE

                }

                showLoader(false)
                binding.swipeToRefresh.isRefreshing = false

            }

            override fun onCancelled(error: DatabaseError) {
                showLoader(false)
                Log.d("DatabaseError", "Error:${error.message}")
            }

        })
    }

    private fun setAdapter() {
        // Initialize the adapter
        allItemAdapter = AllItemAdapter(this, menuItems, databaseReference) { position ->
            deleteMenuItems(position, allItemAdapter)
        }

        // Set up the RecyclerView
        binding.allItemRv.layoutManager = LinearLayoutManager(this)
        binding.allItemRv.adapter = allItemAdapter
    }

    private fun deleteMenuItems(position: Int, allItemAdapter: AllItemAdapter) {


        if (position >= 0 && position < menuItems.size) {
            val menuItemToDelete = menuItems[position]
            val menuItemKey = menuItemToDelete.key

            // Ensure the key is not null before proceeding
            if (menuItemKey != null) {
                val menuReference = databaseReference.child("menu").child(menuItemKey)

                menuReference.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        // Remove the item from the list
                        menuItems.removeAt(position)

                        snackBarBottom(binding.root, "Item Deleted Successfully")
                        retrieveMenuItem()

                        // Notify the adapter of item removal
                        allItemAdapter.notifyItemRemoved(position)

                        // Optionally, notify other items to update their positions
                        allItemAdapter.notifyItemRangeChanged(position, menuItems.size)
                    } else {
                        Log.d("deleteMenuItems", "Failed to delete item from Firebase")
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("deleteMenuItems", "MenuItemKey is null")
                Toast.makeText(this, "Invalid menu item key", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("deleteMenuItems", "Invalid item position: $position")
            Toast.makeText(this, "Invalid item position", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // Get the current focused view and hide the keyboard from it
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }


}