package com.example.dummy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp

class PremiumActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var selectedPlan: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val btnMonthly = findViewById<Button>(R.id.btnMonthly)
        val btnYearly = findViewById<Button>(R.id.btnYearly)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        // Check if "showClose" is passed in intent
        val showClose = intent.getBooleanExtra("showClose", false)
        if (showClose) {
            btnClose.visibility = View.VISIBLE
            btnClose.setOnClickListener {
                finish()
            }
        } else {
            btnClose.visibility = View.GONE
        }

        btnMonthly.setOnClickListener {
            selectedPlan = "Monthly"
            upgradeUserToPremium(selectedPlan)
        }
        btnYearly.setOnClickListener {
            selectedPlan = "Yearly"
            upgradeUserToPremium(selectedPlan)
        }
    }

    private fun upgradeUserToPremium(planType: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        if (uid != null) {
            val subscriptionDate = Timestamp.now()
            val userUpdates = mapOf(
                "isPremium" to true,
                "planType" to planType,
                "subscriptionStartDate" to subscriptionDate
            )

            db.collection("users").document(uid)
                .set(userUpdates, SetOptions.merge())
                .addOnSuccessListener {
                    Toast.makeText(this, "Upgraded to $planType plan!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error updating plan", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
