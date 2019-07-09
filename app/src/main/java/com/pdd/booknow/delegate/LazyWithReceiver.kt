package com.pdd.booknow.delegate

import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class LazyWithReceiver<This: Any,Return> (weak: Boolean?=true, nullable: Nothing?, val initializer:This?.()->Return) {
    constructor(weak: Boolean?=true, initializer:This.()->Return) : this(weak, null, initializer as This?.()->Return)
    constructor(thisRef: KClass<This>, weak: Boolean?=true, nullable: Nothing?, initializer:This?.()->Return) : this(weak, null, initializer)
    constructor(thisRef: KClass<This>, weak: Boolean?=true, initializer:This.()->Return) : this(weak, initializer)

    private val values : MutableMap<This?,Return> = if (weak!=null) {if (weak) WeakHashMap() else HashMap()} else object : TreeMap<This?,Return>() {
        var singleton : Return? = null
        override fun get(key: This?) = singleton
        override fun put(key: This?, value: Return) = singleton ?: let {singleton=value; null}
        /*override val size = singleton?.let {1} ?: 0
        override fun isEmpty() = singleton?.let {false} ?: true
        override fun containsKey(key: This) = singleton?.let {false} ?: true
        override fun containsValue(value: Return): Boolean = singleton?.equals(value) ?: false*/
    }

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef:Any?,property:KProperty<*>):Return = synchronized(values) {
        thisRef as This?
        return values.getOrPut(thisRef) {runCatching {initializer.invoke(thisRef)}.getOrElse {
            throw ExceptionInInitializerError(this::class.java.simpleName+": value not initialized")
        }}
    }

    class Consumer<This: Any> (weak: Boolean?=true, initializer:This.()->Unit) : LazyWithReceiver<This, Unit>(weak,{initializer()})
}