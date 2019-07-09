package com.pdd.booknow.collections

import androidx.recyclerview.widget.SortedList
import kotlin.reflect.KClass

open class SortedMutableList<O: Any, C: SortedList.Callback<O>> @JvmOverloads constructor(private var memberClass: KClass<O>, protected val callback: C, initialCapacity: Int?=null) : MutableList<O> {
    companion object {
        @JvmStatic @JvmOverloads inline fun<reified O: Any> ofAny(callback: SortedList.Callback<O>, initialCapacity: Int?=null)
                = SortedMutableList(O::class, callback, initialCapacity)
        @JvmStatic @JvmOverloads inline fun<reified O: Comparable<O>> ofComparable(initialCapacity: Int?=null)
                = SortedMutableList(O::class, ComparableCallback(), initialCapacity)
    }

    private val sortedList = initialCapacity?.let {SortedList<O>(memberClass.java, callback, initialCapacity)} ?: SortedList<O>(memberClass.java, callback)

    override val size get() = sortedList.size()
    override fun get(index: Int) = sortedList.get(index)
    override fun remove(element: O) = sortedList.remove(element)
    override fun removeAt(index: Int) = sortedList.removeItemAt(index)
    override fun iterator() = listIterator()
    override fun contains(element: O) = sortedList.indexOf(element)!=SortedList.INVALID_POSITION
    override fun indexOf(element: O) = sortedList.indexOf(element)
    override fun isEmpty() = size==0
    override fun lastIndexOf(element: O) = indexOf(element)
    override fun add(element: O) = sortedList.add(element)!=SortedList.INVALID_POSITION
    override fun add(index: Int, element: O) {add(element)}
    override fun addAll(elements: Collection<O>) = let {sortedList.addAll(elements);true}
    override fun addAll(index: Int, elements: Collection<O>) = addAll(elements)
    override fun clear() = sortedList.clear()
    override fun set(index: Int, element: O) = this[index].apply {removeAt(index); add(element)}
    override fun listIterator() = listIterator(0)

    fun addElement(element: O) = sortedList.add(element).let {if (it!=SortedList.INVALID_POSITION) it else null}
    fun removeElement(element: O) = sortedList.indexOf(element).let {if (it!=SortedList.INVALID_POSITION) {sortedList.remove(element);it} else null}
    fun updateElement(index: Int) = sortedList.updateItemAt(index, getOrNull(index))
    fun updateElement(element: O) = sortedList.updateItemAt(indexOf(element), element)
    fun editElement(index: Int, block: O.()->Unit) = getOrNull(index)?.apply(block)?.let {updateElement(it)}
    fun editElement(element: O, block: O.()->Unit) = updateElement(element.apply(block))

    override fun containsAll(elements: Collection<O>): Boolean {
        for (member in elements) if (member !in this) return false
        return true
    }

    override fun retainAll(elements: Collection<O>): Boolean {
        var returnValue = false
        for (member in this) if (member !in elements) {
            remove(member)
            returnValue = true
        }
        return returnValue
    }

    override fun removeAll(elements: Collection<O>): Boolean {
        var returnValue = false
        for (member in this) if (member in elements) {
            remove(member)
            returnValue = true
        }
        return returnValue
    }

    override fun subList(fromIndex: Int, toIndex: Int): SortedMutableList<O, C> {
        val subList = SortedMutableList(memberClass, callback)
        for (index in fromIndex..toIndex) getOrNull(index)?.apply {subList.add(this)}
        return subList
    }

    override fun listIterator(index: Int) = object : MutableListIterator<O> {
        var i = index
        var element : O? = null

        override fun next() = element?.apply{i++;element=null} ?: this@SortedMutableList[i++]
        override fun hasNext() = element?.let {true} ?: let {element = if (i < size) getOrNull(i) else null; element!=null}
        override fun remove() = this@SortedMutableList.removeAt(i).let {element=null}
        override fun hasPrevious() = if (i > 0) getOrNull(i-1)!=null else false;
        override fun previous() = this@SortedMutableList[--i]
        override fun nextIndex() = i
        override fun previousIndex() = i-1
        override fun add(element: O) {sortedList.add(element); Unit}
        override fun set(element: O) = getOrNull(i)?.let {removeAt(i); add(element)} ?: Unit
    }


    abstract class AbstractComparableCallback<D: Comparable<D>> :  SortedList.Callback<D>() {
        override fun compare(a1: D, a2: D) = a1.compareTo(a2)
        override fun areContentsTheSame(oldItem: D, newItem: D) = oldItem===newItem || oldItem.equals(newItem) || compare(oldItem,newItem)==0
        override fun areItemsTheSame(item1: D, item2: D) = areContentsTheSame(item1, item2)
    }

    class ComparableCallback<D: Comparable<D>> :  AbstractComparableCallback<D>() {
        override fun onInserted(position: Int, count: Int) {}
        override fun onMoved(fromPosition: Int, toPosition: Int) {}
        override fun onChanged(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
    }
}

@JvmOverloads inline fun<reified O: Comparable<O>> emptySortedList(initialCapacity: Int?=null)
        = SortedMutableList.ofComparable<O>(initialCapacity)
inline fun<reified O: Comparable<O>> sortedListOf(vararg elements: O)
        = emptySortedList<O>(elements.size).apply {addAll(elements)}