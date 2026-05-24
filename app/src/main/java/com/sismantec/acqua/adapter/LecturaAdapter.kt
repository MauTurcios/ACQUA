package com.sismantec.acqua.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Layer
import androidx.recyclerview.widget.RecyclerView
import com.sismantec.acqua.models.DatosAvisoCobro
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.models.LecturaResumen
import com.sismantec.acqua.R
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager


class LecturaAdapter(
    private var lista: List<LecturaResumen> = listOf()
): RecyclerView.Adapter<LecturaAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtCodigo: TextView = view.findViewById(R.id.lecCodigo)
        val txtDireccion: TextView = view.findViewById(R.id.lecDireccion)
        val txtConsumo: TextView = view.findViewById(R.id.lecConsumo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.aviso_cobro_tarjeta, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tarjeta = lista [position]

        holder.txtCodigo.text = tarjeta.codigoCliente
        holder.txtDireccion.text = tarjeta.direccion
        holder.txtConsumo.text = tarjeta.consumo.toString()
    }

    fun actualizar (nuevaLista: List<LecturaResumen>){
        lista = nuevaLista
        notifyDataSetChanged()
    }

}