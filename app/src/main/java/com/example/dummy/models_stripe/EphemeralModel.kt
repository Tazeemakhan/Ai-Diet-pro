package com.example.dummy.models_stripe

import com.google.gson.annotations.SerializedName

data class EphemeralModel(
    val id: String,

    @SerializedName("object")
    val objectType: String, // ✅ Best solution

    @SerializedName("associated_objects")
    val associatedObjects: List<AssociatedObject>,

    val created: Long,
    val expires: Long,
    val livemode: Boolean,
    val secret: String
)

data class AssociatedObject(
    val id: String,
    val type: String
)
