package com.example.adminkabirstore.activity

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.adminkabirstore.R
import com.google.android.material.snackbar.Snackbar



open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

     fun snackBarTop(view : View , msg: String) {

        val snack: Snackbar = Snackbar.make(view, msg, 1000)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snack.setBackgroundTint(resources.getColor(R.color.lavender))
            .setTextColor(Color.BLACK)
        snack.show()

    }

    fun snackBarBottom(view : View , msg: String) {

        val snack: Snackbar = Snackbar.make(view, msg, 1000)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.BOTTOM
        view.layoutParams = params
        snack.setBackgroundTint(resources.getColor(R.color.lavender))
            .setTextColor(Color.BLACK)
        snack.show()

    }

    fun isInternetOn(context: Context): Boolean {
        try {
            val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) { // connected to the internet
                if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    return true
                } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    return true
                }
            } else {
                // not connected to the internet
                return false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }




}