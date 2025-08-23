package com.example.dummy

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.dummy.Utils.PUBLISHABLE_KEY
import com.example.dummy.models_stripe.PaymentIntentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.temporal.TemporalAmount
import com.stripe.android.PaymentConfiguration
import com.example.dummy.API.ApiUtilities


class PremiumActivity : AppCompatActivity() {

    lateinit var paymentSheet: PaymentSheet
    lateinit var customerid: String
    lateinit var ephemeralkey: String
    lateinit var clientSecret:String
    private var selectedPlanType: String = "monthly"
    private var amount: Int = 0

    private val apiInterface = ApiUtilities.getApiInterface()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        //paymnentconfiguration
        PaymentConfiguration.init(this, PUBLISHABLE_KEY)
        //initialize paymentsheet
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        getCustomerId()

        val btnMonthly = findViewById<Button>(R.id.btnMonthly)
        val btnYearly = findViewById<Button>(R.id.btnYearly)
        val btnClose = findViewById<ImageButton>(R.id.btnClose)

        btnMonthly.setOnClickListener {
            selectedPlanType = "monthly"
            if (::customerid.isInitialized && ::ephemeralkey.isInitialized) {
                paymentFlow()
            } else {
                Toast.makeText(this, "Please wait, loading...", Toast.LENGTH_SHORT).show()
            }
        }
        btnYearly.setOnClickListener {
            selectedPlanType = "yearly"
            if (::customerid.isInitialized && ::ephemeralkey.isInitialized) {
                paymentFlow()
            } else {
                Toast.makeText(this, "Please wait, loading...", Toast.LENGTH_SHORT).show()
            }
        }

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
    }
    private fun paymentFlow() {
        amount = if (selectedPlanType == "monthly") 50000 else 500000
        getPaymentIntent(customerid, ephemeralkey, amount)
    }

    private fun getCustomerId() {
        lifecycleScope.launch(Dispatchers.IO){
            val res = apiInterface.getCustomer()
            withContext(Dispatchers.Main){
                if(res.isSuccessful && res.body()!=null){
                    customerid= res.body()!!.id
                    getEphemeralkey(customerid)
                }
            }
        }
    }
    private fun getEphemeralkey(customerid: String) {
        lifecycleScope.launch(Dispatchers.IO){
            val res = apiInterface.getEphemeralkey(customerid)
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    ephemeralkey = res.body()!!.secret
                }
            }
        }
    }

    private fun getPaymentIntent(customerid:String , ephemeralkey:String , amount: Int){
        lifecycleScope.launch(Dispatchers.IO){
            val res = apiInterface.getPaymentIntent(customerid, amount)

            withContext(Dispatchers.Main){
                if(res.isSuccessful && res.body()!=null){
                    clientSecret = res.body()!!.client_secret
                    Toast.makeText(this@PremiumActivity ,"Proceed for payment",Toast.LENGTH_SHORT).show()

                    // ✅ Payment sheet ab yahan show karein
                    showPaymentSheet()
                }
            }
        }
    }
    private fun showPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(
                "Tazeema khan",
                PaymentSheet.CustomerConfiguration(
                    customerid, ephemeralkey
                )
            )
        )
    }
    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment Done", Toast.LENGTH_SHORT).show()
                upgradeUserToPremium(selectedPlanType)
            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
            }
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