package ru.its_365.agent365.tools;

import javax.annotation.ParametersAreNonnullByDefault

object TemporaryStorage {
    val datalist = mutableMapOf<String,Any?>()
    public fun setItem(name : String, value : Any?){
        datalist.put(name,value)
    }
    public fun getItem(name: String, default: Any? = null): Any?{
        if(datalist.containsKey(name) == false){
            return default;
        }else{
            return datalist[name]
        }
    }

    public fun remItem(name: String){
        if(datalist.containsKey(name) == false){
            return
        }else{
            datalist.remove(name)
        }
    }
}
