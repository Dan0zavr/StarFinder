package com.example.starfinder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.starfinder.models.StarInfo

class StarInfoAdapter(
    context: Context,
    private val stars: List<StarInfo>
) : ArrayAdapter<StarInfo>(context, android.R.layout.simple_list_item_1, stars) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val star = stars[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "${star.name} (RA: ${star.ra}, Dec: ${star.dec})"
        return view
    }
}