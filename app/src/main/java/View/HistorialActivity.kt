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

import Model.DataManagerStatistics
import View.Tools.ActividadAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.widget.ListView
import com.example.greensteps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class HistorialActivity : MenuActivity() {

    private lateinit var adapter: ActividadAdapter
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial)

        //Inicialización del menú inferior
        setupMenu(R.id.btn_menu,R.id.btn_progreso,R.id.btn_registro,R.id.btn_comunidad)

        val dataManager = DataManagerStatistics(auth, db)
        val lvActividades = findViewById<ListView>(R.id.lv_actividades)
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Obtención de las actividades del usuario desde Firestore
            dataManager.getActividades(userId) { actividades ->
                adapter = ActividadAdapter(this, actividades)
                lvActividades.adapter = adapter

                lvActividades.setOnItemClickListener { _, _, position, _ ->
                    val actividad = actividades[position]
                    val duracion = Date(actividad.duracion.toLong() * 1000)
                    val formatoDuracion = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val formatoFechaHora = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val fechaHoraFormateada = formatoFechaHora.format(actividad.fechaHora)

                    // Muestra un popup con los detalles de la actividad
                    AlertDialog.Builder(this)
                        .setTitle(actividad.nombre)
                        .setMessage("""
                            Fecha y hora: $fechaHoraFormateada
                            Distancia: ${String.format("%.2f", actividad.distancia)} Km
                            Duración: ${formatoDuracion.format(duracion)}
                            Emisiones de CO2 evitadas: ${String.format("%.3f", actividad.emisiones)} Kg
                            Combustible ahorrado: ${String.format("%.3f", actividad.combustible)} L
                        """.trimIndent())
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }
}
