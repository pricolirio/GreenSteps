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
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.greensteps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegistroUsuarioActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var dataManagerUser: DataManagerUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nuevousuario)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        dataManagerUser = DataManagerUser(auth, db)

        // Obtención de referencias a los elementos de la interfaz de usuario
        val nombre = findViewById<EditText>(R.id.et_nombre)
        val email = findViewById<EditText>(R.id.et_email)
        val fechaNacimiento = findViewById<EditText>(R.id.et_fecha_nacimiento)
        val vehiculo = findViewById<Spinner>(R.id.sp_vehiculo)
        val password = findViewById<EditText>(R.id.et_password)
        val repeatPassword = findViewById<EditText>(R.id.et_repeat_password)
        val crearUsuario = findViewById<Button>(R.id.btn_crear_usuario)
        val cbAcceptPrivacyPolicy = findViewById<CheckBox>(R.id.cb_accept_privacy_policy)
        val tvPrivacyPolicyLink = findViewById<TextView>(R.id.tv_privacy_policy_link)

        // Listener para seleccionar la fecha de nacimiento
        fechaNacimiento.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(this, { _, year, monthOfYear, dayOfMonth ->
                fechaNacimiento.setText("" + dayOfMonth + "/" + (monthOfYear + 1) + "/" + year)
            }, year, month, day)

            dpd.show()
        }

        // Listener para mostrar la política de privacidad
        tvPrivacyPolicyLink.setOnClickListener {
            val webView = WebView(this).apply {
                webViewClient = WebViewClient()
                loadUrl("file:///android_asset/privacy_policy.html")
            }

            val privacyPolicyDialog = AlertDialog.Builder(this)
                .setTitle("Política de Privacidad")
                .setView(webView)
                .setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                .create()

            privacyPolicyDialog.show()
        }

        // Listener para el botón de crear usuario
        crearUsuario.setOnClickListener {
            val emailText = email.text.toString()
            val passwordText = password.text.toString()
            val repeatPasswordText = repeatPassword.text.toString()

            if (!cbAcceptPrivacyPolicy.isChecked) {
                Toast.makeText(this, "Debes aceptar los términos de nuestra Política de Privacidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                if (passwordText == repeatPasswordText) {
                    dataManagerUser.registrarUsuario(emailText, passwordText, nombre.text.toString(), fechaNacimiento.text.toString(), vehiculo.selectedItem.toString(),
                        onSuccess = {
                            Toast.makeText(baseContext, "Registro exitoso", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onFailure = { error ->
                            Toast.makeText(baseContext, error, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
