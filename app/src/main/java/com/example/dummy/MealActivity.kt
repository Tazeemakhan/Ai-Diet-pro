package com.example.dummy

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dummy.models.Content
import com.example.dummy.models.GeminiRequest
import com.example.dummy.models.GeminiResponse
import com.example.dummy.models.Part
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etAge: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var etGoalWeight: EditText
    private lateinit var spinnerActivity: Spinner
    private lateinit var spinnerAllergy: Spinner
    private lateinit var btnGenerate: Button
    private lateinit var tvResult: TextView
    private lateinit var tvResultHeading: TextView  // ✅ Added

    private val apiKey = "AIzaSyCFR_F_j_MVO1a16BwNeWA2FBFblo0gDM8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mealplan)

        // Bind views
        etName = findViewById(R.id.etName)
        etAge = findViewById(R.id.etAge)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        etGoalWeight = findViewById(R.id.etGoalWeight)
        spinnerActivity = findViewById(R.id.spinnerActivityLevel)
        spinnerAllergy = findViewById(R.id.spinnerAllergy)
        btnGenerate = findViewById(R.id.btnGenerate)
        tvResult = findViewById(R.id.tvResult)
        tvResultHeading = findViewById(R.id.tvResultHeading)  // ✅ Bind heading TextView

        // Set up spinners
        spinnerActivity.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("Sedentary", "Active", "Very Active")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        spinnerAllergy.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf("None", "Milk", "Eggs", "Peanuts", "Wheat", "Soy", "Seafood", "Tree nuts")
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // Button click
        btnGenerate.setOnClickListener {
            val name = etName.text.toString()
            val ageStr = etAge.text.toString()
            val heightStr = etHeight.text.toString()
            val weightStr = etWeight.text.toString()
            val goalWeightStr = etGoalWeight.text.toString()

            val age = ageStr.toIntOrNull()
            if (age == null || age < 18) {
                Toast.makeText(this, "Age must be 18 or older", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (name.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty() || goalWeightStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val activityLevel = spinnerActivity.selectedItem.toString()
            val allergy = spinnerAllergy.selectedItem.toString()

            // Prepare prompt
            val prompt = """
                Create a concise South Asian gain weight diet plan for a $age-year-old male.

                - Current weight: ${weightStr} kg
                - Target weight: ${goalWeightStr} kg within 1 month
                - Daily calorie limit: 1500 kcal
                - Include only: Breakfast, Lunch, and Dinner (no snacks)
                - Use bullet points only
                - Mention estimated calories for each meal
                - Do NOT include any notes, explanations, or tips
                - Output should ONLY be the 3 meals and their contents
                , and only write for how many weeks follow this meal no extra information is written
            """.trimIndent()

            // Create request object
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(prompt))
                    )
                )
            )

            tvResultHeading.visibility = View.VISIBLE  // ✅ Show heading before loading
            tvResult.text = "⏳ Generating meal plan..."
            tvResult.visibility = View.VISIBLE

            // API call
            RetrofitClient.instance.generateContent(apiKey, request)
                .enqueue(object : Callback<GeminiResponse> {
                    override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                        if (response.isSuccessful) {
                            val responseText = response.body()
                                ?.candidates
                                ?.firstOrNull()
                                ?.content
                                ?.parts
                                ?.firstOrNull()
                                ?.text

                            tvResultHeading.visibility = View.VISIBLE  // ✅ Make sure it's visible
                            tvResult.text = responseText ?: "❌ No response from server."
                        } else {
                            tvResultHeading.visibility = View.VISIBLE
                            tvResult.text = "❌ Failed: ${response.code()}"
                        }
                    }

                    override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                        tvResultHeading.visibility = View.VISIBLE
                        tvResult.text = "❌ Error: ${t.message}"
                    }
                })
        }
    }
}
