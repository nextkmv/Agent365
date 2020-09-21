package ru.its_365.agent365.activitys

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import ru.its_365.agent365.R
import ru.its_365.agent365.tools.Const
import ru.its_365.agent365.tools.DialogHelper
import ru.its_365.agent365.tools.HttpHelper
import ru.its_365.agent365.tools.PreferenceStore
import java.io.IOException

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity(), View.OnClickListener, okhttp3.Callback {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        server.setText(PreferenceStore.getString(this,Const.SETTINGS_PROFILE_SERVER_NAME))
        name.setText(PreferenceStore.getString(this,Const.SETTINGS_PROFILE_USER_NAME))
        password.setText(PreferenceStore.getString(this,Const.SETTINGS_PROFILE_USER_PASSWORD))
        uid.setText(PreferenceStore.getString(this,Const.SETTINGS_PROFILE_USER_UID))
        login_sign_in_button.setOnClickListener(this)



    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)
        when(item?.itemId){
            android.R.id.home -> onBackPressed();
        }
        return true;
    }

    fun login(view:View){
        var _server:String = server.text.toString()
        val _userName:String = name.text.toString()
        val _userPwd:String = password.text.toString()
        val _userUid:String = uid.text.toString()

        if(_server.length == 0){
            DialogHelper.AlertDialog(this,"Внимание","Не указан адрес сервера")
            return
        }

        if(_userName.length == 0){
            DialogHelper.AlertDialog(this,"Внимание","Не указано имя пользователя")
            return
        }

        if(_userPwd.length == 0){
            DialogHelper.AlertDialog(this,"Внимание","Не указан пароль")
            return
        }

        if(_userUid.length == 0){
            DialogHelper.AlertDialog(this,"Внимание","Не указан UID")
            return
        }


        // Выполним попытку подключения к серверу 1С

        if(_server[_server.length-1].toString() == "/"){
            _server = _server.dropLast(1);
        }

        val profileUrl = "${_server}/hs/Agent365Service/Profile";

        PreferenceStore.setString(this,Const.SETTINGS_PROFILE_SERVER_NAME,_server)
        PreferenceStore.setString(this,Const.SETTINGS_PROFILE_USER_NAME,_userName)
        PreferenceStore.setString(this,Const.SETTINGS_PROFILE_USER_PASSWORD,_userPwd)
        PreferenceStore.setString(this,Const.SETTINGS_PROFILE_USER_UID,_userUid)

        HttpHelper.Get(profileUrl,_userName,_userPwd,_userUid,this)

    }

    fun saveProfile(jsonString: String){

        // Проверка ответа на ожидаемое значение
        if(jsonString.contains(Const.PROTOCOL_NAME) == false){
            this@LoginActivity.runOnUiThread(java.lang.Runnable {
                DialogHelper.AlertDialog(this,"Ошибка","Сервер вернул неизвестный ответ")
                return@Runnable
            })
        }


        val json = JSONObject(jsonString)

        val UserName: String? = json.getString("UserName")
        val CompanyName: String? = json.getString("CompanyName")
        val ProtocolVersion : Int = json.getInt(Const.PROTOCOL_NAME) as Int

        PreferenceStore.setString(this,Const.SETTINGS_PROFILE_AGENT_NAME,UserName as String)
        PreferenceStore.setString(this,Const.SETTINGS_PROFILE_COMPANY_NAME,CompanyName as String)
        PreferenceStore.setInt(this,Const.SETTINGS_PROTOCOL_VERSION,ProtocolVersion)
        PreferenceStore.setBoolean(this, Const.SETTINGS_PROFILE_LOGED_IN, true)
        this@LoginActivity.runOnUiThread(java.lang.Runnable {
            this.onBackPressed()
        })
    }

    override fun onResponse(call: Call?, response: Response?) {
        var title : String = ""
        var message : String = ""
        when(response?.code()){

            200 -> {
                saveProfile(response.body()?.string() as String)
                return
            }
            501 ->{
                title = "Ошибка"
                message = "Не верное имя пользователя или пароль"
            }
            else ->{
                title = "Внимание"
                message = "Неизвестный ответ сервера: ${response?.code()}"
            }

        }
        this@LoginActivity.runOnUiThread(java.lang.Runnable {
            DialogHelper.AlertDialog(this,title,message)
        })



    }

    override fun onFailure(call: Call?, e: IOException?) {
        this@LoginActivity.runOnUiThread(java.lang.Runnable {
            DialogHelper.AlertDialog(this, "Ошибка", e.toString())
        })
    }

    override fun onClick(view: View?) {
        when (view?.id){
            R.id.login_sign_in_button -> login(view)
        }
    }

}
