package com.weandlaw.aplicacionlegal

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject


class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        Log.d("DATA PAYLOAD", "Ha llegado un msj ")
        if (remoteMessage.data.isNotEmpty()) {

            val json = JSONObject(remoteMessage.data.toString())
            val id=json.getString("idMailbox")

            //val id =remoteMessage.data.toString()
            Log.d("DATA PAYLOAD", "Message data payload: ${id}")
        generarNotificacion(
            remoteMessage.notification!!.body,
            remoteMessage.notification!!.title,
            id)


        }
    }


    private fun generarNotificacion(subject: String?, body: String?, idMailbox: String?) {

        /*val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val notifyPendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)*/

        val notifyIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(Notification.EXTRA_NOTIFICATION_ID, idMailbox)
            var flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val sonidoUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(applicationContext, "ChannelID")
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(subject)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)
            .setSound(sonidoUri)


        with(NotificationManagerCompat.from(applicationContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }

    }



}