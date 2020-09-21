package ru.its_365.agent365.activitys

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.PublishSubject
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import ru.its_365.agent365.R

import kotlinx.android.synthetic.main.activity_order_detail.*


import kotlinx.android.synthetic.main.fragment_order_customer.*
import ru.its_365.agent365.BR
import ru.its_365.agent365.activitys.adapters.RecyclerViewAdapter
import ru.its_365.agent365.db.model.*
import ru.its_365.agent365.db.model.Unit
import ru.its_365.agent365.exchange.Exchange
import ru.its_365.agent365.tools.*
import ru.its_365.agent365.viewmodel.OrderLineViewModel
import java.util.*
import ru.its_365.agent365.tools.AbstractRecyclerAdapter
import ru.its_365.agent365.viewmodel.OrderDetailLineViewModel


class OrderDetailEventSelectGoods(val goodsCode : String)

class OrderLineViewAdapter<OrderDetailLineViewModel> : AbstractRecyclerAdapter<OrderDetailLineViewModel>() {

    override val itemLayoutId: Int
        get() = R.layout.order_detail_item

    override val variableId: Int
        get() = BR.orderDetailLineViewModel
}


class OrderDetail : AppCompatActivity(), View.OnClickListener, LongTermOperation {
    override fun onSuccess(message: String?, data: Any?) {
        runOnUiThread(Runnable({
        order?.state = OrderState.POST.int
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(order)
        realm.commitTransaction()

           Toast.makeText(this,"Успешно отправлено!",Toast.LENGTH_LONG).show()
            updateTitles()
        }))

    }

    override fun onFail(e: Exception) {
        runOnUiThread(Runnable({
            Toast.makeText(this,"Ошибка отправки!",Toast.LENGTH_LONG).show()
        }))
    }

    override fun setState(title: String) {

    }

    private val adapter = RecyclerViewAdapter()
    private var order : Order? = null
    private val ACTION_ORDER_HEADER = 0;
    private val ACTION_CHANGE_ITEMS = 1;
    private val TEMP_KEY_ITEMS = "ORDER.TEMP_KEY_ITEMS"

    private var swipeHelper : SwipeHelper? = null
    private var vmlist = mutableListOf<OrderDetailLineViewModel>()
    private var isModifi : Boolean = false


    companion object {
        var Events: RxBus = RxBus()
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        //setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val code = intent.getStringExtra("code")
        if (code == null){
            supportActionBar?.title = "Новый заказ"
            order = Order()
            order?.code = UUID.randomUUID().toString()
            order?.number = Order.getNewNumber(this.applicationContext)
            order?.date = Calendar.getInstance().time
            order?.deliveryDate = Calendar.getInstance().time
            val defaultStoreCode = PreferenceStore.getString(this,Const.DEFAULT_STORE_CODE)
            if (defaultStoreCode.isNotEmpty() && defaultStoreCode.isNotFreeUid()){
                val s = (StoreModel().get("code",defaultStoreCode) as Store?)
                if(s != null){
                    order?.store = s
                }
            }
            val defaultPriceTypeCode = PreferenceStore.getString(this,Const.DEFAULT_PRICE_TYPE_CODE)
            if (defaultPriceTypeCode.isNotEmpty() && defaultPriceTypeCode.isNotFreeUid()){
                val p = (PriceTypeModel().get("code",defaultPriceTypeCode) as PriceType?)
                if (p != null){
                    order?.priceType = p
                }
            }
            val defaultOrganisationCode = PreferenceStore.getString(this,Const.DEFAULT_ORGANISATION_CODE)
            if (defaultOrganisationCode.isNotEmpty() && defaultOrganisationCode.isNotFreeUid()){
                val o = (OrganisationModel().get("code",defaultOrganisationCode) as Organisation?)
                if (o != null){
                    order?.organisation = o
                }
            }


        }else{
            order =  OrderModel().get("code",code) as Order
            supportActionBar?.title = "Заказ № "+order?.number.toString()
        }

        updateTitles()
        fab.setOnClickListener(this)
        layoutHeader.setOnClickListener(this)

        isModifi = false

        Events = RxBus()
        Events.listen(OrderDetailEventSelectGoods::class.java).subscribe({
            OpenOrderLine(it.goodsCode)
        })

        Events.listen(OrderLineViewModel::class.java).subscribe(){
            val goodsCode = it.goodsCode
            var lines:OrderGoods? = null
            var index = -1
            order?.goods?.forEach {
                if(lines != null) return@forEach
                index++
                if(it.goods?.code == goodsCode){
                    lines = it
                    return@forEach
                }
            }

            if (it.getQty() == 0f) return@subscribe

            var isNewLine : Boolean = false

            if(lines == null){
                isNewLine = true
                lines = OrderGoods()
            }else{
                var isModify = false

                if (lines?.qty  != it.getQty()) isModify = true
                if (lines?.price  != it.getPrice()) isModify = true
                if (lines?.priceType != it.priceType) isModify = true
                if (lines?.unit != it.unit) isModify = true

                if (isModify == false) return@subscribe

            }

            this@OrderDetail.isModifi = true

            lines?.code = order?.code+it.goodsCode+""
            lines?.goods = GoodsModel().get("code",it.goodsCode) as Goods
            lines?.unit = it.unit
            lines?.priceType  = it.priceType
            lines?.qty  = it.getQty()
            lines?.price  = it.getPrice()
            lines?.total  = it.getTotal()

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            if(isNewLine){
                order?.goods?.add(lines)
                Toast.makeText(this,"Добавление: "+it.name,Toast.LENGTH_LONG).show()
            }else{
                order?.goods?.set(index,lines)
                Toast.makeText(this,"Изменение: "+it.name,Toast.LENGTH_LONG).show()
            }
            realm.commitTransaction()

            runOnUiThread(Runnable({
                updateTitles()
            }))
        }



    }


