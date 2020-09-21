package ru.its_365.agent365.activitys

import android.Manifest.permission.READ_CONTACTS
import android.app.AlertDialog
import android.app.Instrumentation
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import okhttp3.Call
import okhttp3.Response
import ru.its_365.agent365.R
import kotlinx.android.synthetic.main.activity_settings.*
import ru.its_365.agent365.db.model.Customer
import ru.its_365.agent365.exchange.Exchange
import ru.its_365.agent365.tools.*
import kotlinx.coroutines.*
import ru.its_365.agent365.BuildConfig
import kotlin.system.*
import okio.Okio
import okio.BufferedSink
import okio.BufferedSource
import okhttp3.ResponseBody
import java.io.*
import java.nio.file.Path
import android.Manifest
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import android.util.Log
import com.google.android.gms.common.internal.service.Common
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import io.realm.RealmConfiguration
import kotlinx.coroutines.selects.SelectClause1
import okhttp3.OkHttpClient
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.lang.Error
import java.util.*
import java.util.concurrent.*
import kotlin.concurrent.thread







class SettingsActivity : AppCompatActivity(), View.OnClickListener, LongTermOperation {
    val realm = Realm.getDefaultInstance()



    override fun setState(title: String) {
        runOnUiThread(Runnable({
            if (progressDialog?.isShowing() == true) {
                progressDialog?.setMessage(title)
            }
        }))
    }

    override fun onSuccess(message: String?, data: Any?) {
        runOnUiThread(Runnable({
            if (progressDialog?.isShowing() == true) {
                progressDialog?.dismiss()
                Toast.makeText(this, "Загрузка завершена!", Toast.LENGTH_LONG).show()
            }
        }))
    }

    override fun onFail(e: Exception) {
        runOnUiThread(Runnable({
            if (progressDialog?.isShowing() == true) {
                progressDialog?.dismiss()

                val title: String
                when (e) {
                    is java.net.SocketTimeoutException -> title = "Сервер не найден"
                    else -> title = e.toString()
                }
                DialogHelper.AlertDialog(this@SettingsActivity, "Что то пошло не так", title)
            }
        }))

    }


