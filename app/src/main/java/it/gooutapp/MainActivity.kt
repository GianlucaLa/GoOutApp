package it.gooutapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import it.gooutapp.fragments.showGroup.ShowGroupFragment
import it.gooutapp.models.myDialog
import kotlinx.android.synthetic.main.fragment_new_proposal.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var userData: DocumentSnapshot
    private var name = ""                                       //valorizza textview nel drawer menu
    private var surname = ""
    val AUTOCOMPLETE_REQUEST_CODE = 1
    //valorizza textview nel drawer menu
    private val fs = FireStore()
    private val sw = ShowGroupFragment()
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
            Log.e("PROVA DEBUG", "---------------------------------------------------------------")
        }
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun joinGroup(item: MenuItem) {
        closeDrawer()
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

    fun refreshFragment(){
        findNavController(R.id.nav_host_fragment).navigate(R.id.nav_showGroup)
    }

    fun startAutocompleteActivity(v: View) {
        Places.initialize(this, "AIzaSyBNPwGd6VZHLf7TToPGuI0ZmecATXvuWGY")
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            listOf(Place.Field.ID, Place.Field.NAME)
        ).setTypeFilter(TypeFilter.ESTABLISHMENT)
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                var place = data?.let { Autocomplete.getPlaceFromIntent(it) }
                Log.e("MAPS", "Place: ${place?.name}, ${place?.id}")
            } else if(resultCode == AutocompleteActivity.RESULT_ERROR) {
                var status = data?.let { Autocomplete.getStatusFromIntent(it) }
                Log.i("MAPS", "An error occurred: $status")
            }
        }
    }
}