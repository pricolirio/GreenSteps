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


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.greensteps.R
import com.google.firebase.auth.FirebaseAuth


open class MenuActivity : AppCompatActivity() {

    // Método para inicializar el menú desplegable y los botones de la barra inferior
    fun setupMenu(buttonId: Int, progresoButtonId: Int, registroButtonId: Int, comunidadButtonId: Int) {
        //menu desplegable
        val btnMenu = findViewById<ImageButton>(buttonId)
        btnMenu.setOnClickListener { showMenu(it) }

        //boton progreso
        val btnProgreso = findViewById<Button>(progresoButtonId)
        btnProgreso.setOnClickListener {
            val intent = Intent(this, ProgresoActivity::class.java)
            startActivity(intent)
        }

        //boton registro
        val btnRegistro = findViewById<Button>(registroButtonId)
        btnRegistro.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                Toast.makeText(this, "Es necesario conceder los permisos de ubicación", Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, RegistroActividadActivity::class.java)
                startActivity(intent)

            }
        }

        //boton comunidad
        val btnComunidad = findViewById<Button>(comunidadButtonId)
        btnComunidad.setOnClickListener {
            val intent = Intent(this, ComunidadActivity::class.java)
            startActivity(intent)
        }
    }

    // Método para mostrar el menú emergente
    fun showMenu(view: View) {
        val popupMenu = PopupMenu(this, view, 0,0, R.style.PopupMenuStyle)

        // Añade los elementos al menú
        popupMenu.menu.add(Menu.NONE, 1, 1, "Inicio")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Perfil")
        popupMenu.menu.add(Menu.NONE, 3, 3, "Acerca de")
        popupMenu.menu.add(Menu.NONE, 4, 4, "Cerrar sesión")

        popupMenu.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                1 -> {
                    val intent = Intent(this, PanelPrincipalActivity::class.java)
                    startActivity(intent)
                    true
                }
                2 -> {
                    val intent = Intent(this, MiPerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                3 -> {
                    val intent = Intent(this, AcercaDeActivity::class.java)
                    startActivity(intent)
                    true
                }
                4 -> {
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}

