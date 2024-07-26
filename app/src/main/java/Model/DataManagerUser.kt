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

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore


class DataManagerUser(private val auth: FirebaseAuth, private val db: FirebaseFirestore) {

    //Función para modificar los datos del usuario en la actividad "Mi Perfil"
    fun guardarCambiosUsuario(nombre: String, fechaNacimiento: String, vehiculo: String, sharedPref: SharedPreferences) {
        val user = auth.currentUser
        val usuario = hashMapOf<String, Any>()
        if (nombre.isNotEmpty()) {
            usuario["nombre"] = nombre

            // Actualiza el nombre del usuario en las Preferencias Compartidas
            with (sharedPref.edit()) {
                putString("nombreUsuario", nombre)
                apply()
            }
        }
        if (fechaNacimiento.isNotEmpty()) {
            usuario["fechaNacimiento"] = fechaNacimiento
        }
        if (vehiculo != "vehiculo") {
            usuario["vehiculo"] = vehiculo
        }
        db.collection("usuarios").document(user?.uid!!)
            .update(usuario)
    }


    //Función para registrarse en la app y añadir los datos tanto a FirebaseAuth como a Firestore,
    // usada en la actividad "Registro Usuario"
    fun registrarUsuario(email: String, password: String, nombre: String, fechaNacimiento: String, vehiculo: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val usuario = hashMapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "fechaNacimiento" to fechaNacimiento,
                    "vehiculo" to vehiculo,
                    "medallaBronce" to 0,
                    "medallaPlata" to 0,
                    "medallaOro" to 0,
                    "medallaSemilla" to 0,
                    "medallaPlanta" to 0,
                    "medallaArbol" to 0
                )
                db.collection("usuarios").document(user?.uid!!)
                    .set(usuario)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e -> onFailure(e.message ?: "Error desconocido") }
            } else {
                if (task.exception is FirebaseAuthUserCollisionException) {
                    onFailure("Este correo electrónico ya está en uso")
                } else if (task.exception is FirebaseAuthWeakPasswordException) {
                    onFailure("La contraseña tiene que tener al menos 6 carácteres")
                } else {
                    onFailure(task.exception?.message ?: "Error desconocido")
                }
            }
        }
    }

    //Función para hacer Login en la app,
    //usada en la actividad "Login"
    fun iniciarSesion(email: String, password: String, onSuccess: (nombre: String, fechaN: String, vehiculo: String) -> Unit, onFailure: (Exception) -> Unit) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                // Obtener los datos del usuario desde Firestore una vez iniciada la sesión
                db.collection("usuarios").document(user?.uid!!)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            val nombre = document.getString("nombre") ?: ""
                            val fechaN = document.getString("fechaNacimiento") ?: ""
                            val vehiculo = document.getString("vehiculo") ?: ""
                            onSuccess(nombre, fechaN, vehiculo)
                        } else {
                            onFailure(Exception("Documento no encontrado"))
                        }
                    }
                    .addOnFailureListener(onFailure)
            } else {
                onFailure(task.exception ?: Exception("Error al iniciar sesión"))
            }
        }
    }


    //Función que devuelve los logros conseguidos por el usuario,
    //usada en la actividad "Logros"
    fun obtenerLogros(onSuccess: (medallaBronce: Long, medallaPlata: Long, medallaOro: Long, medallaSemilla: Long, medallaPlanta: Long, medallaArbol: Long) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val medallaBronce = document.getLong("medallaBronce") ?: 0
                        val medallaPlata = document.getLong("medallaPlata") ?: 0
                        val medallaOro = document.getLong("medallaOro") ?: 0
                        val medallaSemilla = document.getLong("medallaSemilla") ?: 0
                        val medallaPlanta = document.getLong("medallaPlanta") ?: 0
                        val medallaArbol = document.getLong("medallaArbol") ?: 0
                        onSuccess(medallaBronce, medallaPlata, medallaOro, medallaSemilla, medallaPlanta, medallaArbol)
                    } else {
                        onFailure(Exception("Documento no encontrado"))
                    }
                }
                .addOnFailureListener(onFailure)
        }
    }

}
