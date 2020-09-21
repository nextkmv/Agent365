package ru.its_365.agent365.db.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.RealmList
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

open class Customer(
        @PrimaryKey
        var code: String = "",
        var name: String = "",
        var upperName : String = "",
        var INN : String = "",
        var KPP : String = "",
        var coment : String = "",
        var contacts: RealmList<ContactInformation> = RealmList(),
        var contracts: RealmList<Contract> = RealmList()
) : RealmObject() {

    override fun toString(): String {
        return name
    }



}


class CustomerModel() : ModelInterface{
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

    override fun get(fieldName: String, fieldValue: String): RealmObject? {
        val realm = Realm.getDefaultInstance()
        var result  = realm.where<Customer>().equalTo(fieldName,fieldValue).findFirst()
        return realm.copyFromRealm(result) as RealmObject
    }

    override fun getAll(fieldName: String?, fieldValue: String?): MutableList<RealmObject> {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Customer>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Customer>().sort("name").findAll()
        }else{
            result = realm.where<Customer>().equalTo(fieldName,fieldValue).sort("name").findAll()
        }
        val list : MutableList<Customer> = mutableListOf()
        result.forEach {
            list.add(realm.copyFromRealm(it))
        }
        return list as MutableList<RealmObject>
    }

    override fun delete(fieldName: String?, fieldValue: String?): Boolean {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Customer>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Customer>().findAll()
        }else{
            result = realm.where<Customer>().equalTo(fieldName,fieldValue).findAll()
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
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Customer>? = null
        if(fieldValue != null){
            val upKeyword = (fieldValue as String).toUpperCase()
            result = realm.where<Customer>().findAll()
                    .where()
                    .beginsWith("upperName", upKeyword)
                    .or()
                    .contains("upperName",upKeyword)
                    .sort("name")
                    .findAll()
        }else{
            result = realm.where<Customer>().sort("name").findAll()
        }
        val list : MutableList<Customer> = mutableListOf()
        result.forEach {
            list.add(realm.copyFromRealm(it))
        }
        return list as MutableList<RealmObject>
    }

}