package com.nidcard.app.data.entity

/**
 * Lightweight version of NIDCard for list views.
 * Excludes base64 image fields to avoid loading large strings into memory.
 */
data class NIDCardSummary(
    val id: Long = 0,
    val nameBn: String = "",
    val nameEn: String = "",
    val nid: String = "",
    val pin: String = "",
    val father: String = "",
    val mother: String = "",
    val birth: String = "",
    val dob: String = "",
    val blood: String = "",
    val address: String = "",
    val gender: String = "male",
    val issueDate: String = "",
    val createdAt: String = ""
)
