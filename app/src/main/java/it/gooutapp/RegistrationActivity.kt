package it.gooutapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.login.*

class RegistrationActivity: AppCompatActivity()  {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
    }

    fun closeActivity(v: View) {
        var resultIntent = Intent()
        resultIntent.putExtra("result", 1)
        resultIntent.putExtra("result", 2)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}