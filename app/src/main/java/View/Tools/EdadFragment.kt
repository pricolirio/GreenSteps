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

class EdadFragment : Fragment() {

    private lateinit var tvValorImpactoEdad: TextView
    private lateinit var tvPosicion: TextView
    private lateinit var tvValorPosicionEdad: TextView
    private lateinit var tvRankEdad: Array<TextView>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvValorImpactoEdad = view.findViewById(R.id.tv_valor_impacto_edad)
        tvPosicion = view.findViewById(R.id.tv_posicion)
        tvValorPosicionEdad = view.findViewById(R.id.tv_valor_posicion_edad)
        tvRankEdad = arrayOf(
            view.findViewById(R.id.tv_rank_edad_1),
            view.findViewById(R.id.tv_rank_edad_2),
            view.findViewById(R.id.tv_rank_edad_3),
            view.findViewById(R.id.tv_rank_edad_4),
            view.findViewById(R.id.tv_rank_edad_5)
        )

        val dataManager = DataManagerStatistics(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())

        // Obtener emisiones totales del usuario
        dataManager.obtenerEmisionesTotalesUsuario { result ->
            if (result.isSuccess) {
                val totalEmisiones = result.getOrNull()
                tvValorImpactoEdad.text = String.format("%.2f Kg CO2", totalEmisiones)
            } else {
                Log.e("TAG", "Error: ${result.exceptionOrNull()}")
            }
        }

        // Obtener rango de edad del usuario
        dataManager.obtenerRangoEdad { resultRango ->
            if (resultRango.isSuccess) {
                val rangoEdad = resultRango.getOrNull()
                tvPosicion.text = "Posición ($rangoEdad):"

                // Obtener posición del usuario en el ranking de su rango de edad
                dataManager.obtenerPosicionEmisionesEdad { resultPosicion ->
                    if (resultPosicion.isSuccess) {
                        val (posicion, totalUsuariosRango) = resultPosicion.getOrNull() ?: 0 to 0
                        tvValorPosicionEdad.text = "$posicion / $totalUsuariosRango"
                    } else {
                        Log.e("TAG", "Error: ${resultPosicion.exceptionOrNull()}")
                    }
                }

                // Obtener Top 5 emisiones dentro del mismo rango de edad
                dataManager.obtenerTop5EmisionesEdad { result ->
                    if (result.isSuccess) {
                        val top5 = result.getOrNull()
                        top5?.forEachIndexed { index, usuarioDistancia ->
                            tvRankEdad[index].text = "${index + 1} ${usuarioDistancia.nombre} ${"%.2f".format(usuarioDistancia.distancia)}"
                        }
                    } else {
                        Log.e("TAG", "Error: ${result.exceptionOrNull()}")
                    }
                }
            } else {
                Log.e("TAG", "Error: ${resultRango.exceptionOrNull()}")
            }
        }
    }
}

