package it.gooutapp.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.text.Html
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.activity.MainActivity
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Notification

class NotificationService : Service() {
    private val TAG = "Notification_Service"
    private val curr_user_email = Firebase.auth.currentUser?.email.toString()
    private val user_auth_id = Firebase.auth.currentUser?.uid.toString()
    private val CHANNEL_ID = "GoOutApp_Channel"
    private val fs = FireStore()
    private var notificationID = 12145

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        val messagesHistory = ArrayList<Notification>()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent,
            PendingIntent.FLAG_NO_CREATE
        )

        fs.getNotification(this){ messageList ->
            for(message in messageList){
                fs.setSendedNotification(message.proposalCreationDate.toString()) {
                    if(!messagesHistory.contains(message)){
                        var notificationObj = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(Html.fromHtml("<b>${message.groupName}</b>"))          //nome gruppo
                            .setContentText(message.message)                                        //messaggio notifica
                            .setContentIntent(contentIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                        with(NotificationManagerCompat.from(this)) {
                            // notificationId is a unique int for each notification that you must define
                            notificationID++
                            notify(notificationID, notificationObj.build())
                        }
                        //fine if
                        messagesHistory.add(message)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "GoOutAppChannel"
            val descriptionText = "channel_description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.e(TAG, "channel registered")
        }
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        super.onCreate()
        createNotificationChannel() //NotificationChannel
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
    }
}