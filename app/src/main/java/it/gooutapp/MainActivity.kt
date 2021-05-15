package it.gooutapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
    }

    fun checkLogin(view: View) {

    }

    fun registrationListener(view: View) {

        //Toast.makeText(this,  "registrazione", Toast.LENGTH_LONG).show()
    }
}