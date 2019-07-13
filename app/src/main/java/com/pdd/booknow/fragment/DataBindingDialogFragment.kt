package com.pdd.booknow.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager

import com.pdd.booknow.R
import com.pdd.booknow.databinding.inflate
import com.pdd.booknow.delegate.LazyWithLastReceiver
import java.io.*

import kotlin.reflect.KClass

class DataBindingDialogFragment<B : ViewDataBinding> @JvmOverloads constructor(private var bindingClass : KClass<B>, context: Context?=null) : AppCompatDialogFragment() {
    companion object {
        @JvmStatic inline fun <reified B: ViewDataBinding> create(fragmentManager: FragmentManager, tag: String?, noinline block: (B.(DataBindingDialogFragment<B>)->Unit)) = DataBindingDialogFragment(B::class, block).show(fragmentManager, tag).let {true}
        @JvmStatic inline fun <reified B: ViewDataBinding> create(fragmentManager: FragmentManager, noinline block: (B.(DataBindingDialogFragment<B>)->Unit)) = create(fragmentManager, null, block)
    }

    constructor(bindingClass : KClass<B>, context: Context?=null, block: (B.(DataBindingDialogFragment<B>)->Unit)) : this(bindingClass, context) {bind = block}
    constructor(bindingClass : KClass<B>, block: (B.(DataBindingDialogFragment<B>)->Unit)) : this(bindingClass, null, block)
    constructor() : this(ViewDataBinding::class as KClass<B>)

    fun Serializable.serializeToBytes(): ByteArray = ByteArrayOutputStream().use {ObjectOutputStream(it).writeObject(this); it }.toByteArray()
    inline fun <reified T: Any> ByteArray.deserialize(): T = ObjectInputStream(inputStream()).readObject() as T
    inline fun <reified T: Serializable> T.serializeDeserialize() = (this as Serializable).serializeToBytes().deserialize<T>()

    private val (B.(DataBindingDialogFragment<B>)->Unit).funData: ByteArray by LazyWithLastReceiver<(B.(DataBindingDialogFragment<B>)->Unit), ByteArray> {(this as Serializable).serializeToBytes()}

    @FunctionalInterface
    private interface OnBindInterface {
        fun<B: ViewDataBinding> B.bind(fragment: DataBindingDialogFragment<B>)
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        val bindingJavaClass = bindingClass.java
        state.putByteArray("bind", (bind as Serializable).serializeToBytes())
        state.putByteArray("bindingClass", ({bindingJavaClass.kotlin} as Serializable).serializeToBytes())
    }

    private fun onRestoreInstance(state: Bundle?) {
        state?.apply {
            bind = state.getByteArray("bind")?.deserialize()
            bindingClass = state.getByteArray("bindingClass")?.deserialize<()->KClass<B>>()!!.invoke()
        }
    }

    private val binding: B by lazy {bindingClass.inflate(LayoutInflater.from(context?:this.context), null)}
    private val root get() = binding.root

    private var bind : (B.(DataBindingDialogFragment<B>)->Unit)? = null; set(value) {
        runCatching {value?.funData}.getOrElse {e->throw IllegalArgumentException("DataBinding lambda must be serializable (should not contain references to non-serializable objects)", e)}
        field = value
    }
    fun bind(block: B.(DataBindingDialogFragment<B>)->Unit) {
        this.bind = block
    }

    private var onShow: (DataBindingDialogFragment<B>.(Dialog)->Unit)? = null
    fun onShow(block: DataBindingDialogFragment<B>.(Dialog)->Unit) {
        this.onShow = block
    }

    private var onCreateView: (View.(Dialog)->Unit)? = null
    fun onCreateView(block: View.(Dialog)->Unit) {
        this.onCreateView = block
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onRestoreInstance(savedInstanceState)
        if (kotlin.runCatching{binding}.isFailure) return Dialog(context)

        return AlertDialog.Builder(activity!!, R.style.Theme_AppCompat_Light_Dialog_Alert).apply {
            CardView(context).apply {
                ViewCompat.setBackground(this, ColorDrawable(Color.TRANSPARENT))
                useCompatPadding = true
                clipToPadding = false
                clipChildren = false
                radius = 0f
                cardElevation = 0f
                addView(root)
            }.let {setView(it)}
        }.create().apply {
            setOnShowListener {
                Log.e("AAAAA", "onshow")
                bind?.invoke(binding, this@DataBindingDialogFragment)
                onShow?.invoke(this@DataBindingDialogFragment, dialog)
                window.decorView.apply {
                    onCreateView?.invoke(this, dialog)
                }
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            }
            (window.decorView as? ViewGroup)?.apply {
                ViewCompat.setBackground(this, ColorDrawable(Color.TRANSPARENT))
                clipChildren = false
                clipToPadding = false
            }
        }
    }
}