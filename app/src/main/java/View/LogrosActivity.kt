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

import Model.DataManagerUser
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.greensteps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LogrosActivity : MenuActivity() {

    private lateinit var dataManagerUser: DataManagerUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logros)
        //Inicialización del menú inferior
        setupMenu(R.id.btn_menu,R.id.btn_progreso,R.id.btn_registro,R.id.btn_comunidad)

        dataManagerUser = DataManagerUser(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

        // Obtención de los logros del usuario y actualización de la interfaz de usuario con ellos
        dataManagerUser.obtenerLogros(
            onSuccess = { medallaBronce, medallaPlata, medallaOro, medallaSemilla, medallaPlanta, medallaArbol ->
                findViewById<TextView>(R.id.tv_logros_1km).text = medallaBronce.toString()
                findViewById<TextView>(R.id.tv_logros_5km).text = medallaPlata.toString()
                findViewById<TextView>(R.id.tv_logros_10km).text = medallaOro.toString()
                findViewById<TextView>(R.id.tv_logros_semilla).text = medallaSemilla.toString()
                findViewById<TextView>(R.id.tv_logros_planta).text = medallaPlanta.toString()
                findViewById<TextView>(R.id.tv_logros_arbol).text = medallaArbol.toString()
            },
            onFailure = { exception ->
                Log.d("LogrosActivity", "Error obteniendo logros: ", exception)
            }
        )

        // Configuración del botón de información
        val btnInfo = findViewById<Button>(R.id.btn_info)
        btnInfo.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Información de Logros")
            builder.setMessage("Puedes acumular logros con cada actividad (1 de cada por cada objetivo conseguido).\n\n" +
                    "Logros por distancia:\n\n" +
                    "Medalla Bronce: 1 Km recorrido\n" +
                    "Medalla Plata: 5 Km recorridos\n" +
                    "Medalla Oro: 10 Km recorridos\n\n" +
                    "Logros por días seguidos registrando actividades:\n\n" +
                    "Medalla semilla: 2 días seguidos\n" +
                    "Medalla planta: 5 días seguidos\n" +
                    "Medalla árbol: 7 días seguidos")
            builder.setNeutralButton("Cerrar") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }
}