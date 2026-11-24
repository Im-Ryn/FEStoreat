package com.example.food_shelf_life_predictor.features.inventory.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateFoodRequest(
    val name: String,
    @SerialName("storage_location") val storageLocation: String,
    @SerialName("storage_date") val storageDate: String,
    @SerialName("freshness_score") val freshnessScore: Double? = null,
    @SerialName("estimated_days_left") val estimatedDaysLeft: Int? = null//ini kalau misal ga ngitung sendiri backend nya
)