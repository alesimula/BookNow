package com.pdd.booknow.fragment

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.pdd.booknow.Utilities

import com.pdd.booknow.databinding.InputCardBinding
import com.pdd.booknow.widget.ScalableImageView

import java.util.Calendar
import androidx.core.os.HandlerCompat.postDelayed
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import com.pdd.booknow.R


class DataBindingFragment : Fragment() {
    //interazione con il picker

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.input_card, container, false)
        /*val userViewModel = UserViewModel()
        userViewModel.setUser(User("王浩", "12345678912", 24, true))*/
        //binding.setFragmentUserViewModel(userViewModel)

        //Handler().postDelayed(Runnable { userViewModel.setUser(User("新用户名哈哈", "12345678912", 24, false)) }, 3000)

        return binding.getRoot()
    }

    fun show(manager: FragmentManager, tag: String) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commit()
    }


    /*val data: Calendar? = null
    private var listener: DatePickerFragmentListener? = null //variabile listener*/

    /*override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = (activity ?: Utilities.context).let {activity ->
        val context = context as Context
        AlertDialog.Builder(activity).apply {
            setView(InputCardBinding.inflate(LayoutInflater.from(activity)).root)
        }.create()
    }

    fun setDecorView(dialog: Dialog, view: View) {
        try {
            val phoneWindow = this.javaClass.classLoader!!.loadClass("com.android.internal.policy.PhoneWindow")
            val decorView = this.javaClass.classLoader!!.loadClass("com.android.internal.policy.DecorView")
            val field = phoneWindow.getDeclaredField("mDecor")
            val root = decorView.getDeclaredField("mFloatingActionModeOriginatingView")
            field.isAccessible = true
            root.isAccessible = true
            root.set(field.get(dialog.window), view)
        } catch (e: Exception) {
            Log.e("WTF:", "boh", e)
        }

    }

    override fun onResume() {
        super.onResume()
        val window = dialog.window
        (window!!.decorView as ViewGroup).clipToPadding = false
        (window.decorView as ViewGroup).clipChildren = false
        //window.getDecorView().getBackground().setAlpha(0);
        (window.decorView as ViewGroup).setPadding(100, 100, 100, 100)
        val image = ScalableImageView(context!!, null, 0)
        setDecorView(dialog, InputCardBinding.inflate(activity!!.layoutInflater).root)
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    //metodo di setter per il listener
    fun setOnDatePickerFragmentChanged(l: DatePickerFragmentListener) {
        this.listener = l
    }

    //interfaccia con i metodi che gestiranno gli eventi
    interface DatePickerFragmentListener {
        fun onDatePickerFragmentOkButton(dialog: DialogFragment, date: Calendar)
        fun onDatePickerFragmentCancelButton(dialog: DialogFragment)
    }*/
}
