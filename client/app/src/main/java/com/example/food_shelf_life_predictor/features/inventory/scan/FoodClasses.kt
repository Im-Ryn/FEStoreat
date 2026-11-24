package com.example.food_shelf_life_predictor.features.inventory.scan

const val CONFIDENCE_THRESHOLD = 0.60
const val FRESHNESS_THRESHOLD = 0.30

data class FoodClass(val key: String, val label: String)

// PLACEHOLDER: ganti dengan 10 kelas resmi dari tim ML
val FOOD_CLASSES = listOf(
    FoodClass("Tomato", "Tomat Segar"),
    FoodClass("Apple", "Apel"),
    FoodClass("Banana", "Pisang"),
    FoodClass("Orange", "Jeruk"),
    FoodClass("Carrot", "Wortel"),
    FoodClass("Potato", "Kentang"),
    FoodClass("Broccoli", "Brokoli"),
    FoodClass("Cabbage", "Kol"),
    FoodClass("Chicken", "Daging Ayam"),
    FoodClass("Egg", "Telur")
)

fun labelFor(key: String) = FOOD_CLASSES.firstOrNull { it.key == key }?.label ?: key