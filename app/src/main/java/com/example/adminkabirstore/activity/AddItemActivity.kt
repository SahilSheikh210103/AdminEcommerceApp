    package com.example.adminkabirstore.activity


    import android.annotation.SuppressLint
    import android.graphics.Color
    import android.net.Uri
    import android.os.Build
    import android.os.Bundle
    import android.view.View
    import android.view.inputmethod.InputMethodManager
    import android.widget.ArrayAdapter
    import android.widget.Button
    import android.widget.TextView
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.appcompat.app.AlertDialog
    import cn.pedant.SweetAlert.SweetAlertDialog
    import com.example.adminkabirstore.R
    import com.example.adminkabirstore.databinding.ActivityAddItemBinding
    import com.example.adminkabirstore.model.AllMenu
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.storage.FirebaseStorage


    class AddItemActivity : BaseActivity() {
        private lateinit var binding: ActivityAddItemBinding

        private lateinit var productName: String
        private lateinit var productPrice: String
        private var productImageUri: Uri? = null
        private lateinit var productDescription: String
        private lateinit var productIngredients: String
        private lateinit var productCategory: String
        private lateinit var auth: FirebaseAuth
        private lateinit var database: FirebaseDatabase
        private lateinit var dialog: SweetAlertDialog
        private lateinit var confirmBtn: Button
        private lateinit var selectedColors: BooleanArray // Array to track selected colors
        private lateinit var colorArray: Array<String> // List of colors
        private val selectedColorList = ArrayList<String>() // List of selected colors


        @SuppressLint("ClickableViewAccessibility")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityAddItemBinding.inflate(layoutInflater)

            setContentView(binding.root)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor =
                    resources.getColor(R.color.lavender)// Optionally change the status bar background color to white
            }

            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance()
            // Initialize color array
            colorArray = arrayOf("Red", "Green", "Blue", "Yellow", "Black", "White")
            selectedColors = BooleanArray(colorArray.size) // Initialize array for selected colors

            val colorSpinner = findViewById<TextView>(R.id.colorSpinner) // TextView simulating Spinner behavior

            // Set click listener to show color selection dialog
            colorSpinner.setOnClickListener {
                showColorSelectionDialog(colorSpinner)
            }

            binding.addProductImg.setOnClickListener {
                pickImage.launch("image/*")
            }

            binding.backBtn.setOnClickListener {
                finish()
            }


            val listOfCategory = arrayOf("Cosmetics", "Jewellery", "Keychains", "Clature")
            val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfCategory)
            val autoCompleteTextView = binding.listOfCategory
            autoCompleteTextView.setAdapter(arrayAdapter)


            binding.addItemBtn.setOnClickListener {
                val selectedColors = selectedColorList.joinToString(", ")
                productName = binding.addProductName.text.toString().trim()
                productPrice = binding.addProductPrice.text.toString().trim()
                productDescription = binding.descriptionEt.text.toString().trim()
                productIngredients = binding.ingredientsEt.text.toString().trim()
                productCategory = binding.listOfCategory.text.toString().trim()

                val namePattern = "^[A-Za-z\\s]{2,50}$"
                val pricePattern = "^\\d+(\\.\\d{1,2})?$"
                val descriptionPattern = "^[A-Za-z0-9\\s,.]{2,200}$"  // More flexible for descriptions
                val ingredientsPattern = "^[A-Za-z0-9\\s,.]{2,100}$" // More flexible for ingredients


                if ((productName.isBlank() || productPrice.isBlank() || productDescription.isBlank() || productIngredients.isBlank()) || productCategory.isBlank()) {
                    snackBarBottom(binding.root, "Fill all the details")
                } else if (!productName.matches(Regex(namePattern))) {
                    snackBarBottom(
                        binding.root, "Please enter a valid product name (2-50 alphabetic characters)"
                    )
                } else if (!productPrice.matches(Regex(pricePattern))) {
                    snackBarBottom(binding.root, "Please enter a valid product price.")
                } else if (!productDescription.matches(Regex(descriptionPattern))) {
                    snackBarBottom(
                        binding.root,
                        "Please enter a valid product description (2-200 characters, allows alphabets, numbers, commas, periods)"
                    )
                } else if (!productIngredients.matches(Regex(ingredientsPattern))) {
                    snackBarBottom(
                        binding.root,
                        "Please enter a valid product highlights (2-100 characters, allows alphabets, numbers, commas, periods)"
                    )
                } else if (!listOfCategory.contains(productCategory)) {  // Validate category selection
                    snackBarBottom(binding.root, "Please select a valid category.")
                } else {

                    checkInternetAndUploadData()


                }
            }

        }



        private fun showColorSelectionDialog(colorSpinner: TextView) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Colors")

            // Set up multi-choice items
            builder.setMultiChoiceItems(colorArray, selectedColors) { _, which, isChecked ->
                if (isChecked) {
                    // Add selected color to list
                    selectedColorList.add(colorArray[which])
                } else {
                    // Remove unselected color from list
                    selectedColorList.remove(colorArray[which])
                }
            }

            // Handle dialog positive button click
            builder.setPositiveButton("OK") { _, _ ->
                // Update spinner text with selected colors
                colorSpinner.text = selectedColorList.joinToString(", ")
            }

            // Handle dialog negative button click
            builder.setNegativeButton("Cancel", null)

            // Show the dialog
            builder.create().show()
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

        private fun checkInternetAndUploadData() {
            if (isInternetOn(this)) {
                showLoader(true)
                uploadData(selectedColors.toString())
            } else {
                dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText(
                    getString(R.string.title_internet)
                ).setContentText(getString(R.string.content_internet))
                    .setConfirmText("Retry")
                dialog.setCancelable(false)
                dialog.setConfirmClickListener { sweetAlertDialog: SweetAlertDialog ->
                    showLoader(false)// Explicit type
                    sweetAlertDialog.dismissWithAnimation()
                }
                dialog.show()

                confirmBtn =
                    dialog.findViewById<View>(cn.pedant.SweetAlert.R.id.confirm_button) as Button
                confirmBtn.setTextColor(Color.BLACK)
                confirmBtn.textSize = 14f


            }
        }

        private fun uploadData(selectedColors: String) {
            showLoader(true)
            val menuReference = database.getReference("menu")
            val newItemKey = menuReference.push().key
            if (productImageUri != null) {
                val storageReference = FirebaseStorage.getInstance().reference

                val imageReference = storageReference.child("menu_image/${newItemKey}.jpg")

                val uploadTask = imageReference.putFile(productImageUri!!)
                uploadTask.addOnSuccessListener {
                    imageReference.downloadUrl.addOnSuccessListener { downloadUrl ->

                        val newItem = AllMenu(
                            newItemKey,
                            productName = productName,
                            productPrice = productPrice,
                            productDescription = productDescription,
                            productIngredients = productIngredients,
                            productImage = downloadUrl.toString(),
                            category = productCategory,
                            colors = selectedColorList
                        )

                        newItemKey?.let { key ->

                            menuReference.child(key).setValue(newItem).addOnSuccessListener {
                                snackBarBottom(binding.root, "Item Added Successfully")
                                showLoader(false)
                            }.addOnFailureListener {
                                snackBarBottom(binding.root, "Item Added Failed")
                            }
                        }
                    }

                }.addOnFailureListener {

                    snackBarBottom(binding.root, "Image upload failed")

                }
            } else {
                snackBarBottom(binding.root, "Please select image")

            }
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {

                uri ->
            if (uri != null) {
                binding.addProductImg.setImageURI(uri)

                productImageUri = uri
            }

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