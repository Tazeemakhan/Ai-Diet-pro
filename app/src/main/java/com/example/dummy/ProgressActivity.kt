package com.example.dummy

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.*
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat

class ProgressActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var tvName: TextView
    private lateinit var tvInitialWeight: TextView
    private lateinit var etNewWeight: EditText
    private lateinit var etDays: EditText
    private lateinit var btnTrackProgress: Button
    private lateinit var lineChart: LineChart
    private lateinit var resultBox: LinearLayout
    private lateinit var tvChangeResult: TextView
    private lateinit var tvBadge: TextView

    private var progressEntries = mutableListOf<Entry>()
    private var totalDays = 0
    private var initialWeight = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_progress)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupChart()
        loadInitialData()
    }

    private fun initViews() {
        db = FirebaseFirestore.getInstance()
        tvName = findViewById(R.id.tvName)
        tvInitialWeight = findViewById(R.id.tvInitialWeight)
        etNewWeight = findViewById(R.id.etNewWeight)
        etDays = findViewById(R.id.etDays)
        btnTrackProgress = findViewById(R.id.btnTrackProgress)
        lineChart = findViewById(R.id.lineChart)
        resultBox = findViewById(R.id.resultBox)
        tvChangeResult = findViewById(R.id.tvChangeResult)
        tvBadge = findViewById(R.id.tvBadge)

        btnTrackProgress.setOnClickListener { trackProgress() }
    }

    private fun loadInitialData() {
        db.collection("users")
            .orderBy("name")
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val name = doc.getString("name") ?: "User"
                    val weight = doc.getDouble("weight") ?: 0.0

                    tvName.text = "👤 Name: $name"
                    tvInitialWeight.text = "⚖️ Initial Weight: ${weight}kg"
                    initialWeight = weight.toFloat()

                    // Add initial point (Day 0)
                    progressEntries.add(Entry(initialWeight, 0f))
                    updateChart()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun trackProgress() {
        val weightStr = etNewWeight.text.toString()
        val daysStr = etDays.text.toString()

        val newWeight = weightStr.toFloatOrNull()
        val daysToAdd = daysStr.toIntOrNull()

        if (newWeight == null || daysToAdd == null || newWeight <= 0 || daysToAdd <= 0) {
            Toast.makeText(this, "Please enter valid data", Toast.LENGTH_SHORT).show()
            return
        }

        totalDays += daysToAdd
        progressEntries.add(Entry(newWeight, totalDays.toFloat()))

        updateResults(newWeight)
        updateChart()
        clearInputs()
    }

    private fun updateResults(currentWeight: Float) {
        val weightChange = initialWeight - currentWeight
        val changePerDay = weightChange / totalDays

        resultBox.visibility = View.VISIBLE
        tvChangeResult.text = "🎯 Progress: ${formatWeight(weightChange)}kg in $totalDays days" +
                "\n📊 Avg: ${formatWeight(changePerDay)}kg/day"

        tvBadge.text = when {
            weightChange >= 5 -> "🏆 Champion (${formatWeight(weightChange)}kg)"
            weightChange >= 2 -> "💪 Pro (${formatWeight(weightChange)}kg)"
            weightChange > 0 -> "👍 Starter (${formatWeight(weightChange)}kg)"
            else -> "🔍 Keep Going"
        }
    }

    private fun formatWeight(value: Float): String {
        return DecimalFormat("#.#").format(value)
    }

    private fun clearInputs() {
        etNewWeight.text.clear()
        etDays.text.clear()
    }

    private fun setupChart() {
        with(lineChart) {
            setTouchEnabled(true)
            setPinchZoom(true)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false

            // X-Axis (Weight)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textSize = 12f
                textColor = Color.DKGRAY
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()}kg"
                    }
                }
            }

            // Y-Axis (Days)
            axisLeft.apply {
                granularity = 1f
                textSize = 12f
                textColor = Color.DKGRAY
                axisMinimum = 0f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "Day ${value.toInt()}"
                    }
                }
            }
        }
    }

    private fun updateChart() {
        if (progressEntries.isEmpty()) return

        val dataSet = LineDataSet(progressEntries, "").apply {
            color = Color.parseColor("#4CAF50")
            valueTextColor = Color.BLACK
            lineWidth = 3f
            circleRadius = 5f
            setCircleColor(Color.parseColor("#388E3C"))
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#804CAF50")
            fillAlpha = 100
        }

        lineChart.data = LineData(dataSet)

        // Auto-scale axes
        val weights = progressEntries.map { it.x }
        val days = progressEntries.map { it.y }

        lineChart.xAxis.apply {
            axisMinimum = (weights.minOrNull() ?: 0f) - 5f
            axisMaximum = (weights.maxOrNull() ?: 100f) + 5f
        }

        lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = (days.maxOrNull() ?: 7f) + 1f
        }

        // Add marker
        val marker = CustomMarkerView(this, R.layout.custom_marker)
        lineChart.marker = marker

        lineChart.invalidate()
    }
}