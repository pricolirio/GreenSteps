/*
 * Este archivo forma parte del Trabajo Final de Grado para la Universitat Oberta de Catalunya (UOC),
 * realizado por Pedro Rico Lirio.
 *
 * Este proyecto utiliza los siguientes servicios de terceros:
 *
 * 1. Firebase Authentication: Proporciona autenticación de usuario.
 *    Términos de Servicio de Firebase: https://firebase.google.com/terms
 *
 * 2. Firebase Firestore: Proporciona una base de datos en la nube.
 *    Términos de Servicio de Firebase: https://firebase.google.com/terms
 *
 * 3. Google Maps Platform API: Proporciona servicios de mapas y localización.
 *    Términos de Servicio de Google Maps Platform: https://cloud.google.com/maps-platform/terms
 *
 * Esta aplicación está sujeta a la licencia Creative Commons Reconocimiento-NoComercial-SinObraDerivada 3.0 España.
 * Para ver una copia de esta licencia, visita https://creativecommons.org/licenses/by-nc-nd/3.0/es/
 */


package View

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.greensteps.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import Model.DataManagerActivities
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GuardarActividadActivity : AppCompatActivity() {

    private lateinit var tvNombreValor: TextView
    private lateinit var tvDistanciaValor: TextView
    private lateinit var tvDuracionValor: TextView
    private lateinit var tvCombustibleValor: TextView
    private lateinit var tvEmisionesValor: TextView
    private lateinit var btnGuardarActividad: Button
    private lateinit var btnDescartarActividad: Button

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.guardar_actividad)

        // Obtención de referencias a los elementos de la interfaz de usuario
        tvNombreValor = findViewById(R.id.tv_nombre_valor)
        tvDistanciaValor = findViewById(R.id.tv_distancia_valor)
        tvDuracionValor = findViewById(R.id.tv_duracion_valor)
        tvCombustibleValor = findViewById(R.id.tv_combustible_valor)
        tvEmisionesValor = findViewById(R.id.tv_emisiones_valor)
        btnGuardarActividad = findViewById(R.id.btn_guardar_actividad)
        btnDescartarActividad = findViewById(R.id.btn_descartar_actividad)

        // Obtener los datos del registro de la actividad
        val distanceTravelled = intent.getDoubleExtra("distanceTravelled", 0.0) // en metros
        val secondsElapsed = intent.getIntExtra("secondsElapsed", 0)

        // Calcular los valores a mostrar
        val nombre = "Actividad " + SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
            Date()
        )
        val distanciaKm = distanceTravelled / 1000 // Convertir a km para mostrar en la UI
        val duracion = String.format("%02d:%02d:%02d", secondsElapsed / 3600, (secondsElapsed % 3600) / 60, secondsElapsed % 60)

        tvNombreValor.text = nombre
        tvDistanciaValor.text = "%.2f Km".format(distanciaKm)
        tvDuracionValor.text = duracion

        val dataManager = DataManagerActivities(auth, db)

        var combustible = 0.0
        var emisiones = 0.0

        // Obtener el tipo de vehículo y calcular el combustible y las emisiones
        dataManager.obtenerVehiculo { vehiculo ->
            combustible = calcularCombustible(distanciaKm, vehiculo)
            emisiones = calcularEmisiones(combustible, vehiculo)

            runOnUiThread {
                val combustibleStr = String.format("%.6f L", combustible)
                val emisionesStr = String.format("%.6f Kg", emisiones)

                tvCombustibleValor.text = combustibleStr
                tvEmisionesValor.text = emisionesStr
            }
        }

        btnDescartarActividad.setOnClickListener {
            finish()
        }

        btnGuardarActividad.setOnClickListener {
            try {
                val nombreActual = tvNombreValor.text.toString()
                dataManager.guardarActividad(nombreActual, distanciaKm, secondsElapsed, combustible, emisiones)

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    dataManager.verificarMedallasDiasConsecutivos(userId)
                }

                finish()
            } catch (e: Exception) {
                Toast.makeText(this, "Ha habido un error al guardar los datos en la base de datos, inténtelo de nuevo", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun calcularCombustible(distancia: Double, vehiculo: String): Double {
        return when (vehiculo) {
            "Diésel pequeño" -> distancia * 0.06
            "Diésel mediano" -> distancia * 0.08
            "Diésel grande" -> distancia * 0.10
            "Gasolina pequeño" -> distancia * 0.07
            "Gasolina mediano" -> distancia * 0.09
            "Gasolina grande" -> distancia * 0.12
            else -> 0.0
        }
    }

    private fun calcularEmisiones(combustible: Double, vehiculo: String): Double {
        return when (vehiculo) {
            "Diésel pequeño", "Diésel mediano", "Diésel grande" -> combustible * 2.7
            "Gasolina pequeño", "Gasolina mediano", "Gasolina grande" -> combustible * 2.3
            else -> 0.0
        }
    }
}
