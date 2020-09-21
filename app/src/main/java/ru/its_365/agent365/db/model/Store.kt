package ru.its_365.agent365.db.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import io.realm.kotlin.where

open class Store(
        @PrimaryKey
        var code: String = "",
        var name: String = ""

) : RealmObject() {
    override fun toString(): String {
        return name
    }
}


class StoreModel() : ModelInterface{
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
        var result  = realm.where<Store>().equalTo(fieldName,fieldValue).findFirst()
        return result
    }

    override fun getAll(fieldName: String?, fieldValue: String?): MutableList<RealmObject> {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Store>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Store>().findAll()
        }else{
            result = realm.where<Store>().equalTo(fieldName,fieldValue).findAll()
        }
        val list : MutableList<Store> = mutableListOf()
        result.forEach {
            list.add(realm.copyFromRealm(it))
        }
        return list as MutableList<RealmObject>
    }

    override fun delete(fieldName: String?, fieldValue: String?): Boolean {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Store>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Store>().findAll()
        }else{
            result = realm.where<Store>().equalTo(fieldName,fieldValue).findAll()
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