package com.example.adminkabirstore.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminkabirstore.R
import com.example.adminkabirstore.adapter.OrderDetailsAdapter
import com.example.adminkabirstore.databinding.ActivityOrderDetailsBinding
import com.example.adminkabirstore.model.OrderModel

class OrderDetailsActivity : AppCompatActivity() {
    private val binding: ActivityOrderDetailsBinding by lazy {
        ActivityOrderDetailsBinding.inflate(layoutInflater)
    }

    private var name: String? = null
    private var address: String? = null
    private var phone: String? = null
    private var totalPrice: String? = null
    private var productNames: ArrayList<String> = arrayListOf()
    private var productPrices: ArrayList<String> = arrayListOf()
    private var productQuantities: ArrayList<Int> = arrayListOf()
    private var productColors: ArrayList<String> = arrayListOf()
    private var productImages: ArrayList<String> = arrayListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
        }

        binding.backBtn.setOnClickListener {
            finish()
        }



        getDataFromIntent()

    }

    private fun getDataFromIntent() {
        val receiveOrderDetails = intent.getSerializableExtra("userOrderDetails") as OrderModel
        receiveOrderDetails.let {
            name = receiveOrderDetails.userName
            phone = receiveOrderDetails.phone
            address = receiveOrderDetails.address
            totalPrice = receiveOrderDetails.totalAmount
            productNames = receiveOrderDetails.productName as ArrayList<String>
            productPrices = receiveOrderDetails.productPrice as ArrayList<String>
            productImages = receiveOrderDetails.productImage as ArrayList<String>
            productQuantities = receiveOrderDetails.productQuantity as ArrayList<Int>
            productColors = receiveOrderDetails.colors as ArrayList<String>
            setUserDetails()
            setAdapter()
        }

    }

    private fun setAdapter() {
        val orderDetailsAdapter =
            OrderDetailsAdapter(this, productNames, productPrices, productImages, productQuantities,productColors)
        binding.orderDetailsRv.layoutManager = LinearLayoutManager(this)
        binding.orderDetailsRv.adapter = orderDetailsAdapter
    }

    private fun setUserDetails() {

        binding.apply {

            nameTv.text = name
            addressTv.text = address
            phoneTv.text = phone
            totalAmountTv.text = "₹" + productPrices.firstOrNull().toString()
        }
    }


}