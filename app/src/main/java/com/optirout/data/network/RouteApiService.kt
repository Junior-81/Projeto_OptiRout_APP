package com.optirout.data.network

import com.optirout.data.model.RouteRequest
import com.optirout.data.model.RouteResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RouteApiService {
    @POST("api/calculate")
    suspend fun calculateRoute(@Body request: RouteRequest): RouteResponse
}
