package com.example.starfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.starfinder.models.Observation
import com.example.starfinder.services.DataService
import java.text.SimpleDateFormat
import java.util.Locale

class ObservationAdapter(
    private val items: List<Observation>,
    private val dataService: DataService,
    private val itemLayoutRes: Int // R.layout.item_observation или R.layout.item_plan
) : RecyclerView.Adapter<ObservationAdapter.ViewHolder>() {

    // Callback для кликов
    var onViewClick: ((Observation) -> Unit)? = null
    var onDeleteClick: ((Observation) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val objectName: TextView = view.findViewById(R.id.starName)
        val date: TextView = view.findViewById(R.id.observationDate)
        val time: TextView = view.findViewById(R.id.observationTime)
        val coordinates: TextView = view.findViewById(R.id.observationCoordinates)
        val btnDelete: Button? = view.findViewById(R.id.btnDelete)
        val btnView: Button? = view.findViewById(R.id.btnView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(itemLayoutRes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val observation = items[position]
        val starName = dataService.getStarNameByObservation(observation.observationId)

        // Общие данные
        holder.objectName.text = starName
        holder.date.text = "Дата:${formatDate(observation.observationDateTime)}"
        holder.time.text = "Время:${formatTime(observation.observationDateTime)}"
        holder.coordinates.text = "Ш: ${observation.observationLatitude?.toString() ?: "N/A"}, Д: ${observation.observationLongitude?.toString() ?: "N/A"}"

        // Обработка кликов
        holder.btnView?.setOnClickListener {
            onViewClick?.invoke(observation)
        }

        // Кнопка удаления (если есть в layout)
        holder.btnDelete?.setOnClickListener {
            onDeleteClick?.invoke(observation)
        }
    }

    override fun getItemCount() = items.size

    private fun formatDate(dateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateTime)
            return outputFormat.format(date)
        } catch (e: Exception) {
            dateTime
        }
    }

    private fun formatTime(dateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateTime)
            return outputFormat.format(date)
        } catch (e: Exception) {
            dateTime
        }
    }
}
