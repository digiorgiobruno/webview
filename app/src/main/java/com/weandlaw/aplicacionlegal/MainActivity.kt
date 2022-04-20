package com.weandlaw.aplicacionlegal
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.weandlaw.aplicacionlegal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val CHANNEL_ID="ChannelID"
    private lateinit var binding: ActivityMainBinding
    private val URL = "https://weandlaw.com/mi-cuenta/"
    private val URLoriginal=URL
    private var uploadMessage: ValueCallback<Uri>? = null
    private var uploadMessageAboveL: ValueCallback<Array<Uri>>? = null

    fun guardarPreferencia(key: String,value: String) {
        val preferences: SharedPreferences = applicationContext.getSharedPreferences(
            "DatosUser", Context.MODE_PRIVATE)

        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()


        val valor = preferences.getString(key, "Sin nombre")

        Log.d("Preferencia",  "Guardado en preferencia: %s".format(valor))

    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {


        setTheme(R.style.Theme_Webview)
        super.onCreate(savedInstanceState)

        this.binding =  ActivityMainBinding.inflate(layoutInflater)
        val view= binding.root
        setContentView(view)
        //createNotificationChannel()

        val settings = binding.webView.settings
        settings.javaScriptEnabled = true
        settings.builtInZoomControls = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true


        //binding.webView.addJavascriptInterface(WebAppInterface(this,URL), "Android")

        binding.webView.loadUrl(URL)

        //refresh
        binding.swipeRefresh.setOnRefreshListener {binding.webView.reload() }

        //Webview

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                //searchView.setQuery(url, false)
                binding.swipeRefresh.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                binding.swipeRefresh.isRefreshing = false
            }

        }

        /*
        binding.webView.setWebChromeClient(object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                //Required functionality here
                return super.onJsAlert(view, url, message, result)
            }
        })*/




        //Descargas
        binding.webView.setDownloadListener({ url, userAgent, contentDisposition, mimeType, contentLength ->
            downloadByBrowser(url)
        })
        //cargas
        binding.webView.webChromeClient = object : WebChromeClient()  {

            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                //Required functionality here
                return super.onJsAlert(view, url, message, result)
            }


            // For Android < 3.0
            fun openFileChooser(valueCallback: ValueCallback<Uri>) {
                uploadMessage = valueCallback
                openImageChooserActivity()
            }

            // For Android  >= 3.0
            fun openFileChooser(valueCallback: ValueCallback<Uri>, acceptType: String) {
                uploadMessage = valueCallback
                openImageChooserActivity()
            }
            //For Android  >= 4.1
            fun openFileChooser(
                valueCallback: ValueCallback<Uri>,
                acceptType: String,
                capture: String
            ) {
                uploadMessage = valueCallback
                openImageChooserActivity()
            }

            // For Android >= 5.0
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: WebChromeClient.FileChooserParams
            ): Boolean {
                uploadMessageAboveL = filePathCallback
                openImageChooserActivity()
                return true
            }
        }


    }





    private fun downloadByBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }



    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //val name = getString(R.string.channel_name)
            //val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, "NotificacionesYam", importance).apply {
                description = "Canal de notificaciones de Yam Capital Humano"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    override fun onBackPressed() {


        //if(binding.webView.canGoBack()) {
            //Log.d("Mensaje",  "$URLoriginal es igual a:" +binding.webView.url)
            val urlaux=binding.webView.url.toString().trim()

            Log.d("Mensaje",  "$urlaux es igual a:" +URLoriginal.trim())
            if((URLoriginal.trim()+"/")==urlaux ){
                Log.d("Mensaje",  "entro")
                super.onBackPressed()
            }else{
                Log.d("Mensaje",  "entro recargar")
                binding.webView.loadUrl(URLoriginal)
            }
        //}
    }

    private fun openImageChooserActivity() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.addCategory(Intent.CATEGORY_OPENABLE)
        i.type = "image/*"
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return
            val result = if (data == null || resultCode != Activity.RESULT_OK) null else data.data
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data)
            } else if (uploadMessage != null) {
                uploadMessage!!.onReceiveValue(result)
                uploadMessage = null
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onActivityResultAboveL(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return
        var results: Array<Uri>? = null
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                val dataString = intent.dataString
                val clipData = intent.clipData
                if (clipData != null) {
                    results = Array(clipData.itemCount){
                            i -> clipData.getItemAt(i).uri
                    }
                }
                if (dataString != null)
                    results = arrayOf(Uri.parse(dataString))
            }
        }
        uploadMessageAboveL!!.onReceiveValue(results)
        uploadMessageAboveL = null
    }


    companion object {
        private val FILE_CHOOSER_RESULT_CODE = 10000
    }


}