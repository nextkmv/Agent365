package ru.its_365.agent365.db.model

import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import io.realm.annotations.LinkingObjects
import io.realm.kotlin.where


open class Unit(
        @PrimaryKey
    var code: String = "",
    var goodscode: String = "",
    var unitCode : String = "",
    var name: String = "",
    var coefficient: Float = 1f
    ) : RealmObject() {
    override fun toString(): String {
        return name
    }
    }

class UnitModel() : ModelInterface{

    override fun get(fieldName: String, fieldValue: String): RealmObject {
        val realm = Realm.getDefaultInstance()
        var result  = realm.where<Unit>().equalTo(fieldName,fieldValue).findFirst()
        return result as RealmObject
    }

    override fun getAll(fieldName: String?, fieldValue: String?): MutableList<RealmObject> {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Unit>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Unit>().findAll()
        }else{
            result = realm.where<Unit>().equalTo(fieldName,fieldValue).findAll()
        }
        val list : MutableList<Unit> = mutableListOf()
        result.forEach {
            list.add(realm.copyFromRealm(it))
        }
        return list as MutableList<RealmObject>
    }

    override fun delete(fieldName: String?, fieldValue: String?): Boolean {
        val realm = Realm.getDefaultInstance()
        var result : RealmResults<Unit>? = null
        if(fieldName == null && fieldValue == null){
            result = realm.where<Unit>().findAll()
        }else{
            result = realm.where<Unit>().equalTo(fieldName,fieldValue).findAll()
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

    fun addUnit(_goodscode: String, _unitCode: String, _name: String, _coefficient: Float):String{
        val unit = Unit(
                code = "${_goodscode}_${_unitCode}",
                goodscode = _goodscode,
                unitCode = _unitCode,
                name = _name,
                coefficient = _coefficient
        )

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(unit)
        realm.commitTransaction()
        return _unitCode
    }

    fun addBaseUnit(_goodscode: String, _unitCode: String, _name: String, _coefficient: Float){
        val code = addUnit(_goodscode,_unitCode,_name,_coefficient)
        val realm = Realm.getDefaultInstance()
        val goods = realm.where<Goods>().equalTo("code",_goodscode).findFirst()
        if (goods != null){
            goods.baseUnitCode = code
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(goods)
            realm.commitTransaction()
        }
    }

}
