package com.example.adminkabirstore.adapter

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.bumptech.glide.Glide
import com.example.adminkabirstore.databinding.AllItemRvBinding
import com.example.adminkabirstore.model.AllMenu
import com.google.firebase.database.DatabaseReference

class AllItemAdapter(
    private val context: Context,
    private val menuList: ArrayList<AllMenu>,
    databaseReference: DatabaseReference,
    private val onDeleteClickListener: (position : Int) -> Unit
) : RecyclerView.Adapter<AllItemAdapter.MyAllItemViewHolder>() {
    inner class MyAllItemViewHolder(private val binding: AllItemRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val menuItem = menuList[position]
                val uriString = menuItem.productImage
                val imageUri = Uri.parse(uriString)

                allItemProductName.text = menuItem.productName
                allItemPriceTv.text = "₹" + menuItem.productPrice
                Glide.with(context).load(imageUri).into(allItemImage)

                allItemRemoveImgBtn.setOnClickListener {
                    var dialog = SweetAlertDialog(
                        context,
                        SweetAlertDialog.WARNING_TYPE
                    ).setTitleText("Delete Item")
                        .setContentText("Are you Sure ?")
                        .setConfirmText("Yes").setCancelText("No")
                        .setConfirmClickListener { sweetAlertDialog ->
                            sweetAlertDialog.dismissWithAnimation()
                            onDeleteClickListener(position)
                             // Close the dialog

                        }.setCancelClickListener { sweetAlertDialog ->
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
            }

        }

    }


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): MyAllItemViewHolder {

        val view = AllItemRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyAllItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyAllItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

}