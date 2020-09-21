package ru.its_365.agent365.db.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.kotlin.where


open class ContactInformation(
    var type: String = "",
    var value: String = "",
    @LinkingObjects("contacts")
    val owners: RealmResults<Customer>? = null
    ) : RealmObject() {
        override fun toString(): String {
            return value;
        }

        companion object {
            val CONTACT_SKYPE = "CONTACT_SKYPE"
            val CONTACT_LEGAL = "CONTACT_LEGAL"
            val CONTACT_FACT = "CONTACT_FACT"
            val CONTACT_DELIVERY = "CONTACT_DELIVERY"
            val CONTACT_OTHER = "CONTACT_OTHER"
            val CONTACT_PHONE = "CONTACT_PHONE"
            val CONTACT_EMAIL = "CONTACT_EMAIL"
        }
    }

class ContactInformationModel() : ModelInterface{

    override fun get(fieldName: String, fieldValue: String): RealmObject {
        val realm = Realm.getDefaultInstance()
        var result  = realm.where<ContactInformation>().equalTo(fieldName,fieldValue).findFirst()
        return realm.copyFromRealm(result) as RealmObject
    }

    override fun getAll(fieldName: String?, fieldValue: String?): MutableList<RealmObject> {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<ContactInformation>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<ContactInformation>().findAll()
        }else{
            result = realm.where<ContactInformation>().equalTo(fieldName,fieldValue).findAll()
        }
        val list : MutableList<ContactInformation> = mutableListOf()
        result.forEach {
            list.add(realm.copyFromRealm(it))
        }
        return list as MutableList<RealmObject>
    }

    override fun delete(fieldName: String?, fieldValue: String?): Boolean {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<ContactInformation>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<ContactInformation>().findAll()
        }else{
            result = realm.where<ContactInformation>().equalTo(fieldName,fieldValue).findAll()
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
