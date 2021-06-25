package it.gooutapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Group
import it.gooutapp.models.myDialog
import kotlinx.android.synthetic.main.fragment_new_proposal.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.android.synthetic.main.recycle_view_row.view.*

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userData: DocumentSnapshot
    private val fs = FireStore()
    private val user_email = Firebase.auth.currentUser?.email.toString()
    private lateinit var editTextPlacePicker: EditText
    private lateinit var codeCurrentGroup: String
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
            navController.addOnDestinationChangedListener {
                    controller, destination, arguments ->
                when(destination.id) {
                    R.id.nav_group -> {
                        toolbar.title = arguments?.getString("groupName")
                        codeCurrentGroup = arguments?.getString("groupCode").toString()
                    }
                }
            }
            //di seguito il contenitore dei nav nel drawer
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home,
                    R.id.nav_userProfile,
                    R.id.nav_settings,
                    R.id.nav_history
                ), drawerLayout
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //setto dati utente in drawer laterale
        fs.getUserData(user_email){ document ->
            userData = document                                 //setto il documentSnapshot della classe con il valore returnato dal getUserData
            drawerTextViewEmail.text = user_email               //setto email nella textview del DrawerMenu
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

    fun joinGroup(item: MenuItem) {
        var title = resources.getString(R.string.join_group)
        var message = resources.getString(R.string.enter_group_code)
        myDialog(title, message, this, layoutInflater) { groupCode ->
            fs.addUserToGroup(user_email, groupCode) { result ->
                when (result) {
                    "NM" -> {
                        Toast.makeText(applicationContext, R.string.user_successful_added_to_group, Toast.LENGTH_SHORT).show()
                        refreshFragment()
                    }
                    "AM" -> {
                        Toast.makeText(applicationContext, R.string.user_is_already_member, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(applicationContext, R.string.invalid_group_code, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun logout(item: MenuItem) {
        Firebase.auth.signOut()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    private fun closeDrawer(){
        var mDrawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        mDrawerLayout.closeDrawers();
    }

    private fun refreshFragment(){
        findNavController(R.id.nav_host_fragment).navigate(R.id.nav_home)
    }

    fun openNewProposal(item: MenuItem){
        val bundle = bundleOf("groupCode" to codeCurrentGroup)
        findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_group_to_nav_newProposal, bundle)
    }
}