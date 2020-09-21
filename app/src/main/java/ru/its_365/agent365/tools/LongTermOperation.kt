package ru.its_365.agent365.tools



interface LongTermOperation {
    fun onSuccess(message : String? = null, data : Any? = null)
    fun onFail(e:Exception)
    fun setState(title:String)
}