package View.Tools

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class EstadisticasPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        // Crear un nuevo fragmento basado en la posición
        return when (position) {
            0 -> SemanalFragment()
            1 -> MensualFragment()
            2 -> TotalFragment()
            else -> SemanalFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3  // Semanal, Mensual, Total
    }
}
