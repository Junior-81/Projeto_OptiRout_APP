package com.optirout.data.model

data class TransportModal(
    val id: Int,
    val name: String,
    val emoji: String
) {
    companion object {
        fun getDefaultModals() = listOf(
            TransportModal(1, "Bicicleta"),
            TransportModal(2, "Carro"),
            TransportModal(3, "Moto"),
            TransportModal(4, "Ônibus"),
            TransportModal(5, "Uber carro")
        )
    }
}