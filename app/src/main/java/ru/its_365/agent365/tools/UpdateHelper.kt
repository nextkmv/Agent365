


package ru.its_365.agent365.tools

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.widget.ProgressBar
import io.reactivex.Observable
import kotlinx.coroutines.*
import okhttp3.*
import ru.its_365.agent365.activitys.ProgressDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @return Result of request or throw exception
 */
suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Don't bother with resuming the continuation if it is already cancelled.
                if (continuation.isCancelled) return
                continuation.resumeWithException(e)
            }
        })

        continuation.invokeOnCancellation {
            if (continuation.isCancelled)
                try {
                    cancel()
                } catch (ex: Throwable) {
                    //Ignore cancel exception
                }
        }
    }
}


class UpdateHelper(){

  private val client: OkHttpClient = OkHttpClient.Builder()
          .readTimeout(10, TimeUnit.SECONDS)
          .connectTimeout(10, TimeUnit.SECONDS)
          .writeTimeout(10, TimeUnit.SECONDS)
          .build()


    private fun request(url: String): Request {
        return Request.Builder().url(url).build()
    }

    public suspend fun AvalibleUpdate() : Boolean
    {
        return client.newCall(request("http://dl.its-365.ru/agent/android/Version.txt")).await().isSuccessful
    }


    public suspend fun Get(url:String) : Response {
        return client.newCall(request(url)).await()
    }



    public fun getLatestVersion() : Observable<Version>{
        return Observable.create {
            val response = client.newCall(request("http://dl.its-365.ru/agent/android/Version.txt")).execute()
            val v: String = response.body()?.string() ?: ""
            val V = Version(v)
            it.onNext(V)
        }

    }


    public fun downLoadVersion(version: Version, progressBar : ProgressBar? = null, ctx:Activity? = null) : Observable<File>{
        return Observable.create {
            val v = version.toString().replace(".", "_")
            val response = client.newCall(request("http://dl.its-365.ru/agent/android/${v}.apk")).execute()

            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "agent365_${v}.apk");
            val input: InputStream? = response.body()?.byteStream()

            val max = 8000000;

            val fos = FileOutputStream(file)



            var cnt = 0
            var data = input?.read()
            while (data != -1) {
                if (data != null) {
                    fos.write(data)
                    data = input?.read()
                    cnt++
                    if (cnt % (100000) == 0) {
                        val p = cnt / 1000000f
                        if (progressBar!= null){
                            if(ctx != null){
                                ctx.runOnUiThread {
                                    progressBar.isIndeterminate = false
                                    progressBar.max = 1000000000
                                    progressBar.progress = cnt
                                }
                            }

                        }
//                        runOnUiThread {
//                            pd.setMessage("Загрузка обновления...${"%.2f".format(p)} Mb.")
//                        }
                    }
                }
            }

            it.onNext(file)
        }
    }


}