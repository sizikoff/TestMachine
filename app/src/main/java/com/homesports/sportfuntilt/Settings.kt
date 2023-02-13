package com.homesports.sportfuntilt

import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.appsflyer.AppsFlyerLib
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import kotlinx.coroutines.*
import java.lang.Runnable

class Settings: Application() {
    val SIGNAL_ID: String = "76ac182a-facf-4c3e-bcc5-0468c13d9d00"
    private val APPSFLYER_ID: String = "jHBrDoMTUSR7PJ4VLfTz7c"
    lateinit var preferences: Preferences

    companion object {
        lateinit var app: Settings
        fun it(): Settings {
            return app
        }
    }

    override fun onCreate() {
        super.onCreate()
        preferences = Preferences.create(this)
        app = this

        OneSignal.initWithContext(this)
        OneSignal.setAppId(SIGNAL_ID)
        OneSignal.promptForPushNotifications()

        appsflyerInit()
    }

    fun dopParametrAppsFlyer():String{
        val appsFlyerid = AppsFlyerLib.getInstance().getAppsFlyerUID(this)
        return appsFlyerid!!
    }


    fun dopParametrCompanyName():String{
        val company_name = applicationContext.packageName
        return company_name!!
    }


    fun getADVid() {
        val a = GlobalScope.launch {
            val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext())
            val adId = adInfo?.id
            if (adId != null) {
                Log.e("text", adId)
            }
        }
    }
    val result = runBlocking() {

    }


    fun appsflyerInit() {
        println("Appsflyer init")
        AppsFlyerLib.getInstance().init(APPSFLYER_ID, null, this)
        AppsFlyerLib.getInstance().start(this)

            AppsFlyerLib.getInstance().subscribeForDeepLink {

                val deepLink = it.deepLink.deepLinkValue
                val campaign = it.deepLink.campaign
                val campaignId = it.deepLink.campaignId


                println("deeplink ${it.deepLink}")

                //на случай если именем является campaign
                if (!campaign.isNullOrEmpty()) {
                    if (campaign.contains("link.")) {
                        val i = campaign.substringAfterLast("link.")
                        preferences.storeDeeplink("?link=$i")
                    }
                }
                //на случай если именем является id
                if (!campaignId.isNullOrEmpty()) {
                    if (campaignId.contains("link.")) {
                        val i = campaignId.substringAfterLast("link.")
                        preferences.storeDeeplink("?link=$i")
                    }
                }

                //deeplink в приоритете
                if (!deepLink.isNullOrEmpty()) {
                    if (deepLink.contains("link/")) {
                        val i = deepLink.substringAfterLast("link/")
                        preferences.storeDeeplink(i)
                    }
                }
            }
        }
}