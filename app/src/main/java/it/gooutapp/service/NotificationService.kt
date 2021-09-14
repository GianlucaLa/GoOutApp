package it.gooutapp.service

import android.app.NotificationManager
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
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        val messagesHistory = ArrayList<Notification>()

        fs.getNotification(this){ messageList ->
            for(message in messageList){
                fs.setSendedNotification(message.proposalCreationDate.toString()) {
                    if(!messagesHistory.contains(message)){
                        var notificationObj = NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle(Html.fromHtml("<b>${message.groupName}</b>"))          //nome gruppo
                            .setContentText(message.message)                                        //messaggio notifica
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
        return START_NOT_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        super.onCreate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(CHANNEL_ID)
        super.onDestroy()
    }
}