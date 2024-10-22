package com.example.adminkabirstore.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.adminkabirstore.R
import com.example.adminkabirstore.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class LoginActivity : BaseActivity() {

    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var dialog: SweetAlertDialog
    private lateinit var confirmBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            window.statusBarColor = resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
        }

        val googleSignInOption =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(
                getString(
                    R.string.default_web_client_id
                )
            ).requestEmail().build()
        auth = Firebase.auth

        database = Firebase.database.reference

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOption)

        binding.googleBtn.setOnClickListener {
            showLoader(true)
            if (isInternetOn(this)) {

                val signInIntent = googleSignInClient.signInIntent
                launcher.launch(signInIntent)
            } else {
                showLoader(true)
                dialog = SweetAlertDialog(
                    this,
                    SweetAlertDialog.ERROR_TYPE
                ).setTitleText(getString(R.string.title_internet))
                    .setContentText(getString(R.string.content_internet)).setConfirmText("Retry")
                dialog.setCancelable(false)
                dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                    // Optionally check again when the user presses the confirm button
                    sweetAlertDialog.dismissWithAnimation()
                    showLoader(false)
                }
                dialog.show()


                confirmBtn =
                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                confirmBtn.setTextColor(Color.BLACK)
                confirmBtn.textSize = 14f
            }
        }
        binding.notHaveAcTv.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBtn.setOnClickListener {
            email = binding.emailOrPhoneEt.text.toString().trim()
            password = binding.passwordEt.text.toString().trim()
            val passwordPattern = "^(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&])[A-Za-z\\d@\$!%*?&]{8,}$"

            if (email.isBlank() || password.isBlank()) {

                snackBarBottom(binding.root, "Please fill all the details")

            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                snackBarBottom(binding.root, "Please enter a valid email address")
            } else if (!password.matches(Regex(passwordPattern))) {
                snackBarBottom(
                    binding.root,
                    "Password must include 8+ characters, a digit, a capital letter, and a symbol."
                )
            } else {
                showLoader(true)
                if (isInternetOn(this)) {

                    createAccount(email, password)

                } else {
                    showLoader(true)
                    dialog = SweetAlertDialog(
                        this,
                        SweetAlertDialog.ERROR_TYPE
                    ).setTitleText(getString(R.string.title_internet))
                        .setContentText(getString(R.string.content_internet))
                        .setConfirmText("Retry")
                    dialog.setCancelable(false)
                    dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->  // Explicit type
                        // Optionally check again when the user presses the confirm button
                        sweetAlertDialog.dismissWithAnimation()

                    }
                    showLoader(false)
                    dialog.show()

                    confirmBtn =
                        dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                    confirmBtn.setTextColor(Color.BLACK)
                    confirmBtn.textSize = 14f
                }

            }
        }


    }

    private fun showLoader(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.overlayView.visibility = View.VISIBLE // Show the transparent overlay
        } else {
            binding.progressBar.visibility = View.GONE
            binding.overlayView.visibility = View.GONE // Hide the transparent overlay
        }
    }

    private fun createAccount(email: String, password: String) {
        showLoader(true)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
                showLoader(false)

            } else {
                val errorMessage = when (task.exception) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email. Please create an account."
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                    else -> "Login failed. Please try again."
                }

                snackBarBottom(binding.root, errorMessage)

                Log.d("Account", "loginAccount : Failure", task.exception)
                showLoader(false)
            }
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                if (task.isSuccessful) {
                    val account: GoogleSignInAccount = task.result
                    val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credentials).addOnCompleteListener {

                            authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(this, "Google Sign In Successful", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            snackBarBottom(binding.root, "Google Sign In Failed")

                            Log.d("Account", "GoogleSignIn : Failure", task.exception)
                        }
                    }
                }
                else {
                    snackBarBottom(binding.root, "Google Sign In Failed")

                    Log.d("Account", "GoogleSignIn : Failure", task.exception)
                }
            }
        }

    override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}