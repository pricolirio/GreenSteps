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

import View.Tools.ComunidadPagerAdapter
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.example.greensteps.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ComunidadActivity : MenuActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.comunidad)

        //Inicialización del menú inferior
        setupMenu(R.id.btn_menu,R.id.btn_progreso,R.id.btn_registro,R.id.btn_comunidad)

        // Inicialización de ViewPager y TabLayout
        viewPager = findViewById(R.id.view_pager)
        tabLayout = findViewById(R.id.tab_layout)

        val pagerAdapter = ComunidadPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        // Conexión entre el TabLayout y el ViewPager
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Distancia"
                1 -> "Impacto"
                else -> "Edad"
            }
        }.attach()
    }
}