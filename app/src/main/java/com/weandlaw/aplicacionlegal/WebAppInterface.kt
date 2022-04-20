package com.weandlaw.aplicacionlegal

import android.annotation.SuppressLint
import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging


/** Instantiate the interface and set the context  */
class WebAppInterface(private val mContext: Context, private val URL: String) {
    private val PREFERENCIAS = "DatosUser"
    //private val URL = "https://0016-201-190-251-163.ngrok.io/rrhh"

    /*val preferences: SharedPreferences = mContext.getSharedPreferences(
        PREFERENCIAS, Context.MODE_PRIVATE)
    private val BASE_URL= preferences.getString("URL", "Sin nombre")*/

    /** Show a toast from the web page  */
    @JavascriptInterface
    fun notificationYam(subject: String,body: String,key: String,idMailbox: String) {

        Toast.makeText(mContext, subject, Toast.LENGTH_SHORT).show()
        responseNotification(subject,body,idMailbox)

    }

    @JavascriptInterface
    fun guardarToken( idUser:String ){
        Log.d("GUARDAR TOKEN",  "Se ejecutó")

        setToken()
        val preferences: SharedPreferences = mContext.getSharedPreferences(
            PREFERENCIAS, Context.MODE_PRIVATE)
        val token = preferences.getString("token", "Token vacío")

        val url = "$URL/aplicacion.php?ai=rrhh||3733&tcm=popup&tm=1&token=$token&idUser=$idUser"

        Log.d("GUARDAR TOKEN DATOS","USUARIO ID :"+idUser+ " Token enviado"+token )
        Log.d("GUARDAR TOKEN URL",  url)
        //val url = BASE_URL+ "aplicacion.php?ai=rrhh||3732&tcm=popup&tm=1&"+ nombre

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url.toString(), null,
            { response ->
                Log.d("GUARDAR TOKEN",  response.toString())

                //responseNotification(response.getString("subject"), response.getString("body"), response.getString("idMailbox"))
            },
            { error ->
                // TODO: Handle error
                Log.d("Error",  "Response: %s".format(error.toString()))
            })
        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest)

    }


    private fun guardarPreferencia(key: String, value: String) {
        val preferences: SharedPreferences = mContext.getSharedPreferences(
            "DatosUser", Context.MODE_PRIVATE)

        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()


        val valor = preferences.getString(key, "Sin nombre")

        Log.d("Preferencia",  "Guardado en preferencia: %s".format(valor))

    }

    @JavascriptInterface
    fun guardarUsuario(key: String) {
        guardarPreferencia("key",key)
    }



    @SuppressLint("UnspecifiedImmutableFlag")
    private fun responseNotification(subject: String, body: String, idMailbox: String){


        Log.d("Mensaje",  "Id mensaje: %s".format(idMailbox))
        // Create an explicit intent for an Activity in your app
       val notifyIntent = Intent(mContext, MainActivity::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, idMailbox)
            var flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notifyPendingIntent = PendingIntent.getActivity(
            mContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        //val pendingIntent: PendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT0)

        val builder = NotificationCompat.Builder(mContext, "ChannelID")
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(subject)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(notifyPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(mContext)) {
            // notificationId is a unique int for each notification that you must define
            notify(1, builder.build())
        }

    }

    fun setToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener( OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("Error Token", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            // Get new FCM registration token
            val token = task.result
            Log.d("TOKEN", token.toString())
            //peticion
            guardarPreferencia("token",token.toString())
        })
    }

}