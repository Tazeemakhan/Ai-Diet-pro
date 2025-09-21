package com.example.dummy

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.text.method.PasswordTransformationMethod
import android.text.method.HideReturnsTransformationMethod

class SignInActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var isPasswordVisible = false  // Track password visibility

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val signUpText = findViewById<TextView>(R.id.tv_sign_up)
        val loginButton = findViewById<Button>(R.id.btn_login)
        val editTextUsername = findViewById<EditText>(R.id.et_username)
        val editTextPassword = findViewById<EditText>(R.id.et_password)
        val toggleText = findViewById<TextView>(R.id.tv_show_hide_password) // Add this TextView in your XML

        firebaseAuth = FirebaseAuth.getInstance()

        // --- SHOW / HIDE PASSWORD ---
        toggleText.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                editTextPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleText.text = "Hide"
            } else {
                editTextPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleText.text = "Show"
            }
            editTextPassword.setSelection(editTextPassword.text.length) // keep cursor at end
        }
        // --- END SHOW / HIDE PASSWORD ---

        signUpText.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            val message = task.exception?.message ?: "Login Failed"
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