    override fun onDestroy() {
        Events.complete()
        super.onDestroy()
    }


    fun OpenOrderLine(goodsCode : String){
        val realm = Realm.getDefaultInstance()
        var lines :OrderGoods?  = order?.goods?.firstOrNull{it.goods?.code == goodsCode} as OrderGoods?
        if(lines == null){
            lines = OrderGoods()

            val goods = GoodsModel().get("code",goodsCode) as Goods?
            lines.goods = goods
    //        lines.unit = goods?.units?.first()
            lines.priceType = order?.priceType
            lines.qty = 0f;

            var priceValue = realm.where<PriceValue>().equalTo("owners.code",goodsCode).and().equalTo("priceType.code",lines.priceType?.code).findFirst()

            lines.price = priceValue?.value ?: 0f
            lines.total = 0f


        }

        var stock = 0f
        var stocksQuery = realm.where<Stock>().equalTo("owners.code",lines?.goods?.code)
        if(order?.store != null){
            val storecode = order?.store?.code
            stocksQuery = stocksQuery.and().equalTo("store.code",storecode)
        }
        val stocks = stocksQuery.findAll()
        stocks.forEach {
            stock+=it.stock
        }


        var defaultPriceType:PriceType = PriceType()
        val defaultPriceTypeCode = PreferenceStore.getString(this,Const.DEFAULT_PRICE_TYPE_CODE)
        if (defaultPriceTypeCode.isNotEmpty() && defaultPriceTypeCode.isNotFreeUid()){
            val p = (PriceTypeModel().get("code",defaultPriceTypeCode) as PriceType?)
            if (p != null){
                defaultPriceType = p
            }
        }

        var history : String = ""
        if (order?.customer != null){
            val historyList = realm.where<History>().equalTo("goodsCode",goodsCode).equalTo("customerCode",order?.customer?.code).findAll()

            historyList.forEach {
                val sum = it.sum
                val qty = it.qty
                val price = sum / qty;
                history+= """
                ${it.date} - ${it.qty} шт. по ${java.lang.String.format("%.${2}f", price)}""".trimIndent()
            }
        }


        val defaultUnit = realm.where<Unit>().equalTo("code",lines.goods?.baseUnitCode).findFirst()


        var orderLine = ru.its_365.agent365.viewmodel.OrderLineViewModel(
                goodsCode = lines.goods?.code ?: "",
                name = lines.goods?.name ?: "",
                articul = lines.goods?.articul?:"",
                stock = stock,
                priceType = ((lines.priceType ?: defaultPriceType) as PriceType) ,
                price = lines.price,
                qty = lines.qty,
                unit = lines.unit ?: defaultUnit ?: Unit(),
                history = history
                )

        TemporaryStorage.setItem("OrderLineViewModel",orderLine)

        val intent = Intent(this,OrderLine::class.java)
        startActivityForResult(intent, 0)
    }




