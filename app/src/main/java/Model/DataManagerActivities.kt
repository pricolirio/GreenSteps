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

package Model

import android.content.ContentValues
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class DataManagerActivities (private val auth: FirebaseAuth, private val db: FirebaseFirestore) {

    //Función para guardar los datos de la actividad en Firestore,
    //usada en la actividad "Guardar Actividad"
    fun guardarActividad(nombre: String, distancia: Double, duracion: Int, combustible: Double, emisiones: Double) {

        // Crear un mapa con los datos de la actividad
        val actividad = hashMapOf(
            "nombre" to nombre,
            "distancia" to distancia,
            "duracion" to duracion,
            "combustible" to combustible,
            "emisiones" to emisiones,
            "fechaHora" to Timestamp.now()
        )

        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Añadir la actividad a la colección "actividades" del usuario en Firestore
            db.collection("usuarios").document(userId).collection("actividades")
                .add(actividad)
                .addOnSuccessListener { documentReference ->
                    Log.d(ContentValues.TAG, "Documento agregado con ID: ${documentReference.id}")

                    // Comprobar las condiciones para las medallas de distancia
                    val medallaBronce = distancia >= 1.0
                    val medallaPlata = distancia >= 5.0
                    val medallaOro = distancia >= 10.0

                    // Obtener el documento del usuario para leer los valores actuales de las medallas
                    val usuarioRef = db.collection("usuarios").document(userId)
                    usuarioRef.get().addOnSuccessListener { document ->
                        if (document != null) {
                            val medallaBronceActual = document.getLong("medallaBronce") ?: 0
                            val medallaPlataActual = document.getLong("medallaPlata") ?: 0
                            val medallaOroActual = document.getLong("medallaOro") ?: 0

                            // Actualizar las medallas en el documento del usuario
                            if (medallaBronce) usuarioRef.update("medallaBronce", medallaBronceActual + 1)
                            if (medallaPlata) usuarioRef.update("medallaPlata", medallaPlataActual + 1)
                            if (medallaOro) usuarioRef.update("medallaOro", medallaOroActual + 1)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error al agregar el documento", e)
                }
        }
    }

    //Función que maneja las medallas obtenidas por días consecutivos,
    // usada en la actividad "Guardar Actividad"
    fun verificarMedallasDiasConsecutivos(userId: String) {
        // Mapa para almacenar las medallas entregadas hoy
        val medallasEntregadasHoy = mutableMapOf<String, MutableSet<String>>()

        // Consulta las actividades del usuario ordenadas por fecha
        db.collection("usuarios").document(userId).collection("actividades")
            .orderBy("fechaHora")
            .get()
            .addOnSuccessListener { result ->
                var diasConsecutivos = 0
                var fechaAnterior: Date? = null

                for (document in result) {
                    val fechaActividad: Date = document.getDate("fechaHora") ?: Date(0)

                    // Normalizar a medianoche
                    val cal = Calendar.getInstance()
                    cal.time = fechaActividad
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val fechaActividadNormalizada = cal.time

                    if (fechaAnterior != null) {
                        val calAnterior = Calendar.getInstance()
                        calAnterior.time = fechaAnterior
                        calAnterior.add(Calendar.DAY_OF_YEAR, 1)

                        // Verifica si las fechas son consecutivas
                        if (calAnterior.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
                            calAnterior.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR)) {
                            diasConsecutivos++
                        } else {
                            diasConsecutivos = 1
                        }
                    } else {
                        diasConsecutivos = 1
                    }
                    fechaAnterior = fechaActividadNormalizada

                    val usuarioRef = db.collection("usuarios").document(userId)
                    usuarioRef.get().addOnSuccessListener { document ->
                        if (document != null) {
                            val medallaSemilla = document.getLong("medallaSemilla") ?: 0
                            val medallaPlanta = document.getLong("medallaPlanta") ?: 0
                            val medallaArbol = document.getLong("medallaArbol") ?: 0

                            // Verificar si ya se entregó una medalla de este tipo hoy
                            val tipoMedalla = when {
                                diasConsecutivos >= 7 -> "Arbol"
                                diasConsecutivos >= 5 -> "Planta"
                                diasConsecutivos >= 2 -> "Semilla"
                                else -> null
                            }

                            if (tipoMedalla != null) {
                                // Verificar si ya se entregó una medalla de este tipo hoy
                                val medallasHoy = medallasEntregadasHoy.getOrDefault(fechaActividadNormalizada.toString(), mutableSetOf())
                                if (!medallasHoy.contains(tipoMedalla)) {
                                    // Entregar la medalla y registrarla como entregada hoy
                                    usuarioRef.update("medalla${tipoMedalla}", when (tipoMedalla) {
                                        "Semilla" -> medallaSemilla + 1
                                        "Planta" -> medallaPlanta + 1
                                        "Arbol" -> medallaArbol + 1
                                        else -> medallaSemilla
                                    })

                                    medallasHoy.add(tipoMedalla)
                                    medallasEntregadasHoy[fechaActividadNormalizada.toString()] = medallasHoy
                                }
                            }
                        }
                    }
                }
            }
    }

    //Función que devuelve el tipo de vehículo del usuario,
    //usada en la actividad "Guardar Actividad"
    fun obtenerVehiculo(callback: (String) -> Unit){
        val userId = auth.currentUser?.uid
        if (userId != null){
            db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document->
                    if(document!=null){
                        val vehiculo = document.getString("vehiculo")
                        if(vehiculo!=null){
                            callback(vehiculo)
                        }else{
                            Log.d(ContentValues.TAG,"No hay campo vehiculo en la BBDD")
                        }
                    }else{
                        Log.d(ContentValues.TAG,"No hay documento en la BBDD")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(ContentValues.TAG, "Fallo: ", exception)
                }
        }
    }
}