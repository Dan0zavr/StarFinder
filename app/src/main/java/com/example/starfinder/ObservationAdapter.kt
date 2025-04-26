package com.example.starfinder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.starfinder.models.CelestialBody
import com.example.starfinder.models.CelestialBodyInObservation
import com.example.starfinder.models.Observation
import com.example.starfinder.services.DataService
import java.security.PrivateKey
import java.text.SimpleDateFormat
import java.util.Locale

class ObservationAdapter(
    private val observations: List<Observation>,
    private val dataService: DataService
) : RecyclerView.Adapter<ObservationAdapter.ViewHolder>() {


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val objectName: TextView = view.findViewById(R.id.starName)
        val date: TextView = view.findViewById(R.id.observationDate)
        val time: TextView = view.findViewById(R.id.observationTime)
        val coordinates: TextView = view.findViewById(R.id.observationCoordinates)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_observation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val observation = observations[position]
        val obId = observation.observationId
        val starName = dataService.getStarNameByObservation(obId)

        holder.objectName.text = starName
        holder.date.text = "Дата:${formatDate(observation.observationDateTime)}"
        holder.time.text = "Время:${formatTime(observation.observationDateTime)}"
        holder.coordinates.text = "Ш: ${observation.observationLatitude?.toString() ?: "N/A"}, Д: ${observation.observationLongitude?.toString() ?: "N/A"}"
    }

    override fun getItemCount() = observations.size

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
