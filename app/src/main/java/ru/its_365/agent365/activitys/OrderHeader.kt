package ru.its_365.agent365.activitys

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_order_header.*
import ru.its_365.agent365.R
import ru.its_365.agent365.db.model.*
import ru.its_365.agent365.tools.Const
import ru.its_365.agent365.tools.DialogHelper
import java.text.SimpleDateFormat
import java.util.*

class OrderHeader : AppCompatActivity(), View.OnClickListener, DatePickerDialog.OnDateSetListener {


    private var organisation : Organisation? = null
    private var customer : Customer? = null
    private var deliveryAdress : String? = null
    private var deliveryDate : Date? = null
    private var comment : String? = null
    private var store:Store? = null
    private var priceType:PriceType? = null

    private val realm = Realm.getDefaultInstance()
    private val onOrganisation =
            object : OnSelectDialog {
                override fun refresh(realmResults: RealmResults<Any>?, searchString : String?): RealmResults<Any> {
                    return realm.where<Organisation>().findAllAsync() as RealmResults<Any>
                }

                override fun onClick(view: View?) {
                    if(view?.tag is SelectDialogAdapter.TagHolder){
                        organisation = (view?.tag as SelectDialogAdapter.TagHolder).realmObject as Organisation
                        updateTitles()
                    }
                }
            }
    private val onCustomer =
            object : OnSelectDialog {
                override fun refresh(realmResults: RealmResults<Any>?, searchString : String?): RealmResults<Any> {
                    if(searchString == null){
                        return realm.where<Customer>().findAll() as RealmResults<Any>
                    }else{
                        var realmResults: RealmResults<Customer>? = realm.where<Customer>().findAll()
                        val keywords = searchString.split(" ")
                        for (keyword in keywords) {
                            val upKeyword = keyword.toUpperCase()
                            realmResults = realmResults?.where()?.beginsWith("upperName", upKeyword)?.or()?.contains("upperName", upKeyword)?.findAllAsync()
                        }
                        return realmResults as RealmResults<Any>

                    }

                }

                override fun onClick(view: View?) {
                    if(view?.tag is SelectDialogAdapter.TagHolder){
                        customer = (view?.tag as SelectDialogAdapter.TagHolder).realmObject as Customer
                        updateTitles()
                    }
                }
            }
    private val onDeliveryAdress =
            object : OnSelectDialog {
                override fun refresh(realmResults: RealmResults<Any>?, searchString : String?): RealmResults<Any> {
                    if(customer != null){
                        val query = realm.where<ContactInformation>().equalTo("owners.code",customer?.code)
                        query.and()
                        query.beginGroup()
                        query.equalTo("type",ContactInformation.CONTACT_LEGAL)
                        query.or()
                        query.equalTo("type",ContactInformation.CONTACT_FACT)
                        query.or()
                        query.equalTo("type",ContactInformation.CONTACT_DELIVERY)
                        query.endGroup()
                        return query.findAllAsync() as RealmResults<Any>
                    }else{
                        return realm.where<ContactInformation>().equalTo("owners.code","").findAllAsync() as RealmResults<Any>
                    }
                }

                override fun onClick(view: View?) {
                    if(view?.tag is SelectDialogAdapter.TagHolder){
                        deliveryAdress = ((view?.tag as SelectDialogAdapter.TagHolder).realmObject as ContactInformation).value
                        updateTitles()
                    }
                }
            }
    private val onStore =
            object : OnSelectDialog {
                override fun refresh(realmResults: RealmResults<Any>?, searchString : String?): RealmResults<Any> {
                    return realm.where<Store>().findAllAsync() as RealmResults<Any>
                }

                override fun onClick(view: View?) {
                    if(view?.tag is SelectDialogAdapter.TagHolder){
                        store = (view?.tag as SelectDialogAdapter.TagHolder).realmObject as Store
                        updateTitles()
                    }
                }
            }

    private val onPriceType =
            object : OnSelectDialog {
                override fun refresh(realmResults: RealmResults<Any>?, searchString : String?): RealmResults<Any> {
                    return realm.where<PriceType>().findAllAsync() as RealmResults<Any>
                }

                override fun onClick(view: View?) {
                    if(view?.tag is SelectDialogAdapter.TagHolder){
                        priceType = (view?.tag as SelectDialogAdapter.TagHolder).realmObject as PriceType
                        updateTitles()
                    }
                }
            }

