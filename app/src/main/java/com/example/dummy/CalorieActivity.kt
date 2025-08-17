package com.example.dummy

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calorie)

        resultTextView = findViewById(R.id.resultTextView)
        btnCalculate = findViewById(R.id.btnCalculate)

        firestore = FirebaseFirestore.getInstance()

        loadUserData()

        btnCalculate.setOnClickListener {
            calculateCalories()
        }

        findViewById<Button>(R.id.btnBackHome).setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val user = documents.last()  // Latest added user

                    userName = user.getString("name") ?: "Unknown"
                    userAge = user.getLong("age") ?: 0
                    userGender = user.getString("gender") ?: "Unknown"
                    userHeight = user.getString("height") ?: "0"
                    userWeight = user.getDouble("weight") ?: 0.0
                    userActivity = user.getString("activityLevel") ?: "sedentary"
                    userAllergy = user.getString("allergy") ?: "None"

                    findViewById<TextView>(R.id.tvName).text = "Name: $userName"
                    findViewById<TextView>(R.id.tvAge).text = "Age: $userAge"
                    findViewById<TextView>(R.id.tvGender).text = "Gender: $userGender"
                    findViewById<TextView>(R.id.tvHeight).text = "Height: $userHeight"
                    findViewById<TextView>(R.id.tvWeight).text = "Weight: $userWeight kg"
                    findViewById<TextView>(R.id.tvActivity).text = "Activity level: $userActivity"
                    findViewById<TextView>(R.id.tvAllergy).text = "Allergy: $userAllergy"

                    btnCalculate.isEnabled = true
                } else {
                    Toast.makeText(this, "No user data found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateCalories() {
        val heightInCm = convertHeightToCm(userHeight)

        val bmr = if (userGender.equals("female", ignoreCase = true)) {
            655 + (9.6 * userWeight) + (1.8 * heightInCm) - (4.7 * userAge)
        } else {
            66 + (13.7 * userWeight) + (5 * heightInCm) - (6.8 * userAge)
        }

        val activityLevel = when (userActivity.trim().lowercase()) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "very active" -> 1.9
            else -> 1.2
        }

        val calories = (bmr * activityLevel).toInt()
        resultTextView.text = "You need approx $calories calories per day"
    }

    private fun convertHeightToCm(height: String): Double {
        return try {
            if (height.contains("cm")) {
                height.replace("cm", "").trim().toDouble()
            } else if (height.contains("'")) {
                val parts = height.split("'")
                val feet = parts[0].toDouble()
                val inches = parts[1].replace("\"", "").trim().toDouble()
                (feet * 30.48) + (inches * 2.54)
            } else {
                height.toDouble()
            }
        } catch (e: Exception) {
            170.0
        }
    }
}
