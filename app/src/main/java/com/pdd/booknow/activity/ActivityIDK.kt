package com.pdd.booknow.activity

import android.content.Intent
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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.*
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pdd.booknow.databinding.*
import com.pdd.booknow.fragment.DataBindingDialogFragment
import kotlinx.android.synthetic.main.fragment_coupon_redeem.view.*
import kotlinx.android.synthetic.main.fragment_menu_add.view.*
import kotlinx.android.synthetic.main.fragment_menu_add.view.button_confirm
import kotlinx.android.synthetic.main.fragment_menu_main.*
import kotlinx.android.synthetic.main.fragment_user_info.view.*
import kotlinx.android.synthetic.main.layout_editable_info.view.*
import java.io.Serializable


typealias Food = ActivityIDK.FoodType.Food
fun foodListOf(categoty: String, vararg food: Food) = ActivityIDK.FoodType(categoty,R.drawable.ic_food_first_course, *food).food

object Restaurant {
    @JvmStatic val toolbar_size by lazy {Utilities.context.theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize)).let {typedArray ->
        val size = typedArray.getDimension(0,TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56f, Utilities.displayMetrics)); typedArray.recycle(); size.toInt()}
    }
    @JvmStatic val icon_size_toolbar by lazy {(toolbar_size * 0.7).toInt()}

    val icon by lazy {ResourcesCompat.getDrawable(Utilities.resources, R.drawable.ic_restaurant, null)}
    val icon_toolbar by lazy {icon?.apply{setBounds(0, 0, intrinsicWidth, intrinsicHeight)}?.let {icon ->
        val bitmap = Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888).apply {Canvas(this).apply {icon.draw(this)}}
        BitmapDrawable(Utilities.resources, Bitmap.createScaledBitmap(bitmap, icon_size_toolbar, icon_size_toolbar, false))
    }}
}

class ActivityIDK : AppCompatActivity() {
    class FoodType(val name: String, private var resId: Int, vararg foods: Food) {
        private companion object {
            @JvmStatic val ICON_EMPTY = ColorDrawable(Color.TRANSPARENT)
        }

        val image by lazy {ResourcesCompat.getDrawable(Utilities.resources, resId, null)}
        val food by lazy {FoodList().apply {addAll(foods)}}

        class Food(val name: String="", private var resId: Int?=null, val price: Double = 4.89, val time: Int = 3, val rating: Int = 0) : Serializable {
            @Transient
            private var mImage : Drawable? = null
            val image; get() = runCatching {mImage?:ResourcesCompat.getDrawable(Utilities.resources, resId!!, null)?.let {mImage=it; it}}.getOrElse {ICON_EMPTY}
            fun setImage(resId: Int) {this.resId = resId}
        }

