package ru.its_365.agent365.activitys

import android.annotation.TargetApi
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import io.realm.*
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_order_detail.*
import kotlinx.android.synthetic.main.activity_select_goods.*
import ru.its_365.agent365.R
import ru.its_365.agent365.db.model.*
import ru.its_365.agent365.tools.*


class SelectGoodsAdapter(val realmResults: OrderedRealmCollection<Goods>,val store: Store?, val priceType : PriceType?, var customerCode:String, val listener: View.OnClickListener) : RealmRecyclerViewAdapter<Goods,SelectGoodsAdapter.ViewHolder>(realmResults, true) {

    public class ViewHolder(itemView: View?,val listener: View.OnClickListener) : RecyclerView.ViewHolder(itemView) {

        var Title: String
            get() {
                return (itemView.findViewById<TextView>(R.id.title) as TextView).text.toString()
            }

            set(value) {
                (itemView.findViewById<TextView>(R.id.title) as TextView).text = value
            }


        var Stock : Float
        get(){
            return (itemView.findViewById<TextView>(R.id.stock) as TextView).text.toString().toFloat()
        }
        set(value){
            if(value <= 0){
                (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorAccentGray))
            }else{
                (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.color_order_state_new))
            }
            (itemView.findViewById<TextView>(R.id.stock) as TextView).text = value.toString()
        }

        var Price : Float
            get(){
                return (itemView.findViewById<TextView>(R.id.price) as TextView).text.toString().toFloat()
            }
            set(value){
                (itemView.findViewById<TextView>(R.id.price) as TextView).text = value.toString()
            }

        public fun setFolder(){
            val d = ContextCompat.getDrawable(itemView.context, R.drawable.ic_iconmonstr_folder_2)
            (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).setImageDrawable(d)
            (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).setBackgroundColor(Color.TRANSPARENT)
            val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, itemView.resources.getDisplayMetrics());
            (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).layoutParams.width = width.toInt()
            (itemView.findViewById<TextView>(R.id.price) as TextView).text = ""
            (itemView.findViewById<TextView>(R.id.stock) as TextView).text = ""
        }

        public fun setGoods(){
            (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).setImageDrawable(null)
            (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorAccentGray))
            val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, itemView.resources.getDisplayMetrics());
            (itemView.findViewById<ImageView>(R.id.thumbnail) as ImageView).layoutParams.width = width.toInt()
            (itemView.findViewById<TextView>(R.id.price) as TextView).text = "0"
            (itemView.findViewById<TextView>(R.id.stock) as TextView).text = "0"
        }

        public fun setTAG(tag:String){
            val cs = (itemView.findViewById<ConstraintLayout>(R.id.item) as ConstraintLayout)
            cs.setOnClickListener(listener)
            cs.tag = tag
        }

        public fun setPopular(flag:Boolean){
            if(flag){
                (itemView.findViewById<TextView>(R.id.title) as TextView).setTextColor(ContextCompat.getColor(itemView.context,R.color.color_order_state_post))
            }else{
                (itemView.findViewById<TextView>(R.id.title) as TextView).setTextColor(ContextCompat.getColor(itemView.context,R.color.colorTextDark))
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView : View = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_select_item, parent, false);
        return  ViewHolder(itemView,listener);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val obj = getItem(position) as Goods
        holder.setTAG(obj.code)
        if(obj.isGroup == false){
            holder.setGoods()
            holder.Title = obj.name
            var stock : Float = 0f;

            obj.stocks.forEach {
                if(store == null){
                    stock += it.stock
                }else{
                    if (store == it.store){
                        stock = it.stock
                        return@forEach
                    }
                }
            }


            var priceValue : Float = 0f
            if(priceType != null) {
                obj.prices.forEach {
                    if (priceType == it.priceType){
                        priceValue = it.value
                        return@forEach
                    }
                }
            }


            holder.Price = priceValue
            holder.Stock = stock

            val isPopular = HistoryModel.isPopular(goodsCode = obj.code,customerCode = customerCode)
            holder.setPopular(isPopular)

        }else{
            holder.setFolder()
            holder.Title = obj.name

        }



    }

//    override fun getItemCount(): Int {
//        return realmResults.count()
//    }

}





class SelectGoods : AppCompatActivity(), View.OnClickListener {


    val PARAMS_SELECT_GOODS_IS_GROUP_VIEW = "PARAMS_SELECT_GOODS_IS_GROUP_VIEW"
    val PARAMS_SELECT_GOODS_SORT_BY_FIELD = "PARAMS_SELECT_GOODS_SORT_BY_FIELD"
    val PARAMS_SELECT_GOODS_IS_STOCK_ONLY = "PARAMS_SELECT_GOODS_IS_STOCK_ONLY"

    private var realmResults: RealmResults<Any>? = null
    private var realm = Realm.getDefaultInstance()

