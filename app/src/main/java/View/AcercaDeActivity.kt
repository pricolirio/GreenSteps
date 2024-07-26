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

import android.os.Bundle
import android.text.Html
import android.widget.TextView
import com.example.greensteps.R
import java.io.BufferedReader
import java.io.InputStreamReader

class AcercaDeActivity : MenuActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acercade)

        val tvInfo = findViewById<TextView>(R.id.tv_info)

        // Leer el contenido del archivo HTML desde assets
        val htmlContent = try {
            assets.open("acercade_texto.html").bufferedReader().use {
                it.readText()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

        tvInfo.text = Html.fromHtml(htmlContent)

        setupMenu(R.id.btn_menu,R.id.btn_progreso,R.id.btn_registro,R.id.btn_comunidad)
    }
}