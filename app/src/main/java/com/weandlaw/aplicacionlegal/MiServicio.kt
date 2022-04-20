package com.weandlaw.aplicacionlegal

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.weandlaw.aplicacionlegal.databinding.ActivityMainBinding
import java.util.*


class MiServicio(): Service() {
    private val PREFERENCIAS = "DatosUser"
    //https://www.youtube.com/watch?v=REJ3pDLGTmA
    private lateinit var binding: ActivityMainBinding
    private val URL = "https://demo.yamcapitalhumano.com"  //"https://0016-201-190-251-163.ngrok.io/rrhh"

    /*val preferences: SharedPreferences = applicationContext.getSharedPreferences(
        PREFERENCIAS, Context.MODE_PRIVATE)
    private val BASE_URL= preferences.getString("URL", "Sin nombre")*/

    private val TAG: String ="Mi Servicio"
    init {
        Log.d(TAG,"Servicio corriendo")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate() {
        super.onCreate()

        //val settings = binding.webView.settings
        //settings.javaScriptEnabled = true
        //binding.webView.addJavascriptInterface(WebAppInterface(this), "Android")
        //binding.webView.loadUrl(BASE_URL)
        Log.d(TAG,"On create")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestYam(){
        val queue = Volley.newRequestQueue(this)

        val preferences: SharedPreferences = applicationContext.getSharedPreferences(
            PREFERENCIAS, Context.MODE_PRIVATE)

        val nombre = preferences.getString("key", "Sin nombre")

            val url = StringBuilder()
            url.append(URL)
            .append("/aplicacion.php?ai=rrhh||3732&tcm=popup&tm=1&key=")
            .append(nombre)

        Log.d(TAG,  "Response: %s".format(nombre))

        //val url = BASE_URL+ "aplicacion.php?ai=rrhh||3732&tcm=popup&tm=1&"+ nombre

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url.toString(), null,
            { response ->
                Log.d(TAG,  "Response: %s".format(response.toString()))
                if(response.getBoolean("notificacion")==true){
                responseNotification(response.getString("subject"), response.getString("body"), response.getString("idMailbox"))
                }
            },
            { error ->
                // TODO: Handle error
                Log.d(TAG,  "Response: Error")
            }
        )

        // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)


    }

    @SuppressLint("UnspecifiedImmutableFlag")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun responseNotification(subject: String, body: String, idMailbox: String){
        val mContext=applicationContext
        // Create an explicit intent for an Activity in your app
        val notifyIntent = Intent(mContext, MainActivity::class.java).apply {
            putExtra(Notification.EXTRA_NOTIFICATION_ID, idMailbox)
            var flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val notifyPendingIntent = PendingIntent.getActivity(
            mContext, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        var builder = NotificationCompat.Builder(mContext, "ChannelID")
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


    override fun onBind(p0: Intent?): IBinder?=null
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"servicio destruido" )
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val handler = Handler()
        val timer = Timer()

        val task: TimerTask = object : TimerTask() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                handler.post {
                    try {
                        requestYam()

                    } catch (e: Exception) {
                        Log.e("error", e.message!!)
                    }
                }
            }
        }

        timer.schedule(task, 0, 60000) //ejecutar en intervalo de 60 segundos.
        return START_STICKY
    }

}