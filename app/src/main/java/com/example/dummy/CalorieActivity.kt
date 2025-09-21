package com.example.dummy

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CalorieActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var resultTextView: TextView
    private lateinit var btnCalculate: Button

    private var userName: String = ""
    private var userAge: Long = 0
    private var userGender: String = ""
    private var userHeight: String = ""
    private var userWeight: Double = 0.0
    private var userActivity: String = ""
    private var userAllergy: String = ""
    private var userGoalWeight: Double = 0.0   // ✅ NEW FIELD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calorie)

        resultTextView = findViewById(R.id.resultTextView)
        btnCalculate = findViewById(R.id.btnCalculate)

        firestore = FirebaseFirestore.getInstance()

        // Load user data from Firestore
        loadUserData()

        // Button click -> calculate & save calories
        btnCalculate.setOnClickListener {
            calculateCalories()
        }
    }

    private fun loadUserData() {
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userName = document.getString("name") ?: "Unknown"
                        userAge = document.getLong("age") ?: 0
                        userGender = document.getString("gender") ?: "Unknown"
                        userHeight = document.getString("height") ?: "0"
                        userWeight = document.getDouble("weight") ?: 0.0
                        userActivity = document.getString("activityLevel") ?: "sedentary"
                        userAllergy = document.getString("allergy") ?: "None"
                        userGoalWeight = document.getDouble("goalWeight") ?: 0.0   // ✅ FETCH GOAL WEIGHT

                        // Show user info
                        findViewById<TextView>(R.id.tvName).text = "Name: $userName"
                        findViewById<TextView>(R.id.tvAge).text = "Age: $userAge"
                        findViewById<TextView>(R.id.tvGender).text = "Gender: $userGender"
                        findViewById<TextView>(R.id.tvHeight).text = "Height: $userHeight"
                        findViewById<TextView>(R.id.tvWeight).text = "Weight: $userWeight kg"
                        findViewById<TextView>(R.id.tvActivity).text = "Activity: $userActivity"
                        findViewById<TextView>(R.id.tvAllergy).text = "Allergy: $userAllergy"
                        findViewById<TextView>(R.id.tvGoalWeight).text = "Goal Weight: $userGoalWeight kg"
                        // ✅ DISPLAY GOAL WEIGHT

                        btnCalculate.isEnabled = true
                    } else {
                        Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun calculateCalories() {
        val heightInCm = convertHeightToCm(userHeight)

        // Calculate BMR
        val bmr: Double = if (userGender.equals("female", ignoreCase = true)) {
            655 + (9.6 * userWeight) + (1.8 * heightInCm) - (4.7 * userAge)
        } else {
            66 + (13.7 * userWeight) + (5 * heightInCm) - (6.8 * userAge)
        }

        // Activity factor
        val activityLevel = when (userActivity.trim().lowercase()) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "very active" -> 1.9
            else -> 1.2
        }

        // Final calories
        val calories = (bmr * activityLevel).toInt()
        resultTextView.text = "You need approx $calories calories per day"

        // ✅ Save to Firestore
        val auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid

        if (uid != null) {
            val userRef = firestore.collection("users").document(uid)

            val updates = mapOf(
                "calories" to calories,
                "currentWeight" to userWeight,
                "goalWeight" to userGoalWeight   // ✅ FIRESTORE ME SAVE
            )

            userRef.update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Saved ✅", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Convert height string into cm
    private fun convertHeightToCm(height: String): Double {
        return try {
            when {
                height.contains("cm") -> {
                    height.replace("cm", "").trim().toDouble()
                }
                height.contains("'") -> {
                    val parts = height.split("'")
                    val feet = parts[0].toDouble()
                    val inches = parts[1].replace("\"", "").trim().toDouble()
                    (feet * 30.48) + (inches * 2.54)
                }
                else -> height.toDouble()
            }
        } catch (e: Exception) {
            170.0 // default
        }
    }
}
