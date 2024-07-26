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

import Controller.LocationController
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.greensteps.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.SupportMapFragment
import android.view.View
import android.widget.ViewFlipper
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.PolylineOptions

class RegistroActividadActivity : MenuActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var locationController: LocationController
    private lateinit var mapView: MapView
    private lateinit var tvRegistrarActividad: TextView
    private lateinit var btnIniciar: Button
    private lateinit var btnPausar: Button
    private lateinit var btnTerminar: Button
    private lateinit var btnDatos: Button
    private lateinit var btnReanudar: Button
    private lateinit var tvDistancia: TextView
    private lateinit var tvVelocidadMedia: TextView

    // Variables para el registro de actividad
    var isRecording = false
    private var secondsElapsed = 0
    var distanceTravelled = 0.0
    private lateinit var timerHandler: Handler
    private lateinit var timerRunnable: Runnable
    private val polylinePoints = ArrayList<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registroactividad)
        setupMenu(R.id.btn_menu,R.id.btn_progreso,R.id.btn_registro,R.id.btn_comunidad)

        locationController = LocationController(this)

        Log.d("Debug", "onCreate called")

        // Referencias a las vistas del diseño de la actividad
        mapView = findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        tvRegistrarActividad = findViewById(R.id.tv_registrar_actividad)
        btnIniciar = findViewById(R.id.btn_iniciar)
        btnPausar = findViewById(R.id.btn_pausar)
        btnTerminar = findViewById(R.id.btn_terminar)
        btnDatos = findViewById(R.id.btn_datos)
        btnReanudar = findViewById(R.id.btn_reanudar)
        val tvTimer = findViewById<TextView>(R.id.tv_timer)
        tvDistancia = findViewById<TextView>(R.id.tv_distancia)
        tvVelocidadMedia = findViewById<TextView>(R.id.tv_velocidad_media)

        tvTimer.visibility = View.GONE
        btnPausar.visibility = View.GONE
        btnTerminar.visibility = View.GONE

        // Manejo del temporizador para mostrar el tiempo transcurrido
        timerHandler = Handler(Looper.getMainLooper())
        timerRunnable = object : Runnable {
            override fun run() {
                if (isRecording) {
                    secondsElapsed++
                    val hours = secondsElapsed / 3600
                    val minutes = (secondsElapsed % 3600) / 60
                    val seconds = secondsElapsed % 60
                    tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                    timerHandler.postDelayed(this, 1000)
                }
            }
        }

        // Configuración de los listeners para los botones de la actividad
        btnIniciar.setOnClickListener {
            if (!isRecording) {
                isRecording = true
                tvRegistrarActividad.setBackgroundColor(ContextCompat.getColor(this, R.color.verde_3))
                tvRegistrarActividad.text = "Grabando"
                btnIniciar.visibility = View.GONE
                btnPausar.visibility = View.VISIBLE
                tvTimer.visibility = View.VISIBLE
                locationController.startLocationUpdates()
                timerHandler.post(timerRunnable)
                btnDatos.visibility = View.VISIBLE
            }
        }

        btnDatos.setOnClickListener {
            val viewFlipper = findViewById<ViewFlipper>(R.id.view_flipper)
            viewFlipper.showNext()
            if (viewFlipper.displayedChild==0){
                btnDatos.text = "Datos"
            }else{
                btnDatos.text = "Mapa"
            }
        }

        btnPausar.setOnClickListener {
            if (isRecording) {
                isRecording = false
                tvRegistrarActividad.setBackgroundColor(ContextCompat.getColor(this, R.color.amarillo))
                tvRegistrarActividad.text = "Pausa"
                btnPausar.visibility = View.GONE
                btnReanudar.visibility = View.VISIBLE
                btnTerminar.visibility = View.VISIBLE
                locationController.stopLocationUpdates()
                timerHandler.removeCallbacks(timerRunnable)
            }
        }


        btnTerminar.setOnClickListener {
            // Finalizar la actividad y pasar los datos a la actividad para guardar
            isRecording = false
            locationController.stopLocationUpdates()
            timerHandler.removeCallbacks(timerRunnable)

            val intent = Intent(this, GuardarActividadActivity::class.java)
            intent.putExtra("distanceTravelled",distanceTravelled)
            intent.putExtra("secondsElapsed", secondsElapsed)

            startActivity(intent)
            secondsElapsed = 0
            finish()
        }

        btnReanudar.setOnClickListener {
            if (!isRecording) {
                isRecording = true
                tvRegistrarActividad.setBackgroundColor(ContextCompat.getColor(this, R.color.verde_medio))
                tvRegistrarActividad.text = "Grabando"
                btnReanudar.visibility = View.GONE
                btnTerminar.visibility = View.GONE
                btnPausar.visibility = View.VISIBLE
                locationController.startLocationUpdates()
                timerHandler.post(timerRunnable)
            }
        }

    }

    // Función para actualizar la interfaz de usuario con la distancia y la velocidad media
    fun updateUI(distance: Double) {
        val distanciaKm = distance / 1000 // Convertir a km
        tvDistancia.text = "Distancia: %.2f Km".format(distanciaKm)

        val tiempoHoras = secondsElapsed / 3600.0
        val velocidadMedia = if (tiempoHoras > 0) distanciaKm / tiempoHoras else 0.0
        tvVelocidadMedia.text = "Velocidad media: %.2f Km/h".format(velocidadMedia)

    }

    // Función para actualizar el mapa con la ubicación actual del usuario
    fun updateMap(location: Location){
        val userLatLng = LatLng(location.latitude, location.longitude)
        polylinePoints.add(userLatLng)
        googleMap.addPolyline(PolylineOptions().addAll(polylinePoints))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
    }

    // Método de la interfaz OnMapReadyCallback que se llama cuando el mapa está listo para usarse
    override fun onMapReady(map: GoogleMap) {
        Log.d("Debug", "onMapReady called")
        googleMap = map

        // Obtener la última ubicación conocida del usuario y mostrarla en el mapa
        locationController.getLastLocation { location ->
            Log.d("Debug", "getLastLocation called")
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                googleMap.addMarker(MarkerOptions().position(userLatLng).title("Tu posición"))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                Log.d("Debug", "Location obtained: $userLatLng")
            }else{
                Log.d("Debug", "Location not obtained")
            }
        }
    }

    // Métodos del ciclo de vida del mapa
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }




}


