package com.pdd.booknow.databinding

import android.view.View
import androidx.constraintlayout.widget.Guideline
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import android.view.ViewGroup
import androidx.databinding.BindingAdapter

@BindingMethods(
    BindingMethod(
        type = Guideline::class,
        attribute = "app:layout_constraintGuide_percent",
        method = "setGuidelinePercent"
    )
)
private object DataBindingMethods {
    fun View.setMarginParams(block: ViewGroup.MarginLayoutParams.()->Unit) = (this.getLayoutParams() as? ViewGroup.MarginLayoutParams?)?.apply(block)?.let {layoutParams=it}

    @JvmStatic @BindingAdapter("android:layout_marginTop")
    fun View.setLayoutMarginTop(margin: Int) = setMarginParams {this.setMargins(this.leftMargin, margin, this.rightMargin, this.bottomMargin)}

    @JvmStatic @BindingAdapter("android:layout_marginBottom")
    fun View.setLayoutMarginBottom(margin: Int) = setMarginParams {this.setMargins(this.leftMargin, this.topMargin, this.rightMargin, margin)}

    @JvmStatic @BindingAdapter("android:layout_marginLeft")
    fun View.setLayoutMarginLeft(margin: Int) = setMarginParams {this.setMargins(margin, this.topMargin, this.rightMargin, this.leftMargin)}

    @JvmStatic @BindingAdapter("android:layout_marginRight")
    fun View.setLayoutMarginRight(margin: Int) = setMarginParams {this.setMargins(this.leftMargin, this.topMargin, margin, this.leftMargin)}

    @JvmStatic @BindingAdapter("android:layout_marginVertical")
    fun View.setLayoutMarginVertical(margin: Int) = setMarginParams {this.setMargins(this.leftMargin, margin, this.rightMargin, margin)}

    @JvmStatic @BindingAdapter("android:layout_marginHorizontal")
    fun View.setLayoutMarginHorizontal(margin: Int) = setMarginParams {this.setMargins(margin, this.topMargin, margin, this.leftMargin)}
}