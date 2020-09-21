package ru.its_365.agent365.activitys

import android.content.Context
import android.os.Bundle
import android.app.Fragment
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import ru.its_365.agent365.R
import android.view.*
import android.widget.Toast
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_order_customer_list.view.*

import ru.its_365.agent365.db.model.Order
import ru.its_365.agent365.db.model.OrderModel

import io.realm.RealmObject
import kotlinx.android.synthetic.main.fragment_order_customer.view.*
import ru.its_365.agent365.exchange.Exchange
import ru.its_365.agent365.tools.*


/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [OrderFragment.OnListFragmentInteractionListener] interface.
 */
class OrderFragment : Fragment(), View.OnClickListener {

    val adapter = RecyclerViewAdapter(this)
    var listOrder : RecyclerView? = null
    var swipeHelper : SwipeHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_order_customer_list, container, false)
        listOrder = view.findViewById<RecyclerView>(R.id.list_order)
        listOrder?.layoutManager = LinearLayoutManager(view.context)
        view.fab_order_list.setOnClickListener(this)
        refreshList()
        setHasOptionsMenu(true)
        swipeHelper = object : SwipeHelper(this.activity,listOrder){
            override fun instantiateUnderlayButton(viewHolder: RecyclerView.ViewHolder?, underlayButtons: MutableList<UnderlayButton>?) {
                underlayButtons?.add(SwipeHelper.UnderlayButton(
                        "Удалить",
                        null,
                        Color.parseColor("#FF3C30"),
                        object : UnderlayButtonClickListener{
                            override fun onClick(pos: Int) {
                                OrderModel().delete("code",(adapter.list.get(pos) as Order).code)
                                listOrder?.adapter = null
                                listOrder?.removeAllViews()
                                refreshList()
                                return
                            }
                        }

                ))


            }
        }


        return view
    }


    override fun onClick(view: View?) {
        when(view?.id){
            R.id.fab_order_list -> {
                val intent = Intent(this.activity,OrderDetail::class.java);
                startActivityForResult(intent,0);
            }
            R.id.line -> {
                val order = view.tag as Order
                val intent = Intent(this.activity,OrderDetail::class.java);
                intent.putExtra("code",order.code)
                startActivityForResult(intent,0);
            }

        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        refreshList()
    }

    fun refreshList(serach : String = ""){
        val orderModel = OrderModel()
        adapter.clear()
        adapter.set(orderModel.getAll().toList() as List<Order>)
        listOrder?.adapter = adapter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        super.onDetach()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
//        super.onCreateOptionsMenu(menu, inflater)
//        menu?.clear();
//        inflater?.inflate(R.menu.order_list_menu, menu);
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

    }

}


class RecyclerViewAdapter(val listener : View.OnClickListener? = null): RecyclerView.Adapter<RecyclerViewHolder>() {

    var list : MutableList<RealmObject> = ArrayList<RealmObject>()

    public fun set(items : List<RealmObject>){
        list = items.toMutableList();
        notifyDataSetChanged()
    }

    public fun clear(){
        list.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_order_customer, parent, false)
        return RecyclerViewHolder(view,listener)
    }

    override fun getItemCount(): Int {
        return list.size
        //return stringList.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.bind(list[position] as Order)
        //holder.bind(stringList[position])
    }
}


class RecyclerViewHolder(val view: View, val listener : View.OnClickListener? = null) : RecyclerView.ViewHolder(view){
    val line        = view.line
    val thumbnail   = view.thumbnail
    val content     = view.title
    val content2    = view.content2
    val total       = view.total
    public fun bind(order : Order) {
        line.tag = order
        content.setText(order.customer?.toString() ?: "<Контрагент не указан>")
        content2.setText(order.toString())
        total.setText("${order.total.toString()} ${Const.SYMBOL_RUSSIAN_RUB}")
        var colorId = R.color.color_order_state_new
        when(order.state){
            OrderState.NEW.int -> thumbnail.setBackgroundResource(R.color.color_order_state_new)
            OrderState.POST.int -> thumbnail.setBackgroundResource(R.color.color_order_state_post)
            OrderState.PROCESSED.int -> thumbnail.setBackgroundResource( R.color.color_order_state_proceed)
            OrderState.CANCELED.int -> thumbnail.setBackgroundResource(R.color.color_order_state_canceled)
        }

        val bgDraw = ContextCompat.getDrawable(view.context,colorId)



        if(listener != null){
            line.setOnClickListener(listener)
        }
    }
}
