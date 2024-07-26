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

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.greensteps.R

class ProgresoActivity: MenuActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.progreso)

        //Inicialización menú inferior
        setupMenu(R.id.btn_menu,R.id.btn_progreso,R.id.btn_registro,R.id.btn_comunidad)

        val btnEstadisticas = findViewById<Button>(R.id.btn_estadisticas)
        btnEstadisticas.setOnClickListener {
            val intent = Intent(this, EstadisticasActivity::class.java)
            startActivity(intent)
        }

        val btnLogros = findViewById<Button>(R.id.btn_logros)
        btnLogros.setOnClickListener {
            val intent = Intent(this, LogrosActivity::class.java)
            startActivity(intent)
        }

        val btnHistorial = findViewById<Button>(R.id.btn_historial)
        btnHistorial.setOnClickListener {
            val intent = Intent(this, HistorialActivity::class.java)
            startActivity(intent)
        }
    }
}
