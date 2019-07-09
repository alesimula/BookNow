@file:Suppress("UNCHECKED_CAST")
package com.pdd.booknow.databinding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.pdd.booknow.delegate.LazyWithReceiver
import java.lang.reflect.Method
import java.io.InvalidClassException
import kotlin.reflect.KClass

private val <B: ViewDataBinding> KClass<B>.inflateMethod by LazyWithReceiver<KClass<B>, Method> {
    this.java.getMethod("inflate",
        LayoutInflater::class.java,
        ViewGroup::class.java,
        Boolean::class.java)
}

fun <B: ViewDataBinding> KClass<B>.validate() {
    try {this.inflateMethod}
    catch (e: NoSuchMethodException) {throw InvalidClassException("Invalid data binding class: "+this.java.name)}
}

fun <B: ViewDataBinding> KClass<B>.inflate(inflater: LayoutInflater, root: ViewGroup?, attachToRoot: Boolean=false) : B
        = this.inflateMethod.invoke(null, inflater, root, attachToRoot) as B

fun <B: ViewDataBinding> B.inflate(inflater: LayoutInflater, root: ViewGroup?, attachToRoot: Boolean=false) : B
        = this::class.inflate(inflater, root, attachToRoot)

val ViewDataBinding.context; get() = this.root.context
val ViewDataBinding.applicationContext; get() = this.context.applicationContext
val ViewDataBinding.resources; get() = this.applicationContext.resources
val ViewDataBinding.metrics; get() = this.resources.displayMetrics