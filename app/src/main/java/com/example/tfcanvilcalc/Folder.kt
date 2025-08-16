package com.example.tfcanvilcalc

data class Folder(
    val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
) 