    private fun updateTitles(){
        txtDate.setText(ValueHelper.DateToString(order?.date)+"/"+ValueHelper.DateToString(order?.deliveryDate))
        txtCustomer.setText(order?.customer?.name ?: "<Покупатель не выбран>")
        txtDeliveryAdress.setText(order?.deliveryAddress ?: "")
//     adapter.set(order?.goods?.toList() as List<Customer>)
//     list.layoutManager = LinearLayoutManager(this)
//     list.adapter = adapter

        val dlist = order?.goods?.toList()
        vmlist = mutableListOf<OrderDetailLineViewModel>()
        dlist?.forEach {
            vmlist.add(OrderDetailLineViewModel(it.goods?.code?:"",it.goods?.name?:"",it.goods?.articul?:"",it.price?:0f,it.qty?:0f))
        }

        var vmadapter = OrderLineViewAdapter<OrderDetailLineViewModel>()
        if(vmlist != null){
            vmadapter.setElements(vmlist)
        }

        val l = findViewById<RecyclerView>(R.id.list)
        l.adapter = vmadapter
        l.layoutManager = LinearLayoutManager(this)

        if(swipeHelper == null){
            swipeHelper = object : SwipeHelper(this,l){
                override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?, underlayButtons: MutableList<UnderlayButton>?) {
                    underlayButtons?.add(SwipeHelper.UnderlayButton(
                            "Удалить",
                            null,
                            Color.parseColor("#FF3C30"),
                            object : UnderlayButtonClickListener{
                                override fun onClick(pos: Int) {
                                    var c = vmlist.get(pos)
                                    deleteLinesByCide(c.goodsCode)
                                    updateTitles()
                                    vmadapter.notifyDataSetChanged()
                                    return
                                }

                            }

                    ))
                }
            }
        }



        vmadapter.notifyDataSetChanged()

        var total : Float = 0f
        order?.goods?.forEach{
            total += it.total
        }
        supportActionBar?.title = "Заказ № "+order?.number.toString()
        txtTotalSum.setText(String.format("%.2f", total) + Const.SYMBOL_RUSSIAN_RUB)
        txtTotalWeight.setText("")

