package View.Tools

import Model.DataManagerStatistics
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.greensteps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TotalFragment : Fragment() {

    private lateinit var tvActividadesValor: TextView
    private lateinit var tvDistanciaValor: TextView
    private lateinit var tvTiempoValor: TextView
    private lateinit var tvCombustibleValor: TextView
    private lateinit var tvEmisionesValor: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_total, container, false)

        tvActividadesValor = view.findViewById(R.id.tv_actividades_valor)
        tvDistanciaValor = view.findViewById(R.id.tv_distancia_valor)
        tvTiempoValor = view.findViewById(R.id.tv_tiempo_valor)
        tvCombustibleValor = view.findViewById(R.id.tv_combustible_valor)
        tvEmisionesValor = view.findViewById(R.id.tv_emisiones_valor)

        val dataManager = DataManagerStatistics(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())
        val userId = FirebaseAuth.getInstance().currentUser?.uid // ID del usuario actual
        if (userId != null) {
            dataManager.getEstadisticasUsuario(userId, "Total") { estadisticas ->
                if (estadisticas != null) {
                    tvActividadesValor.text = estadisticas.actividades.toString()
                    tvDistanciaValor.text = String.format("%.2f Km", estadisticas.distancia)
                    tvTiempoValor.text = String.format("%02d:%02d:%02d", estadisticas.tiempo / 3600, (estadisticas.tiempo % 3600) / 60, (estadisticas.tiempo % 60))
                    tvCombustibleValor.text = String.format("%.3f L", estadisticas.combustible)
                    tvEmisionesValor.text = String.format("%.3f Kg", estadisticas.emisiones)

                } else {
                    // Manejar el caso en que las estadísticas son null
                }
            }
        }

        return view
    }
}
