package it.gooutapp


import android.R.attr.password
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.registration.*


class RegistrationActivity: AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
    }

    fun closeActivity() {
        var resultIntent = Intent()
        val nickname = editTextNickname.text.toString()
        val password = editTextPassword.text.toString()
        resultIntent.putExtra("nick", nickname)
        resultIntent.putExtra("pwd", password)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

     fun checkRegistration(v: View)  {
        if(SetValidation()) {
            closeActivity()
        } else {
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
        }
    }

    fun  SetValidation(): Boolean {
        var isEmailValid = false
        var isPasswordValid = false
        var ok = false

        // Check for a valid email address.
        if (editTextEmail.text.toString().isEmpty()) {
            editTextEmail.setError(getString(R.string.email_error))
            isEmailValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.text.toString()).matches()) {
            editTextEmail.setError(resources.getString(R.string.error_invalid_email))
            isEmailValid = false
        } else {
            isEmailValid = true
        }

        // Check for a valid password.
        if (editTextPassword.text.toString().isEmpty()) {
            editTextPassword.setError(resources.getString(R.string.password_error))
            isPasswordValid = false
        } else if (editTextPassword.text.length < 6) {
            editTextPassword.setError(resources.getString(R.string.error_invalid_password))
            isPasswordValid = false
        } else {
            isPasswordValid = true
        }
        if (isEmailValid && isPasswordValid) {
            ok = true
        }
        return ok
    }
}