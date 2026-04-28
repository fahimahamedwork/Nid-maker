package com.nidcard.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "nid_cards",
    indices = [Index(value = ["nid"], unique = true), Index(value = ["pin"])]
)
data class NIDCard(
    @PrimaryKey(autoGenerate = true)
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
    val createdAt: String = "",

    // Images stored as base64 strings
    val photoBase64: String = "",
    val photoType: String = "image/jpeg",
    val signBase64: String = "",
    val signType: String = "image/jpeg"
)
