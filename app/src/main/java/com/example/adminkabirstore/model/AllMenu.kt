package com.example.adminkabirstore.model

data class AllMenu(
    val key : String? = null,
    val productName : String? = null,
    val productPrice : String? = null,
    val productImage : String? = null,
    val productDescription : String? = null,
    val productIngredients : String? = null,
    val category : String? = null,
    val colors: List<String>? = null
)
