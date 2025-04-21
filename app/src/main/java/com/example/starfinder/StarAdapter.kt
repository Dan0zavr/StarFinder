package com.example.starfinder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.starfinder.models.StarInfo

class StarAdapter(context: Context, stars: List<StarInfo>) : ArrayAdapter<StarInfo>(context, 0, stars) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val star = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.star_list_item, parent, false)

        // Инициализация элементов в списке
        val nameTextView = view.findViewById<TextView>(R.id.starName)
        val coordinatesTextView = view.findViewById<TextView>(R.id.starCoordinates)


        // Заполнение данными
        nameTextView.text = star?.name
        coordinatesTextView.text = "RA: ${star?.ra}, Dec: ${star?.dec}"

        return view
    }
}