package it.gooutapp

import android.R
import android.R.attr.password
import android.app.Activity
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

    fun closeActivity(v: View) {
        var resultIntent = Intent()
        val nickname = editTextNickname.text.toString()
        val password = editTextPassword.text.toString()
        resultIntent.putExtra("nick", nickname)
        resultIntent.putExtra("pwd", password)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun SetValidation() {
        // Check for a valid email address.
        if (editTextEmail.getText().toString().isEmpty()) {
            editTextEmail.setError(resources.getString(R.string.email_error))
            isEmailValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString()).matches()) {
            editTextEmail.setError(resources.getString(R.string.error_invalid_email))
            isEmailValid = false
        } else {
            isEmailValid = true
        }

        // Check for a valid password.
        if (editTextPassword.getText().toString().isEmpty()) {
            editTextPassword.setError(resources.getString(R.string.password_error))
            isPasswordValid = false
        } else if (editTextPassword.getText().length() < 6) {
            editTextPassword.setError(resources.getString(R.string.error_invalid_password))
            isPasswordValid = false
        } else {
            isPasswordValid = true
        }
        if (isEmailValid && isPasswordValid) {
            Toast.makeText(applicationContext, "Successfully", Toast.LENGTH_SHORT).show()
        }
    }
}