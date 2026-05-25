package com.example.projectpbd.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path

data class ExchangeRateResponse(
    val result: String? = null,
    @SerializedName("base_code")
    val baseCode: String? = null,
    @SerializedName("rates")
    val rates: Map<String, Double>? = null
)

interface ExchangeRateService {
    @GET("v6/latest/{base}")
    suspend fun getLatestRates(
        @Path("base") base: String = "LKR"
    ): ExchangeRateResponse
}
