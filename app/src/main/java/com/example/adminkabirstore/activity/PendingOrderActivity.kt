package com.example.adminkabirstore.activity

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.adminkabirstore.R
import com.example.adminkabirstore.adapter.PendingOrderAdapter
import com.example.adminkabirstore.databinding.ActivityPendingOrderBinding
import com.example.adminkabirstore.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PendingOrderActivity : BaseActivity(), PendingOrderAdapter.OnItemClicked {
    private lateinit var binding: ActivityPendingOrderBinding
    private var listOfName: MutableList<String> = mutableListOf()
    private var listOfTotalPrice: MutableList<String> = mutableListOf()
    private var listOfImageFirstProductOrder: MutableList<String> = mutableListOf()
    private var listOfOrderItem: MutableList<OrderModel> = mutableListOf()
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var dataBaseOrderDetailsReference: DatabaseReference
    private lateinit var pendingOrderAdapter: PendingOrderAdapter
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
        }

        dataBase = FirebaseDatabase.getInstance()
        dataBaseOrderDetailsReference = dataBase.reference.child("OrderDetails")

        showLoader(true)

        binding.swipeToRefresh.setOnRefreshListener {
            checkInternetAndGetOrderDetails()
        }
        checkInternetAndGetOrderDetails()

        binding.backBtn.setOnClickListener {
            finish()
        }


    }

    private fun checkInternetAndGetOrderDetails() {
        if (isInternetOn(this)) {
            getOrderDetails()
        } else {
            dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText(
                getString(R.string.title_internet)
            ).setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->
                // Explicit type
                getOrderDetails()
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
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


    private fun getOrderDetails() {

        // Clear existing lists to avoid duplication
        listOfOrderItem.clear()
        listOfName.clear()
        listOfTotalPrice.clear()
        listOfImageFirstProductOrder.clear()

        dataBaseOrderDetailsReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfOrderItem.clear()
                for (orderDetailsSnapshot in snapshot.children) {
                    val orderDetailsData = orderDetailsSnapshot.getValue(OrderModel::class.java)
                    orderDetailsData?.let {
                        listOfOrderItem.add(it)
                    }
                }
                listOfOrderItem.reverse()
                if (listOfOrderItem.isEmpty()) {
                    binding.emptyPendingOrderTextView.visibility = View.VISIBLE
                    binding.pendingOrderRv.visibility = View.GONE
                } else {
                    addDataToListForRecyclerView()
                    binding.emptyPendingOrderTextView.visibility = View.GONE
                    binding.pendingOrderRv.visibility = View.VISIBLE
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

    private fun addDataToListForRecyclerView() {


        for (orderItem in listOfOrderItem) {

            orderItem.userName?.let { listOfName.add(it) }
            orderItem.productPrice?.let { listOfTotalPrice.add("₹" + it.firstOrNull()?.toString() ?: "N/A") }
            orderItem.productImage?.filterNot { it.isEmpty() }?.forEach {
                listOfImageFirstProductOrder.add(it)
            }
        }
        setAdapter()
    }

    private fun setAdapter() {

        pendingOrderAdapter = PendingOrderAdapter(
            this, listOfName, listOfTotalPrice, listOfImageFirstProductOrder, this
        )

        binding.pendingOrderRv.layoutManager = LinearLayoutManager(this)
        binding.pendingOrderRv.adapter = pendingOrderAdapter
        pendingOrderAdapter.notifyDataSetChanged()

    }

    override fun onItemClickListener(position: Int) {
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val userOrderDetails = listOfOrderItem[position]
        intent.putExtra("userOrderDetails", userOrderDetails)
        startActivity(intent)
    }

    override fun onItemAcceptClickListener(position: Int) {
        val childItemPushKey = listOfOrderItem[position].itemPushKey
        if (childItemPushKey != null) {
            val clickItemOrderReference =
                dataBase.reference.child("OrderDetails").child(childItemPushKey)
            // Update only the 'AcceptedOrder' field, not the entire object
            clickItemOrderReference.child("orderAccepted").setValue(true)
            updateOrderAcceptStatus(position)

        } else {
            snackBarBottom(binding.root,"Invalid order key")
        }
    }

    private fun updateOrderAcceptStatus(position: Int) {
        val userIdOfClickedItem = listOfOrderItem[position].userId
        val pushKeyOfClickedItem = listOfOrderItem[position].itemPushKey
        if (userIdOfClickedItem != null && pushKeyOfClickedItem != null) {
            val buyHistoryReference =
                dataBase.reference.child("user").child(userIdOfClickedItem).child("BuyHistory")
                    .child(pushKeyOfClickedItem)

            // Update only the 'AcceptedOrder' field, not the entire object
            buyHistoryReference.child("orderAccepted").setValue(true)
            dataBaseOrderDetailsReference.child(pushKeyOfClickedItem).child("orderAccepted")
                .setValue(true)
        } else {
            snackBarBottom(binding.root,"Invalid user ID or order key")
        }
    }

    override fun onItemDispatchClickListener(position: Int) {
        val dispatchItemPushKey = listOfOrderItem[position].itemPushKey
        val orderDataToMove = listOfOrderItem[position]

        if (dispatchItemPushKey != null && orderDataToMove != null) {
            // Set the OrderModel object as the value in the CompletedOrder node
            val dispatchItemReference =
                dataBase.reference.child("CompletedOrder").child(dispatchItemPushKey)
            dispatchItemReference.setValue(orderDataToMove).addOnSuccessListener {
                // If successful, remove the order from OrderDetails
                deleteThisItemFromOrderDetails(dispatchItemPushKey)
            }.addOnFailureListener {

                snackBarBottom(binding.root,"Failed to dispatch order: ${it.message}")
            }
        } else {

            snackBarBottom(binding.root,"Invalid order key or order data")
        }
    }

    private fun deleteThisItemFromOrderDetails(dispatchItemPushKey: String) {

        val orderDetailsReference =
            dataBase.reference.child("OrderDetails").child(dispatchItemPushKey)
        orderDetailsReference.removeValue().addOnSuccessListener {

        }.addOnFailureListener {
            snackBarBottom(binding.root,"Order Dispatch Failed")
        }
    }


}