    private fun updateTitles(){
        if(organisation != null){
            editTextOrganisation.setText(organisation.toString())
            freeOrganisation.visibility = View.VISIBLE
        }else{
            editTextOrganisation.setText("")
            freeOrganisation.visibility = View.GONE
        }

        if(customer != null){
            editTextCustomer.setText(customer.toString())
            clientInfo.visibility = View.VISIBLE
            freeCustomer.visibility = View.VISIBLE
        }else{
            editTextCustomer.setText("")
            clientInfo.visibility = View.GONE
            freeCustomer.visibility = View.GONE
        }

        if(deliveryAdress != null){
            editTextDeliveryAdress.setText(deliveryAdress.toString())
            freeDeliveryAdress.visibility = View.VISIBLE
        }else{
            editTextDeliveryAdress.setText("")
            freeDeliveryAdress.visibility = View.GONE
        }

        if(deliveryDate != null){
            editTextDeliveryDate.setText(SimpleDateFormat("dd.MM.yy").format(deliveryDate))
        }else{
            editTextDeliveryDate.setText("")
        }

        if (store != null){
            editTextStore.setText(store?.name)
            freeStore.visibility = View.VISIBLE
        }else{
            editTextStore.setText("")
            freeStore.visibility = View.GONE
        }

        if (priceType != null){
            editTextPriceType.setText(priceType?.name)
            freePriceType.visibility = View.VISIBLE
        }else{
            editTextPriceType.setText("")
            freePriceType.visibility = View.GONE
        }

        editTextComment.setText(comment)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_header)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        editTextOrganisation.setOnClickListener(this)
        freeOrganisation.setOnClickListener(this)
        editTextCustomer.setOnClickListener(this)
        freeCustomer.setOnClickListener(this)
        editTextDeliveryAdress.setOnClickListener(this)
        freeDeliveryAdress.setOnClickListener(this)
        editTextDeliveryDate.setOnClickListener(this)
        freeStore.setOnClickListener(this)
        editTextStore.setOnClickListener(this)
        freePriceType.setOnClickListener(this)
        editTextPriceType.setOnClickListener(this)
        clientInfo.setOnClickListener(this)

        var code : String? = intent.getStringExtra("organisation.code")
        if(code != null){
            organisation = OrganisationModel().get("code", code) as Organisation?
        }

        code  = intent.getStringExtra("customer.code")
        if(code != null){
            customer = CustomerModel().get("code", code) as Customer?
        }

        deliveryAdress  = intent.getStringExtra("deliveryAddress")
        deliveryDate = intent.getSerializableExtra("deliveryDate") as Date
        comment = intent.getStringExtra("comment")



        val storeCode = intent.getStringExtra("store.code")
        if (storeCode != null){
            store = StoreModel().get("code",storeCode) as Store
        }
        val priceTypeCode = intent.getStringExtra("priceType.code")
        if (priceTypeCode != null){
            priceType = PriceTypeModel().get("code",priceTypeCode) as PriceType
        }


        updateTitles()

        supportActionBar?.title = "Новый заказ"
    }


    private fun onOrganisationClick(){
       val selectDialog = DialogHelper.getSelectDialog(this,"Организации", onOrganisation)
        selectDialog.show()
    }

    private fun onCustomerClick(){
        val allRealms  = realm.where<Customer>().findAll().toList()
        val list = mutableListOf<SearchebleSelectDialog.Item>()
        allRealms.forEach {
            list.add(SearchebleSelectDialog.Item(it.name,it.code))
        }
        SearchebleSelectDialog.dataList = list
        SearchebleSelectDialog.Show(this)
        SearchebleSelectDialog.Events.listen(OnSearchebleSelectDialogOnSelectItems::class.java).subscribe({
            customer = CustomerModel().get("code",it.id) as Customer
            deliveryAdress = null
            updateTitles()
        })

    }

    private fun onDeliveryAdressClick(){
        val selectDialog = DialogHelper.getSearchableSelectDialog(this,"Адреса", onDeliveryAdress)
        selectDialog.show()
    }

    private fun onDeliveryDateClick(){
        val selectDialog = DialogHelper.getDatePickerDialog(this,this,deliveryDate)
        selectDialog.show()
    }

    private fun onStoreClick(){
        val selectDialog = DialogHelper.getSelectDialog(this,"Склады", onStore)
        selectDialog.show()
    }

    private fun onPriceTypeClick(){
        val selectDialog = DialogHelper.getSelectDialog(this,"Типы цен", onPriceType)
        selectDialog.show()
    }

    private fun onClientInfoClick(){
        if(customer != null){
            val intent = Intent(this, ClientInfo::class.java).apply {
                putExtra("CustomerCode", customer?.code)
            }
            startActivity(intent)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.editTextOrganisation -> onOrganisationClick()
            R.id.freeOrganisation -> {organisation = null; updateTitles()}
            R.id.editTextCustomer -> onCustomerClick()
            R.id.freeCustomer -> {customer = null; updateTitles()}
            R.id.editTextDeliveryAdress-> onDeliveryAdressClick()
            R.id.freeDeliveryAdress -> {deliveryAdress = ""; updateTitles()}
            R.id.editTextDeliveryDate-> onDeliveryDateClick()
            R.id.freeStore -> {store = null; updateTitles()}
            R.id.editTextStore -> onStoreClick()
            R.id.freePriceType -> {priceType = null; updateTitles()}
            R.id.editTextPriceType -> onPriceTypeClick()
            R.id.clientInfo -> onClientInfoClick()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)
        when(item?.itemId){
            android.R.id.home -> {

                comment = editTextComment.text.toString();
                val intent = Intent()
                intent.putExtra("organisation.code",organisation?.code)
                intent.putExtra("customer.code",customer?.code)
                intent.putExtra("deliveryAddress",deliveryAdress ?:"")
                intent.putExtra("deliveryDate",deliveryDate)
                intent.putExtra("comment",comment)
                intent.putExtra("store.code",store?.code)
                intent.putExtra("priceType.code",priceType?.code)
                setResult(Activity.RESULT_OK,intent)
                finish()
                //onBackPressed()
            }
        }
        return true
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val clndr = Calendar.getInstance()
        clndr.set(year,monthOfYear,dayOfMonth)
        deliveryDate = clndr.time
        updateTitles()
    }



}
