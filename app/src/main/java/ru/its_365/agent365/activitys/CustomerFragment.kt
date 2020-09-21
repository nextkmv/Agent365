package ru.its_365.agent365.activitys

import android.content.Context
import android.os.Bundle
import android.app.Fragment
import android.content.Intent
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_customer_list.*
import ru.its_365.agent365.R
import ru.its_365.agent365.db.model.Customer
import ru.its_365.agent365.db.model.CustomerModel
import android.support.v4.view.MenuItemCompat.getActionView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.ListView
import android.widget.Toast
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.kotlin.where
import ru.its_365.agent365.activitys.adapters.RealmListViewAdapter
import ru.its_365.agent365.activitys.adapters.RecyclerViewAdapter





/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [CustomerFragment.OnListFragmentInteractionListener] interface.
 */
class CustomerFragment : Fragment(), View.OnClickListener {

    private var adapter : RealmListViewAdapter? = null
    var listCustomer : ListView? = null
    var realmResults: RealmResults<Customer>? = null
    var realm = Realm.getDefaultInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_customer_list, container, false)
        listCustomer = view.findViewById<ListView>(R.id.list_customer)
        refreshList()
        setHasOptionsMenu(true)
        return view
    }


    override fun onClick(view: View?) {
        if(view?.tag is Customer){
            val customer = view?.tag as Customer
            if(customer != null){
                val intent = Intent(this.activity, ClientInfo::class.java).apply {
                    putExtra("CustomerCode", customer?.code)
                }
                startActivity(intent)
            }
        }
    }

    fun refreshList(search : String = ""){



        if(search.isEmpty() || realmResults == null){
            realmResults = realm.where<Customer>().findAllAsync()
        }else{
            val keywords = search.split(" ")
            for (keyword in keywords) {
                val upKeyword = keyword.toUpperCase()
                realmResults =realmResults?.where()?.beginsWith("upperName", upKeyword)?.or()?.contains("upperName", upKeyword)?.findAllAsync()
            }
        }

        val adapter = RealmListViewAdapter(realmResults as OrderedRealmCollection<RealmObject>,this)
        listCustomer?.adapter = adapter


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onDetach() {
        realm.close()
        super.onDetach()

    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        menu?.clear()
        inflater?.inflate(R.menu.search_only, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        val item = menu?.findItem(R.id.action_search)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                refreshList(newText)
                return false
            }
        })

    }

}
