package com.example.starfinder

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.starfinder.models.StarInfo

class StarAdapter(
    private val context: Context,
    private val stars: List<StarInfo>
) : BaseAdapter() {

    private var itemClickListener: ((StarInfo) -> Unit)? = null

    fun setOnItemClickListener(listener: (StarInfo) -> Unit) {
        itemClickListener = listener
    }

    override fun getCount(): Int = stars.size

    override fun getItem(position: Int): StarInfo = stars[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val star = stars[position]
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = star.name
        textView.setTextColor(Color.WHITE) // Устанавливаем белый цвет текста

        view.setOnClickListener {
            itemClickListener?.invoke(star)
        }

        return view
    }
}