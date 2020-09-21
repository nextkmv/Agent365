package ru.its_365.agent365.activitys

import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import io.realm.*
import io.realm.kotlin.where
import ru.its_365.agent365.R
import ru.its_365.agent365.db.model.Customer
import java.util.HashSet
import android.content.Context
import android.content.res.Resources
import android.support.v7.widget.DialogTitle
import android.view.*
import android.widget.SearchView
import kotlinx.android.synthetic.main.activity_select_dialog.*
import kotlinx.android.synthetic.main.activity_select_goods_filter.*
import kotlinx.android.synthetic.main.app_bar_main.*




class OnSelectGoodsFilterDialog(var ctx: SelectGoods) : Dialog(ctx), View.OnClickListener {
    override fun onClick(view: View?) {
        if (view?.id == R.id.btn_save){
            ctx.isGroupView = swIsGroupView.isChecked
            ctx.isStockOnly = sw_is_stock_only.isChecked
            ctx.sortByField = sp_sort_by_field.selectedItem.toString()
            ctx.onClick(view)
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_select_goods_filter)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        var width = (context.resources.displayMetrics.widthPixels * 1f).toInt();
        var height = (600 * 1f).toInt();
        window.setLayout(width, height)

        swIsGroupView.isChecked = ctx.isGroupView
        sw_is_stock_only.isChecked = ctx.isStockOnly


        setTitle("Настройка списка")
        btn_save.setOnClickListener(this)

    }



}


