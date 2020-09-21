package ru.its_365.agent365.db.model

import android.content.Context
import android.renderscript.Sampler
import io.realm.*
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.kotlin.where
import org.w3c.dom.Comment
import ru.its_365.agent365.tools.ValueHelper
import java.text.SimpleDateFormat
import java.util.*


open class Order(
        @PrimaryKey
        var code: String = "",                              // GUID Заказа
        var erpCode: String = "",                           // GUID Заказа выгруженного в учетную систему, для проверки статуса
        var number: Int = 0,                            // Номер заказа в мобильном приложении
        var date: Date = Date(),                            // Дата создания заказа
        var state: Int = 0,                                 // Состояние
        var organisation: Organisation? = null,             // Организация
        var customer: Customer? = null,                     // Покупатель
        var deliveryAddress : String = "",                  // Адрес доставки
        var deliveryDate : Date = Date(),                   // Дата доставки
        var store: Store? = null,                           // Склад с корого будет отгружен заказ, если склад не указан то показываем остатки всех складов
        var priceType: PriceType? = null,                   // Тип цены клиента по умолчанию
        var goods: RealmList<OrderGoods> = RealmList(),
        var comment: String = ""                            // Комментарий
    ) : RealmObject() {

        val total : Float get()
        {
            if(goods == null){
                return 0f;
            }else{
                var t : Float = 0f;
                goods.forEach { t += it.total }
                return t
            }
        }

        companion object {
            public fun getNewNumber(context: Context) : Int
            {
                Realm.init(context)
                val config = RealmConfiguration.Builder()
                        .name("db.realm")
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm.setDefaultConfiguration(config)
                val realm = Realm.getDefaultInstance()
                val max : Int = realm.where<Order>().max("number")?.toInt() ?: 0
                return max + 1
            }
        }

    override fun toString(): String {

        return "Заказ № ${number} от ${ValueHelper.DateToString(date)}"
    }
    }

class OrderModel() : ModelInterface{

    override fun get(fieldName: String, fieldValue: String): RealmObject {
        val realm = Realm.getDefaultInstance()
        var result  = realm.where<Order>().equalTo(fieldName,fieldValue).findFirst()
        return realm.copyFromRealm(result) as RealmObject
    }

    override fun getAll(fieldName: String?, fieldValue: String?): MutableList<RealmObject> {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Order>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Order>().findAll()
        }else{
            result = realm.where<Order>().equalTo(fieldName,fieldValue).findAll()
        }
        val list : MutableList<Order> = mutableListOf()
        result.forEach {
            list.add(realm.copyFromRealm(it))
        }
        return list as MutableList<RealmObject>
    }

    override fun delete(fieldName: String?, fieldValue: String?): Boolean {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Order>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Order>().findAll()
        }else{
            result = realm.where<Order>().equalTo(fieldName,fieldValue).findAll()
        }
        try {
            realm.beginTransaction()
            result?.deleteAllFromRealm()
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }
    }

    override fun fullTextSearch(fieldValue: String?): MutableList<RealmObject> {
        throw NotImplementedError("Not implemented")
    }

}
