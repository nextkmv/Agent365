package ru.its_365.agent365.activitys.adapters

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import io.realm.RealmObject
import ru.its_365.agent365.R
import kotlinx.android.synthetic.main.fragment_order_customer.view.*


class RecyclerViewAdapter(val listener : View.OnClickListener? = null): RecyclerView.Adapter<RecyclerViewHolder>() {


    var list : MutableList<RealmObject> = ArrayList<RealmObject>()
   // val stringList : MutableList<String> = ArrayList<String>()

    public fun set(items : List<RealmObject>){
        list = items.toMutableList();
        notifyDataSetChanged()
    }

    public fun clear(){
        list.clear()
        //stringList.clear()
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
        holder.bind(list[position])
        //holder.bind(stringList[position])
    }
}




class RecyclerViewHolder( view: View, val listener : View.OnClickListener? = null) : RecyclerView.ViewHolder(view){
    val content     = view.title
    public fun bind(customer : RealmObject) {
        content.setText(customer.toString())
        content.tag = customer
        if(listener != null){
            content.setOnClickListener(listener)
        }
    }
}