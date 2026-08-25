package com.sismantec.acqua.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Layer
import androidx.recyclerview.widget.RecyclerView
import com.sismantec.acqua.entities.LecturaEntity
import com.sismantec.acqua.R
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sismantec.acqua.AvisoCobroDetalle


class LecturaAdapter(
    private val itemClick: (LecturaEntity) -> Unit
): RecyclerView.Adapter<LecturaAdapter.ViewHolder>() {

    private var lista : List<LecturaEntity> = emptyList()
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val txtEstado: TextView = view.findViewById(R.id.lecEstado)
        val txtCodigo: TextView = view.findViewById(R.id.lecCodigo)
        val txtLectura: TextView = view.findViewById(R.id.lecLectura)
        val txtConsumo: TextView = view.findViewById(R.id.lecConsumo)

        fun bind(tarjeta: LecturaEntity){
            var estado = "ENVIADA"
            if (tarjeta.Lectura_enviada == true){
                estado = "LECTURA ENVIADA"
                txtEstado.setBackgroundResource(R.color.colorVerde)
            }else{
                estado = "LECTURA NO ENVIADA"
                txtEstado.setBackgroundResource(R.color.rojo)
            }
            txtEstado.text = estado
            txtCodigo.text = tarjeta.Cuenta
            txtLectura.text = tarjeta.Lectura_actual.toString()
            txtConsumo.text = tarjeta.Consumo.toString()

            itemView.setOnClickListener {
                itemClick(tarjeta)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.aviso_cobro_tarjeta, parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    //@SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lista [position])
    }

    fun actualizar (nuevaLista: List<LecturaEntity>){
        lista = nuevaLista
        notifyDataSetChanged()
    }

}