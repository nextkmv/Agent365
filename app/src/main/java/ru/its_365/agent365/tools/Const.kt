package ru.its_365.agent365.tools

import android.content.Context
import ru.its_365.agent365.BuildConfig
import java.text.SimpleDateFormat
import java.util.*

class Const() {
    companion object {

        val APPLICATION_ID:String get() = "ITS365RUAGENT365"
        val PROTOCOL_NAME:String get() = "Agent365"
        val APPLICATION_VERSION:String get() = "3"
        val APPLICATION_BUILD:String get() {
            return SimpleDateFormat("dd.MM.yyyy").format(Date(BuildConfig.TIMESTAMP))
        }


        // SETTINGS KEY
        val SETTINGS_PROFILE_LOGED_IN:String get() = "SETTINGS_PROFILE_LOGED_IN";
        val SETTINGS_PROFILE_SERVER_NAME:String get() = "SETTINGS_PROFILE_SERVER_NAME";
        val SETTINGS_PROFILE_USER_NAME:String get() = "SETTINGS_PROFILE_USER_NAME";
        val SETTINGS_PROFILE_USER_PASSWORD:String get() = "SETTINGS_PROFILE_USER_PASSWORD";
        val SETTINGS_PROFILE_USER_UID:String get() = "SETTINGS_PROFILE_USER_UID";
        val SETTINGS_PROFILE_AGENT_NAME:String get() = "SETTINGS_PROFILE_AGENT_NAME";
        val SETTINGS_PROFILE_COMPANY_NAME:String get() = "SETTINGS_PROFILE_COMPANY_NAME";
        val SETTINGS_PROTOCOL_VERSION:String get() = "SETTINGS_PROTOCOL_VERSION";



        // ЗНАЧЕНИЯ ПО УМОЛЧАНИЮ
        val DEFAULT_STORE_CODE:String get() = "DEFAULT_STORE_CODE";
        val DEFAULT_PRICE_TYPE_CODE:String get() = "DEFAULT_PRICE_TYPE_CODE";
        val DEFAULT_ORGANISATION_CODE:String get() = "DEFAULT_ORGANISATION_CODE";
        // SYSTEM KEY
        val DB_ORDER_MAX_NUMBER:String get() = "DB_ORDER_MAX_NUMBER";

        // Error titles
        val ERR_EXCHANGE_PROTOCOL_EXCEPTION :String get() = "Ошибка протокола обмена";

        val SYMBOL_RUSSIAN_RUB = "\u20BD"

    }
}

enum class OrderState(val int:Int){
    NEW(0),         // Новый заказ, создан но не отправлен
    POST(1),        // Отправлен в учетную систему
    PROCESSED(2),   // Обработан в учетной системе
    CANCELED(3)     // Отменен в учетной систмеме
}