package com.example.tfcanvilcalc

data class SavedResult(
    val id: Int = 0,
    val name: String,
    val targetNumber: Int,
    val actions: List<Action>,
    val solution: List<Action>,
    val folderId: Int? = null,
    // Calculator fields - nullable for backward compatibility
    val calcTotalUnits: Int? = null,
    val calcMaxPerItem: Int? = null,
    val calcAutoPickEnabled: Boolean? = null,
    val calcComponentsJson: String? = null
) 