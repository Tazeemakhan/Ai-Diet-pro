package com.example.dummy

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.dummy.models.Content
import com.example.dummy.models.GeminiRequest
import com.example.dummy.models.GeminiResponse
import com.example.dummy.models.Part
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvGender: TextView
    private lateinit var tvGoalWeight: TextView
    private lateinit var tvActivity: TextView
    private lateinit var tvAllergy: TextView
    private lateinit var btnGenerate: Button
    private lateinit var tvResult: TextView
    private lateinit var tvResultHeading: TextView

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val apiKey = "AIzaSyB6lpmZ_H0FB_y2ceJk0FBynEM_XzOR2vo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mealplan)

        // Bind views
        tvName = findViewById(R.id.tvName)
        tvAge = findViewById(R.id.tvAge)
        tvHeight = findViewById(R.id.tvHeight)
        tvWeight = findViewById(R.id.tvWeight)
        tvGender = findViewById(R.id.tvGender)
        tvGoalWeight = findViewById(R.id.tvGoalWeight)
        tvActivity = findViewById(R.id.tvActivity)
        tvAllergy = findViewById(R.id.tvAllergy)
        btnGenerate = findViewById(R.id.btnGenerate)
        tvResult = findViewById(R.id.tvResult)
        tvResultHeading = findViewById(R.id.tvResultHeading) // Bind heading TextView


        // Disable button until data is fetched
        btnGenerate.isEnabled = false

        fun getNumberAsString(doc: DocumentSnapshot, field: String): String {
            return try {
                val num = doc.getDouble(field)
                num?.toString() ?: doc.getString(field) ?: "N/A"
            } catch (e: Exception) {
                doc.getString(field) ?: "N/A"
            }
        }

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        tvName.text = "Name: ${doc.getString("name") ?: "N/A"}"

                        tvAge.text = "Age: ${getNumberAsString(doc, "age")}"
                        tvHeight.text = "Height: ${getNumberAsString(doc, "height")} cm"
                        tvWeight.text = "Weight: ${getNumberAsString(doc, "weight")} kg"
                        tvGender.text = "Gender: ${doc.getString("gender") ?: "N/A"}"
                        tvGoalWeight.text = "Goal Weight: ${getNumberAsString(doc, "goalWeight")} kg"

                        tvActivity.text = "Activity level: ${doc.getString("activityLevel") ?: "N/A"}"
                        tvAllergy.text = "Allergy: ${doc.getString("allergy") ?: "N/A"}"

                        btnGenerate.isEnabled = true
                    } else {
                        Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnGenerate.setOnClickListener {
            val name = tvName.text.toString().replace("Name: ", "")
            val ageStr = tvAge.text.toString().replace("Age: ", "")
            val heightStr = tvHeight.text.toString().replace("Height: ", "").replace(" cm", "")
            val weightStr = tvWeight.text.toString().replace("Weight: ", "").replace(" kg", "")
            val goalWeightStr = tvGoalWeight.text.toString().replace("Goal Weight: ", "").replace(" kg", "")
            val gender = tvGender.text.toString().replace("Gender: ", "")
            val activityLevel = tvActivity.text.toString().replace("Activity level: ", "")
            val allergy = tvAllergy.text.toString().replace("Allergy: ", "")


            // Convert age string safely to Int
            val age = ageStr.toDoubleOrNull()?.toInt()
            if (age == null || age < 18) {
                Toast.makeText(this, "Age must be 18 or older", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            // Prepare prompt
            val prompt = """
  
            # Mobile-Optimized Diet Plan Prompt - Clean Output Only

            You are a professional nutritionist. Create a South Asian diet plan (mainly for Pakistan) using these details:

            Age: $age, Gender: $gender, Current: $weightStr kg, Target: $goalWeightStr kg,
            Height: $heightStr cm, Activity: $activityLevel, Allergies: $allergy

            GOAL DETERMINATION:
            - If $goalWeightStr < $weightStr: Weight Loss (1200-1500 kcal)
            - If $goalWeightStr > $weightStr: Weight Gain (2000-2500 kcal)
            - If $goalWeightStr = $weightStr: Maintenance (1600-2000 kcal)
            - Seniors 65+: Reduce by 200 kcal
            - Sedentary: Use lower range, Active: Use higher range

            ALLERGY RULES - CRITICAL:
            - Milk/Dairy: Avoid milk, yogurt, paneer, ghee, butter, cream, raita
            - Peanuts: Avoid peanut oil, groundnut preparations
            - Nuts: Avoid all tree nuts, nut-based gravies
            - Gluten: Avoid wheat roti, naan, regular flour

            REQUIREMENTS:
            - ONE daily plan (repeat 7 days)
            - 3 meals: Breakfast (25-30%), Lunch (35-40%), Dinner (30-35%)
            - South Asian foods only
            - Include portion sizes
            - Realistic calorie estimates

            OUTPUT FORMAT - MOBILE FRIENDLY:
            Return ONLY the meal plan in this exact format with NO extra text, explanations, or notes:

            • Breakfast (XXX kcal)
            * Food item with portion (XX kcal)
            * Food item with portion (XX kcal)

            • Lunch (XXX kcal)
            * Food item with portion (XX kcal)
            * Food item with portion (XX kcal)

            • Dinner (XXX kcal)
            * Food item with portion (XX kcal)
            * Food item with portion (XX kcal)

            Provide ONLY the formatted meal plan above. No validation statements, no explanations, no safety notes.
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
                            tvResult.text = responseText ?: " No response from server."
                        } else {
                            tvResultHeading.visibility = View.VISIBLE
                            tvResult.text = "Failed: ${response.code()}"
                        }
                    }

                    override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                        tvResultHeading.visibility = View.VISIBLE
                        tvResult.text = "Error: ${t.message}"
                    }
                })
        }
    }
}
