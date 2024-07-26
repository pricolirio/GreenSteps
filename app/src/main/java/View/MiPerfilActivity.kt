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
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.example.greensteps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class MiPerfilActivity : MenuActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dataManagerUser: DataManagerUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dataManagerUser = DataManagerUser(auth, db)

        // Obtención de referencias a los elementos de la interfaz de usuario
        val nombre = findViewById<EditText>(R.id.et_nombre)
        val fechaNacimiento = findViewById<EditText>(R.id.et_fecha_nacimiento)
        val vehiculo = findViewById<Spinner>(R.id.sp_vehiculo)
        val guardarCambios = findViewById<Button>(R.id.btn_guardar_cambios)

        // Configuración del DatePickerDialog para la selección de fecha de nacimiento
        fechaNacimiento.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                // Muestra la fecha seleccionada en el cuadro de texto
                fechaNacimiento.setText("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year)
            }, year, month, day)

            dpd.show()
        }

        // Obtener los datos del usuario almacenados en SharedPreferences
        val sharedPref = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE)
        val nombreUsuario = sharedPref.getString("nombreUsuario", "Usuario predeterminado")
        val fechaN = sharedPref.getString("fechaN","Usuario predeterminado")
        val vehiculoSpinner = sharedPref.getString("vehiculo", "Usuario predeterminado")

        nombre.setText(nombreUsuario)
        fechaNacimiento.setText(fechaN)
        val vehiculosArray = resources.getStringArray(R.array.vehiculo_array)
        val indice = obtenerIndiceDeVehiculo(vehiculoSpinner,vehiculosArray)
        vehiculo.setSelection(indice)


        guardarCambios.setOnClickListener {
            // Guardar los cambios del usuario
            dataManagerUser.guardarCambiosUsuario(nombre.text.toString(), fechaNacimiento.text.toString(), vehiculo.selectedItem.toString(), sharedPref)
        }

        //Inicialización del menú inferior
        setupMenu(R.id.btn_menu,R.id.btn_progreso,R.id.btn_registro,R.id.btn_comunidad)
    }


    private fun obtenerIndiceDeVehiculo(vehiculo: String?, vehiculosArray: Array<String>): Int {
        return vehiculosArray.indexOf(vehiculo ?: vehiculosArray[0])
    }

}
