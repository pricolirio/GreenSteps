package View.Tools

import java.util.Date

data class Actividad(
    val combustible: Double,
    val distancia: Double,
    val duracion: Double,
    val emisiones: Double,
    val fechaHora: Date,
    val nombre: String
)

