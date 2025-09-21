package com.example.dummy

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.lzyzsd.circleprogress.ArcProgress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WeightProgressActivity : AppCompatActivity() {

    private lateinit var arcProgress: ArcProgress
    private lateinit var tvWeightLost: TextView
    private lateinit var tvCurrentWeight: TextView
    private lateinit var tvGoalWeightCircle: TextView
    private lateinit var tvInitialWeight: TextView
    private lateinit var btnAddWeight: Button

    // User info
    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvGoalWeightInfo: TextView
    private lateinit var tvActivity: TextView
    private lateinit var tvAllergy: TextView

    private var initialWeight = 0f
    private var currentWeight = 0f
    private var goalWeight = 0f

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progress)

        // Bind views
        arcProgress = findViewById(R.id.arcProgress)
        tvWeightLost = findViewById(R.id.tvWeightLost)
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight)
        tvGoalWeightCircle = findViewById(R.id.tvGoalWeightCircle)
        tvInitialWeight = findViewById(R.id.tvInitialWeight)
        btnAddWeight = findViewById(R.id.btnAddWeight)

        // User info views
        tvName = findViewById(R.id.tvName)
        tvAge = findViewById(R.id.tvAge)
        tvGender = findViewById(R.id.tvGender)
        tvHeight = findViewById(R.id.tvHeight)
        tvWeight = findViewById(R.id.tvWeight)
        tvGoalWeightInfo = findViewById(R.id.tvGoalWeightInfo)
        tvActivity = findViewById(R.id.tvActivity)
        tvAllergy = findViewById(R.id.tvAllergy)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadUserData()

        btnAddWeight.setOnClickListener {
            showWeightInputDialog()
        }
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {

                    // --- SAFE FETCH ---
                    fun getSafeFloat(key: String): Float {
                        return when (val value = doc.get(key)) {
                            is Number -> value.toFloat()
                            is String -> value.toFloatOrNull() ?: 0f
                            else -> 0f
                        }
                    }

                    val name = doc.getString("name") ?: "N/A"
                    val age = (doc.getLong("age") ?: 0L).toInt()
                    val gender = doc.getString("gender") ?: "N/A"
                    val height = getSafeFloat("height").toInt()
                    val weight = getSafeFloat("weight")
                    val goalW = getSafeFloat("goalWeight")
                    val activity = doc.getString("activityLevel") ?: "N/A"
                    val allergy = doc.getString("allergy") ?: "N/A"

                    // Set UI
                    tvName.text = "Name: $name"
                    tvAge.text = "Age: $age"
                    tvGender.text = "Gender: $gender"
                    tvHeight.text = "Height: $height cm"
                    tvWeight.text = "Weight: ${weight.toInt()} kg"
                    tvGoalWeightInfo.text = "Goal Weight: ${goalW.toInt()} kg"
                    tvActivity.text = "Activity: $activity"
                    tvAllergy.text = "Allergy: $allergy"

                    // For progress
                    initialWeight = weight
                    goalWeight = goalW
                    tvInitialWeight.text = "Initial: ${initialWeight.toInt()} kg"
                    tvGoalWeightCircle.text = "Goal: ${goalWeight.toInt()} kg"

                    currentWeight = getSafeFloat("currentWeight")
                    if (currentWeight > 0f) {
                        updateUI()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI() {
        if (currentWeight == 0f || goalWeight == 0f || initialWeight == 0f) return

        // --- Calculate progress properly ---
        val totalDiff = initialWeight - goalWeight
        val currentDiff = initialWeight - currentWeight

        val progress = if (totalDiff != 0f) {
            ((currentDiff / totalDiff) * 100).toInt()
        } else 0

        arcProgress.progress = progress.coerceIn(0, 100)

        // Weight lost/gained text
        val weightDiff = currentWeight - initialWeight
        tvWeightLost.text = when {
            weightDiff < 0 -> String.format("%.1f kg lost", -weightDiff)
            weightDiff > 0 -> String.format("%.1f kg gained", weightDiff)
            else -> "No change"
        }

        tvCurrentWeight.text = "Current: ${currentWeight.toInt()} kg"
    }

    private fun showWeightInputDialog() {
        val editText = EditText(this)
        editText.hint = "Enter current weight"

        AlertDialog.Builder(this)
            .setTitle("Update Current Weight")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newWeight = editText.text.toString().toFloatOrNull()
                if (newWeight != null) {
                    currentWeight = newWeight
                    saveWeightToFirestore(newWeight)
                    updateUI()
                } else {
                    Toast.makeText(this, "Invalid weight", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveWeightToFirestore(newWeight: Float) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid)
            .update("currentWeight", newWeight)
            .addOnSuccessListener {
                Toast.makeText(this, "Weight updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update weight", Toast.LENGTH_SHORT).show()
            }
    }
}
