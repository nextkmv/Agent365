package ru.its_365.agent365.db.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.kotlin.where

open class OrderGoods(
        @PrimaryKey
        var code : String = "",                              // GUID Заказа
        var goods : Goods? = null,
        var unit : Unit? = null,
        var priceType : PriceType? = null,
        var qty : Float = 0f,
        var price : Float = 0f,
        var total : Float = 0f,
        @LinkingObjects("goods")
        val owners: RealmResults<Order>? = null
) : RealmObject() {


}


