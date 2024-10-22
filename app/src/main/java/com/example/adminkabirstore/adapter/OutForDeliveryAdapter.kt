package com.example.adminkabirstore.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminkabirstore.R
import com.example.adminkabirstore.databinding.OutfordeliveryRvBinding

class OutForDeliveryAdapter(
    private val customerName: MutableList<String>,
    private val customerPaymentStatus: MutableList<Boolean>
) : RecyclerView.Adapter<OutForDeliveryAdapter.OutForDeliveryViewHolder>() {
    inner class OutForDeliveryViewHolder(private val binding: OutfordeliveryRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                customerNameOFD.text = customerName[position]

                if (customerPaymentStatus[position] == true) {
                    paymentStatusOfd.text = "Received"
                } else {
                    paymentStatusOfd.text = "Not Received"
                }

                val colorMap = mapOf(
                    true to Color.GREEN, false to Color.RED
                )
                paymentStatusOfd.setTextColor(
                    colorMap[customerPaymentStatus[position]] ?: Color.BLACK
                )
                deliveredImgOfd.backgroundTintList =
                    ColorStateList.valueOf(colorMap[customerPaymentStatus[position]] ?: Color.BLACK)

            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutForDeliveryViewHolder {
        val view =
            OutfordeliveryRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OutForDeliveryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return customerName.size
    }

    override fun onBindViewHolder(holder: OutForDeliveryViewHolder, position: Int) {
        holder.bind(position)
    }
}