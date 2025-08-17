package com.example.dummy

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class HomeActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var isFirstLaunch = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bmiText = findViewById<TextView>(R.id.bmiText)
        val mealCard = findViewById<LinearLayout>(R.id.mealCard)
        val hydrationCard = findViewById<LinearLayout>(R.id.hydrationCard)
        val calorieCard = findViewById<LinearLayout>(R.id.calorieCard)
        val progressCard = findViewById<LinearLayout>(R.id.progressCard)
        val diamondButton = findViewById<ImageButton>(R.id.diamondButton)

        // Show premium popup on first launch or login/signup (simulated here with isFirstLaunch)
        if (isFirstLaunch) {
            handler.postDelayed({
                showPremiumPopup()
                isFirstLaunch = false // Reset after first show
            }, 2000) // 2-second delay
        }

        diamondButton.setOnClickListener {
            val intent = Intent(this, PremiumActivity::class.java)
            intent.putExtra("showClose", false) // Full screen, no close button
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out) // No slide-up
        }

        val motivationalLines = listOf(
            "One step, one meal, one win at a time!",
            "Eat good, feel good, live better!",
            "Your health journey starts with one bite.",
            "Fuel your body, feed your dreams.",
            "Small meals. Big goals.",
            "Healthy today, stronger tomorrow.",
            "Discipline is the secret sauce!",
            "Stay consistent, not perfect!"
        )

        val randomLine = motivationalLines.random()
        val welcomeSubText = findViewById<TextView>(R.id.welcomeSubText)
        welcomeSubText.text = randomLine

        bmiText.setOnClickListener {
            startActivity(Intent(this, BmiActivity::class.java))
        }
        mealCard.setOnClickListener {
            startActivity(Intent(this, MealActivity::class.java))
        }
        hydrationCard.setOnClickListener {
            startActivity(Intent(this, HydrationActivity::class.java))
        }
        calorieCard.setOnClickListener {
            startActivity(Intent(this, CalorieActivity::class.java))
        }
        progressCard.setOnClickListener {
            startActivity(Intent(this, ProgressActivity::class.java))
        }
    }

    private fun showPremiumPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_premium, null)
        val builder = AlertDialog.Builder(this, R.style.PopupTheme)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = builder.create()
        dialog.show()

        // Get screen dimensions
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Set dialog window size dynamically (e.g., 80% width, 50% height as a base, adjustable)
        val layoutParams = dialog.window?.attributes
        layoutParams?.width = (screenWidth * 0.8).toInt() // 80% of screen width
        layoutParams?.height = (screenHeight * 0.5).toInt() // 50% of screen height
        dialog.window?.attributes = layoutParams

        // Apply custom slide-up animation
        val slideUpAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        dialogView.startAnimation(slideUpAnimation)

        // Find the close button and position it at top-right
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
        btnClose.visibility = View.VISIBLE
        val constraintLayout = dialogView as? ConstraintLayout
        if (constraintLayout != null) {
            val constraintSet = ConstraintSet()
            constraintSet.clone(constraintLayout)
            constraintSet.connect(btnClose.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 16)
            constraintSet.connect(btnClose.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 16)
            constraintSet.applyTo(constraintLayout)
        }
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        // Hide the yearly plan card
        val yearlyCard = dialogView.findViewById<LinearLayout>(R.id.yearlyCard)
        yearlyCard.visibility = View.GONE

        // Set click listener for the entire dialog to navigate to premium screen
        dialogView.setOnClickListener {
            val intent = Intent(this, PremiumActivity::class.java)
            intent.putExtra("showClose", false) // Full screen, no close button
            startActivity(intent)
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}