package com.example.adminkabirstore.model

import java.io.Serializable

class OrderModel() : Serializable {

    var userId: String? = null
    var userName: String? = null
    var address: String? = null
    var phone: String? = null
    var totalAmount: String? = null
    var productName: MutableList<String>? = null
    var productPrice: MutableList<String>? = null
    var productImage: MutableList<String>? = null
    var productQuantity: MutableList<Int>? = null
    var colors: MutableList<String>? = null
    var itemPushKey: String? = null
    var orderAccepted: Boolean = false
    var paymentReceived: Boolean = false
    var currentTime: Long = 0


    constructor(
        userId: String,
        name: String,
        address: String,
        phone: String,
        totalAmount: String,
        productName: ArrayList<String>,
        productPrice: ArrayList<String>,
        productImage: ArrayList<String>,
        productQuantity: ArrayList<Int>,
        colors: ArrayList<String>,
        itemPushKey: String?,
        orderAccepted: Boolean,
        paymentReceived: Boolean,
        time: Long
    ) : this() {
        this.userId = userId
        this.userName = name
        this.address = address
        this.phone = phone
        this.totalAmount = totalAmount
        this.productName = productName
        this.productPrice = productPrice
        this.productImage = productImage
        this.productQuantity = productQuantity
        this.colors = colors
        this.itemPushKey = itemPushKey
        this.orderAccepted = orderAccepted
        this.paymentReceived = paymentReceived
        this.currentTime = time
    }

    override fun toString(): String {
        return "OrderModel(userName=$userName, totalAmount=$totalAmount, productImage=$productImage, acceptedOrder=$orderAccepted, itemPushKey=$itemPushKey, userId=$userId)"
    }

}