    var isGroupView : Boolean = false
    var sortByField : String = "name"
    var isStockOnly : Boolean = false
    var store: ru.its_365.agent365.db.model.Store? = null
    var priceType: ru.its_365.agent365.db.model.PriceType? = null
    var currentParrentCode: String? = null
    var list : RecyclerView? = null
    var customerCode : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_goods)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Выбор товара"

        list = this.findViewById(R.id.list)

        isGroupView  = PreferenceStore.getBoolean(this,PARAMS_SELECT_GOODS_IS_GROUP_VIEW,false)
        sortByField  = PreferenceStore.getString(this,PARAMS_SELECT_GOODS_SORT_BY_FIELD,resources.getStringArray(R.array.select_dialog_filter_sort_mode)[0])
        isStockOnly  = PreferenceStore.getBoolean(this,PARAMS_SELECT_GOODS_IS_STOCK_ONLY,false)
        val storeCode = intent.getStringExtra("store.code") ?: ""
        if (storeCode.isNotEmpty()) {
            store = StoreModel().get("code", storeCode) as Store
        }else{
            store = null
        }

        val priceTypeCode = intent.getStringExtra("priceType.code") ?: ""
        if (priceTypeCode.isNotEmpty()) {
            priceType = PriceTypeModel().get("code", priceTypeCode) as PriceType
        }else{
            priceType = null
        }

        customerCode = intent.getStringExtra("customer.code")

        refreshList()
    }


    override fun onPrepareOptionsMenu(menu: Menu?) : Boolean{
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_search)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                refreshList(null,newText)
                return false
            }
        })
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.clear()
        menuInflater.inflate(R.menu.select_goods_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)
        when(item?.itemId){
            android.R.id.home -> {
                if(currentParrentCode == null || currentParrentCode == "null"){
                    onBackPressed()
                }else{
                    val g = GoodsModel().get("code",currentParrentCode.toString()) as Goods
                    refreshList(g.parent)
                }

            };
            R.id.item_filter -> showFilterDialog()
        }
        return true
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_save -> {
                PreferenceStore.setBoolean(this,PARAMS_SELECT_GOODS_IS_GROUP_VIEW,isGroupView)
                PreferenceStore.setString(this,PARAMS_SELECT_GOODS_SORT_BY_FIELD,sortByField)
                PreferenceStore.setBoolean(this,PARAMS_SELECT_GOODS_IS_STOCK_ONLY,isStockOnly)
                //isGroupView = ((view.parent as ConstraintLayout).getViewById(R.id.swIsGroupView) as Switch).isChecked
                refreshList()
            }

            R.id.item -> {
                val code = view.tag as String
                val goods = GoodsModel().get("code",code) as Goods
                if(goods == null){
                    return
                }else{
                    if(goods.isGroup){
                        refreshList(code)
                    }else {
                        //Toast.makeText(this,goods.name,Toast.LENGTH_LONG).show()
                        OrderDetail.Events.publish(OrderDetailEventSelectGoods(code))
                    }
                }

            }
        }
    }


    private fun getBreadCrumb(parentCode : String?) : String{
        if(parentCode == null || parentCode.isEmpty() || parentCode.isNotFreeUid() == false){
            return ""
        }else{
            val p = GoodsModel().get("code", parentCode) as Goods?
            if(p != null){
                return getBreadCrumb(p.parent)+" ⟩ ${p.name}"
            }else{
                return ""
            }
        }
    }

    private fun refreshList(parentCode:String?=null, searchString:String? = null){
        var data = realm.where<Goods>()

        if(searchString != null && searchString.isNotEmpty()){
            isGroupView = false
        }

            if(isGroupView){
                breadcrumbText.visibility = View.VISIBLE
                // Режим иерархического просмотра
                // Отображаем только группы и товары в подчиненной группе
                if(parentCode != null){
                    currentParrentCode = parentCode
                    data = data.equalTo("parent",parentCode)
                    var breaCrumbstring : String = getBreadCrumb(parentCode)
                    breadcrumbText.setText(breaCrumbstring)
                }else{
                    currentParrentCode = null
                    data = data.equalTo("parent","null")
                    breadcrumbText.setText("")
                }

                if (isStockOnly == true){
                    data = data.beginGroup()
                    data = data.beginGroup()
                    data = data.greaterThan("stocks.stock",0f)
                    if(store != null){
                        data = data.and().equalTo("stocks.store.code",store?.code)
                    }
                    data = data.endGroup()
                    data = data.or().equalTo("isGroup",true)
                    data = data.endGroup()
                }

            }else{
                breadcrumbText.visibility = View.GONE
                // Режим просмотра всего списка
                // Отображаем только товары
                data.equalTo("isGroup",false)
                if (isStockOnly == true){
                    data = data.beginGroup()
                    data = data.greaterThan("stocks.stock",0f)
                    if(store != null){
                        data = data.and().equalTo("stocks.store.code",store?.code)
                    }

                    data = data.endGroup()
                }
            }


        data = data.sort("name",Sort.ASCENDING)

//        val n = sortByField.get(0).toString()
//        when(n){
//            "1" -> data = data.sort("name",Sort.ASCENDING)
//            "2" -> data = data.sort("name",Sort.ASCENDING)
//            "3" -> data = data.sort("stocks.stock",Sort.ASCENDING)
//        }


        var data1 = data.findAll()

        if(searchString != null && searchString.isNotEmpty()){
            val keywords = searchString.split(" ")
            for (keyword in keywords) {
                if(keyword.isEmpty() == false){
                    data1 = data1.where().contains("searchName",keyword.toUpperCase()).findAll()
                }
                    //data = data.contains("name", keyword, Case.INSENSITIVE)
             }
        }


        val adapter     = SelectGoodsAdapter(data1,store,priceType,customerCode,this)
        list?.layoutManager = LinearLayoutManager(this)
        list?.adapter   = adapter
    }

    private fun showFilterDialog(){

        val filterDialog = OnSelectGoodsFilterDialog(this)
        filterDialog.setOnCancelListener(DialogInterface.OnCancelListener {
            val e = it.javaClass
        })
        filterDialog.show()
    }
}
