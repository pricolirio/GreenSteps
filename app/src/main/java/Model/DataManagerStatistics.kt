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

import View.Tools.Actividad
import View.Tools.Estadisticas
import android.content.ContentValues
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DataManagerStatistics (private val auth: FirebaseAuth, private val db: FirebaseFirestore) {

    //Función que devuelve una lista con los datos de cada una de las actividades del usuario,
    //usada en la actividad "Historial"
    fun getActividades(userId: String, callback: (List<Actividad>) -> Unit) {
        db.collection("usuarios").document(userId).collection("actividades")
            .orderBy("fechaHora", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val actividades = result.map { document ->
                    Actividad(
                        combustible = document.getDouble("combustible") ?: 0.0,
                        distancia = document.getDouble("distancia") ?: 0.0,
                        duracion = document.getDouble("duracion") ?: 0.0,
                        emisiones = document.getDouble("emisiones") ?: 0.0,
                        fechaHora = document.getDate("fechaHora") ?: Date(),
                        nombre = document.getString("nombre") ?: ""
                    )
                }
                callback(actividades)
            }
            .addOnFailureListener { exception ->
                Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            }
    }


    //Función para calcular las estadisticas del usuario
    fun getEstadisticasUsuario(userId: String, rangoTiempo: String, callback: (Estadisticas?) -> Unit) {
        val calendar = Calendar.getInstance()

        // Configura el rango de tiempo basado en la opción seleccionada (semanal, mensual, total)
        when (rangoTiempo) {
            "Semanal" -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }
            "Mensual" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
            }
            // No se realiza ninguna modificación para el rango de tiempo "Total"
        }

        // Realiza la consulta para recuperar las actividades dentro del rango de tiempo
        val actividadesRef = db.collection("usuarios").document(userId).collection("actividades")
        val actividadesQuery = if (rangoTiempo == "Total") {
            actividadesRef
        } else {
            actividadesRef.whereGreaterThan("fechaHora", calendar.time)
        }

        actividadesQuery.get()
            .addOnSuccessListener { result ->
                // Inicializa variables para calcular las estadísticas
                var totalActividades = 0
                var totalDistancia = 0.0
                var totalTiempo = 0L
                var totalCombustible = 0.0
                var totalEmisiones = 0.0

                // Calcula las estadísticas sumando los valores de todas las actividades
                for (document in result) {
                    val timestamp = document.getTimestamp("fechaHora")
                    val fechaActividad = timestamp?.toDate()
                    if (fechaActividad != null && (rangoTiempo == "Total" || fechaActividad.after(calendar.time))) {
                        totalActividades++
                        totalDistancia += document.getDouble("distancia") ?: 0.0
                        totalTiempo += document.getLong("duracion") ?: 0L
                        totalCombustible += document.getDouble("combustible") ?: 0.0
                        totalEmisiones += document.getDouble("emisiones") ?: 0.0
                    }
                }

                // Crea un objeto Estadisticas con los valores calculados
                val estadisticas = Estadisticas(
                    totalActividades,
                    totalDistancia,
                    totalTiempo,
                    totalCombustible,
                    totalEmisiones
                )

                // Llama al callback con el objeto Estadisticas
                callback(estadisticas)
            }
            .addOnFailureListener { exception ->
                Log.e("DataManager", "Error al recuperar estadísticas: $exception")
                // Llama al callback con null si hay un error
                callback(null)
            }
    }

    //Función para obtener la distancia mayor recorrida por el usuario
    fun obtenerActividadConMayorDistancia(callback: (Result<Double>) -> Unit) {
        db.collection("usuarios")
            .document(auth.currentUser?.uid ?: "")
            .collection("actividades")
            .orderBy("distancia", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val distancia = result.documents.firstOrNull()?.getDouble("distancia")
                callback(Result.success(distancia ?: 0.0))
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }


    //Función para obtener la posicion del usuario en el Ranking de Distancia
    fun obtenerPosicionDistancia(callback: (Result<Int>) -> Unit) {
        val usuarioId = auth.currentUser?.uid ?: return callback(Result.failure(Exception("Usuario no autenticado")))

        // Obtener la distancia máxima registrada por el usuario
        obtenerActividadConMayorDistancia { result ->
            if (result.isSuccess) {
                val distanciaUsuarioActual = result.getOrNull()

                // Obtener todos los usuarios y sus distancias
                db.collection("usuarios")
                    .get()
                    .addOnSuccessListener { usuariosResult ->
                        val usuarios = usuariosResult.documents
                        val distanciasUsuarios = mutableListOf<UsuarioDistancia>()

                        if (usuarios.isEmpty()) {
                            callback(Result.success(0))
                            return@addOnSuccessListener
                        }

                        var processedCount = 0

                        // Iterar sobre todos los usuarios para obtener sus distancias
                        usuarios.forEach { usuario ->
                            val nombre = usuario.getString("nombre") ?: "Desconocido"
                            usuario.reference.collection("actividades")
                                .orderBy("distancia", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { actividadResult ->
                                    val distancia = actividadResult.documents.firstOrNull()?.getDouble("distancia") ?: 0.0
                                    distanciasUsuarios.add(UsuarioDistancia(nombre, distancia))

                                    processedCount++
                                    // Cuando se hayan procesado todas las distancias, ordenarlas y encontrar la posición del usuario actual
                                    if (processedCount == usuarios.size) {
                                        val distanciasOrdenadas = distanciasUsuarios.sortedByDescending { it.distancia }
                                        val posicion = distanciasOrdenadas.indexOfFirst { it.distancia == distanciaUsuarioActual } + 1
                                        callback(Result.success(posicion))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    callback(Result.failure(e))
                                    return@addOnFailureListener
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        callback(Result.failure(e))
                    }
            } else {
                callback(Result.failure(result.exceptionOrNull()!!))
            }
        }
    }

    // Clase de datos para representar el nombre del usuario y su distancia
    data class UsuarioDistancia(val nombre: String, val distancia: Double)

    //Función para obtener el Top 5 usuarios en el ranking Distancia
    fun obtenerTop5Distancia(callback: (Result<List<UsuarioDistancia>>) -> Unit) {
        val top5 = mutableListOf<UsuarioDistancia>()
        val db = FirebaseFirestore.getInstance()
        // Obtener todos los usuarios y sus distancias
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val usuarios = result.documents
                if (usuarios.isEmpty()) {
                    callback(Result.success(emptyList()))
                    return@addOnSuccessListener
                }

                var processedCount = 0

                // Iterar sobre todos los usuarios para obtener sus distancias
                usuarios.forEach { usuario ->
                    val nombre = usuario.getString("nombre") ?: "Desconocido"
                    usuario.reference.collection("actividades")
                        .orderBy("distancia", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { activityResult ->
                            val distancia = activityResult.documents.firstOrNull()?.getDouble("distancia")
                            if (distancia != null) {
                                top5.add(UsuarioDistancia(nombre, distancia))
                            }

                            processedCount++
                            // Cuando se hayan procesado todas las distancias, ordenarlas y obtener el Top 5
                            if (processedCount == usuarios.size) {
                                val finalTop5 = top5.sortedByDescending { it.distancia }.take(5)
                                callback(Result.success(finalTop5))
                            }
                        }
                        .addOnFailureListener { e ->
                            callback(Result.failure(e))
                            return@addOnFailureListener
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }




    //Funcion para obtener el total de usuarios registrados
    fun obtenerTotalUsuarios(callback: (Result<Int>) -> Unit) {
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val totalUsuarios = result.size()
                callback(Result.success(totalUsuarios))
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }

    //Funcion para obtener la suma total de emisiones del usuario
    fun obtenerEmisionesTotalesUsuario(callback: (Result<Double>) -> Unit) {
        db.collection("usuarios")
            .document(auth.currentUser?.uid ?: "")
            .collection("actividades")
            .get()
            .addOnSuccessListener { result ->
                val totalEmisiones = result.documents.sumOf { it.getDouble("emisiones") ?: 0.0 }
                callback(Result.success(totalEmisiones))
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }
    // Función para obtener la posición del usuario en el ranking de emisiones
    fun obtenerPosicionEmisiones(callback: (Result<Int>) -> Unit) {
        val usuarioId = auth.currentUser?.uid ?: return callback(Result.failure(Exception("Usuario no autenticado")))

        // Obtener las emisiones totales del usuario
        obtenerEmisionesTotalesUsuario { result ->
            if (result.isSuccess) {
                val emisionesUsuarioActual = result.getOrNull()

                // Obtener todos los usuarios y sus emisiones
                db.collection("usuarios")
                    .get()
                    .addOnSuccessListener { usuariosResult ->
                        val usuarios = usuariosResult.documents
                        val emisionesUsuarios = mutableListOf<UsuarioDistancia>()

                        if (usuarios.isEmpty()) {
                            callback(Result.success(0))
                            return@addOnSuccessListener
                        }

                        var processedCount = 0

                        // Iterar sobre todos los usuarios para obtener sus emisiones
                        usuarios.forEach { usuario ->
                            val nombre = usuario.getString("nombre") ?: "Desconocido"
                            usuario.reference.collection("actividades")
                                .get()
                                .addOnSuccessListener { actividadesResult ->
                                    val totalEmisiones = actividadesResult.documents.sumOf { it.getDouble("emisiones") ?: 0.0 }
                                    emisionesUsuarios.add(UsuarioDistancia(nombre, totalEmisiones))

                                    processedCount++
                                    // Cuando se hayan procesado todas las emisiones, ordenarlas y encontrar la posición del usuario actual
                                    if (processedCount == usuarios.size) {
                                        val actividadesOrdenadas = emisionesUsuarios.sortedByDescending { it.distancia }
                                        val posicion = actividadesOrdenadas.indexOfFirst { it.distancia == emisionesUsuarioActual } + 1
                                        callback(Result.success(posicion))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    callback(Result.failure(e))
                                    return@addOnFailureListener
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        callback(Result.failure(e))
                    }
            } else {
                callback(Result.failure(result.exceptionOrNull()!!))
            }
        }
    }


    // Función para obtener el Top 5 usuarios en el ranking de emisiones
    fun obtenerTop5Emisiones(callback: (Result<List<UsuarioDistancia>>) -> Unit) {
        val top5 = mutableListOf<UsuarioDistancia>()
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val usuarios = result.documents
                if (usuarios.isEmpty()) {
                    callback(Result.success(emptyList()))
                    return@addOnSuccessListener
                }

                var processedCount = 0

                // Iterar sobre todos los usuarios para obtener sus emisiones
                usuarios.forEach { usuario ->
                    val nombre = usuario.getString("nombre") ?: "Desconocido"
                    usuario.reference.collection("actividades")
                        .orderBy("emisiones", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { activityResult ->
                            val emisiones = activityResult.documents.firstOrNull()?.getDouble("emisiones")
                            if (emisiones != null) {
                                top5.add(UsuarioDistancia(nombre, emisiones))
                            }

                            processedCount++
                            // Cuando se hayan procesado todas las emisiones, ordenarlas y obtener el Top 5
                            if (processedCount == usuarios.size) {
                                val finalTop5 = top5.sortedByDescending { it.distancia }.take(5)
                                callback(Result.success(finalTop5))
                            }
                        }
                        .addOnFailureListener { e ->
                            callback(Result.failure(e))
                            return@addOnFailureListener
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }

    // Función para calcular la edad a partir de la fecha de nacimiento
    fun calcularEdad(fechaNacimiento: String): Int {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaNacimientoDate = formato.parse(fechaNacimiento)
        val fechaActual = Date()
        val diferenciaEnMilisegundos = fechaActual.time - fechaNacimientoDate.time
        val edadEnMilisegundos = diferenciaEnMilisegundos / (1000 * 60 * 60 * 24 * 365.25)
        return edadEnMilisegundos.toInt()
    }


    // Función para obtener el rango de edad
    fun obtenerRangoEdad(edad: Int): String {
        return when {
            edad < 25 -> "menor de 25"
            edad in 25..35 -> "de 25 a 35"
            edad in 36..45 -> "de 36 a 45"
            else -> "mayor de 45"
        }
    }

    fun obtenerRangoEdad(callback: (Result<String>) -> Unit) {
        val usuarioId = auth.currentUser?.uid ?: return callback(Result.failure(Exception("Usuario no autenticado")))

        db.collection("usuarios")
            .document(usuarioId)
            .get()
            .addOnSuccessListener { document ->
                val fechaNacimiento = document.getString("fechaNacimiento")
                if (fechaNacimiento != null) {
                    val edad = calcularEdad(fechaNacimiento)
                    val rangoEdad = obtenerRangoEdad(edad)
                    callback(Result.success(rangoEdad))
                } else {
                    callback(Result.failure(Exception("La fecha de nacimiento del usuario no está disponible")))
                }
            }
            .addOnFailureListener { e ->
                callback(Result.failure(e))
            }
    }


    fun obtenerPosicionEmisionesEdad(callback: (Result<Pair<Int, Int>>) -> Unit) {
        val usuarioId = auth.currentUser?.uid ?: return callback(Result.failure(Exception("Usuario no autenticado")))

        obtenerRangoEdad { rangoResult ->
            if (rangoResult.isSuccess) {
                // Obtiene el rango de edad del usuario actual
                val rangoEdad = rangoResult.getOrNull() ?: return@obtenerRangoEdad callback(Result.failure(Exception("Rango de edad no disponible")))
                obtenerEmisionesTotalesUsuario { emisionesResult ->
                    if (emisionesResult.isSuccess) {
                        // Obtiene las emisiones totales del usuario actual
                        val emisionesUsuarioActual = emisionesResult.getOrNull() ?: return@obtenerEmisionesTotalesUsuario callback(Result.failure(Exception("Emisiones no disponibles")))
                        db.collection("usuarios")
                            .get()
                            .addOnSuccessListener { usuariosResult ->
                                // Obtiene todos los usuarios de la base de datos
                                val usuarios = usuariosResult.documents.filter { doc ->
                                    val fechaNacimiento = doc.getString("fechaNacimiento")
                                    if (fechaNacimiento != null) {
                                        val edad = calcularEdad(fechaNacimiento)
                                        obtenerRangoEdad(edad) == rangoEdad
                                    } else {
                                        false
                                    }
                                }
                                val emisionesUsuarios = mutableListOf<UsuarioDistancia>()

                                if (usuarios.isEmpty()) {
                                    callback(Result.success(0 to 0))
                                    return@addOnSuccessListener
                                }

                                var processedCount = 0

                                usuarios.forEach { usuario ->
                                    val nombre = usuario.getString("nombre") ?: "Desconocido"
                                    // Obtiene las actividades de cada usuario
                                    usuario.reference.collection("actividades")
                                        .get()
                                        .addOnSuccessListener { actividadesResult ->
                                            // Calcula las emisiones totales de cada usuario
                                            val totalEmisiones = actividadesResult.documents.sumOf { it.getDouble("emisiones") ?: 0.0 }
                                            emisionesUsuarios.add(UsuarioDistancia(nombre, totalEmisiones))

                                            processedCount++
                                            if (processedCount == usuarios.size) {
                                                // Ordena los usuarios por emisiones y encuentra la posición del usuario actual
                                                val emisionesOrdenadas = emisionesUsuarios.sortedByDescending { it.distancia }
                                                val posicion = emisionesOrdenadas.indexOfFirst { it.distancia == emisionesUsuarioActual } + 1
                                                callback(Result.success(posicion to usuarios.size))
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            callback(Result.failure(e))
                                            return@addOnFailureListener
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                callback(Result.failure(e))
                            }
                    } else {
                        callback(Result.failure(emisionesResult.exceptionOrNull()!!))
                    }
                }
            } else {
                callback(Result.failure(rangoResult.exceptionOrNull()!!))
            }
        }
    }




    fun obtenerTop5EmisionesEdad(callback: (Result<List<UsuarioDistancia>>) -> Unit) {
        val usuarioId = auth.currentUser?.uid ?: return callback(Result.failure(Exception("Usuario no autenticado")))

        obtenerRangoEdad { rangoResult ->
            if (rangoResult.isSuccess) {
                // Obtiene el rango de edad del usuario
                val rangoEdad = rangoResult.getOrNull() ?: return@obtenerRangoEdad callback(Result.failure(Exception("Rango de edad no disponible")))
                db.collection("usuarios")
                    .get()
                    .addOnSuccessListener { usuariosResult ->
                        // Obtiene todos los usuarios de la base de datos que están dentro del mismo rango de edad que el usuario actual
                        val usuarios = usuariosResult.documents.filter { doc ->
                            val fechaNacimiento = doc.getString("fechaNacimiento")
                            if (fechaNacimiento != null) {
                                val edad = calcularEdad(fechaNacimiento)
                                obtenerRangoEdad(edad) == rangoEdad
                            } else {
                                false
                            }
                        }
                        val emisionesUsuarios = mutableListOf<UsuarioDistancia>()

                        if (usuarios.isEmpty()) {
                            callback(Result.success(emptyList()))
                            return@addOnSuccessListener
                        }

                        var processedCount = 0

                        usuarios.forEach { usuario ->
                            val nombre = usuario.getString("nombre") ?: "Desconocido"
                            // Obtiene las actividades de cada usuario
                            usuario.reference.collection("actividades")
                                .get()
                                .addOnSuccessListener { actividadesResult ->
                                    // Calcula las emisiones totales de cada usuario y las agrega a la lista de emisiones
                                    val totalEmisiones = actividadesResult.documents.sumOf { it.getDouble("emisiones") ?: 0.0 }
                                    emisionesUsuarios.add(UsuarioDistancia(nombre, totalEmisiones))

                                    processedCount++
                                    if (processedCount == usuarios.size) {
                                        // Ordena la lista de emisiones por distancia y toma los primeros 5 elementos como el Top 5
                                        val finalTop5 = emisionesUsuarios.sortedByDescending { it.distancia }.take(5)
                                        callback(Result.success(finalTop5))
                                    }
                                }
                                .addOnFailureListener { e ->
                                    callback(Result.failure(e))
                                    return@addOnFailureListener
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        callback(Result.failure(e))
                    }
            } else {
                callback(Result.failure(rangoResult.exceptionOrNull()!!))
            }
        }
    }
}