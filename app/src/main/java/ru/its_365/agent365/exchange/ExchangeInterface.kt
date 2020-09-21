package ru.its_365.agent365.exchange

import junit.runner.Version
import ru.its_365.agent365.db.model.Order
import ru.its_365.agent365.db.model.OrderModel

interface ExchangeInterface {

    fun getProfile()

    /**
     * Выполняет запрос на полную перезагрузку данных, удаляет все данные из БД и перезагружает заново
     * @return успешно ли выполнен запрос
     */
    fun fullLoad()

    /**
     * Выполняет запрос на обновление остатков на складах
     * @return успешно ли выполнен запрос
     */
    fun updateStock()

    /**
     * Выполняет запрос на обновление истории продаж
     * @return успешно ли выполнен запрос
     */
    fun updateHistory()

    /**
     * Выполняет запрос на обновление задолжености покупателей
     * @return успешно ли выполнен запрос
     */
    fun updateDebt()

    /**
     * Выполняет запрос на выгрузку заказа в учетную систему
     */
    fun sendOrder(orderJson:String)

    /**
     * Проверяет доступны ли обновления
     */
    fun updateAvalible(version:String)
}