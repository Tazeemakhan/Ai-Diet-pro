package com.example.dummy

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BmiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bmi_screen)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val heightInput = findViewById<EditText>(R.id.heightInput)
        val weightInput = findViewById<EditText>(R.id.weightInput)
        val bmiValue = findViewById<TextView>(R.id.bmiValue)
        val bmiMessage = findViewById<TextView>(R.id.bmiMessage)
        val calculateBtn = findViewById<Button>(R.id.calculateBtn)
        val previousBtn = findViewById<Button>(R.id.previousBtn)
        previousBtn.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }


        calculateBtn.setOnClickListener {
            val heightFeetStr = heightInput.text.toString()
            val weightStr = weightInput.text.toString()

            if (heightFeetStr.isEmpty() || weightStr.isEmpty()) {
                Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val heightFeet = heightFeetStr.toFloat()
            val heightMeters = heightFeet * 0.3048f
            val weight = weightStr.toFloat()

            val bmi = weight / (heightMeters * heightMeters)
            val bmiRounded = String.format("%.1f", bmi)
            bmiValue.text = bmiRounded
            bmiMessage.text = getBMIStatus(bmi)
        }
    }

    private fun getBMIStatus(bmi: Float): String {
        return when {
            bmi < 18.5 -> "You are underweight."
            bmi in 18.5..24.9 -> "You have normal body weight."
            bmi in 25.0..29.9 -> "You are overweight."
            else -> "You are obese."
        }
    }
}

