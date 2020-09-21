package ru.its_365.agent365.tools
import android.support.v7.widget.RecyclerView
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup


abstract class AbstractRecyclerAdapter<E> : RecyclerView.Adapter<AbstractRecyclerAdapter.BindingHolder>() {

    private var mElements: List<E>? = null

    abstract val itemLayoutId: Int
    abstract val variableId: Int

    init {
        mElements = ArrayList()
    }

    fun setElements(elements: List<E>) {
        mElements = elements
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingHolder {
        val v = LayoutInflater.from(parent.context).inflate(itemLayoutId,
                parent, false)
        return BindingHolder(v)
    }

    override fun onBindViewHolder(holder: BindingHolder, position: Int) {
        val element = mElements!![position]
        holder.binding!!.setVariable(variableId, element)
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return mElements!!.size
    }

    class BindingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding: ViewDataBinding?

        init {
            binding = DataBindingUtil.bind(itemView)
        }
    }
}