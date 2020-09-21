package ru.its_365.agent365.activitys

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about.*
import ru.its_365.agent365.R
import ru.its_365.agent365.exchange.Exchange
import ru.its_365.agent365.tools.Const
import ru.its_365.agent365.tools.LongTermOperation
import ru.its_365.agent365.tools.PreferenceStore
import ru.its_365.agent365.tools.ValueHelper

class Splash : AppCompatActivity() {


    private val stockCallBack = object : LongTermOperation{
        override fun onSuccess(message: String?, data: Any?) {
            runOnUiThread {
                Toast.makeText(this@Splash,"Остатки обновлены", Toast.LENGTH_SHORT).show()
                val logedIn = PreferenceStore.getBoolean(this@Splash, Const.SETTINGS_PROFILE_LOGED_IN)
                if(logedIn) {
                    this@Splash.updateHistory()
                }
            }

        }

        override fun onFail(e: Exception) {
            runOnUiThread {
                Toast.makeText(this@Splash,"Не удалось обновить остатки", Toast.LENGTH_SHORT).show()
            }

        }

        override fun setState(title: String) {

        }
    }

    private val historyCallBack = object : LongTermOperation{
        override fun onSuccess(message: String?, data: Any?) {
            runOnUiThread {
                Toast.makeText(this@Splash,"История продаж обновлена", Toast.LENGTH_SHORT).show()
            }

        }

        override fun onFail(e: Exception) {
            runOnUiThread {
                Toast.makeText(this@Splash,"Не удалось обновить историю продаж", Toast.LENGTH_SHORT).show()
            }

        }

        override fun setState(title: String) {

        }
    }



    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAY: Long = 3000 //3 seconds

    internal val mRunnable: Runnable = Runnable {
        if (!isFinishing) {

            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateStocks(){
        val exchange = Exchange(this, false, stockCallBack )
        exchange?.updateStock()
    }

    private fun updateHistory(){
        val exchange = Exchange(this, false, historyCallBack )
        exchange?.updateHistory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val logedIn = PreferenceStore.getBoolean(this, Const.SETTINGS_PROFILE_LOGED_IN)
        if(logedIn) {
            updateStocks()
        }



        txtVersion.setText(ValueHelper.getVersionName(this))
        mDelayHandler = Handler()

        //Navigate with delay
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAY)
    }

    public override fun onDestroy() {

        if (mDelayHandler != null) {
            mDelayHandler!!.removeCallbacks(mRunnable)
        }

        super.onDestroy()
    }
}
