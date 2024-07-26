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

class ImpactoFragment : Fragment() {

    private lateinit var tvValorImpacto: TextView
    private lateinit var tvValorPosicionImpacto: TextView
    private lateinit var tvRankImp: Array<TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_impacto, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvValorImpacto = view.findViewById(R.id.tv_valor_impacto)
        tvValorPosicionImpacto = view.findViewById(R.id.tv_valor_posicion_impacto)
        tvRankImp = arrayOf(
            view.findViewById(R.id.tv_rank_imp_1),
            view.findViewById(R.id.tv_rank_imp_2),
            view.findViewById(R.id.tv_rank_imp_3),
            view.findViewById(R.id.tv_rank_imp_4),
            view.findViewById(R.id.tv_rank_imp_5)
        )

        val dataManager = DataManagerStatistics(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

        dataManager.obtenerEmisionesTotalesUsuario { result ->
            if (result.isSuccess) {
                val emisionesTotales = result.getOrNull()
                tvValorImpacto.text = String.format("%.2f Kg CO2", emisionesTotales)
            } else {
                Log.e("TAG", "Error: ${result.exceptionOrNull()}")
            }
        }

        dataManager.obtenerTotalUsuarios { resultTotalUsuarios ->
            if (resultTotalUsuarios.isSuccess) {
                val totalUsuarios = resultTotalUsuarios.getOrNull()
                dataManager.obtenerPosicionEmisiones { resultPosicion ->
                    if (resultPosicion.isSuccess) {
                        val posicion = resultPosicion.getOrNull()
                        tvValorPosicionImpacto.text = "$posicion / $totalUsuarios"
                    } else {
                        Log.e("TAG", "Error: ${resultPosicion.exceptionOrNull()}")
                    }
                }
            } else {
                Log.e("TAG", "Error: ${resultTotalUsuarios.exceptionOrNull()}")
            }
        }

        dataManager.obtenerTop5Emisiones { result ->
            if (result.isSuccess) {
                val top5 = result.getOrNull()
                top5?.forEachIndexed { index, usuarioDistancia ->
                    tvRankImp[index].text = "${index + 1} ${usuarioDistancia.nombre} ${"%.2f".format(usuarioDistancia.distancia)} Kg CO2"
                }
            } else {
                Log.e("TAG", "Error: ${result.exceptionOrNull()}")
            }
        }
    }
}
