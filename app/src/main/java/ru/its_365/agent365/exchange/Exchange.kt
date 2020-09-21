package ru.its_365.agent365.exchange

import android.app.Application
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
//import com.google.gson.JsonObject
//import com.beust.klaxon.JsonObject
//import com.beust.klaxon.Parser
import io.realm.Realm
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import ru.its_365.agent365.R
import ru.its_365.agent365.db.model.*
import ru.its_365.agent365.db.model.Unit
import ru.its_365.agent365.tools.*
import java.io.IOException
import java.util.*


class Exchange(val ctx: Context, val isTest: Boolean = false, val callBack: LongTermOperation) : ExchangeInterface, okhttp3.Callback {



    val version = PreferenceStore.getInt(ctx, Const.SETTINGS_PROTOCOL_VERSION)
    var server = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_SERVER_NAME)
    val userName = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_USER_NAME)
    val userPassword = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_USER_PASSWORD)
    val uid = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_USER_UID)

    init {

        if ((isTest == false) && (ctx !is AppCompatActivity)) {
            throw Exception("The constructor parameter must inherit the type AppCompatActivity")
        }

        if (server == "" || userName == "" || userPassword == "" || uid == "") {
            throw Exception("Информация необходимая для подключения не указана")
        }
        // Запросим на сервере данные профайла
        if (server[server.length - 1].toString() == "/") {
            server = server.dropLast(1);
        }
    }


    companion object {
        fun orderToJsonString(order: Order): String {

            var jsonObject = JSONObject()

            // LocalCode - Идентификатор заказа на мобильном устройстве
            // Code - идентификатор заказа в учетной системе, при первой выгрузке принимает значение Неопределено
            // Date - дата создания заказа
            // OrganisationCode - код организации
            // CustomerCode
            // DeliveryAdress
            // DeliveryDate
            // StoreCode
            // Coment
            // Goods array from
            // GoodsCode
            // Price
            // Qty
            jsonObject.put("LocalCode", order.code)
            jsonObject.put("Code", order.erpCode)
            jsonObject.put("Date", ValueHelper.DateToString(order.date))
            jsonObject.put("OrganisationCode", order.organisation?.code ?: "")
            jsonObject.put("CustomerCode", order.customer?.code.toString() ?: "")
            jsonObject.put("DeliveryAdress", order.deliveryAddress)
            jsonObject.put("DeliveryDate", ValueHelper.DateToString(order.deliveryDate))
            jsonObject.put("StoreCode", order.store?.code ?: "")
            jsonObject.put("Coment", order.comment)

            var goods = JSONArray()
            order.goods.forEach {
                var lines = JSONObject()
                lines.put("GoodsCode", it.goods?.code ?: "")
                lines.put("PriceTypeCode", it.priceType?.code ?: "")
                lines.put("UnitCode", it.unit?.code ?: "")
                lines.put("Price", it.price.toFloat())
                lines.put("Qty", it.qty.toFloat())
                goods.put(lines)
            }
            jsonObject.put("Goods", goods)
            val jsonString = jsonObject.toString()
            return jsonString
        }
    }


    override fun sendOrder(orderJson: String) {

        val url = "${server}/hs/Agent365Service/Order";
        val mt = MediaType.parse("application/json; charset=utf-8") as MediaType
        HttpHelper.Post(url, userName, userPassword, uid, orderJson, mt, this)

    }

    private fun sendOrderСontinue(jsonResponse: String) {
        callBack.onSuccess("sendOrder -> OK", jsonResponse)
    }


    override fun onFailure(call: Call?, e: IOException?) {
        callBack.onFail(e as Exception)
    }

    override fun onResponse(call: Call?, response: Response?) {
        try {
            var requestUrl: String = response?.request()?.url().toString()
            if (requestUrl[requestUrl.length - 1].toString() == "/") {
                requestUrl = requestUrl.dropLast(1);
            }
            val mask = "/hs/Agent365Service/"
            val method: String = requestUrl.substring(requestUrl.indexOf(mask) + mask.length, requestUrl.length)

            when (method) {
                "Profile" -> getProfileСontinue(response?.body()?.string() as String)
                "FullSync" -> fullLoadСontinue(response?.body()?.string() as String)
                "Order" -> sendOrderСontinue(response?.body()?.string() as String)
                "Stocks" -> updateStockContinue(response?.body()?.string() as String)
                "History" -> updateHistoryContinue(response?.body()?.string() as String)
            }
        } catch (e: Exception) {
            callBack.onFail(e)
        }
    }

    override fun getProfile() {
        callBack.setState("Подключение к серверу")
        val profileUrl = "${server}/hs/Agent365Service/Profile";
        HttpHelper.Get(profileUrl, userName, userPassword, uid, this)
    }

    private fun getProfileСontinue(jsonResponse: String) {
        callBack.onSuccess("getProfile -> OK", jsonResponse)
    }


    override fun fullLoad() {
        var useAsyncload = true
        if(useAsyncload == false) {
            callBack.setState("Подключение к серверу")
            val profileUrl = "${server}/hs/Agent365Service/FullSync";
            HttpHelper.Get(profileUrl, userName, userPassword, uid, this)
        }else{
            callBack.setState("Подключение к серверу")
            // 1. Запросить профайл проверить корректность ответа


        }
    }

    private fun fullLoadСontinue(jsonResponse: String) {
        callBack.setState("Обработка ответа")
        val realm = Realm.getDefaultInstance()

        if (jsonResponse.contains(Const.PROTOCOL_NAME) == false) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Неверное имя протокола"))
            return
        }


        // ПРОВЕРКА ОТВЕТА

        val stringBuilder: StringBuilder = StringBuilder(jsonResponse)
        var json: JSONObject =
                try {
                    JSONObject(jsonResponse)
                } catch (e: Exception) {
                    callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Ошибка серриализации"))
                    return
                }

        // ПАРСИМ ПРОФАЙЛ
        val profile = json.getJSONObject("Profile")
        if (profile == null) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Profile не найден"))
            return
        }

        val protocolVersion: Int? = profile.getInt(Const.PROTOCOL_NAME)
        if (protocolVersion == null) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Неверная версия пртокола"))
            return
        }
        if (protocolVersion != 1001) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Неверная версия пртокола"))
            return
        }

        callBack.setState("Удаление старых данных")
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
        callBack.setState("Загрузка организаций")

        // Счетчик загруженных элементов
        var loadCounter = 0

        // ОРГАНИЗАЦИИ
        try {
            val organisationModel = OrganisationModel()
            organisationModel.delete()
            realm.beginTransaction()


            val organisationItems = json.getJSONArray("Organisation")
            for (i in 0..(organisationItems.length() - 1)) {
                val it = organisationItems.getJSONObject(i)
                val organisation: Organisation = Organisation(
                        code = it.getString("Code")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        name = it.getString("Name")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        INN = it.getString("INN") ?: "",
                        KPP = it.getString("KPP") ?: ""
                )
                //organisationModel.set(organisation)
                realm.copyToRealmOrUpdate(organisation)
            }
            realm.commitTransaction()
            loadCounter + organisationModel.getAll().size


        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки организаций
                | ${t}"""".trimMargin()))
            return
        }


        // ПОКУПАТЕЛИ
        callBack.setState("Загрузка покупателей")
        try {
            val customerItems = json.getJSONArray("Customer")
            val customerModel = CustomerModel()
            customerModel.delete()
            val contactInformationModel = ContactInformationModel()
            contactInformationModel.delete()

            realm.beginTransaction()
            for (i in 0..(customerItems.length() - 1)) {
                val it = customerItems.getJSONObject(i)
                val customer: Customer = Customer(
                        code = it.getString("Code")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        name = it.getString("Name")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        upperName = (it.getString("Name") ?: "").toUpperCase(),
                        INN = it.getString("INN") ?: "",
                        KPP = it.getString("KPP") ?: "",
                        coment = it.getString("Coment") ?: ""
                )
                //customerModel.set(customer)
                val ciItems = it.getJSONArray("ContactInformation")
                        ?: throw Exception("Отсутствует необходимый реквизит объекта")
                for (k in 0..(ciItems.length() - 1)) {
                    val it1 = ciItems.getJSONObject(k)
                    var ci: ContactInformation = ContactInformation(
                            type = it1.getString("Type")
                                    ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                            value = it1.getString("Value")
                                    ?: throw Exception("Отсутствует необходимый реквизит объекта")
                    )
                    customer.contacts.add(ci)

                    //customerModel.set(customer)
                }
                realm.copyToRealmOrUpdate(customer)
            }
            realm.commitTransaction()

            loadCounter + customerModel.getAll().size
            loadCounter + contactInformationModel.getAll().size

        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки покупателей
                | ${t}"""".trimMargin()))
            return
        }


        // ДОГОВОРЫ И ДОЛГИ
        callBack.setState("Загрузка договоров")
        try {
            val contractsItems = json.getJSONArray("Contracts")
            val contractModel = ContractModel()
            contractModel.delete()
            val customerModel = CustomerModel()

            realm.beginTransaction()

            for (i in 0..(contractsItems.length() - 1)) {
                val it = contractsItems.getJSONObject(i)
                val customer = customerModel.get("code",
                        it.getString("CustomerCode") ?: "") as Customer
                if (customer != null) {
                    val contract = Contract(
                            code = it.getString("Code")
                                    ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                            name = it.getString("Name")
                                    ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                            debt = it.getDouble("Debt")?.toFloat()
                    )
                    customer.contracts.add(contract)
                    //customerModel.set(customer)
                    realm.copyToRealmOrUpdate(customer)
                }
            }
            realm.commitTransaction()

            loadCounter + contractModel.getAll().size
        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки договоров
                | ${t}"""".trimMargin()))
            return
        }


        // НОМЕНКЛАТУРА
        callBack.setState("Загрузка товаров")
        try {
            val goodsItems = json.getJSONArray("Goods")
            val goodsModel = GoodsModel()
            goodsModel.delete()

            UnitModel().delete()

            realm.beginTransaction()
            for (i in 0..(goodsItems.length() - 1)) {
                val it = goodsItems.getJSONObject(i)
                val goods = Goods(
                        code = it.getString("Code")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        isGroup = it.getBoolean("IsGroup"),
                        parent = it.getString("Parent"),
                        name = it.getString("Name")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        searchName = it.getString("Name").toUpperCase(),
                        articul = try {
                            it.getString("Articul")
                        } catch (t: Throwable) {
                            ""
                        }
                )



                if (goods.isGroup == false) {

                    val jBaseUnit = it.getJSONObject("BaseUnit")

                    val unit = Unit(
                            code = "${goods.code}_${jBaseUnit.getString("Code")}",
                            goodscode = goods.code,
                            unitCode = jBaseUnit.getString("Code"),
                            name = "*"+jBaseUnit.getString("Name"),
                            coefficient = jBaseUnit.getDouble("Coefficient").toFloat()
                    )
                    realm.copyToRealmOrUpdate(unit)

                    goods.baseUnitCode = unit.code
                    val unitsItems = it.getJSONArray("Units")
                    for (k in 0..(unitsItems.length() - 1)) {
                        val itUnits = unitsItems.getJSONObject(k)

                        val unit = Unit(
                                code = "${goods.code}_${itUnits.getString("Code")}",
                                goodscode = goods.code,
                                unitCode = itUnits.getString("Code"),
                                name = itUnits.getString("Name"),
                                coefficient = itUnits.getDouble("Coefficient").toFloat()
                        )
                        realm.copyToRealmOrUpdate(unit)
                    }
                }



                realm.copyToRealmOrUpdate(goods)

            }
            realm.commitTransaction()
            loadCounter + goodsModel.getAll().size

        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки товаров
                | ${t}"""".trimMargin()))
            return
        }


        // ЦЕНЫ
        callBack.setState("Загрузка цен")
        try {
            val priceTypesItems = json.getJSONArray("PriceTypes")
            val priceTypeModel = PriceTypeModel()
            priceTypeModel.delete()
            for (i in 0..(priceTypesItems.length() - 1)) {
                val it = priceTypesItems.getJSONObject(i)
                val priceType = PriceType(
                        code = it.getString("Code")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        name = it.getString("Name")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта")
                )
                priceTypeModel.set(priceType)
            }

            val priceValueItems = json.getJSONArray("PriceValues")
            val priceValueModel = PriceValueModel()
            val goodsModel = GoodsModel()

            priceValueModel.delete()
            realm.beginTransaction()
            for (i in 0..(priceValueItems.length() - 1)) {
                val it = priceValueItems.getJSONObject(i)
                val goods: Goods = goodsModel.get("code", it.getString("GoodsCode")
                        ?: throw Exception("Отсутствует необходимый реквизит объекта")) as Goods
                val priceType: PriceType = priceTypeModel.get("code", it.getString("PriceTypeCode")
                        ?: throw Exception("Отсутствует необходимый реквизит объекта")) as PriceType
                val priceValueCode: String = priceType.code + goods.code
                val value: Float = it.getDouble("Value").toFloat()


                val priceValue = PriceValue(
                        code = priceValueCode,
                        priceType = priceType,
                        //goods = goods,
                        value = value
                )
                //priceValueModel.set(priceValue)

                goods.prices.add(priceValue)

                realm.copyToRealmOrUpdate(goods)
            }
            realm.commitTransaction()
        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки цен
                | ${t}"""".trimMargin()))
            return
        }

        // ОСТАТКИ
        callBack.setState("Загрузка остатков")
        try {
            val stores = json.getJSONObject("Stores")
            val storesItems = stores.getJSONArray("Array")
            val storeModel = StoreModel()
            storeModel.delete()
            for (i in 0..(storesItems.length() - 1)) {
                val it = storesItems.getJSONObject(i)
                val store: Store = Store(
                        code = it.getString("Code")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        name = it.getString("Name")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта")
                )
                storeModel.set(store)
            }

            val stocksItems = json.getJSONArray("Stocks")
            val stockModel = StockModel()
            stockModel.delete()
            val goodsModel = GoodsModel()
            realm.beginTransaction()
            for (i in 0..(stocksItems.length() - 1)) {
                val it = stocksItems.getJSONObject(i)
                val store: Store = storeModel.get("code", it.getString("StoreCode")
                        ?: throw Exception("Отсутствует необходимый реквизит объекта")) as Store
                val goods: Goods = goodsModel.get("code", it.getString("GoodsCode")
                        ?: throw Exception("Отсутствует необходимый реквизит объекта")) as Goods
                val stockCode: String = store.code + goods.code
                val value: Float = it.getDouble("Stock")?.toFloat()
                val stock: Stock = Stock(
                        code = stockCode,
                        store = store,
                        stock = value
                )
                goods.stocks.add(stock);
                //stockModel.set(stock)
                realm.copyToRealmOrUpdate(goods)
            }
            realm.commitTransaction()


            // ЗНАЧЕНИЯ ПО УМОЛЧАНИЮ

            PreferenceStore.setString(ctx, Const.DEFAULT_STORE_CODE, profile.getString("DefaultStoreCode"))
            PreferenceStore.setString(ctx, Const.DEFAULT_PRICE_TYPE_CODE, profile.getString("DefaultPriceTypeCode"))
            PreferenceStore.setString(ctx, Const.DEFAULT_ORGANISATION_CODE, profile.getString("DefaultOrganisation"))
        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки остатков
                | ${t}"""".trimMargin()))
            return
        }

        // ИСТОРИЯ ПРОДАЖ
        callBack.setState("Загрузка продаж")
        try {
            val storesItems = json.getJSONArray("History")
            val historyModel = HistoryModel()
            historyModel.delete()
            realm.beginTransaction()
            for (i in 0..(storesItems.length() - 1)) {
                val it = storesItems.getJSONObject(i)
                val history: History = History(
                        code = UUID.randomUUID().toString(),
                        date = it.getString("Date")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        goodsCode = it.getString("GoodsCode")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        customerCode = it.getString("CustomerCode")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        qty = it.getDouble("Qty").toFloat(),
                        sum = it.getDouble("Sum").toFloat()
                )
                realm.copyToRealmOrUpdate(history)
            }
            realm.commitTransaction()
        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки истории продаж
                | ${t}"""".trimMargin()))
            return
        }

        if (realm != null) {
            realm.close()
        }
        callBack.onSuccess("Загрузка завершена")

    }

    override fun updateStock() {
        callBack.setState("Подключение к серверу")
        val profileUrl = "${server}/hs/Agent365Service/Stocks";
        HttpHelper.Get(profileUrl, userName, userPassword, uid, this)
    }

    private fun updateStockContinue(jsonResponse: String) {
        callBack.setState("Обработка ответа")
        val realm = Realm.getDefaultInstance()

        if (jsonResponse.contains("StoreCode") == false) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Неверное имя протокола"))
            return
        }

        if (jsonResponse.contains("GoodsCode") == false) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Неверное имя протокола"))
            return
        }


        // ПРОВЕРКА ОТВЕТА

        val stringBuilder: StringBuilder = StringBuilder(jsonResponse)
        var json: JSONArray =
                try {
                    JSONArray(jsonResponse)
                } catch (e: Exception) {
                    callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Ошибка серриализации"))
                    return
                }

        val length = json.length()

        callBack.setState("Загрузка остатков")
        try {


            realm.beginTransaction()
            for (i in 0..(json.length() - 1)) {
                val it = json.getJSONObject(i)
                val storeCode = it.getString("StoreCode")

                val goodsCode = it.getString("GoodsCode")

                val stockCode: String = storeCode + goodsCode
                val value: Float = it.getDouble("Stock")?.toFloat()

                val stockObj = (StockModel().get("code", stockCode) as Stock?)
                if (stockObj != null) {
                    if (stockObj.stock != value) {
                        stockObj.stock = value
                        realm.copyToRealmOrUpdate(stockObj)
                    }
                }


            }
            realm.commitTransaction()


        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки остатков
                | ${t}"""".trimMargin()))
            return
        }



        if (realm != null) {
            realm.close()
        }

        callBack.onSuccess()
    }

    override fun updateHistory() {
        callBack.setState("Подключение к серверу")
        val profileUrl = "${server}/hs/Agent365Service/History";
        HttpHelper.Get(profileUrl, userName, userPassword, uid, this)
    }

    private fun updateHistoryContinue(jsonResponse: String) {
        callBack.setState("Обработка ответа")
        val realm = Realm.getDefaultInstance()

        if (jsonResponse.contains("Date") == false) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Неверное имя протокола"))
            return
        }

        if (jsonResponse.contains("GoodsCode") == false) {
            callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Неверное имя протокола"))
            return
        }


        // ПРОВЕРКА ОТВЕТА

        val stringBuilder: StringBuilder = StringBuilder(jsonResponse)
        var json: JSONArray =
                try {
                    JSONArray(jsonResponse)
                } catch (e: Exception) {
                    callBack.onFail(Exception("${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION}: Ошибка серриализации"))
                    return
                }

        val length = json.length()

        callBack.setState("Загрузка истории продаж")
        try {

            val historyModel = HistoryModel()
            historyModel.delete()
            realm.beginTransaction()
            for (i in 0..(json.length() - 1)) {
                val it = json.getJSONObject(i)

                val history: History = History(
                        code = UUID.randomUUID().toString(),
                        date = it.getString("Date")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        goodsCode = it.getString("GoodsCode")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        customerCode = it.getString("CustomerCode")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        qty = it.getDouble("Qty").toFloat(),
                        sum = it.getDouble("Sum").toFloat()
                )
                realm.copyToRealmOrUpdate(history)

            }
            realm.commitTransaction()


        } catch (t: Throwable) {
            if (realm != null) {
                realm.close()
            }
            callBack.onFail(Exception(""""${Const.ERR_EXCHANGE_PROTOCOL_EXCEPTION} : Ошибка загрузки остатков
                | ${t}"""".trimMargin()))
            return
        }


        if (realm != null) {
            realm.close()
        }


        callBack.onSuccess()
    }

    override fun updateDebt() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateAvalible(version:String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun updateAvalibleContinue(jsonResponse: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}