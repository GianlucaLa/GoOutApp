package it.gooutapp

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userData: DocumentSnapshot
    private var name = ""                                       //valorizza textview nel drawer menu
    private var surname = ""                                    //valorizza textview nel drawer menu
    private val fs = FireStore()
    private val user_email = Firebase.auth.currentUser?.email.toString()
    private val TAG = "MAIN_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser = Firebase.auth.currentUser                      //controllo se utente è loggato (non nullo) e aggiorno l'interfaccia di conseguenza
        if(currentUser == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }else {
            //Carico il layout
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
        menuInflater.inflate(R.menu.main, menu)
        //prendo da FireStore i dati dell'utente
        fs.getUserData(user_email){ document ->
            userData = document                                 //setto il documentSnapshot della classe con il valore returnato dal getUserData
            drawerTextViewEmail.text = user_email                    //setto email nella textview del DrawerMenu
            var name = userData.get("name")
            var surname = userData.get("surname")
            drawerTextViewUser.text = "$name $surname"          //setto nome e cognome nella textview del DrawerMenu
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun joinGroup(item: MenuItem){
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.edittext_join_group, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextJoinGroup)

        with(builder) {
            setTitle(R.string.join_group)
            setPositiveButton(R.string.ok) { dialog, which ->
                val groupId = editText.text.toString()
                if(!groupId.equals("")){
                    fs.addUserToGroup(user_email, groupId){result ->
                        if(result.equals("userAlreadyIn")){
                            Toast.makeText(applicationContext, R.string.user_is_already_member, Toast.LENGTH_SHORT).show()
                        }else if(result.equals("groupNotExists")){
                            Toast.makeText(applicationContext, R.string.group_not_exists, Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(applicationContext, R.string.user_successful_added_to_group, Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Toast.makeText(applicationContext, R.string.error_empty_value, Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton(R.string.cancel) { dialog, which ->
                //do nothing
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