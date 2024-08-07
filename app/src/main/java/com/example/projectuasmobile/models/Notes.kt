package com.example.projectuasmobile.models

import com.google.firebase.Timestamp

data class Notes(
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val colorIndex: Int = 0,
    val address: String = "",
    val documentedId: String = ""
)
