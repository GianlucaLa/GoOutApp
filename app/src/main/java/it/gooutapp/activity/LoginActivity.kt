package it.gooutapp.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.registration.*

class LoginActivity: AppCompatActivity() {
    private val TAG = "LOGIN_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // controllo se utente non è loggato (nullo) e aggiorno l'interfaccia di conseguenza
        val currentUser = Firebase.auth.currentUser
        setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        if(currentUser != null) {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }else {
            setContentView(R.layout.login)
            editTextLPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    editTextLPasswordView.isErrorEnabled = false
                }
                override fun afterTextChanged(s: Editable?) {}
            })
            editTextLEmail.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    editTextLEmailView.isErrorEnabled = false
                }
                override fun afterTextChanged(s: Editable?) {}
            })
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
        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    //val user = auth.currentUser
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    hideProgressBar()
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, R.string.login_fail, Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun credentialsCheck(view: View) {
        showProgressBar()
        val email = editTextLEmail.text?.toString()
        val password = editTextLPassword.text?.toString()
        var isEmailValid = false
        var isPasswordValid = false

        // Check for a valid email address.
        when {
            email!!.isEmpty() -> {
                editTextLEmailView.error = resources.getString(R.string.email_empty_error)
                isEmailValid = false
            }
            email.length < 6 -> {
                editTextLEmailView.error = resources.getString(R.string.error_invalid_email)
                isEmailValid = false
            }
            else -> {
                isEmailValid = true
            }
        }

        // Check for a valid password.
        when {
            password!!.isEmpty() -> {
                editTextLPasswordView.error = resources.getString(R.string.password_empty_error)
                isPasswordValid = false
            }
            password.length < 6 -> {
                editTextLPasswordView.error = resources.getString(R.string.error_invalid_password)
                isPasswordValid = false
            }
            else -> {
                isPasswordValid = true
            }
        }

        //final check
        if (isEmailValid && isPasswordValid) {
            showProgressBar()
            login(email, password)
        }else{
            hideProgressBar()
        }
    }

    private fun showProgressBar() {
        Lpb.visibility = View.VISIBLE
        btnConfirmL.isEnabled = false
        editTextLEmailView.isEnabled = false
        editTextLPasswordView.isEnabled = false
        GOALogo.setColorFilter(resources.getColor(R.color.quantum_grey400))
        textViewRegister.setTextColor(resources.getColor(R.color.quantum_grey400))
        drawerTextViewEmail.setTextColor(resources.getColor(R.color.quantum_grey400))
    }

    private fun hideProgressBar() {
        Lpb.visibility = View.INVISIBLE
        btnConfirmL.isEnabled = true
        editTextLEmailView.isEnabled = true
        editTextLPasswordView.isEnabled = true
        GOALogo.colorFilter = null
        textViewRegister.setTextColor(resources.getColor(R.color.colorPrimary))
        drawerTextViewEmail.setTextColor(resources.getColor(R.color.quantum_grey600))
    }
}