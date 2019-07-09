package com.pdd.booknow.delegate

import kotlin.reflect.KClass
import kotlin.reflect.KProperty

open class LazyWithLastReceiver<This: Any,Return> (val initializer:This.()->Return) {
    constructor(thisRef: KClass<This>, initializer:This.()->Return) : this(initializer)

    private var lastRef: This? = null
    private var value: Return? = null

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef:This,property:KProperty<*>):Return = runCatching {
        if (thisRef==lastRef) return value as Return
        else {
            lastRef = thisRef
            value = initializer.invoke(thisRef)
            value
        }
    }.getOrElse {
        throw ExceptionInInitializerError(this::class.java.simpleName+": value not initialized")
    } as Return
}