        inner class FoodList : ArrayList<Food>() {
            val name; get() = this@FoodType.name
            private fun Food.setImage() = apply {if (image === ICON_EMPTY) setImage(resId)}
            private fun Collection<Food>.setImage() = onEach {it.setImage()}

            override fun add(element: Food) = super.add(element.setImage())
            override fun add(index: Int, element: Food) = super.add(index, element.setImage())
            override fun addAll(elements: Collection<Food>) = super.addAll(elements.setImage())
            override fun addAll(index: Int, elements: Collection<Food>) = super.addAll(index, elements.setImage())
        }
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
            if (!logged) {
                val showResults = Intent(this@ActivityIDK, MainActivity::class.java)
                //showResults.putExtra(EXTRA_USER, User())
                startActivity(showResults)
                true
            }
            else DataBindingDialogFragment.create<FragmentUserInfoBinding>(supportFragmentManager) { fragment ->
                user = User("Luiggi", R.drawable.ic_user_default)
                fragment.onShow {dialog->
                    //dialog.setCanceledOnTouchOutside(false)
                }
                fragment.onCreateView {
                    for (view in layout_user_info.children) view.apply { editable_info_button.setOnClickListener {
                        val binding = DataBindingUtil.getBinding<LayoutEditableInfoBinding>(this)
                        binding?.editing?.let {binding.editing = !it}
                        val text = info_edit_text_edit.text.toString()
                        binding?.text = text
                    }}
                    layout_user_info.getChildAt(3).apply { editable_info_button.setOnClickListener {
                        DataBindingDialogFragment.create<FragmentUserInfoCreditcardBinding>(fragment.activity!!.supportFragmentManager) {fragment ->

                        }
                    }}
                }
            }
        }
        R.id.toolbar_cart -> {
            val activityCart = Intent(this, ActivityCart::class.java)
            //activityCart.putExtra(EXTRA_USER, User())
            startActivity(activityCart)
            true
        }
        R.id.toolbar_coupon -> {
            DataBindingDialogFragment.create<FragmentCouponRedeemBinding>(supportFragmentManager) { fragment ->
                //user = User("Luiggi", R.drawable.ic_user_default)
                fragment.onShow {dialog ->
                    //dialog.setCanceledOnTouchOutside(false)
                }
                fragment.onCreateView {dialog ->
                    button_confirm.setOnClickListener {dialog.dismiss()}
                    val addCoupon : (name: String, icon: Int)->Unit = {name, icon-> LayoutCouponBinding.inflate(LayoutInflater.from(this.context), coupon_container, true).apply {
                        this.desc = name
                        this.icon = ResourcesCompat.getDrawable(root.context.resources, icon, null)
                        root.setOnClickListener {selected = !selected}
                    }}
                    addCoupon("10% discount", R.drawable.ic_coupon_single)
                    addCoupon("Free drink", R.drawable.ic_coupon_single)
                    addCoupon("Free dessert", R.drawable.ic_coupon_single)
                    addCoupon("5% discount", R.drawable.ic_coupon_single)
                    addCoupon("10% discount", R.drawable.ic_coupon_single)
                    //coupon_container.addView()
                }
            }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    var logged = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu_toolbar.inflateMenu(if (logged) R.menu.menu_user else R.menu.menu_guest)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        logged = runCatching {intent?.extras?.getBoolean("logged")?:false}.getOrDefault(false)
        super.onCreate(savedInstanceState)
        val binding : ActivityBohBinding = DataBindingUtil.setContentView(this, R.layout.activity_boh)
        //menu_toolbar.inflateMenu(R.menu.menu_user)
        setSupportActionBar(menu_toolbar)

        if (savedInstanceState==null) supportFragmentManager.beginTransaction().add(R.id.fragment_container, FragmentMenuMain(logged)).commit()

        val user = User("Table n. 5", R.drawable.ic_restaurant)

        binding.user = user

        /*fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/
    }

    class FragmentMenuMain @JvmOverloads constructor(var isLogged: Boolean?=null) : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentMenuMainBinding.inflate(inflater).apply {
            if (isLogged==null) isLogged = savedInstanceState?.getBoolean("logged", false)
            logged = isLogged?:false
            title = "Menù"
            paddingHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
            paddingVertical = paddingHorizontal
        }.let {it.root}

        override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putBoolean("logged", isLogged?:false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val menuList = arrayListOf<FoodType>()
            menuList.add(FoodType("Starters", R.drawable.ic_food_starters,
                    Food("Marinara meatballs", R.drawable.ic_starters_marinara_meatballs),
                    Food("Shrimp cocktail", R.drawable.ic_starters_cocktail_shrimp),
                    Food("Mozzarella in Carrozza", R.drawable.ic_starters_mozzarella_in_carrozza),
                    Food("Hummus", R.drawable.ic_starters_hummus),
                    Food("Rice supplì", R.drawable.ic_starters_rice_suppli),
                    Food("Zucchini flowers", R.drawable.ic_starters_zucchini_flowers),
                    Food("Puff pastry Pizza", R.drawable.ic_starters_puff_pastry_pizza)
            ))
            menuList.add(FoodType("First course", R.drawable.ic_food_first_course))
            menuList.add(FoodType("Main course", R.drawable.ic_food_main_course))
            menuList.add(FoodType("Pizza", R.drawable.ic_food_pizza))
            menuList.add(FoodType("Burgers", R.drawable.ic_food_burgers))
            menuList.add(FoodType("Side dishes", R.drawable.ic_food_side_dishes))
            menuList.add(FoodType("Fruit", R.drawable.ic_food_fruit))
            menuList.add(FoodType("Desserts", R.drawable.ic_food_dessert))
            menuList.add(FoodType("Drinks", R.drawable.ic_food_drinks,
                    Food("Water (still)", R.drawable.ic_drinks_water),
                    Food("Water (sparkling)", R.drawable.ic_drinks_water_sparkling),
                    Food("Coke", R.drawable.ic_drinks_coke),
                    Food("Sprite", R.drawable.ic_drinks_sprite),
                    Food("Fanta", R.drawable.ic_drinks_fanta),
                    Food("Red wine", R.drawable.ic_drinks_wine_red),
                    Food("White wine", R.drawable.ic_drinks_wine_white),
                    Food("Beer", R.drawable.ic_drinks_beer),
                    Food("Root beer", R.drawable.ic_drinks_root_beer)
            ))

            val inflater1 = GridLayoutManager(context, if (Utilities.isPortrait()) 2 else 3)
            val adapter = object : DataBindingAdapter<LayoutCardBinding, FoodType>(menuList, LayoutCardBinding::class) {
                override fun FoodType.bind(binding: LayoutCardBinding) {
                    binding.marginHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
                    binding.marginVertical = binding.marginHorizontal
                    binding.image = image
                    binding.imageAspectRatio = 1.0
                    binding.title = name
                    onClick {element ->
                        activity!!.supportFragmentManager.beginTransaction().apply {
                            setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down)
                            val args = Bundle().apply {putSerializable("list", element.food)}
                            replace(R.id.fragment_container, FragmentMenuCategory().apply {arguments = args})
                            addToBackStack(null)
                        }.commit()
                    }
                }
            }

            val recents = foodListOf("Just for you",
                    Food("Beer", R.drawable.ic_drinks_beer),
                    Food("Mozzarella in Carrozza", R.drawable.ic_starters_mozzarella_in_carrozza),
                    Food("Zucchini flowers", R.drawable.ic_starters_zucchini_flowers),
                    Food("Marinara meatballs", R.drawable.ic_starters_marinara_meatballs),
                    Food("Root beer", R.drawable.ic_drinks_root_beer)
            )

            val offers = foodListOf("Today's offers",
                    Food("Coke", R.drawable.ic_drinks_coke),
                    Food("Sprite", R.drawable.ic_drinks_sprite),
                    Food("Mozzarella in Carrozza", R.drawable.ic_starters_mozzarella_in_carrozza),
                    Food("Hummus", R.drawable.ic_starters_hummus),
                    Food("Rice supplì", R.drawable.ic_starters_rice_suppli),
                    Food("Zucchini flowers", R.drawable.ic_starters_zucchini_flowers)
            )

            button_recent.setOnClickListener {
                activity!!.supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down)
                    val args = Bundle().apply {putSerializable("list", recents)}
                    replace(R.id.fragment_container, FragmentMenuCategory().apply {arguments = args})
                    addToBackStack(null)
                }.commit()
            }

            button_offers.setOnClickListener {
                activity!!.supportFragmentManager.beginTransaction().apply {
                    setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up, R.anim.slide_in_down, R.anim.slide_out_down)
                    val args = Bundle().apply {putSerializable("list", offers)}
                    replace(R.id.fragment_container, FragmentMenuCategory().apply {arguments = args})
                    addToBackStack(null)
                }.commit()
            }

            my_recycler_view.setLayoutManager(inflater1)
            my_recycler_view.setAdapter(adapter);
            my_recycler_view.isNestedScrollingEnabled = false
        }
    }

    class FragmentMenuCategory : Fragment() {
        val menuList by lazy {arguments?.getSerializable("list") as FoodType.FoodList}

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentMenuCategoryBinding.inflate(inflater).apply {
            title = menuList.name
            paddingHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
            paddingVertical = paddingHorizontal
        }.let {it.root}

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            val inflater1 = LinearLayoutManager(context)
            val adapter = object : DataBindingAdapter<LayoutCardHorizontalBinding, Food>(menuList, LayoutCardHorizontalBinding::class) {
                override fun Food.bind(binding: LayoutCardHorizontalBinding) {
                    binding.marginHorizontal = Utilities.scale(resources.displayMetrics.mindimPixels,0.03)
                    binding.marginVertical = binding.marginHorizontal
                    binding.aspectRatio = 4.0
                    binding.name = name
                    binding.image = image
                    binding.price = price
                    binding.timeMinutes = time
                    binding.ratingStars = rating
                    onClick {element ->
                        DataBindingDialogFragment.create<FragmentMenuAddBinding>(activity!!.supportFragmentManager) { fragment ->
                            food = element
                            quantity = 1
                            fragment.onShow {dialog->
                                //dialog.setCanceledOnTouchOutside(false)
                            }
                            fragment.onCreateView {dialog ->
                                button_selection_add.setOnClickListener {
                                    quantity++
                                }
                                button_selection_remove.setOnClickListener {
                                    if (quantity>1) quantity--
                                }
                                button_menu_edit.setOnClickListener(object : OnClickWithFood(element) {
                                    override fun onClick(v: View?, foodParam: Food) {
                                        DataBindingDialogFragment.create<FragmentMenuCustomizeBinding>(fragment.activity!!.supportFragmentManager) { fragment ->
                                            food = foodParam
                                            fragment.onCreateView {dialog -> button_confirm.setOnClickListener {dialog.dismiss()}}
                                        }
                                    }
                                })
                                button_confirm.setOnClickListener {dialog.dismiss()}
                            }
                        }
                    }
                }
            }

            my_recycler_view.setLayoutManager(inflater1)
            my_recycler_view.setAdapter(adapter);
            my_recycler_view.isNestedScrollingEnabled = false
        }
    }

    abstract class OnClickWithFood (private val foodParam: Food) : View.OnClickListener {
        abstract fun onClick(v: View?, foodParam: Food) : Unit
        override final fun onClick(v: View?) = onClick(v, foodParam)
    }
}
