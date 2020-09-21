package ru.its_365.agent365.tools

import android.app.Application
import io.reactivex.Observable
import io.realm.Realm
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import ru.its_365.agent365.db.model.*
import java.io.IOException
import java.lang.Error
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException




class ExchangeHelper() {


    val ctx = ApplicationContextProvider.getContext()
    val version = PreferenceStore.getInt(ctx, Const.SETTINGS_PROTOCOL_VERSION)
    var server = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_SERVER_NAME)
    val userName = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_USER_NAME)
    val userPassword = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_USER_PASSWORD)
    val uid = PreferenceStore.getString(ctx, Const.SETTINGS_PROFILE_USER_UID)




    private val client: OkHttpClient = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    val credentials = okhttp3.Credentials.basic(userName,userPassword)


    private fun request(url: String): Request {
        return Request.Builder().url(url).build()
    }

    // Выполняет запрос профайл, проверяет его на корректность и возвращает true если профайл корректен
    fun checkProfile() : Observable<Boolean> {
        return Observable.create {
            val url = "${server}/hs/Agent365Service/Profile"
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .build()
            val response = client.newCall(request).execute()
            val body = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Profile")
            if(!body.contains("Agent365")) throw Error("Не верный протокол обмена")
            var profile = JSONObject(body)
            val versionNumber = profile.getInt("Agent365")
            if(versionNumber != 1001) {
                throw Error("Несовместивая версия протокола обмена - $versionNumber")
            }
            PreferenceStore.setString(ctx, Const.DEFAULT_STORE_CODE, profile.getString("DefaultStoreCode"))
            PreferenceStore.setString(ctx, Const.DEFAULT_PRICE_TYPE_CODE, profile.getString("DefaultPriceTypeCode"))
            PreferenceStore.setString(ctx, Const.DEFAULT_ORGANISATION_CODE, profile.getString("DefaultOrganisation"))
            it.onNext(true)
        }
    }


    fun deleteAllRealmData():Observable<Boolean>{
        return Observable.create{
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.deleteAll()
            realm.commitTransaction()
            it.onNext(true)
        }

    }

    fun updateOrganisation():Observable<Boolean>{
        return Observable.create{
            val url = "${server}/hs/Agent365Service/Organisations"
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .build()
            val response = client.newCall(request).execute()
            val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Organisations")
            var array = JSONArray(body)

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()

            for (i in 0..(array.length() - 1)) {
                val it = array.getJSONObject(i)
                val organisation: Organisation = Organisation(
                        code = it.getString("Code")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        name = it.getString("Name")
                                ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                        INN = it.getString("INN") ?: "",
                        KPP = it.getString("KPP") ?: ""
                )
                realm.copyToRealmOrUpdate(organisation)
            }
            realm.commitTransaction()
            it.onNext(true)
        }
    }

    fun updateCustomer():Observable<Boolean>{
        return Observable.create{
            val url = "${server}/hs/Agent365Service/Customers"
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .build()
            val response = client.newCall(request).execute()
            val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Customers")
            var array = JSONArray(body)

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()

            for (i in 0..(array.length() - 1)) {
                val it = array.getJSONObject(i)
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
                }
                realm.copyToRealmOrUpdate(customer)
            }
            realm.commitTransaction()
            it.onNext(true)
        }
    }

    fun updateContracts():Observable<Boolean>{
        return Observable.create{
            val url = "${server}/hs/Agent365Service/Contracts"
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .build()
            val response = client.newCall(request).execute()
            val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Contracts")
            var array = JSONArray(body)

            val customerModel = CustomerModel()
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()

            for (i in 0..(array.length() - 1)) {
                val it = array.getJSONObject(i)
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
            it.onNext(true)
        }
    }

    fun updateGoods():Observable<Boolean>{
        return Observable.create{
            val url = "${server}/hs/Agent365Service/Goods"
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .build()
            val response = client.newCall(request).execute()
            val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Goods")
            var array = JSONArray(body)

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()

            for (i in 0..(array.length() - 1)) {
                val it = array.getJSONObject(i)
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
                            name = "*" + jBaseUnit.getString("Name"),
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
            it.onNext(true)
        }
    }

    private fun updatePriceTypes(){
        val url = "${server}/hs/Agent365Service/PriceTypes"
        val request = Request.Builder()
                .addHeader("Authorization",credentials)
                .addHeader("UID",uid)
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - PriceTypes")
        var array = JSONArray(body)

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        for (i in 0..(array.length() - 1)) {
            val it = array.getJSONObject(i)
            val priceType = PriceType(
                    code = it.getString("Code")
                            ?: throw Exception("Отсутствует необходимый реквизит объекта"),
                    name = it.getString("Name")
                            ?: throw Exception("Отсутствует необходимый реквизит объекта")
            )
            realm.copyToRealmOrUpdate(priceType)
        }
        realm.commitTransaction()
    }

    private fun updatePriceValues(){
        val url = "${server}/hs/Agent365Service/PriceValues"
        val request = Request.Builder()
                .addHeader("Authorization",credentials)
                .addHeader("UID",uid)
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - PriceValues")
        var array = JSONArray(body)
        val goodsModel = GoodsModel()
        val priceTypeModel = PriceTypeModel()
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        for (i in 0..(array.length() - 1)) {
            val it = array.getJSONObject(i)
            val goods: Goods = goodsModel.get("code", it.getString("GoodsCode")
                    ?: throw Exception("Отсутствует необходимый реквизит объекта")) as Goods
            val priceType: PriceType = priceTypeModel.get("code", it.getString("PriceTypeCode")
                    ?: throw Exception("Отсутствует необходимый реквизит объекта")) as PriceType
            val priceValueCode: String = priceType.code + goods.code
            val value: Float = it.getDouble("Value").toFloat()
            val priceValue = PriceValue(
                    code = priceValueCode,
                    priceType = priceType,
                    value = value
            )
            goods.prices.add(priceValue)
            realm.copyToRealmOrUpdate(goods)
        }
        realm.commitTransaction()
    }

    fun updatePrice():Observable<Boolean>{
        return Observable.create{
            updatePriceTypes()
            updatePriceValues()
            it.onNext(true)
        }
    }

    private fun updateStores(){
        val url = "${server}/hs/Agent365Service/Stores"
        val request = Request.Builder()
                .addHeader("Authorization",credentials)
                .addHeader("UID",uid)
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Stores")
        var jsonObject = JSONObject(body)
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val storesItems = jsonObject.getJSONArray("Array")
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
            realm.copyToRealmOrUpdate(store)
        }
        realm.commitTransaction()
    }

    private fun updateStocks(){
        val url = "${server}/hs/Agent365Service/Stocks"
        val request = Request.Builder()
                .addHeader("Authorization",credentials)
                .addHeader("UID",uid)
                .url(url)
                .build()
        val response = client.newCall(request).execute()
        val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Stocks")
        var array = JSONArray(body)
        val goodsModel = GoodsModel()
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val storeModel = StoreModel()
        for (i in 0..(array.length() - 1)) {
            val it = array.getJSONObject(i)
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
            goods.stocks.add(stock)
            realm.copyToRealmOrUpdate(goods)
        }
        realm.commitTransaction()
    }


    fun updateStock():Observable<Boolean>{
        return Observable.create{
            updateStores()
            updateStocks()
            it.onNext(true)
        }
    }

    fun updateHistory():Observable<Boolean>{
        return Observable.create{
            val url = "${server}/hs/Agent365Service/History"
            val request = Request.Builder()
                    .addHeader("Authorization",credentials)
                    .addHeader("UID",uid)
                    .url(url)
                    .build()
            val response = client.newCall(request).execute()
            val body: String = response.body()?.string() ?: throw Error("Сервер вернул пустой ответ - Stocks")
            var array = JSONArray(body)
            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            for (i in 0..(array.length() - 1)) {
                val it = array.getJSONObject(i)
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
            it.onNext(true)
        }
    }

}