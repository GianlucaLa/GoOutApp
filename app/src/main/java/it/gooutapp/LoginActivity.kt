package it.gooutapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.registration.*

class LoginActivity: AppCompatActivity() {
    val REGISTRATION_ACTIVITY = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
    }

    fun openRegistration(v: View) {
        //Toast.makeText(this,  "registrazione", Toast.LENGTH_LONG).show()
        val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
        try {
            startActivityForResult(intent, REGISTRATION_ACTIVITY)
        } catch(e: Exception) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "si è verificato un errore", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if((requestCode == REGISTRATION_ACTIVITY) and (resultCode == Activity.RESULT_OK)) {
            val returnNick = data?.getStringExtra("nick")
            val returnPwd = data?.getStringExtra("pwd")
            editTextName.setText("$returnNick")
            editTextPassword.setText("$returnPwd")
        }
    }
}