package com.example.food_shelf_life_predictor.features.inventory.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PredictionDto(
    @SerialName("detected_food") val detectedFood: String,
    val confidence: Double,
    @SerialName("freshness_score") val freshnessScore: Double,
    @SerialName("storage_recommendations") val storageRecommendations: List<StorageRecommendationDto>
)

@Serializable
data class StorageRecommendationDto(
    @SerialName("storage_location") val storageLocation: String,
    @SerialName("estimated_days_left") val estimatedDaysLeft: Int
)