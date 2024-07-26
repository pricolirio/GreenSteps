package View.Tools

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ComunidadPagerAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        // Crear un nuevo fragmento basado en la posición
        return when (position) {
            0 -> DistanciaFragment()
            1 -> ImpactoFragment()
            2 -> EdadFragment()
            else -> DistanciaFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3  // Distancia, Impacto, Edad
    }
}