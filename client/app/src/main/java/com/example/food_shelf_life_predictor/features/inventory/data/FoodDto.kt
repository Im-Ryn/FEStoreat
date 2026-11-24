package com.example.food_shelf_life_predictor.features.inventory.data

import com.example.food_shelf_life_predictor.features.inventory.FoodItem
import com.example.food_shelf_life_predictor.features.inventory.FoodStatus
import com.example.food_shelf_life_predictor.features.inventory.StorageLocation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodDto(
    val id: String,
    val name: String,
    @SerialName("storage_location") val storageLocation: String,
    @SerialName("storage_date") val storageDate: String,
    @SerialName("freshness_score") val freshnessScore: Double? = null,
    @SerialName("estimated_days_left") val estimatedDaysLeft: Int,
    @SerialName("expiry_date") val expiryDate: String,
    @SerialName("days_left") val daysLeft: Int,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun toFoodItem() = FoodItem(
        id = id,
        name = name,
        storageLocation = StorageLocation.fromApi(storageLocation),
        storageDate = storageDate,
        freshnessScore = freshnessScore,
        estimatedDaysLeft = estimatedDaysLeft,
        expiryDate = expiryDate,
        daysLeft = daysLeft,
        status = FoodStatus.fromApi(status)
    )
}