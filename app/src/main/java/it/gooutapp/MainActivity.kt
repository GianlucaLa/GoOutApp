package it.gooutapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.ktx.Firebase
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var testo: String
    private lateinit var auth: FirebaseAuth
    val fs : FireStore = FireStore()
    val email = Firebase.auth.currentUser?.email.toString()
    lateinit var userData: DocumentSnapshot
    private val TAG = "MAIN_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        // controllo se utente è loggato (non nullo) e aggiorno l'interfaccia di conseguenza
        val currentUser = auth.currentUser
        if(currentUser == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }else {
            setContentView(R.layout.activity_main)
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            val navView: NavigationView = findViewById(R.id.nav_view)
            val navController = findNavController(R.id.nav_host_fragment)
            //di seguito il contenitore dei nav nel drawer
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_showGroup,
                    R.id.nav_newProposal,
                    R.id.nav_createGroup,
                    R.id.nav_groupManagement,
                    R.id.nav_settings
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        drawerTextViewEmail.text = email
        //setto nome e cognome nella textview del drawermenu
        fs.getUserData(email){document -> userData = document
            var name = userData.get("name")
            var surname = userData.get("surname")
            drawerTextViewUser.text = "$name $surname"
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun searchGroup(item: MenuItem){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.popup_layout, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextPopup)

        with(builder) {
            setTitle(R.string.search_group)
            setPositiveButton(R.string.ok) { dialog, which ->
                val testo = editText.text.toString()
            }
            setNegativeButton(R.string.cancel) { dialog, which ->
                //null operation
            }
            setView(dialogLayout)
            show()
        }
    }

    fun logout(item: MenuItem) {
        Firebase.auth.signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }
}