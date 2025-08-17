package com.example.dummy

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class HydrationActivity : AppCompatActivity() {

    private lateinit var tvGlassCount: TextView
    private lateinit var tvBoastMessage: TextView
    private lateinit var glassContainer: GridLayout
    private var currentGlass = 0
    private val maxGlass = 8
    private val glassViews = mutableListOf<ImageView>()

    private val incrementMessages = listOf(
        "Nice sip! 💧",
        "You're crushing it! 💪",
        "Hydration hero! 🦸",
        "Keep going! 🔥",
        "Water power activated! ⚡",
        "Sip by sip, you're glowing! ✨",
        "Cheers to health! 🥂",
        "Refreshing, isn't it? 😄"
    )

    private val decrementMessages = listOf(
        "Oops! Adjusted it. 👀",
        "Being mindful is good! 🤓",
        "Tracking properly! ✅",
        "Hydration in balance. ⚖️",
        "All good! Modify as needed 🛠️",
        "Less water? No worries! 🧘"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_hydration)

            tvGlassCount = findViewById(R.id.tvGlassCount)
            tvBoastMessage = findViewById(R.id.tvBoastMessage)
            glassContainer = findViewById(R.id.glassContainer)

            val btnIncrement: Button = findViewById(R.id.btnIncrement)
            val btnDecrement: Button = findViewById(R.id.btnDecrement)
            val btnNext: Button = findViewById(R.id.btnNext)
            val btnPrevious: Button = findViewById(R.id.btnPrevious)

            // Create 8 ImageViews for glasses
            for (i in 0 until maxGlass) {
                val imageView = ImageView(this)
                imageView.setImageResource(R.drawable.glass)
                imageView.layoutParams = LinearLayout.LayoutParams(120, 160)
                imageView.alpha = 0.3f // unfilled
                glassContainer.addView(imageView)
                glassViews.add(imageView)
            }

            updateUI()

            btnIncrement.setOnClickListener {
                if (currentGlass < maxGlass) {
                    currentGlass++
                    updateUI()
                    showBoastMessage(incrementMessages.random())
                } else {
                    showBoastMessage("You're fully hydrated! 🥤")
                }
            }

            btnDecrement.setOnClickListener {
                if (currentGlass > 0) {
                    currentGlass--
                    updateUI()
                    showBoastMessage(decrementMessages.random())
                } else {
                    showBoastMessage("Already at zero. Let's get started! 🔁")
                }
            }

            btnNext.setOnClickListener {
                Toast.makeText(this, "Next clicked", Toast.LENGTH_SHORT).show()
            }

            btnPrevious.setOnClickListener {
                finish()
            }

        } catch (e: Exception) {
            Log.e("HydrationActivity", "Error in onCreate: ${e.message}")
            Toast.makeText(this, "Error loading screen", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateUI() {
        tvGlassCount.text = "$currentGlass/$maxGlass Glasses"
        for (i in 0 until maxGlass) {
            glassViews[i].alpha = if (i < currentGlass) 1.0f else 0.3f
        }
    }

    private fun showBoastMessage(message: String) {
        tvBoastMessage.text = message
        tvBoastMessage.visibility = View.VISIBLE
    }
}
