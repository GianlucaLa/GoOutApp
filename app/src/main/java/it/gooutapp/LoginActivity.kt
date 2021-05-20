package it.gooutapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.registration.*

class LoginActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val TAG = "LOGIN_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }else {
            setContentView(R.layout.login)
        }
    }

    fun openRegistration(v: View) {
        val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
        try {
            startActivity(intent)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }

    fun login(email: String, password: String) {
        //eseguo autenticazione
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    //val user = auth.currentUser
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun credentialsCheck(view: View) {
        val email = editTextLEmail.text?.toString()
        val password = editTextLPassword.text?.toString()
        var isEmailValid = false
        var isPasswordValid = false


        // Check for a valid email address.
        if (email!!.isEmpty()) {
            editTextLEmail.setError(getString(R.string.email_error))
            isEmailValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextLEmail.setError(resources.getString(R.string.error_invalid_email))
            isEmailValid = false
        } else {
            isEmailValid = true
        }

        // Check for a valid password.
        if (password!!.isEmpty()) {
            editTextLPassword.setError(resources.getString(R.string.password_error))
            isPasswordValid = false
        } else if (password.length < 6) {
            editTextLPassword.setError(resources.getString(R.string.error_invalid_password))
            isPasswordValid = false
        } else {
            isPasswordValid = true
        }

        //final check
        if (isEmailValid && isPasswordValid) {
            login(email, password)
        }
    }
}