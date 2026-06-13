package com.optirout.data.repository

import com.optirout.data.model.RouteRequest
import com.optirout.data.model.RouteResponse
import com.optirout.data.network.ApiClient

class RouteRepository {

    private val apiService = ApiClient.routeApiService

    suspend fun calculateRoute(initialMode: String): RouteResponse {
        val request = RouteRequest(
            origin = listOf(-8.0623949, -34.8737916),
            destination = listOf(-8.1179317, -34.8999959),
            initialMode = initialMode,
        )
        return apiService.calculateRoute(request)
    }
}
