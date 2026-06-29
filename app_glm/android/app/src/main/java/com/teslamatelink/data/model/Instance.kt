package com.teslamatelink.data.model

data class Instance(
    val id: String,
    val name: String,
    val serverUrl: String,
    val apiToken: String,
    val carId: Int = 1
)
