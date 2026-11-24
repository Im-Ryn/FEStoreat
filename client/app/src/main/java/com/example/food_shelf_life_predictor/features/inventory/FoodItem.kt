package com.example.food_shelf_life_predictor.features.inventory

data class FoodItem(
    val id: String,                 // UUID dari backend
    val name: String,
    val storageLocation: StorageLocation,
    val storageDate: String,        // "YYYY-MM-DD"
    val freshnessScore: Double?,    // 0..1 atau null
    val estimatedDaysLeft: Int,
    val expiryDate: String,         // "YYYY-MM-DD"
    val daysLeft: Int,
    val status: FoodStatus,
    val photoPath: String? = null   // path foto lokal (tidak dari API)
)

enum class StorageLocation(val apiValue: String, val label: String) {
    FREEZER("freezer", "Freezer"),
    CHILLER("chiller", "Chiller"),
    ROOM("room", "Pantry");

    companion object {
        fun fromApi(value: String): StorageLocation =
            entries.firstOrNull { it.apiValue == value } ?: ROOM
    }
}

enum class FoodStatus(val apiValue: String) {
    FRESH("fresh"),
    EXPIRING_SOON("expiring_soon"),
    EXPIRED("expired");

    companion object {
        fun fromApi(value: String): FoodStatus =
            entries.firstOrNull { it.apiValue == value } ?: FRESH
    }
}