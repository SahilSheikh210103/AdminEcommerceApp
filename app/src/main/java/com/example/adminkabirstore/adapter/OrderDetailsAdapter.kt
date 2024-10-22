package com.example.adminkabirstore.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminkabirstore.databinding.OrderDetailsRvBinding

class OrderDetailsAdapter(
    private val context: Context,
    private val productName: ArrayList<String>,
    private val productPrice: ArrayList<String>,
    private val productImage: ArrayList<String>,
    private val productQuantity: ArrayList<Int>,
    private val productColor: ArrayList<String>
) : RecyclerView.Adapter<OrderDetailsAdapter.MyViewHolder>() {
    inner class MyViewHolder(private val binding: OrderDetailsRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int){
            binding.apply {
                orderDetailsProductNameAt.text = productName[position]
                orderDetailsPriceAt.text = "₹" + productPrice[position]
                orderDetailsQuantityAt.text = productQuantity[position].toString()
                colorTv.text = productColor[position]
                val uriString = productImage[position]
                val uri  = Uri.parse(uriString)
                Glide.with(context).load(uri).into(orderDetailsProductImgAt)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OrderDetailsAdapter.MyViewHolder {
        val view = OrderDetailsRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderDetailsAdapter.MyViewHolder, position: Int) {
        holder.bind(position)

    }

    override fun getItemCount(): Int {
        return productName.size
    }
}