package com.pdd.booknow.activity

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.pdd.booknow.R
import com.pdd.booknow.Utilities
import com.pdd.booknow.databinding.*
import com.pdd.booknow.fragment.DataBindingDialogFragment
import com.pdd.booknow.mindimPixels
import kotlinx.android.synthetic.main.activity_cart.*
import kotlinx.android.synthetic.main.fragment_menu_add.view.*

class ActivityCart : AppCompatActivity() {

    companion object {
        @JvmStatic fun kthanksbye(activity: FragmentActivity) = DataBindingDialogFragment.create<FragmentMenuHurrayBinding>(activity.supportFragmentManager) { fragment ->
            fragment.onShow {dialog ->
                dialog.setCanceledOnTouchOutside(false)
            }
            fragment.onCreateView {
                button_confirm.setOnClickListener{fragment.activity?.finish()}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val logged = intent.extras.getBoolean("logged")
        val binding : ActivityCartBinding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        //setSupportActionBar(null)
        supportActionBar?.hide()


        val menuList = arrayListOf(
                Food("Marinara meatballs", R.drawable.ic_starters_marinara_meatballs),
                Food("Water (sparkling)", R.drawable.ic_drinks_water_sparkling),
                Food("Coke", R.drawable.ic_drinks_coke),
                Food("Hummus", R.drawable.ic_starters_hummus),
                Food("Rice supplì", R.drawable.ic_starters_rice_suppli),
                Food("Zucchini flowers", R.drawable.ic_starters_zucchini_flowers),
                Food("Red wine", R.drawable.ic_drinks_wine_red),
                Food("Beer", R.drawable.ic_drinks_beer)
        )

        val inflater1 = LinearLayoutManager(baseContext)
        val adapter = object : DataBindingAdapter<LayoutCardHorizontalBinding, Food>(menuList, LayoutCardHorizontalBinding::class) {
            override fun Food.bind(binding: LayoutCardHorizontalBinding) {
                binding.marginVertical = Utilities.scale(resources.displayMetrics.mindimPixels,0.015)
                binding.marginHorizontal = if (Utilities.isPortrait()) binding.marginVertical else binding.marginVertical*10
                binding.aspectRatio = 4.0
                binding.name = name
                binding.image = image
                binding.price = price
                binding.quantity = 2
                binding.timeMinutes = time
                binding.ratingStars = rating
                onClick {}
            }
        }

        cart_review_recyclerview.setLayoutManager(inflater1)
        cart_review_recyclerview.setAdapter(adapter);
        cart_review_recyclerview.isNestedScrollingEnabled = false

        button_pay_register.setOnClickListener {kthanksbye(this)}
        button_pay_card.setOnClickListener {
            if (!logged) DataBindingDialogFragment.create<FragmentUserInfoCreditcardBinding>(supportFragmentManager) { fragment ->
                showConfirm = true
                fragment.onCreateView {
                    button_confirm.setOnClickListener {
                        //fragment.activity?.finish()
                        kthanksbye(fragment.activity!!)
                    }
                }
            }
            else {kthanksbye(this)}
        }

        val user = ActivityIDK.User("Table n. 5", R.drawable.ic_user_default)
    }
}
