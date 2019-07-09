package com.pdd.booknow.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_boh.*

import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.pdd.booknow.R
import com.pdd.booknow.Utilities
import com.pdd.booknow.mindimPixels
import android.graphics.drawable.BitmapDrawable
import android.graphics.Canvas
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pdd.booknow.databinding.*
import com.pdd.booknow.fragment.DataBindingDialogFragment
import kotlinx.android.synthetic.main.fragment_menu_main.*


class ActivityIDK : AppCompatActivity() {
    class FoodType(val name: String, resId: Int) {
        val image by lazy {ResourcesCompat.getDrawable(Utilities.resources, resId, null)}
    }

    class User(val name: String, iconId: Int) {
        companion object {
            @JvmStatic val toolbar_size by lazy {Utilities.context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize)).let {typedArray ->
                val size = typedArray.getDimension(0,TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, Utilities.displayMetrics)); typedArray.recycle(); size.toInt()}
            }
            @JvmStatic val icon_size_toolbar by lazy {(toolbar_size * 0.7).toInt()}
        }
        val icon by lazy {ResourcesCompat.getDrawable(Utilities.resources, iconId, null)}
        val icon_toolbar by lazy {icon?.apply{setBounds(0, 0, intrinsicWidth, intrinsicHeight)}?.let {icon ->
            val bitmap = Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888).apply {Canvas(this).apply {icon.draw(this)}}
            BitmapDrawable(Utilities.resources, Bitmap.createScaledBitmap(bitmap, icon_size_toolbar, icon_size_toolbar, false))
        }}
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.toolbar_profile -> {
            DataBindingDialogFragment.create<FragmentUserInfoBinding>(supportFragmentManager) { fragment ->
                user = User("Luiggi", R.drawable.ic_user_default)
                fragment.onShow {dialog->
                    //dialog.setCanceledOnTouchOutside(false)
                }
            }
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu_toolbar.inflateMenu(R.menu.menu_user)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding : ActivityBohBinding = DataBindingUtil.setContentView(this, R.layout.activity_boh)
        //menu_toolbar.inflateMenu(R.menu.menu_user)
        setSupportActionBar(menu_toolbar)

        if (savedInstanceState==null) supportFragmentManager.beginTransaction().add(R.id.fragment_container, FragmentMenuMain()).commit()

        val user = User("Table n. 5", R.drawable.ic_user_default)

        binding.user = user
        binding.paddingHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
        binding.paddingVertical = binding.paddingHorizontal

        /*fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/
    }

    class FragmentMenuMain : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentMenuMainBinding.inflate(inflater).apply {
            title = "Menù"
            paddingHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
            paddingVertical = paddingHorizontal
        }.let {it.root}

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val menuList = arrayListOf<FoodType>()
            menuList.add(FoodType("Starters", R.drawable.ic_food_starters))
            menuList.add(FoodType("First course", R.drawable.ic_food_first_course))
            menuList.add(FoodType("Main course", R.drawable.ic_food_main_course))
            menuList.add(FoodType("Pizza", R.drawable.ic_food_pizza))
            menuList.add(FoodType("Burgers", R.drawable.ic_food_burgers))
            menuList.add(FoodType("Side dishes", R.drawable.ic_food_side_dishes))
            menuList.add(FoodType("Fruit", R.drawable.ic_food_fruit))
            menuList.add(FoodType("Desserts", R.drawable.ic_food_dessert))
            menuList.add(FoodType("Drinks", R.drawable.ic_food_drinks))

            val inflater1 = GridLayoutManager(context, if (Utilities.isPortrait()) 2 else 3)
            val adapter = object : DataBindingAdapter<LayoutCardBinding, FoodType>(menuList, LayoutCardBinding::class) {
                override fun FoodType.bind(binding: LayoutCardBinding) {
                    binding.marginHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
                    binding.marginVertical = binding.marginHorizontal
                    binding.image = image
                    binding.imageAspectRatio = 1.0
                    binding.title = name
                    onClick {
                        activity!!.supportFragmentManager.beginTransaction().apply {
                            setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down)
                            replace(R.id.fragment_container, FragmentMenuCategory())
                            addToBackStack(null)
                        }.commit()
                    }
                }
            }

            my_recycler_view.setLayoutManager(inflater1)
            my_recycler_view.setAdapter(adapter);
            my_recycler_view.isNestedScrollingEnabled = false
        }
    }

    class FragmentMenuCategory : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentMenuMainBinding.inflate(inflater).apply {
            title = "Primi"
            paddingHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
            paddingVertical = paddingHorizontal
        }.let {it.root}

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val menuList = arrayListOf<FoodType>()
            menuList.add(FoodType("Starters", R.drawable.ic_food_starters))
            menuList.add(FoodType("First course", R.drawable.ic_food_first_course))
            menuList.add(FoodType("Main course", R.drawable.ic_food_main_course))
            menuList.add(FoodType("Pizza", R.drawable.ic_food_pizza))
            menuList.add(FoodType("Burgers", R.drawable.ic_food_burgers))
            menuList.add(FoodType("Side dishes", R.drawable.ic_food_side_dishes))
            menuList.add(FoodType("Fruit", R.drawable.ic_food_fruit))
            menuList.add(FoodType("Desserts", R.drawable.ic_food_dessert))
            menuList.add(FoodType("Drinks", R.drawable.ic_food_drinks))

            val inflater1 = LinearLayoutManager(context)
            val adapter = object : DataBindingAdapter<LayoutCardVarticalBinding, FoodType>(menuList, LayoutCardVarticalBinding::class) {
                override fun FoodType.bind(binding: LayoutCardVarticalBinding) {
                    binding.marginHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
                    binding.marginVertical = binding.marginHorizontal
                    binding.image = image
                    binding.imageAspectRatio = 1.0
                    binding.title = name
                    onClick {
                        activity!!.supportFragmentManager.beginTransaction().apply {
                            setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down)
                            replace(R.id.fragment_container, FragmentMenuMain())
                            addToBackStack(null)
                        }.commit()
                    }
                }
            }

            my_recycler_view.setLayoutManager(inflater1)
            my_recycler_view.setAdapter(adapter);
            my_recycler_view.isNestedScrollingEnabled = false
        }
    }
}
