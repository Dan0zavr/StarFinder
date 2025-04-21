package com.example.starfinder

import android.os.Bundle
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.starfinder.services.DataService

class ObservationHistoryActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ObservationAdapter
    private lateinit var dataService: DataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = findViewById<FrameLayout>(R.id.contentFrame)
        layoutInflater.inflate(R.layout.history, container, true)

        recyclerView = findViewById(R.id.observationHistoryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dataService = DataService(this)
        val observations = dataService.getAllObservationsForUser(1) // Заменить на текущего пользователя

        adapter = ObservationAdapter(observations)
        recyclerView.adapter = adapter
    }
}
