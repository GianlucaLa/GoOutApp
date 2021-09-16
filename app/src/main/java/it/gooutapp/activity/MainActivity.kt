 package it.gooutapp.activity

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.MyDialog
import it.gooutapp.model.Proposal
import it.gooutapp.model.User
import it.gooutapp.service.NotificationService
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_new_proposal.*
import kotlinx.android.synthetic.main.nav_header_main.*
import java.util.*
import kotlin.collections.ArrayList

 class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var idCurrentGroup: String
    private lateinit var nameCurrentGroup: String
    private lateinit var prefs: SharedPreferences
    private lateinit var prefsEditor: SharedPreferences.Editor
    private var mLastClickTime: Long = 0
    private var thisUserData = User()
    private val fs = FireStore()
    private val curr_user_email = Firebase.auth.currentUser?.email.toString()
    private val TAG = "MAIN_ACTIVITY"
    private val CHANNEL_ID = "GoOutApp_Channel"

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        val currentUser = Firebase.auth.currentUser
        if(currentUser == null) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }else {
            FirebaseFirestore.getInstance().enableNetwork()
            //Carico il layout
            setContentView(R.layout.activity_main)
            val toolbar: Toolbar = findViewById(R.id.toolbar)
            setSupportActionBar(toolbar)
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            val navView: NavigationView = findViewById(R.id.nav_view)
            val navController = findNavController(R.id.nav_host_fragment)
            navController.addOnDestinationChangedListener {
                    _, destination, arguments ->
                when(destination.id) {
                    R.id.nav_group -> {
                        toolbar.title = arguments?.getString("groupName")
                        toolbar.setOnClickListener {
                            fs.getUserData(curr_user_email) { userData ->
                                val bundle = bundleOf(
                                    "groupName" to nameCurrentGroup,
                                    "groupId" to idCurrentGroup
                                )
                                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                                    return@getUserData;
                                }
                                mLastClickTime = SystemClock.elapsedRealtime()
                                findNavController(R.id.nav_host_fragment).navigate(R.id.action_nav_group_to_nav_member, bundle)
                            }
                        }
                        idCurrentGroup = arguments?.getString("groupId").toString()
                        nameCurrentGroup = arguments?.getString("groupName").toString()
                    }
                    R.id.nav_member -> {
                        toolbar.isClickable = false
                        toolbar.title = "${arguments?.getString("groupName").toString()}: ${resources.getString(R.string.members)}"
                    }
                    R.id.nav_home -> {
                        toolbar.isClickable = false
                    }
                    R.id.nav_chat -> {
                        toolbar.title = "Chat: ${arguments?.getString("proposalName")}"
                        toolbar.isClickable = false
                    }
                    R.id.nav_profile -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.settings, SettingsFragment())
                            .commit()
                        prefs = getDefaultSharedPreferences(this)
                        prefsEditor = prefs.edit()
                        prefsEditor.putString("appLanguage", Locale.getDefault().language)
                        prefsEditor.putString("name", thisUserData.name)
                        prefsEditor.putString("surname", thisUserData.surname)
                        prefsEditor.putString("nickname", thisUserData.nickname)
                        prefsEditor.putString("email", curr_user_email)
                        prefsEditor.apply()
                        prefs.registerOnSharedPreferenceChangeListener {sp , _ ->
                            Log.i(TAG, "PREF   ----> ${sp.all["appLanguage"]}")
                            val loc = Locale(sp.all["appLanguage"] as String)
                            Locale.setDefault(Locale.ITALIAN)
                            val config = Configuration()
                            config.setLocale(Locale.ITALIAN)
                            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
                        }
                    }
                }
            }
            //di seguito il contenitore dei nav nel drawer
            appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.nav_home,
                    R.id.nav_profile,
                    R.id.nav_history
                ), drawerLayout
            )
            fs.getUserData(curr_user_email){ userData ->
                thisUserData = userData
                drawerTextViewEmail.text = curr_user_email
                drawerTextViewUser.text = "${userData.name} ${userData.surname}"
            }
            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRestart() {
        Log.e(TAG, "onRestart")
        super.onRestart()
        FirebaseFirestore.getInstance().disableNetwork()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(CHANNEL_ID)
        FirebaseFirestore.getInstance().enableNetwork()
        val intent = Intent(this, NotificationService::class.java)
        stopService(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStop() {
        Log.e(TAG, "onStop")
        super.onStop()
        if(Firebase.auth.currentUser?.uid != null){
            val intent = Intent(this, NotificationService::class.java)
            startService(intent)
        }
    }

    fun joinGroup(item: MenuItem) {
        var title = resources.getString(R.string.join_group)
        var message = resources.getString(R.string.enter_group_code)
        MyDialog(title, message, this, layoutInflater, false) { groupId ->
            HomePB?.visibility = View.VISIBLE
            fs.addUserToGroup(groupId) { result ->
                when (result) {
                    "NM" -> {
                        Toast.makeText(applicationContext, R.string.user_successful_added_to_group, Toast.LENGTH_SHORT).show()
                        refreshHome()
                    }
                    "AM" -> {
                        Toast.makeText(applicationContext, R.string.user_is_already_member, Toast.LENGTH_SHORT).show()
                    }
                    "ERROR" -> {
                        Toast.makeText(applicationContext, R.string.error, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(applicationContext, R.string.invalid_group_code, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            fs.addReadGroupNotifications(groupId, curr_user_email, "join"){
                //HomePB?.visibility = View.INVISIBLE
            }
        }
    }

    fun logout(item: MenuItem) {
        Log.e(TAG, "Logout")
        Firebase.auth.signOut()
        FirebaseFirestore.getInstance().disableNetwork()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    fun refreshHome(){
        findNavController(R.id.nav_host_fragment)?.navigate(R.id.nav_home)
    }

    fun openNewProposal(item: MenuItem){
        val bundle = bundleOf(
            "groupId" to idCurrentGroup,
            "groupName" to nameCurrentGroup
            )
        findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_group_to_nav_newProposal, bundle)
    }

     @SuppressLint("UseCompatLoadingForDrawables")
     @RequiresApi(Build.VERSION_CODES.O)
     fun showNotificationPopup(item: MenuItem){
         //blocco per settare a read tutte le proposal non lette nel gruppo
         fs.getGroupProposalData(idCurrentGroup) { proposalListData ->
             var proposalToRead = ArrayList<Proposal>()
             var proposalToReadCanceled = ArrayList<Proposal>()
             var proposalToReadModified = ArrayList<Proposal>()
             for (proposal in proposalListData) {
                 if (proposal.read?.contains(curr_user_email) != true)
                     proposalToRead.add(proposal)
                 if (proposal.canceled == "canceled" && proposal.readCanceled?.contains(curr_user_email) != true)
                     proposalToReadCanceled.add(proposal)
                 if (proposal.modified == "modified" && proposal.readModified?.contains(curr_user_email) != true)
                     proposalToReadModified.add(proposal)
             }
             if (proposalToRead.size > 0)
                fs.setReadProposal(proposalToRead)
             if (proposalToReadCanceled.size > 0)
                fs.setReadCanceledProposal(proposalToReadCanceled)
             if (proposalToReadModified.size > 0)
                fs.setReadModifiedProposal(proposalToReadModified)
         }
         fs.getGroupPopupNotification(this, idCurrentGroup){ notificationList ->
             var items = notificationList.toTypedArray()
             if(notificationList.isEmpty()) {
                 notificationList.add(resources.getString(R.string.empty_notifications))
                 items = notificationList.toTypedArray()
             }
             MaterialAlertDialogBuilder(this)
                 .setTitle(resources.getString(R.string.notifications))
                 .setPositiveButton(R.string.ok){ _, _ ->
                     invalidateOptionsMenu()
                 }
                 .setItems(items){ _, _ ->
                     invalidateOptionsMenu()
                 }
                 .show()
         }
     }

    fun popupInvitationCode(item: MenuItem){
        val invitationCode = idCurrentGroup
        val title = resources.getString(R.string.group_invitation_code)
        MyDialog(title, invitationCode, this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

     override fun onDestroy() {
         Log.e(TAG, "onDestroy")
         super.onDestroy()
     }
}