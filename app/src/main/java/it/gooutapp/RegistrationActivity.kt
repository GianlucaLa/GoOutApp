package it.gooutapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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
}