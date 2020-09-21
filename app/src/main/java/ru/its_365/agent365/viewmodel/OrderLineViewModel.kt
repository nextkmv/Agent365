package ru.its_365.agent365.viewmodel

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.text.TextWatcher
import ru.its_365.agent365.BR
import android.text.Editable
import android.databinding.adapters.TextViewBindingAdapter.setPassword
import android.service.autofill.FillEventHistory
import ru.its_365.agent365.db.model.PriceType
import ru.its_365.agent365.db.model.Stock
import ru.its_365.agent365.db.model.Unit


class OrderLineViewModel(val goodsCode:String, val name:String, val articul:String, var stock: Float,var priceType: PriceType, price:Float, qty:Float, var unit:Unit, var history:String) : BaseObservable() {

    private var _qty:Float = qty
    private var _stock:Float = stock
    private var _price:Float = price
    private var _unit:Unit = unit


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
            notifyPropertyChanged(BR.total)
        }
    }

    @Bindable
    public fun getQtyTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                try {
                    setQty(s.toString().toFloat())
                }
                catch (e:NumberFormatException){
                    setQty(0f)
                }
            }

            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
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
            notifyPropertyChanged(BR.total)
        }
    }

    @Bindable
    public fun getPriceTextWatcher(): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                setPrice(s.toString().toFloat())
            }

            override fun afterTextChanged(s: Editable) {
                // Do nothing.
            }
        }
    }

    @Bindable
    fun getTotal(): Float {
        return _qty * _price
    }


}