    var logedIn: Boolean = false
    var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        progressDialog = ProgressDialog(this)
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.title = "Настройки"
        cv_set_connection.setOnClickListener(this)
        cv_run_full_load.setOnClickListener(this)
        cv_log_out.setOnClickListener(this)
        cv_check_update.setOnClickListener(this)
        hardware_id.setText(HardWare.getHardWareId(this))
        refreshScreen()
    }


    fun setConnectionProperties(v: View) {
        val intent = Intent(this, LoginActivity::class.java);
        //startActivity(intent);
        startActivityForResult(intent, 1)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refreshScreen()
    }


    fun runFullLoad(v: View) {
        if (progressDialog?.isShowing() == true) {
            progressDialog?.dismiss()
        }
        progressDialog?.show()
        progressDialog?.hideCancelButton()
        progressDialog?.setMessage("Полная загрузка")

//        val ex = Exchange(this, false, this)
//        ex.fullLoad()
        val helper = ExchangeHelper()
        helper.checkProfile()
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.newThread())
                .filter {
                    if(it) {
                        return@filter true
                    }else{
                        throw Error("Неожиданный ответ сервера")
                    }
                }
                .flatMap {
                    setState("Удаление старых данных")
                    return@flatMap helper.deleteAllRealmData()
                }
                .flatMap {
                    setState("Загрузка организаций")
                    return@flatMap helper.updateOrganisation()
                }
                .flatMap {
                    setState("Загрузка покупателей")
                    return@flatMap helper.updateCustomer()
                }
                .flatMap {
                    setState("Загрузка договоров")
                    return@flatMap helper.updateContracts()
                }
                .flatMap {
                    setState("Загрузка товаров")
                    return@flatMap helper.updateGoods()
                }
                .flatMap {
                    setState("Загрузка цен")
                    return@flatMap helper.updatePrice()
                }
                .flatMap {
                    setState("Загрузка остатков")
                    return@flatMap helper.updateStock()
                }
                .flatMap {
                    setState("Загрузка истории продаж")
                    return@flatMap helper.updateHistory()
                }
                .subscribe (
                        {
                            runOnUiThread {
                                if (progressDialog?.isShowing() == true) {
                                    progressDialog?.dismiss()
                                }
                                Toast.makeText(this,"Загрузка успешно завершена!",Toast.LENGTH_LONG).show()
                                Realm.init(this)
                                val config = RealmConfiguration.Builder()
                                        .name("db.realm")
                                        .deleteRealmIfMigrationNeeded()
                                        .build();
                                Realm.setDefaultConfiguration(config)
                            }
                        },
                        {
                            runOnUiThread {
                                if (progressDialog?.isShowing() == true) {
                                    progressDialog?.dismiss()
                                }
                                Toast.makeText(this,"Ошибка: "+it.toString(),Toast.LENGTH_LONG).show()
                            }
                        }
                )




        //ex.getProfile()
    }

    fun refreshScreen() {
        logedIn = PreferenceStore.getBoolean(this, Const.SETTINGS_PROFILE_LOGED_IN)
        if (logedIn != true) {
            cv_log_out.visibility = CardView.GONE
            cv_set_connection.visibility = CardView.VISIBLE
            cv_run_full_load.visibility = CardView.GONE
        } else {
            cv_log_out.visibility = CardView.VISIBLE
            cv_set_connection.visibility = CardView.GONE
            cv_run_full_load.visibility = CardView.VISIBLE
        }

        user_profile_user_name.setText(PreferenceStore.getString(this, Const.SETTINGS_PROFILE_AGENT_NAME))
        user_profile_company_name.setText(PreferenceStore.getString(this, Const.SETTINGS_PROFILE_COMPANY_NAME))
    }

    fun logOut(v: View) {
        PreferenceStore.setString(this, Const.SETTINGS_PROFILE_AGENT_NAME, getString(R.string.user_name_placeholder))
        PreferenceStore.setString(this, Const.SETTINGS_PROFILE_COMPANY_NAME, getString(R.string.company_name_placeholder))
        PreferenceStore.setBoolean(this, Const.SETTINGS_PROFILE_LOGED_IN, false)
        refreshScreen()
    }


    suspend fun downloadFile(file: File, input: InputStream?) {
        FileOutputStream(file).use { fos ->
            fos.write(input?.readBytes())
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        }
    }


    fun downloadAndUpdate(version: String) = runBlocking {

        val pd = ProgressDialog(this@SettingsActivity)
        pd.show()
        pd.hideCancelButton()
        pd.setMessage("Загрузка обновления...")

        val v = version.replace(".", "_")
        val r = async { UpdateHelper().Get("http://dl.its-365.ru/agent/android/${v}.apk") }.await()
        if (!r.isSuccessful) return@runBlocking

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(path, "agent365_${v}.apk")


        val input: InputStream? = r.body()?.byteStream()



        try {

            val c = Observable.create<Unit> {



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
                            runOnUiThread {
                                pd.setMessage("Загрузка обновления...${"%.2f".format(p)} Mb.")
                            }
                        }
                    }
                }

                runOnUiThread { if (pd.isShowing == true) pd.dismiss() }
            }
                    .timeout(10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Unit> {
                        override fun onComplete() {

                        }

                        override fun onSubscribe(d: Disposable) {

                        }

                        override fun onNext(t: Unit) {

                        }

                        override fun onError(e: Throwable) {
                            runOnUiThread {
                                if (pd.isShowing == true) pd.dismiss()
                                Toast.makeText(this@SettingsActivity, "Превышен интервал ожидания", Toast.LENGTH_LONG).show()
                            }
                        }

                    })


        } catch (e: java.lang.Exception) {
            val ab = AlertDialog.Builder(this@SettingsActivity)
            ab.setTitle(title)
                    .setMessage("Не удалось загрузить обновление. ${e.toString()}")
                    .setCancelable(false)
                    .setNegativeButton("OK", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, id: Int) {
                            dialog?.cancel()
                        }
                    })
            val alertDialog = ab.create()
            alertDialog.show()
        }
    }


    fun checkPermAndUpdate(version: String) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this@SettingsActivity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@SettingsActivity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this@SettingsActivity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        0)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            downloadAndUpdate(version)
        }
    }


    fun checkUpdate() = runBlocking {

        val r = async { UpdateHelper().Get("http://dl.its-365.ru/agent/android/Version.txt") }.await()

        //val a = async {checkUpdateFromServer()}.await()

        val v: String = r.body()?.string() ?: ""

        val avalibleV = Version(v);
        val currentV = Version(BuildConfig.VERSION_NAME)

        val c = avalibleV.compareTo(currentV)

        // заменить на 1 в релизе
        if (c == -1) {
            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        dialog?.dismiss()
                        checkPermAndUpdate(v)
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                        dialog?.dismiss()
                    }
                }//Yes button clicked
                //No button clicked
            }

            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setMessage("Доступна новая версия программы.").setPositiveButton("Обновить", dialogClickListener)
                    .setNegativeButton("Не обновлять", dialogClickListener).show()
        }

    }


    fun checkUpdateRx() {

        // Нужно выполнить последовательность:
        // Запросить файл версий
        // Сравнить версию в файле с текущей, если версия больше предложить обновить
        // Загрузить новый файл установки
        // Уставновить новую версию


        // должна либо завершится либо вызвать ошибку либо прерватся
        val needUpdate = Completable.create {

        }

        val currentV = Version(BuildConfig.VERSION_NAME)

        val updateHelper = UpdateHelper()

        updateProgress.visibility = View.VISIBLE
        updateProgress.isIndeterminate = true

        var targetVersion : Version?  = null

        updateHelper.getLatestVersion()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter {
                    targetVersion = it;
                    if (it.compareTo(currentV) > 0) {
                        runOnUiThread { Toast.makeText(this@SettingsActivity, it.toString(), Toast.LENGTH_LONG).show() }
                        return@filter true

                    } else {
                        updateProgress.visibility = View.GONE
                        runOnUiThread { Toast.makeText(this@SettingsActivity, "Нет доступных обновлений", Toast.LENGTH_LONG).show() }
                        // заменить на false в релизе
                        return@filter false
                    }

                }.flatMap({ event ->
                      RxAlertDialog.show(this, R.string.alert_title, R.string.alert_message_update_avalible, android.R.string.yes,android.R.string.no)
                 }).filter({
                    if (it == RxAlertDialog.BUTTON_POSITIVE){
                        return@filter true
                    }else{
                        updateProgress.visibility = View.GONE
                        return@filter false
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).flatMap {
                    updateHelper.downLoadVersion(targetVersion ?: throw IllegalArgumentException(),updateProgress,this@SettingsActivity)
                }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateProgress.visibility = View.GONE
                    runOnUiThread { Toast.makeText(this@SettingsActivity, "Загружено успешно", Toast.LENGTH_LONG).show() }

                    var fileUri : Uri = Uri.fromFile(it)
                    if (Build.VERSION.SDK_INT >= 24) {
                        fileUri = FileProvider.getUriForFile(this, "agent365.provider",
                                it)

                    }


                    val intent = Intent(Intent.ACTION_VIEW, fileUri);
                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    intent.setDataAndType(fileUri, "application/vnd.android" + ".package-archive")
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or  Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    this.startActivity(intent)
                    this.finish()
                }
                        , {
                    updateProgress.visibility = View.GONE
                    runOnUiThread { Toast.makeText(this@SettingsActivity, it.toString(), Toast.LENGTH_LONG).show() }
                }
                        , {

                })


    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cv_set_connection -> setConnectionProperties(v)
            R.id.cv_run_full_load -> runFullLoad(v)
            R.id.cv_log_out -> logOut(v)
            R.id.cv_check_update -> checkUpdateRx()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)
        when (item?.itemId) {
            android.R.id.home -> onBackPressed();
        }
        return true
    }
}


