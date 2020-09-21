package ru.its_365.agent365.activitys

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.EventLog
import android.view.*
import android.view.View.OnClickListener
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_select_dialog.*
import ru.its_365.agent365.R
import ru.its_365.agent365.tools.RxBus
import android.view.MenuInflater



class OnSearchebleSelectDialogOnSelectItems(val id:String)

class SearchebleSelectDialog() : AppCompatActivity(), OnClickListener {

    class Item(val title:String, val id:String)

    override fun onClick(p0: View?) {
        val id:String = p0?.tag as String
        try {
            selectItems(id)
        } catch (t: Throwable) {

        }
    }

    private var recyclerView : RecyclerView? = null

    companion object {
        var Events: RxBus = RxBus()
        var dataList : List<Item> = emptyList()

        fun Show(ctx:Context){
            val intent = Intent(ctx,SearchebleSelectDialog::class.java);
            ctx.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searcheble_select_dialog)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Выбор элемента"

        recyclerView = this.findViewById(R.id.list)

        if(dataList.size > 0){
            filligList(dataList)
        }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.search_only, menu)

        val item = menu?.findItem(R.id.action_search)
        val searchView = item?.actionView as android.support.v7.widget.SearchView
        searchView.setOnQueryTextListener(object : android.support.v7.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filligList(dataList,query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filligList(dataList,newText)
                return true
            }
        })

        return true
    }

    private fun filligList(list: List<Item>,query:String? = null){

        if(query == null){
            val adapter     = SearchebleSelectDialogAdapter(list,this)
            recyclerView?.layoutManager = LinearLayoutManager(this)
            recyclerView?.adapter   = adapter
        }else{
            val match = list.filter { it.title.contains(query,true) }
            val adapter     = SearchebleSelectDialogAdapter(match,this)
            recyclerView?.layoutManager = LinearLayoutManager(this)
            recyclerView?.adapter   = adapter
        }


    }


    private fun selectItems(id:String){
        Events.publish(OnSearchebleSelectDialogOnSelectItems(id))
        //Events = RxBus()
        onBackPressed()
    }



    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        //return super.onOptionsItemSelected(item)
        when(item?.itemId){
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return true
    }

    override fun onDestroy() {
        //Events.complete()
        super.onDestroy()
    }

}


public class SearchebleSelectDialogAdapter(val list:List<SearchebleSelectDialog.Item>,val itemSelectClickListener:OnClickListener):RecyclerView.Adapter<SearchebleSelectDialogAdapter.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView : View = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.count()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setText(list.get(position).title)
        holder.setListener(itemSelectClickListener)
        holder.setId(list.get(position).id)
    }

    public class  ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        private val _title:TextView = itemView.findViewById(android.R.id.text1) as TextView
        fun setText(value:String){
            _title.setText(value)
        }

        fun setListener(listener: OnClickListener){
            _title.setOnClickListener(listener)
        }

        fun setId(id:String){
            _title.tag = id
        }
    }
}
