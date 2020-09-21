package ru.its_365.agent365.activitys

import android.app.Dialog
import android.os.Bundle
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import io.realm.*
import ru.its_365.agent365.R
import java.util.HashSet
import android.content.Context
import android.view.*
import kotlinx.android.synthetic.main.activity_select_dialog.*


interface OnSelectDialog : View.OnClickListener{
    fun refresh(realmResults: RealmResults<Any>?, searchString : String? = null) : RealmResults<Any>
}

class SelectDialog(context: Context, val refresheble : OnSelectDialog, val serachheble:Boolean = false, val title: String? = null) : Dialog(context), View.OnClickListener {

    private var list : ListView? = null
    private var realmResults: RealmResults<Any>? = null
    private var realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_select_dialog)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        var width = (context.resources.displayMetrics.widthPixels * 1f).toInt();
        var height = (context.resources.displayMetrics.heightPixels * 1f).toInt();
        window.setLayout(width, height)



        toolbarDialog.setTitle(title)
        toolbarDialog.setOnClickListener(this)
        if(serachheble == true) {
            toolbarDialog.inflateMenu(R.menu.search_only)
            val item = toolbarDialog.menu?.findItem(R.id.action_search)
            val searchView = item?.actionView as android.support.v7.widget.SearchView
            searchView.setOnQueryTextListener(object : android.support.v7.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    refreshList(query)
                    return true
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    refreshList(newText)
                    return true
                }
            })
        }
        list = findViewById<ListView>(R.id.select_dialog_list_view)
        refreshList()
    }



    private fun refreshList(searchString : String? = null){
        realmResults    = refresheble.refresh(realmResults as RealmResults?, searchString)
        val adapter     = SelectDialogAdapter(realmResults as OrderedRealmCollection<RealmObject>, this)
        list?.adapter   = adapter
    }

    override fun onStop() {
        realm.close()
        super.onStop()
    }

    override fun onClick(view: View?) {
        if (view?.id == android.R.id.home) {
            dismiss()
        }else{
            refresheble.onClick(view)
            dismiss()
        }
    }
}

class SelectDialogAdapter(realmResults: OrderedRealmCollection<RealmObject>,val sd : SelectDialog) : RealmBaseAdapter<RealmObject>(realmResults), ListAdapter {

    private var inDeletionMode = false
    private val countersToDelete = HashSet<String>()

    init {
        notifyDataSetChanged()
    }

    public class ViewHolder {
        internal var content: TextView? = null
    }

    public class TagHolder(var viewHolder : ViewHolder, var realmObject: RealmObject)

    internal fun enableDeletionMode(enabled: Boolean) {
        inDeletionMode = enabled
        if (!enabled) {
            countersToDelete.clear()
        }
        notifyDataSetChanged()
    }

    internal fun getCountersToDelete(): Set<String> {
        return countersToDelete
    }

    override fun getCount(): Int {
        //return super.getCount()
        if (adapterData != null) {
            return adapterData!!.count()
        }else{
            return 0
        }
    }


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.select_dialog_item, parent, false)
            viewHolder = ViewHolder()
            viewHolder.content = convertView!!.findViewById<View>(R.id.title) as TextView
            convertView.tag = TagHolder(viewHolder,adapterData!![position])
        } else {
            val tagHolder = convertView.tag as TagHolder
            viewHolder = tagHolder.viewHolder
        }

        if (adapterData != null) {
            val item = adapterData!![position]
            viewHolder.content!!.text = item.toString()
            convertView.setOnClickListener(sd)
        }
        return convertView
    }
}


