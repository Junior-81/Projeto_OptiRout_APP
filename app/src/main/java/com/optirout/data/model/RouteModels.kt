package com.optirout.data.model

import com.google.gson.annotations.SerializedName

enum class TransportMode(
    val apiValue: String,
    val displayName: String,
    val description: String,
) {
    AUTO(
        apiValue = "auto",
        displayName = "Automático",
        description = "Combinação inteligente de modais para o trajeto mais eficiente entre a origem e o destino.",
    ),
    UBER(
        apiValue = "uber_car",
        displayName = "Uber",
        description = "Solicite um carro via Uber. Ideal para conforto e praticidade sem preocupação com estacionamento.",
    ),
    UBER_MOTO(
        apiValue = "uber_moto",
        displayName = "Uber Moto",
        description = "Solicite uma moto via Uber. Ideal para valocidade e praticidade sem preocupação com trânsito.",
    ),
    WALK(
        apiValue = "walk",
        displayName = "A pé",
        description = "Percurso caminhando. Indicado para curtas distâncias, saudável e sem custo.",
    ),
    MOTO(
        apiValue = "moto",
        displayName = "Moto",
        description = "Utilize sua motocicleta particular. Ágil no trânsito e econômica para médias distâncias.",
    ),
    CAR(
        apiValue = "car",
        displayName = "Carro",
        description = "Use seu veículo particular. Confortável e flexível para qualquer distância.",
    ),
    BUS(
        apiValue = "bus",
        displayName = "Ônibus",
        description = "Transporte público coletivo. Econômico e sustentável para rotas urbanas.",
    ),
}

data class RouteRequest(
    val origin: List<Double>,
    val destination: List<Double>,
    @SerializedName("initialMode")
    val initialMode: String,
)

data class RouteResponse(
    val edges: List<Edge>,
    val segments: List<Segment>,
    val summary: Summary,
)

data class Edge(
    val mode: String,
    val means: String,
    val origin: List<Double>,
    val destination: List<Double>,
    val distance: Double,
    val time: Double,
    val cost: Double,
    val effort: Double,
    val risk: Double,
    val weight: Double,
    val averageSpeedKmh: Double,
    val geometry: List<List<Double>>,
    val line: String?,
    val gtfsShapeId: String?,
    val gtfsValidation: Boolean?,
)

data class Segment(
    val mode: String,
    val means: String,
    val origin: List<Double>,
    val destination: List<Double>,
    val time: Double,
    val distance: Double,
    val cost: Double,
    val effort: Double,
    val averageRisk: Double,
    val averageSpeedKmh: Double,
    val edgeCount: Int,
    val line: String?,
    val gtfsShapeId: String?,
    val gtfsValidation: Boolean?,
)

data class Summary(
    val totalTime: Double,
    val totalCost: Double,
    val totalEffort: Double,
    val totalDistance: Double,
    val averageRisk: Double,
    val totalAverageSpeed: Double,
)
