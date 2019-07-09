package com.pdd.booknow.databinding

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.pdd.booknow.collections.SortedMutableList
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

abstract class DataBindingAdapter<B : ViewDataBinding, O: Any> protected @JvmOverloads constructor (protected val elementList: MutableList<O>, private val bindingClass: KClass<B>, val listener: OnClickListener<B, O>?=null) :
    RecyclerView.Adapter<DataBindingAdapter<B, O>.DataBindingViewHolder>() {

    private val viewHolderMap: HashMap<O, DataBindingViewHolder> = HashMap()
    private var layoutInflater: LayoutInflater? = null
    private var bindFunction : (B.(element: O) -> Unit)? = null
    private var onClickFunction : (B.(element: O) -> Unit)? = null
    protected var onClickEditInfoFunction : (O.() -> Unit)? =null; set(block) {onClickFunction=null;field=block}

    inner class DataBindingViewHolder(val binding: B) : RecyclerView.ViewHolder(binding.root) {
        var element : O? = null
        fun update() = element?.let{bind(binding,it)}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = (layoutInflater ?: LayoutInflater.from(parent.context)).let {inflater->
        layoutInflater = inflater
        DataBindingViewHolder(bindingClass.inflate(inflater, parent))
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
        val element = elementList[position]
        holder.element = element
        viewHolderMap[element] = holder

        holder.binding.root.setOnClickListener {
            listener?.onClick(holder.binding, element)
            onClickFunction?.invoke(holder.binding, element) ?: onClickEditInfoFunction?.also {onClick->
                onClick(element)
                holder.update()
            }
        }
        holder.update()
    }

    override fun onViewRecycled(holder: DataBindingViewHolder) {
        viewHolderMap.remove(holder.element)
    }

    override fun getItemCount(): Int = elementList.size

    abstract protected fun O.bind(binding: B)

    private fun bind(binding: B, element: O) = bindFunction?.invoke(binding, element) ?: element.bind(binding)
    fun bind(block: B.(element: O) -> Unit) = apply{bindFunction=block}

    fun onClick(block: B.(element: O) -> Unit) = apply{onClickFunction=block}

    fun update(element: O) = viewHolderMap[element]?.update()
    fun update(index: Int) = elementList.getOrNull(index)?.let {update(it)}

    @JvmOverloads
    fun update(reload: Boolean=false) {
        if (reload) {
            viewHolderMap.clear()
            notifyDataSetChanged()
        }
        else for (element in elementList) update(element)
    }

    fun edit(element: O, block: O.()-> Unit) {
        block(element)
        update(element)
    }
    fun edit(index: Int, block: O.()-> Unit) = elementList.getOrNull(index)?.let {edit(it,block)}

    fun editView(element: O, block: B.(element: O)-> Unit) = viewHolderMap[element]?.binding?.apply {block(this, element)}
    fun editView(index: Int, block: B.(element: O) -> Unit) = elementList.getOrNull(index)?.let {editView(it,block)}

    fun remove(index: Int) : Boolean = if (elementList.size>index && index>=0) {
        viewHolderMap.remove(elementList.removeAt(index))
        if (elementList !is DataBindingList<*, *, *>) {notifyItemRemoved(index)}
        true
    } else false

    @JvmOverloads
    fun add(element: O, index: Int=elementList.size) : Boolean = when(elementList) {
        is DataBindingList<*, *, *> -> {
            elementList.add(element)
        }
        is SortedMutableList<O,*> -> {
            elementList.addElement(element)?.let {notifyItemInserted(it); true} ?: false
        }
        else -> {
            val i = max(0, min(index, elementList.size))
            elementList.add(i, element)
            notifyItemInserted(i)
            true
        }
    }

    interface OnClickListener<B: ViewDataBinding, O : Any?> {
        fun onClick(binding: B, post: O)
    }



    class DataBindingList<B : ViewDataBinding, O: Comparable<O>, A: DataBindingAdapter<B, O>> : SortedMutableList<O, DataBindingList.DataBindingCallback<B, O, A>> {
        class DataBindingCallback<B : ViewDataBinding, O: Comparable<O>, A: DataBindingAdapter<B, O>> (mAdapter: A?) : SortedMutableList.AbstractComparableCallback<O>() {
            private var mAdapter : A? = mAdapter
                set(adapter) {if (field==null) field = adapter}
            fun initAdapter(adapter: A) : A = let{mAdapter=adapter; adapter}

            val adapter by lazy {this.mAdapter!!}

            override fun onInserted(position: Int, count: Int) = this.mAdapter?.notifyItemRangeInserted(position, count) ?: Unit
            override fun onMoved(fromPosition: Int, toPosition: Int) = this.mAdapter?.notifyItemMoved(fromPosition, toPosition) ?: Unit
            override fun onChanged(position: Int, count: Int) = this.mAdapter?.notifyItemRangeChanged(position, count) ?: Unit
            override fun onRemoved(position: Int, count: Int) = this.mAdapter?.notifyItemRangeRemoved(position, count) ?: Unit
        }

        @JvmOverloads constructor(memberClass: KClass<O>, mAdapter: A, initialSize: Int?=null) : super (memberClass, DataBindingCallback(mAdapter), initialSize)
        constructor(memberClass: KClass<O>, block: DataBindingList<B, O, A>.()->A) : this(memberClass, null, block)
        constructor(memberClass: KClass<O>, elements: Collection<O>?, block: DataBindingList<B, O, A>.()->A) : super(memberClass, DataBindingCallback(null)) {
            elements?.let {addAll(elements)}
            callback.initAdapter(block.invoke(this))
        }

        val adapter get() = callback.adapter
    }
}