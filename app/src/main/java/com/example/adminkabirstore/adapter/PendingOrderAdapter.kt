package com.example.adminkabirstore.adapter

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.adminkabirstore.R
import com.example.adminkabirstore.databinding.PendingOrderRvBinding
import com.google.android.material.snackbar.Snackbar

class PendingOrderAdapter(
    private val context: Context,
    private val pendingCustomerName: MutableList<String>,
    private val pendingCustomerQuantity: MutableList<String>,
    private val pendingCustomerImage: MutableList<String>,
    private val itemClicked: OnItemClicked

) : RecyclerView.Adapter<PendingOrderAdapter.PendingViewHolder>() {

    interface OnItemClicked {
        fun onItemClickListener(position: Int)
        fun onItemAcceptClickListener(position: Int)
        fun onItemDispatchClickListener(position: Int)
    }

    inner class PendingViewHolder(private val binding: PendingOrderRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var isAccepted = false
        fun bind(position: Int) {
            binding.apply {
                pendingCustomerNameTv.text = pendingCustomerName[position]
                pendingQuantityTv.text = pendingCustomerQuantity[position]
                val uriString = pendingCustomerImage[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(pendingImage)

                pendingAcceptBtn.apply {

                    if (!isAccepted) {
                        text = "Accept"
                    } else {
                        text = "Dispatch"
                    }
                    setOnClickListener {
                        if (!isAccepted) {
                            text = "Dispatch"
                            isAccepted = true
                            snackBarBottom(binding.root,"Order Accepted")
                            itemClicked.onItemAcceptClickListener(position)
                        } else {

                            pendingCustomerName.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            snackBarBottom(binding.root,"Order Dispatched")
                            itemClicked.onItemDispatchClickListener(position)

                        }
                    }


                }
                itemView.setOnClickListener {
                    itemClicked.onItemClickListener(position)
                }

            }

        }

        fun snackBarBottom(view : View, msg: String) {

            val snack: Snackbar = Snackbar.make(view, msg, 1000)
            val view = snack.view
            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.BOTTOM
            view.layoutParams = params
            snack.setBackgroundTint(getColor(context,R.color.lavender))
                .setTextColor(Color.BLACK)
            snack.show()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val view = PendingOrderRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pendingCustomerName.size
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        holder.bind(position)
    }
}