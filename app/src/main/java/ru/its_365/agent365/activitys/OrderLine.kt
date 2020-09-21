package ru.its_365.agent365.activitys

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.android.databinding.library.baseAdapters.BR
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_order_customer.*
import kotlinx.android.synthetic.main.order_line.*

import ru.its_365.agent365.R
import ru.its_365.agent365.databinding.OrderLineBinding
import ru.its_365.agent365.db.model.*
import ru.its_365.agent365.db.model.Unit
import ru.its_365.agent365.tools.DialogHelper
import ru.its_365.agent365.tools.TemporaryStorage
import ru.its_365.agent365.viewmodel.OrderLineViewModel

class OrderLine : AppCompatActivity(), View.OnClickListener{

    var lines : OrderGoods? = null
    var store : Store? = null
    var orderLine = TemporaryStorage.getItem("OrderLineViewModel") as OrderLineViewModel
    var binding : OrderLineBinding? = null
    var priceType:PriceType? = null
    var unit:Unit? = null

    private val onPriceType =
            object : OnSelectDialog {
                override fun refresh(realmResults: RealmResults<Any>?, searchString : String?): RealmResults<Any> {
                    val realm = Realm.getDefaultInstance()
                    return realm.where<PriceType>().findAllAsync() as RealmResults<Any>
                }

                override fun onClick(view: View?) {
                    if(view?.tag is SelectDialogAdapter.TagHolder){
                        priceType = (view?.tag as SelectDialogAdapter.TagHolder).realmObject as PriceType
                        checkPrice()
//                        val realm = Realm.getDefaultInstance()
//
//                        val priceTypeCode : String = priceType?.code ?: ""
//                        val priceValue = realm.where<PriceValue>().equalTo("owners.code",orderLine.goodsCode).and().equalTo("priceType.code",priceTypeCode).findFirst()
//                        if(priceValue != null){
//                            edtPriceValue.setText(priceValue.value.toString())
//                        }
//                        edtPriceType.setText(priceType?.name)
                    }
                }
            }

    private val onUnit =
            object : OnSelectDialog {
                override fun refresh(realmResults: RealmResults<Any>?, searchString : String?): RealmResults<Any> {
                    val realm = Realm.getDefaultInstance()
                    return realm.where<Unit>().equalTo("goodscode",orderLine.goodsCode).findAllAsync() as RealmResults<Any>
                }

                override fun onClick(view: View?) {
                    if(view?.tag is SelectDialogAdapter.TagHolder){
                        unit = (view?.tag as SelectDialogAdapter.TagHolder).realmObject as Unit
                        checkPrice()
                        //val realm = Realm.getDefaultInstance()

//                        val priceTypeCode : String = priceType?.code ?: ""
//                        val priceValue = realm.where<PriceValue>().equalTo("owners.code",orderLine.goodsCode).and().equalTo("priceType.code",priceTypeCode).findFirst()
//                        if(priceValue != null){
//                            edtPriceValue.setText(priceValue.value.toString())
//                        }
//                        edtUnit.setText(unit?.name)
                    }
                }
            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_line)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Выбор товара"

        binding = DataBindingUtil.setContentView(this, R.layout.order_line)
        binding?.orderLine = orderLine
        edtPriceType.setOnClickListener(this)
        priceType = orderLine.priceType
        edtPriceType.setText(priceType?.name)

        edtUnit.setOnClickListener(this)
        unit = orderLine.unit
        edtUnit.setText(unit?.name)
    }


    private fun checkPrice(){
        edtPriceType.setText(priceType?.name)
        edtUnit.setText(unit?.name)
        val realm = Realm.getDefaultInstance()
        val priceTypeCode : String = priceType?.code ?: ""
        val priceValue = realm.where<PriceValue>().equalTo("owners.code",orderLine.goodsCode).and().equalTo("priceType.code",priceTypeCode).findFirst()
        val goods = realm.where<Goods>().equalTo("code",orderLine.goodsCode).findFirst() as Goods?

        val c : Float = unit?.coefficient ?: 1f

        var basePrice : Float = 0f
        if(priceValue != null){
            basePrice = priceValue.value
        }

        if(goods != null && unit?.code != goods.baseUnitCode){
            // Если выбрана не базовая единица измерения
            // нужно пересчитать цену с учетом коэффициента
            basePrice = basePrice * c
        }

        edtPriceValue.setText(basePrice.toString())
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.edtPriceType -> onPriceTypeClick()
            R.id.edtUnit  -> onUnitClick()
        }
    }


    private fun onUnitClick(){
        val selectDialog = DialogHelper.getSelectDialog(this,"Единица измерения", onUnit)
        selectDialog.show()
    }

    private fun onPriceTypeClick(){
        val selectDialog = DialogHelper.getSelectDialog(this,"Типы цен", onPriceType)
        selectDialog.show()
    }

    override fun onBackPressed() {


        orderLine?.priceType = this!!.priceType!!
        orderLine?.unit = this!!.unit!!


        TemporaryStorage.setItem("OrderLineViewModel",orderLine)
        OrderDetail.Events.publish(orderLine)
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)
        when(item?.itemId){
            android.R.id.home -> onBackPressed();
        }
        return true
    }
}
