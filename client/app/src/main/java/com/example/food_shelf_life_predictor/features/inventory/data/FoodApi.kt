package com.example.food_shelf_life_predictor.features.inventory.data

import com.example.food_shelf_life_predictor.core.network.ApiConfig
import com.example.food_shelf_life_predictor.core.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.util.UUID

class ApiException(val status: Int, override val message: String) : Exception(message)

fun Throwable.toFriendlyMessage(): String {
    android.util.Log.e("FoodApi", "Request gagal", this)
    return when (this) {
        is ApiException -> message
        is HttpRequestTimeoutException -> "Server terlalu lama merespons. Coba lagi."
        is IOException -> "Tidak dapat terhubung ke server. Cek koneksi dan alamat server."
        else -> "Terjadi kesalahan. Coba lagi."
    }
}

class FoodApi {
    private val client = HttpClientProvider.client
    private val base = ApiConfig.BASE_URL

    suspend fun predictImage(file: File): PredictionDto {
        if (ApiConfig.USE_MOCK) {
            delay(2000)
            return PredictionDto(
                detectedFood = "Tomato",
                confidence = 0.92,        // coba 0.5 untuk tes sorotan dropdown
                freshnessScore = 0.85,    // coba 0.2 untuk tes pop-up dan tombol terkunci
                storageRecommendations = listOf(
                    StorageRecommendationDto("freezer", 30),
                    StorageRecommendationDto("chiller", 7),
                    StorageRecommendationDto("room", 3)
                )
            )
        }
        val response = client.submitFormWithBinaryData(
            url = "$base/api/v1/prediction/predict-image",
            formData = formData {
                append("image", file.readBytes(), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                })
            }
        )
        if (!response.status.isSuccess()) throw response.toApiException()
        return response.body()
    }

    suspend fun createFood(request: CreateFoodRequest): FoodDto {
        if (ApiConfig.USE_MOCK) {
            delay(600)
            val days = request.estimatedDaysLeft ?: 0
            return FoodDto(
                id = UUID.randomUUID().toString(),
                name = request.name,
                storageLocation = request.storageLocation,
                storageDate = request.storageDate,
                freshnessScore = request.freshnessScore,
                estimatedDaysLeft = days,
                expiryDate = LocalDate.parse(request.storageDate).plusDays(days.toLong()).toString(),
                daysLeft = days,
                status = if (days <= 0) "expired" else if (days <= 3) "expiring_soon" else "fresh"
            )
        }
        val response = client.post("$base/api/v1/foods") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (!response.status.isSuccess()) throw response.toApiException()
        return response.body()
    }

    private suspend fun HttpResponse.toApiException(): ApiException {
        val serverMessage = runCatching {
            Json.parseToJsonElement(bodyAsText()).jsonObject["error"]
                ?.jsonObject?.get("message")?.jsonPrimitive?.content
        }.getOrNull()
        val text = when (status.value) {
            413 -> "Ukuran foto terlalu besar (maksimal 5 MB)."
            415 -> "Format foto tidak didukung. Gunakan JPG atau PNG."
            502 -> "Server AI memberi respons tidak valid. Coba lagi."
            503 -> "Layanan sedang tidak tersedia. Coba beberapa saat lagi."
            504 -> "Server AI terlalu lama merespons. Coba lagi."
            else -> serverMessage ?: "Terjadi kesalahan di server (${status.value})."
        }
        return ApiException(status.value, text)
    }

    suspend fun getFoods(): List<FoodDto> {
        val response = client.get("$base/api/v1/foods") { parameter("limit", 100) }
        if (!response.status.isSuccess()) throw response.toApiException()
        return response.body<FoodListDto>().data
    }

    suspend fun deleteFood(id: String) {
        if (ApiConfig.USE_MOCK) { delay(300); return }
        val response = client.delete("$base/api/v1/foods/$id")
        if (!response.status.isSuccess()) throw response.toApiException()
    }
}

@Serializable
data class FoodListDto(val data: List<FoodDto>)