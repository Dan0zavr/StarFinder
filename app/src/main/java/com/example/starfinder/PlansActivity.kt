package com.example.starfinder

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.starfinder.ObservationHistoryActivity
import com.example.starfinder.UserSession.getCurrentUserId
import com.example.starfinder.databinding.ActivityPlansBinding
import com.example.starfinder.databinding.HistoryBinding
import com.example.starfinder.models.Observation
import com.example.starfinder.services.DataService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlansActivity : BaseActivity() {
    private lateinit var binding: ActivityPlansBinding
    private lateinit var dataService: DataService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlansBinding.inflate(layoutInflater)
        baseBinding.contentFrame.addView(binding.root)

        setToolbarTitle("Планы наблюдений")

        dataService = DataService(applicationContext).also {
            if (!it.checkDatabase()) {
                Toast.makeText(this, "Ошибка доступа к базе данных", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }


        val userId = getCurrentUserId(this)
        if (userId == -1) return

        loadObservations(userId)

    }

    private fun loadObservations(userId: Int) {
        try {
            val currentDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                .format(Date()) // Текущая дата и время

            Log.d("DEBUG", "Загружаем наблюдения для userId = $userId до $currentDateTime")

            val observations = dataService.getWithQuery(
                "SELECT * FROM Observation WHERE UserId = ? AND ObservationDateTime > ? ORDER BY ObservationDateTime DESC",
                arrayOf(userId.toString(), currentDateTime)
            ) { cursor ->
                Observation(
                    observationId = cursor.getInt(cursor.getColumnIndexOrThrow("ObservationId")),
                    observationDateTime = cursor.getString(cursor.getColumnIndexOrThrow("ObservationDateTime")),
                    observationLatitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLatitude")),
                    observationLongitude = cursor.getDouble(cursor.getColumnIndexOrThrow("ObservationLongitude")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("UserId"))
                )
            }

            if (observations.isEmpty()) {
                showEmptyState()
            } else {
                setupRecyclerView(observations)
                binding.emptyPlansView.visibility = View.GONE
                binding.plansRecyclerView.visibility = View.VISIBLE
                Log.d("DEBUG", "Загружено")
            }

        } catch (e: Exception) {
            Log.e("LOAD_ERROR", "Error loading observations", e)
            showError("Ошибка загрузки данных")
        }
    }

    private fun setupRecyclerView(plans: List<Observation>) {
        binding.plansRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlansActivity)
            adapter = ObservationAdapter( // Не забываем присвоить адаптер
                items = plans,
                dataService = dataService,
                itemLayoutRes = R.layout.item_plan
            ).apply {
                onItemClick = { plan ->
//                    showPlanDetails(plan)
                }

                onDeleteClick = { plan ->
                    AlertDialog.Builder(this@PlansActivity)
                        .setTitle("Удаление")
                        .setMessage("Удалить план наблюдения за ${dataService.getStarNameByObservation(plan.observationId)}?")
                        .setPositiveButton("Удалить") { _, _ ->
//                            deletePlan(plan.observationId)
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyPlansView.text = "У вас нет планов"
        binding.emptyPlansView.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        binding.emptyPlansView.text = message
        binding.emptyPlansView.visibility = View.VISIBLE
        binding.plansRecyclerView.visibility = View.GONE
    }
}