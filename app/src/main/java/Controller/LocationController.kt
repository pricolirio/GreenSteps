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



package Controller

import View.RegistroActividadActivity
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng

class LocationController(private val activity: Activity) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)

    // Callback para manejar los resultados de la ubicación
    private val locationCallback = object : LocationCallback() {
        // Última ubicación conocida
        private var lastLocation: Location? = null

        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations){
                // Verificar si la actividad está grabando y actualizar la UI y el mapa
                if (lastLocation != null && activity is RegistroActividadActivity && activity.isRecording) {
                    val distance = lastLocation!!.distanceTo(location).toDouble()
                    activity.distanceTravelled += distance
                    activity.updateUI(activity.distanceTravelled)
                    activity.updateMap(location)

                }
                lastLocation = location
            }
        }
    }

    // Obtener la última ubicación conocida
    fun getLastLocation(callback: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            callback(location)
        }
    }

    // Comenzar a recibir actualizaciones de ubicación
    fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        fusedLocationClient.requestLocationUpdates(LocationRequest.create().apply {
            interval = 1000  // Intervalo de actualización de ubicación en milisegundos
            fastestInterval = 500 // Intervalo de actualización más rápido en milisegundos
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY // Prioridad alta para la precisión de la ubicación
        }, locationCallback, Looper.getMainLooper())  // Asociar el callback con el hilo principal
    }

    // Detener la recepción de actualizaciones de ubicación
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


}

