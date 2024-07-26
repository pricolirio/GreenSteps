package View.Tools

import Model.DataManagerStatistics
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.greensteps.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DistanciaFragment : Fragment() {

    private lateinit var tvValorDistancia: TextView
    private lateinit var tvValorPosicionDistancia: TextView
    private lateinit var tvRankDist: Array<TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_distancia, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvValorDistancia = view.findViewById(R.id.tv_valor_distancia)
        tvValorPosicionDistancia = view.findViewById(R.id.tv_valor_posicion_distancia)
        tvRankDist = arrayOf(
            view.findViewById(R.id.tv_rank_dist_1),
            view.findViewById(R.id.tv_rank_dist_2),
            view.findViewById(R.id.tv_rank_dist_3),
            view.findViewById(R.id.tv_rank_dist_4),
            view.findViewById(R.id.tv_rank_dist_5)
        )

        val dataManager = DataManagerStatistics(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

        dataManager.obtenerActividadConMayorDistancia { result ->
            if (result.isSuccess) {
                val distancia = result.getOrNull()
                tvValorDistancia.text = String.format("%.2f Km", distancia)
            } else {
                Log.e("TAG", "Error: ${result.exceptionOrNull()}")
            }
        }

        dataManager.obtenerTotalUsuarios { resultTotalUsuarios ->
            if (resultTotalUsuarios.isSuccess) {
                val totalUsuarios = resultTotalUsuarios.getOrNull()
                dataManager.obtenerPosicionDistancia { resultPosicion ->
                    if (resultPosicion.isSuccess) {
                        val posicion = resultPosicion.getOrNull()
                        tvValorPosicionDistancia.text = "$posicion / $totalUsuarios"
                    } else {
                    }
                }
            } else {
                Log.e("TAG", "Error: ${resultTotalUsuarios.exceptionOrNull()}")
            }
        }

        dataManager.obtenerTop5Distancia { result ->
            if (result.isSuccess) {
                val top5 = result.getOrNull()
                top5?.forEachIndexed { index, usuarioDistancia ->
                    tvRankDist[index].text = "${index + 1} ${usuarioDistancia.nombre} ${"%.2f".format(usuarioDistancia.distancia)} Km"
                }
            } else {
                Log.e("TAG", "Error: ${result.exceptionOrNull()}")
            }
        }
    }
}



