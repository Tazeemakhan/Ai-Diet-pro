package com.example.dummy

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class BasicInformationActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        db = FirebaseFirestore.getInstance()

        val etName = findViewById<EditText>(R.id.etName)
        val etAge = findViewById<EditText>(R.id.etage)
        val etGender = findViewById<AutoCompleteTextView>(R.id.etgender)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<AutoCompleteTextView>(R.id.etHeight)
        val etActivity = findViewById<AutoCompleteTextView>(R.id.etActivity)
        val etAllergy = findViewById<AutoCompleteTextView>(R.id.etAllergy)
        val etNextButton = findViewById<Button>(R.id.etnextButton)

        val genders = arrayOf("Male", "Female")
        val genderAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        etGender.setAdapter(genderAdapter)
        etGender.setOnClickListener { etGender.showDropDown() }

        // Heights in feet.inches decimal format as requested
        val heightOptions = arrayOf(
            "4.5", "4.6", "4.7",
            "5.0", "5.1", "5.2", "5.3", "5.4", "5.5", "5.6", "5.7",
            "6.0", "6.1", "6.2", "6.3", "6.4", "6.5", "6.6", "6.7"
        )
        val heightAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, heightOptions)
        etHeight.setAdapter(heightAdapter)
        etHeight.setOnClickListener { etHeight.showDropDown() }

        val activityOptions = arrayOf("Sedentary", "Moderate", "Very Active")
        val activityAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, activityOptions)
        etActivity.setAdapter(activityAdapter)
        etActivity.setOnClickListener { etActivity.showDropDown() }

        val allergyOptions = arrayOf(
            "Milk / Dairy", "Eggs", "Peanuts", "Tree Nuts (almonds, cashews etc.)",
            "Wheat/gluten", "Soybean", "Fish", "Corn", "None"
        )
        val allergyAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, allergyOptions)
        etAllergy.setAdapter(allergyAdapter)
        etAllergy.setOnClickListener { etAllergy.showDropDown() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etNextButton.setOnClickListener {
            val name = etName.text.toString().trim()
            val ageStr = etAge.text.toString().trim()
            val gender = etGender.text.toString().trim()
            val weightStr = etWeight.text.toString().trim()
            val height = etHeight.text.toString().trim()
            val activityLevel = etActivity.text.toString().trim()
            val allergy = etAllergy.text.toString().trim()

            // Name validation (no numbers or special chars allowed in name)
            if (name.isEmpty() || !name.matches("^[a-zA-Z\\s]+$".toRegex())) {
                Toast.makeText(this, "Please enter a valid name (only letters and spaces)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Age validation numeric and range
            val age = ageStr.toIntOrNull()
            if (age == null || age < 18 || age > 80) {
                Toast.makeText(this, "Please enter a valid age between 18 and 80", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gender validation
            if (gender.isEmpty() || !genders.contains(gender)) {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Weight validation numeric and positive
            val weight = weightStr.toDoubleOrNull()
            if (weight == null || weight <= 0) {
                Toast.makeText(this, "Please enter a valid weight (positive number)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Height validation (must be from dropdown options)
            if (height.isEmpty() || !heightOptions.contains(height)) {
                Toast.makeText(this, "Please select your height from the list", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Activity level validation
            if (activityLevel.isEmpty() || !activityOptions.contains(activityLevel)) {
                Toast.makeText(this, "Please select your activity level", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Allergy validation - optional if needed (can be none)
            if (allergy.isEmpty() || !allergyOptions.contains(allergy)) {
                Toast.makeText(this, "Please select your allergy from the list", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = hashMapOf(
                "name" to name,
                "age" to age,
                "gender" to gender,
                "weight" to weight,
                "height" to height,
                "activityLevel" to activityLevel,
                "allergy" to allergy
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data Saved Successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
