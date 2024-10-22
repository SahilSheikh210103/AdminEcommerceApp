package com.example.adminkabirstore.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminkabirstore.R
import com.example.adminkabirstore.adapter.OutForDeliveryAdapter
import com.example.adminkabirstore.databinding.ActivityOutForDeliveryBinding
import com.example.adminkabirstore.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OutForDeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOutForDeliveryBinding
    private lateinit var database: FirebaseDatabase
    private var listOfCompleteOrder: ArrayList<OrderModel> = arrayListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOutForDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
        }

        showLoader(true)
        binding.swipeToRefresh.setOnRefreshListener {
            retrieveCompleteOrderDetails()
        }
        retrieveCompleteOrderDetails()

        binding.backBtn.setOnClickListener {
            finish()
        }

    }


    private fun showLoader(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun retrieveCompleteOrderDetails() {
        database = FirebaseDatabase.getInstance()
        val completeOrderReference =
            database.reference.child("CompletedOrder").orderByChild("currentTime")
        completeOrderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfCompleteOrder.clear()

                for (completeOrder in snapshot.children) {
                    val completeOrderList = completeOrder.getValue(OrderModel::class.java)
                    completeOrderList?.let {
                        listOfCompleteOrder.add(it)
                    }
                }
                listOfCompleteOrder.reverse()
                if (listOfCompleteOrder.isEmpty()) {
                    binding.emptyOutForDeliveryTextView.visibility = View.VISIBLE
                    binding.outForDeliveryRv.visibility = View.GONE
                } else {
                    setAdapter()
                    binding.emptyOutForDeliveryTextView.visibility = View.GONE
                    binding.outForDeliveryRv.visibility = View.VISIBLE
                }

                showLoader(false)
                binding.swipeToRefresh.isRefreshing = false

            }

            override fun onCancelled(error: DatabaseError) {
                showLoader(false)
                binding.swipeToRefresh.isRefreshing = false
            }

        })
    }

    private fun setAdapter() {

        val customerName = mutableListOf<String>()
        val paymentStatus = mutableListOf<Boolean>()
        for (order in listOfCompleteOrder) {

            order.userName?.let {
                customerName.add(it)
            }

            order.paymentReceived.let {
                paymentStatus.add(it)
            }
        }


        val outForDeliveryAdapter = OutForDeliveryAdapter(customerName, paymentStatus)
        binding.outForDeliveryRv.adapter = outForDeliveryAdapter
        binding.outForDeliveryRv.layoutManager = LinearLayoutManager(this)

    }
}