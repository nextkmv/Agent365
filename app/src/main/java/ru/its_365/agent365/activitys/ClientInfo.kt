package ru.its_365.agent365.activitys

import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.MenuItem
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_client_info.*
import ru.its_365.agent365.R
import ru.its_365.agent365.db.model.Customer

class ClientInfo : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_info)
        val customerCode:String = intent.getStringExtra("CustomerCode")
        val realm = Realm.getDefaultInstance()
        val customer = realm.where<Customer>().equalTo("code",customerCode).findFirst()
        if(customer == null) onBackPressed()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(customer?.name)

        var info : String = """
Наименование: ${customer?.name};
ИНН: ${customer?.INN};
КПП: ${customer?.KPP};

Контакты:
""".trimMargin()

        customer?.contacts?.forEach {
            info+="""${it.value};
               |
           """.trimMargin()
        }

        info+="""
            Взаиморасчеты:

        """.trimIndent()

        customer?.contracts?.forEach {
            info+="""${it.name} - ${it.debt};
                |
            """.trimMargin()
        }




        clientInfoText.setText(info)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> onBackPressed();
        }
        return super.onOptionsItemSelected(item)
    }
}
