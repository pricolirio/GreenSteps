package View.Tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.greensteps.R
import java.text.SimpleDateFormat

class ActividadAdapter(context: Context, private val actividades: List<Actividad>) : ArrayAdapter<Actividad>(context, R.layout.actividad_item, actividades) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.actividad_item, parent, false)

        val nombre = view.findViewById<TextView>(R.id.nombre)

        val actividad = actividades[position]
        nombre.text = actividad.nombre

        return view
    }
}

