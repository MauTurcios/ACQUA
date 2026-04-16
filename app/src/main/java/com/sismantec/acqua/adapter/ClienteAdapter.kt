package com.sismantec.acqua.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.sismantec.acqua.DatosCliente
import com.sismantec.acqua.MenuClientes
import com.sismantec.acqua.R
import com.sismantec.acqua.funciones.Funciones
import com.sismantec.acqua.entities.ClientesEntity
import com.sismantec.acqua.models.Cliente


class ClienteAdapter(
    private val itemClick: (ClientesEntity) -> Unit
) : RecyclerView.Adapter<ClienteAdapter.ViewHolder>(){

    //var funciones :Funciones? = null
    private var lista: List<ClientesEntity> = emptyList()
    private var listaBusqueda : List<ClientesEntity> = emptyList()
    //private val rutaSeleccionada: Int? = null
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val codigo : TextView = itemView.findViewById<TextView>(R.id.Codigo)
        val cliente : TextView = itemView.findViewById<TextView>(R.id.Cliente)
        val casa : TextView = itemView.findViewById<TextView>(R.id.Direccion)
        fun bind(item: ClientesEntity){
            codigo.text = item.Codigo
            cliente.text = item.Cliente
            casa.text = item.Casa

            itemView.setOnClickListener {
                itemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(
        R.layout.cliente_tarjeta, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, conta: Int) {
        holder.bind(lista[conta])
        //holder.codigo.text = lista[conta].Codigo
        //holder.cliente.text = lista[conta].Cliente
        //holder.casa.text = lista[conta].Casa
        /*
        //MOVER FUNCIONALIDAD A LA ACTIVITY
        val item = lista[conta]
        holder.itemView.setOnClickListener {
            Log.d("CLIENTE_CLICK", "ID: ${item.Id}")
            val context = holder.itemView.context
            val intent = Intent(context, DatosCliente::class.java)
            intent.putExtra("Id_cliente", item.Id)
            context.startActivity(intent)
        }*/
    }

    override fun getItemCount()= lista.size

    fun actualizarLista(nuevaLista: List<ClientesEntity>){
        listaBusqueda = nuevaLista
        lista = nuevaLista.toList()
        notifyDataSetChanged()
    }

    fun busquedaFiltro(texto: String = "", rutaId: Int? = null){
        val textoLimpio = texto.trim()
        lista = listaBusqueda.filter { cliente ->
            val coincideTexto =
                texto.isEmpty() ||
                        cliente.Cliente.trim().contains(textoLimpio,true) ||
                        cliente.Codigo.trim().contains(textoLimpio,true)

            val coincideRuta =
                rutaId == null || cliente.Id_ruta == rutaId

            coincideTexto && coincideRuta

        }
        notifyDataSetChanged()
    }
}
