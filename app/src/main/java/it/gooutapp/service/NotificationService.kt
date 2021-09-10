package it.gooutapp.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.text.Html
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Notification

class NotificationService : Service() {
    private var TAG = "Notification_Service"
    private val handler = Handler()
    private val CHANNEL_ID = "GoOutApp_Channel"
    private val curr_user_email = Firebase.auth.currentUser?.email.toString()
    private var user_auth_id = Firebase.auth.currentUser?.uid.toString()
    private val fs = FireStore()
    private var notification = 0

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val messagesHistory = ArrayList<Notification>()
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        handler.post(Runnable {
            fs.getNotification(this){ messageList ->
                Log.e("LISTA MESSAGGI", messagesHistory.toString())
                for(message in messageList){
                    fs.setSendedNotification(message.proposalCreationDate.toString()) {
                        if(!messagesHistory.contains(message)){
                            var builder = NotificationCompat.Builder(this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(Html.fromHtml("<b>${message.groupName}</b>"))    //nome gruppo
                                .setContentText(message.message)                                        //messaggio notifica
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                            with(NotificationManagerCompat.from(this)) {
                                // notificationId is a unique int for each notification that you must define
                                notification++
                                notify(notification, builder.build())
                            }
                            messagesHistory.add(message)
                        }
                    }
                }
            }
        })
        return START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        notification = 0
    }
}