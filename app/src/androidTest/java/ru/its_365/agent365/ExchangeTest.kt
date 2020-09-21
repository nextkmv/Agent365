package ru.its_365.agent365

import android.app.Activity
import android.app.Instrumentation
import android.support.test.InstrumentationRegistry
import android.support.v7.app.AppCompatActivity
import android.test.InstrumentationTestCase
import org.junit.Assert
import org.junit.Test
import ru.its_365.agent365.activitys.SettingsActivity
import ru.its_365.agent365.exchange.Exchange
import ru.its_365.agent365.tools.LongTermOperation
import java.util.concurrent.TimeUnit

class ExchangeTest   {
    var getProfile : Boolean = false

    @Test(timeout = 1000 * 15)
    fun getProfile() {
        println("getProfile - begin")
        val e : Exchange = Exchange(InstrumentationRegistry.getTargetContext(),true, object:LongTermOperation {
            override fun setState(title: String) {
                println(title)
            }

            override fun onSuccess(message: String?, data: Any?) {
                val dt : String = data as String
                if(dt.length == 0){
                    Assert.fail()
                }
            }

            override fun onFail(e: Exception) {
                Assert.fail()
            }
        })
        try {
            e.getProfile()
        }catch (e : Exception){
            Assert.fail(e.toString())
        }
        Thread.sleep(2000)
        println("getProfile - end")
    }


    @Test(timeout = 1000 * 15)
    fun fullOad() {
        val e : Exchange = Exchange(InstrumentationRegistry.getTargetContext(),true, object:LongTermOperation {
            override fun setState(title: String) {
                println(title)
            }

            override fun onSuccess(message: String?, data: Any?) {
                val dt : String = data as String
                if(dt.length == 0){
                    Assert.fail()
                }
            }

            override fun onFail(e: Exception) {
                Assert.fail()
            }
        })
        try {
            e.fullLoad()
        }catch (e : Exception){
            Assert.fail(e.toString())
        }
        Thread.sleep(5000)
    }

}