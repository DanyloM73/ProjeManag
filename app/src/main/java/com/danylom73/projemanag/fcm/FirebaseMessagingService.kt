package com.danylom73.projemanag.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.danylom73.projemanag.MainActivity
import com.danylom73.projemanag.R
import com.danylom73.projemanag.utils.Constants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import androidx.core.content.edit
import com.danylom73.projemanag.SignInActivity
import com.danylom73.projemanag.firebase.FireStoreClass

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("REMOTE_MESSAGE", "FROM: ${message.from}")

        message.data.isNotEmpty().let {
            Log.d("REMOTE_MESSAGE", "Message data payload: ${message.data}")

            val title = message.data["title"]!!
            val message = message.data["body"]!!
            sendNotification(title, message)
        }

        message.notification?.let {
            Log.d("REMOTE_MESSAGE", "Message notification body: ${it.body}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("REMOTE_MESSAGE", "Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        val sharedPreferences = getSharedPreferences(
            Constants.PREFERENCES, MODE_PRIVATE
        )
        sharedPreferences.edit {
            putString(Constants.FCM_TOKEN, token)
        }
    }

    private fun sendNotification(title: String, message: String) {
        Log.d("REMOTE_MESSAGE", "$title, $message")
        val intent: Intent = if (FireStoreClass().getCurrentUserID().isNotEmpty()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, SignInActivity::class.java)
        }
        intent.flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TASK
                or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = resources.getString(R.string.defaultNotificationChannelId)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_stat_ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel ProjeManag title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }
}