        var colorId = R.color.color_order_state_new
        when(order?.state){
            OrderState.NEW.int -> {
                stateLayout.setBackgroundResource(R.color.color_order_state_new)
                textState.setText(R.string.order_state_new)
            }
            OrderState.POST.int -> {
                stateLayout.setBackgroundResource(R.color.color_order_state_post)
                textState.setText(R.string.order_state_post)
            }
            OrderState.PROCESSED.int -> {
                stateLayout.setBackgroundResource( R.color.color_order_state_proceed)
                textState.setText(R.string.order_state_processed)
            }
            OrderState.CANCELED.int -> {
                stateLayout.setBackgroundResource(R.color.color_order_state_canceled)
                textState.setText(R.string.order_state_canceled)
            }
        }
    }

    private fun deleteLinesByCide(code:String){
        var de : OrderGoods? = null
        order?.goods?.forEach {
            if(it.goods?.code == code) {
                de = it
                return@forEach
            }
        }
        if(de != null){
            order?.goods?.remove(de)
            updateTitles()
        }
        isModifi = true
    }

    private fun openOrderHeader(){
        val intent = Intent(this,OrderHeader::class.java)
        intent.putExtra("organisation.code",order?.organisation?.code)
        intent.putExtra("customer.code",order?.customer?.code)
        intent.putExtra("deliveryAddress",order?.deliveryAddress)
        intent.putExtra("deliveryDate",order?.deliveryDate)
        intent.putExtra("comment",order?.comment)
        intent.putExtra("priceType.code",order?.priceType?.code)
        intent.putExtra("store.code",order?.store?.code)
        startActivityForResult(intent, ACTION_ORDER_HEADER)
    }

    private fun changeLines(orderGoods : OrderGoods? = null){

        if(order == null || order?.priceType == null){
            DialogHelper.AlertDialog(this,"Предупреждение","Выберите тип цены!")
            return
        }

        if(order == null || order?.customer == null){
            DialogHelper.AlertDialog(this,"Предупреждение","Выберите покупателя!")
            return
        }


        TemporaryStorage.remItem(TEMP_KEY_ITEMS)
        val intent = Intent(this,SelectGoods::class.java)
        if(intent != null){
            val _orderGoods = OrderGoods()
            _orderGoods.code = UUID.randomUUID().toString()
            TemporaryStorage.setItem(TEMP_KEY_ITEMS,_orderGoods)
        }else{
            TemporaryStorage.setItem(TEMP_KEY_ITEMS,orderGoods)
        }
        intent.putExtra("order.ref",TEMP_KEY_ITEMS)
        intent.putExtra("store.code",order?.store?.code)
        intent.putExtra("priceType.code",order?.priceType?.code)
        intent.putExtra("customer.code",order?.customer?.code)
        startActivityForResult(intent, ACTION_CHANGE_ITEMS)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.layoutHeader -> openOrderHeader()
            R.id.fab -> changeLines()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        menuInflater.inflate(R.menu.order_detail_menu,menu)
        return true
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
    }


    private fun saveOrder(){
        if(order?.state != OrderState.NEW.int) {
            return
        }
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(order)
        realm.commitTransaction()
        updateTitles()
        Toast.makeText(this, "Заказ сохранен",Toast.LENGTH_SHORT).show()
        isModifi = false
    }




    fun sendOrder(){
        if(order?.state != 0){
            Toast.makeText(this,"Заказ был выгружен ранее!",Toast.LENGTH_SHORT).show()
            return
        }
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(order)
        realm.commitTransaction()
        realm.close()

        val jsonOrder = Exchange.orderToJsonString(order ?: return)
        if(order != null) Exchange(this,false, this).sendOrder(jsonOrder)

    }


    fun onBackClick(){

        if(isModifi == false){
            onBackPressed()
        }else{

            val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
                when (which) {
                    DialogInterface.BUTTON_POSITIVE -> {
                        saveOrder()
                        onBackPressed()
                    }

                    DialogInterface.BUTTON_NEGATIVE -> {
                        onBackPressed()
                    }
                }//Yes button clicked
                //No button clicked
            }

            val builder = AlertDialog.Builder(this)
            builder.setMessage("Изменения не сохранены. Сохранить перед выходом?").setPositiveButton("Сохранить", dialogClickListener)
                    .setNegativeButton("Не сохранять", dialogClickListener).show()

    }
}




    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)
        when(item?.itemId){
            android.R.id.home -> onBackClick()
            R.id.save -> saveOrder()
            R.id.send -> sendOrder()
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_OK){
            return
        }
        if(data == null){
            return
        }

        val realm = Realm.getDefaultInstance()

        var code : String? = data.getStringExtra("organisation.code")
        if(code != null){
            realm.beginTransaction()
            order?.organisation = OrganisationModel().get("code",code) as Organisation?
            realm.commitTransaction()
        }

        code = data.getStringExtra("customer.code")
        if(code != null){
            realm.beginTransaction()
            order?.customer = CustomerModel().get("code",code) as Customer?
            realm.commitTransaction()
        }
        realm.beginTransaction()
        order?.deliveryAddress  = data.getStringExtra("deliveryAddress")
        order?.deliveryDate = data.getSerializableExtra("deliveryDate") as Date
        order?.comment = data.getStringExtra("comment")

        val storeCode = data.getStringExtra("store.code")
        if (storeCode != null){
            order?.store = StoreModel().get("code",storeCode) as Store
        }

        val priceTypeCode = data.getStringExtra("priceType.code")
        if (priceTypeCode != null){
            order?.priceType = PriceTypeModel().get("code",priceTypeCode) as PriceType
        }

        realm.commitTransaction()
        updateTitles()

        isModifi = true
    }
}
