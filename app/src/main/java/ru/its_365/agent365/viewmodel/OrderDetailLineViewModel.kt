package ru.its_365.agent365.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.text.TextWatcher
import ru.its_365.agent365.BR
import android.text.Editable
import android.databinding.adapters.TextViewBindingAdapter.setPassword
import android.service.autofill.FillEventHistory
import android.view.View
import ru.its_365.agent365.activitys.OrderDetail
import ru.its_365.agent365.activitys.OrderDetailEventSelectGoods
import ru.its_365.agent365.db.model.PriceType
import ru.its_365.agent365.db.model.Stock
import ru.its_365.agent365.db.model.Unit


class OrderDetailLineViewModel(val goodsCode:String, val name:String, val articul:String, price:Float, qty:Float) : BaseObservable() {

    private var _qty:Float = qty
    private var _price:Float = price

    init {

    }

    @Bindable
    fun getQty(): Float {
        return _qty
    }

    fun setQty(value: Float) {
        // Avoids infinite loops.
        if (_qty != value) {
            _qty = value
            notifyPropertyChanged(BR.orderLine)
        }
    }


    @Bindable
    fun getPrice(): Float {
        return _price
    }

    fun setPrice(value: Float) {
        // Avoids infinite loops.
        if (_price != value) {
            _price = value
            notifyPropertyChanged(BR.orderLine)
        }
    }


    @Bindable
    fun getTotal(): Float {
        return _qty * _price
    }

    fun onClickItem(view: View) {
        OrderDetail.Events.publish(OrderDetailEventSelectGoods(view.tag as String))
    }

}