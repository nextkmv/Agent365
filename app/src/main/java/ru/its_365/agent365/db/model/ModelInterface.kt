package ru.its_365.agent365.db.model

import io.realm.Realm
import io.realm.RealmObject

interface ModelInterface {
    fun set(o: RealmObject) : Boolean {
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
    fun get(fieldName:String, fieldValue: String) : RealmObject?
    fun getAll(fieldName: String? = null, fieldValue: String? = null) : MutableList<RealmObject>
    fun delete(fieldName: String? = null, fieldValue: String? = null) : Boolean
    fun fullTextSearch(fieldValue: String? = null) : MutableList<RealmObject>
}