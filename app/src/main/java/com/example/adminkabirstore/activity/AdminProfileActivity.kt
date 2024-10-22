package com.example.adminkabirstore.activity

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.adminkabirstore.R
import com.example.adminkabirstore.databinding.ActivityAdminProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminProfileActivity : BaseActivity() {
    private lateinit var binding: ActivityAdminProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adminReference: DatabaseReference
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showLoader(true)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor =
                resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        adminReference = database.reference.child("user")

        binding.adminName.isEnabled = false
        binding.adminEmail.isEnabled = false
        binding.adminAddress.isEnabled = false
        binding.adminNumber.isEnabled = false
        binding.shopName.isEnabled = false
        binding.adminSaveInfoBtn.isEnabled = false
        binding.adminSaveInfoBtn.setBackgroundResource(R.drawable.proceed_disabled)

        var isEnabled = false
        binding.adminEditProfile.setOnClickListener {
            isEnabled = !isEnabled

            binding.adminName.isEnabled = isEnabled
            binding.adminEmail.isEnabled = isEnabled
            binding.adminAddress.isEnabled = isEnabled
            binding.adminNumber.isEnabled = isEnabled
            binding.shopName.isEnabled = isEnabled
            binding.adminSaveInfoBtn.isEnabled = isEnabled

            if (!binding.adminSaveInfoBtn.isEnabled) {
                binding.adminSaveInfoBtn.setBackgroundResource(R.drawable.proceed_disabled)
            } else {
                binding.adminSaveInfoBtn.setBackgroundResource(R.drawable.login_btn_shape)
            }


            if (isEnabled) {
                binding.adminName.requestFocus()
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.adminSaveInfoBtn.setOnClickListener {

            if (isInternetOn(this)) {
                var updateName = binding.adminName.text.toString()
                var updateShopName = binding.shopName.text.toString()
                var updateEmail = binding.adminEmail.text.toString()
                var updatePhone = binding.adminNumber.text.toString()
                var updateAddress = binding.adminAddress.text.toString()

                val namePattern = "^[A-Za-z\\s]{2,50}$"
                val mobilePattern = "^(?:\\+91|91)?[789]\\d{9}$"
                val addressPattern = "^[a-zA-Z0-9\\s,.#-]+$"

                if (updateName.isBlank() || updateShopName.isBlank() || updateAddress.isBlank() || updatePhone.isBlank() || updateEmail.isBlank()) {
                    snackBarBottom(binding.root, "Please fill all the details")

                } else if (!updateName.matches(Regex(namePattern))) {
                    snackBarBottom(
                        binding.root, "Please enter a valid name (2-50 alphabetic characters)"
                    )
                } else if (!updateShopName.matches(Regex(namePattern))) {
                    snackBarBottom(
                        binding.root, "Please enter a valid shop name (2-50 alphabetic characters)"
                    )
                } else if (!Patterns.EMAIL_ADDRESS.matcher(updateEmail).matches()) {
                    snackBarBottom(binding.root, "Please enter a valid email address")
                } else if (!updateAddress.matches(Regex(addressPattern))) {
                    snackBarBottom(binding.root, "Please enter a valid address.")
                } else if (!updatePhone.matches(Regex(mobilePattern))) {
                    snackBarBottom(binding.root, "Please enter a valid mobile number.")
                } else {
                    showLoader(true)
                    updateAdminData()
                }
            } else {
                dialog = SweetAlertDialog(
                    this, SweetAlertDialog.ERROR_TYPE
                ).setTitleText(getString(R.string.title_internet))
                    .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
                dialog.setCancelable(false)
                dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                    sweetAlertDialog.dismissWithAnimation()
                }
                dialog.show()


                confirmBtn =
                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                confirmBtn.setTextColor(Color.BLACK)
                confirmBtn.textSize = 14f
            }
        }

        checkInternetAndRetrieveAdminData()

    }

    private fun showLoader(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.overlayView.visibility = View.VISIBLE
            binding.overlayView.isClickable = true

            hideKeyboard()// Show the transparent overlay
        } else {
            binding.progressBar.visibility = View.GONE
            binding.overlayView.visibility = View.GONE
            binding.overlayView.isClickable = false// Hide the transparent overlay
        }
    }

    private fun checkInternetAndRetrieveAdminData() {
        if (isInternetOn(this)) {
            retrieveAdminData()
        } else {
            dialog = SweetAlertDialog(
                this, SweetAlertDialog.ERROR_TYPE
            ).setTitleText(getString(R.string.title_internet))
                .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
            dialog.setCancelable(false)
            dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                // Optionally check again when the user presses the confirm button
                retrieveAdminData()
                sweetAlertDialog.dismissWithAnimation()
            }
            dialog.show()


            confirmBtn =
                dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
            confirmBtn.setTextColor(Color.BLACK)
            confirmBtn.textSize = 14f
        }
    }

    private fun updateAdminData() {

showLoader(true)
        var updateName = binding.adminName.text.toString()
        var updateShopName = binding.shopName.text.toString()
        var updateEmail = binding.adminEmail.text.toString()
        var updatePhone = binding.adminNumber.text.toString()
        var updateAddress = binding.adminAddress.text.toString()
        val currentUserId = auth.currentUser?.uid
        if (currentUserId != null) {

            val userReference = adminReference.child(currentUserId)
            userReference.child("username").setValue(updateName)
            userReference.child("nameOfShop").setValue(updateShopName)
            userReference.child("email").setValue(updateEmail)
            userReference.child("phone").setValue(updatePhone)
            userReference.child("address").setValue(updateAddress)


snackBarBottom(binding.root, "Profile Updated")
            auth.currentUser?.updateEmail(updateEmail)
            showLoader(false)
        } else {
            showLoader(false)
            snackBarBottom(binding.root, "Profile Update failed")
        }


    }

    private fun retrieveAdminData() {

        var currentUser = auth.currentUser?.uid
        if (currentUser != null) {
            var userReference = adminReference.child(currentUser)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val username = snapshot.child("username").value
                        val nameOfShop = snapshot.child("nameOfShop").value
                        val address = snapshot.child("address").value
                        val email = snapshot.child("email").value
                        val phone = snapshot.child("phone").value

                        setDataToTv(username, nameOfShop, address, email, phone)

                    }

                    showLoader(false)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

    private fun setDataToTv(
        username: Any?, nameOfShop: Any?, address: Any?, email: Any?, phone: Any?
    ) {

        binding.adminName.setText(username.toString())
        binding.shopName.setText(nameOfShop.toString())
        binding.adminAddress.setText(address.toString())
        binding.adminEmail.setText(email.toString())
        binding.adminNumber.setText(phone.toString())

    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // Get the current focused view and hide the keyboard from it
        var view = currentFocus
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}