package com.example.movilwebexamen

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class PermissionAdapter(
    context: Context,
    private val permisos: List<String>,
    private val colores: List<Int>
) : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, permisos) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        view.setBackgroundColor(getContext().getColor(colores[position]))
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.setTextColor(getContext().getColor(R.color.black)) // Cambia el color del texto si es necesario
        return view
    }
}
