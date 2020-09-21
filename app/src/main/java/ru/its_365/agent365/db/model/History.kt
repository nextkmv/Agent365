package ru.its_365.agent365.db.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.LinkingObjects
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

open class History(
        @PrimaryKey
        var code : String = "",
        var date : String = "",
        var goodsCode: String = "",
        var customerCode: String = "",
        var qty : Float = 0f,
        var sum : Float = 0f
) : RealmObject() {

}


class HistoryModel() : ModelInterface{
    override fun set(o: RealmObject): Boolean {
        val realm = Realm.getDefaultInstance()
        try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(o)
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }
    }

    override fun get(fieldName: String, fieldValue: String): RealmObject {
        val realm = Realm.getDefaultInstance()
        var result  = realm.where<History>().equalTo(fieldName,fieldValue).findFirst()
        return result as RealmObject
    }

    override fun getAll(fieldName: String?, fieldValue: String?): MutableList<RealmObject> {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<History>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<History>().findAll()
        }else{
            result = realm.where<History>().equalTo(fieldName,fieldValue).findAll()
        }
        val list : MutableList<History> = mutableListOf()
        result.forEach {
            list.add(realm.copyFromRealm(it))
        }
        return list as MutableList<RealmObject>
    }

    override fun delete(fieldName: String?, fieldValue: String?): Boolean {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<History>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<History>().findAll()
        }else{
            result = realm.where<History>().equalTo(fieldName,fieldValue).findAll()
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


    companion object {
        fun isPopular(goodsCode: String, customerCode: String): Boolean{
            val realm = Realm.getDefaultInstance()
            if(realm.where<History>().equalTo("goodsCode",goodsCode).equalTo("customerCode",customerCode).count().toInt() == 0) {
                return false
            }else{
                return true
            }
        }
    }

}