package com.example.adminkabirstore.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.adminkabirstore.R
import com.example.adminkabirstore.databinding.ActivityMainBinding
import com.example.adminkabirstore.model.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var completeOrderReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()

        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor =
                resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
        }

        binding.addMenu.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }
        binding.allItemMenu.setOnClickListener {
            val intent = Intent(this, AllItemActivity::class.java)
            startActivity(intent)
        }

        binding.orderDispatchMenu.setOnClickListener {
            val intent = Intent(this, OutForDeliveryActivity::class.java)
            startActivity(intent)
        }


        binding.profileMenu.setOnClickListener {
            val intent = Intent(this, AdminProfileActivity::class.java)
            startActivity(intent)
        }

        binding.pendingOrder.setOnClickListener {
            val intent = Intent(this, PendingOrderActivity::class.java)
            startActivity(intent)
        }

        binding.logOutMenu.setOnClickListener {
            showLogoutDialog()
        }
        binding.swipeToRefresh.setOnRefreshListener {
            pendingOrder()
            completeOrder()
            wholeTimeEarning()
        }

        pendingOrder()
        completeOrder()
        wholeTimeEarning()

    }

    private fun wholeTimeEarning() {
        var listOfTotalPay = mutableListOf<Int>()
        completeOrderReference = database.reference.child("CompletedOrder")
        completeOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (orderSnapshot in snapshot.children) {
                    var completeOrder = orderSnapshot.getValue(OrderModel::class.java)
                    completeOrder?.productPrice.toString().replace("\\D".toRegex(), "").trim()
                        .let { cleanedAmount ->

                            val amountInt = cleanedAmount.toIntOrNull()
                            if (amountInt != null) {
                                listOfTotalPay.add(amountInt)
                                Log.d(
                                    "AmountAdded", amountInt.toString()
                                ) // Check if it's added correctly
                            } else {
                                Log.d("ConversionFailed", "Couldn't convert $cleanedAmount to Int")
                            }
                            Log.d("EarningsList", listOfTotalPay.toString())
                        }

                }
                binding.totalEarningTv.text = "₹" + listOfTotalPay.sum().toString()
                binding.swipeToRefresh.isRefreshing = false

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun completeOrder() {
        completeOrderReference = database.reference.child("CompletedOrder")
        var completeItemCount = 0
        completeOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                completeItemCount = snapshot.childrenCount.toInt()
                binding.completeOrderCountTv.text = completeItemCount.toString()
                binding.swipeToRefresh.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun pendingOrder() {
        var pendingOrderReference = database.reference.child("OrderDetails")
        var pendingItemCount = 0
        pendingOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                pendingItemCount = snapshot.childrenCount.toInt()
                binding.pendingOrderCountTv.text = pendingItemCount.toString()
                binding.swipeToRefresh.isRefreshing = false
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    fun showLogoutDialog() {
        var dialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Are you sure?")
            .setContentText("You won't be able to access your account until you log in again!")
            .setConfirmText("Yes, log out")
            .setCancelText("Cancel")
            .setConfirmClickListener { sweetAlertDialog ->
                sweetAlertDialog.dismissWithAnimation() // Close the dialog
                performLogout() // Call the logout function
            }
            .setCancelClickListener { sweetAlertDialog ->
                sweetAlertDialog.dismissWithAnimation() // Simply dismiss the dialog on cancel
            }
        dialog.show()

        val confirmButton =
            dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
        val cancelButton =
            dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.cancel_button) as Button

// Change the text color of the buttons
        confirmButton.setTextColor(Color.BLACK)
        confirmButton.textSize = 14f// Confirm button text color
        cancelButton.setTextColor(Color.BLACK)
        cancelButton.textSize = 14f


    }

    fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        // Redirect to login or splash screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close current activity
    }
}