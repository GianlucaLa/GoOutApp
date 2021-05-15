package it.gooutapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity: AppCompatActivity() {
    val REGISTRATION_ACTIVITY = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
    }

    fun openRegistration(view: View) {
        //Toast.makeText(this,  "registrazione", Toast.LENGTH_LONG).show()
        val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
        try {
            startActivityForResult(intent, REGISTRATION_ACTIVITY)
        } catch(e: NumberFormatException) {
            Toast.makeText(applicationContext, "si è verificato un errore", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((requestCode == REGISTRATION_ACTIVITY) and (resultCode == Activity.RESULT_OK)) {
            val returnValue = data?.getDoubleExtra("result", 0.0)
            //editText.setText("$returnValue")
        }
    }
}