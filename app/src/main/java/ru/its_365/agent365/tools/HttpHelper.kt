package ru.its_365.agent365.tools

import android.telephony.TelephonyManager
import okhttp3.*
import java.util.concurrent.TimeUnit
import okhttp3.RequestBody



class HttpHelper {
    companion object {
        fun Get(url:String, responseCallback:Callback,readTimeout : Long = 90, connectTimeout : Long = 90, writeTimeout : Long = 90){
            // should be a singleton
            val client = OkHttpClient.Builder()
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .connectTimeout(connectTimeout,TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout,TimeUnit.SECONDS)
                    .build()

            val request = Request.Builder()
                    .url(url)
                    .build()

            client.newCall(request).enqueue(responseCallback)
        }

        fun Get(url:String, user:String, pwd:String, uid:String, responseCallback:Callback,readTimeout : Long = 90, connectTimeout : Long = 90, writeTimeout : Long = 90){
            // should be a singleton
            val client = OkHttpClient.Builder()
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .connectTimeout(connectTimeout,TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout,TimeUnit.SECONDS)
                    .build()
            val credentials = okhttp3.Credentials.basic(user,pwd)
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .build()
            client.newCall(request).enqueue(responseCallback)
        }

        fun Get(url:String, user:String, pwd:String, uid:String, headerKey:String, headerValue:String, responseCallback:Callback,readTimeout : Long = 90, connectTimeout : Long = 90, writeTimeout : Long = 90){
            // should be a singleton
            val client = OkHttpClient.Builder()
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .connectTimeout(connectTimeout,TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout,TimeUnit.SECONDS)
                    .build()
            val credentials = okhttp3.Credentials.basic(user,pwd)
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .addHeader(headerKey,headerValue)
                    .url(url)
                    .build()

            client.newCall(request).enqueue(responseCallback)
        }

        fun Post(url:String, user:String, pwd:String, uid:String, data:String,mediaType: MediaType, responseCallback:Callback,readTimeout : Long = 90, connectTimeout : Long = 90, writeTimeout : Long = 90){
            //val JSON = MediaType.parse("application/json; charset=utf-8")
            val body = RequestBody.create(mediaType, data)
            // should be a singleton
            val client = OkHttpClient.Builder()
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .connectTimeout(connectTimeout,TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout,TimeUnit.SECONDS)
                    .build()
            val credentials = okhttp3.Credentials.basic(user,pwd)
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .post(body)
                    .build()

            client.newCall(request).enqueue(responseCallback)
        }

    }
}