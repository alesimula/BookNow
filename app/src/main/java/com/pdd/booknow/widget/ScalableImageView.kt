package com.pdd.booknow.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log

import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import com.pdd.booknow.R

import java.util.regex.Pattern
import android.view.View
import kotlin.math.pow


@BindingMethods(
    BindingMethod(
        type = ScalableImageView::class,
        attribute = "app:srcCompat",
        method = "setImageDrawable"
    )
)
class ScalableImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    androidx.appcompat.widget.AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private val sFixedScaleArray = arrayOf(FixedScale.WIDTH, FixedScale.HEIGHT)

        @BindingAdapter("aspectRatio")
        @JvmStatic fun ScalableImageView.setAspectRatio(aspectRatio: Double) {
            val defaultAspectRatio = if (mRelativeAspectRatio) defaultAspectRatio else 1.0
            mAspectRatio = defaultAspectRatio * aspectRatio
            requestLayout()
        }
    }
    enum class FixedScale private constructor(internal val nativeInt: Int) {
        WIDTH(0),
        HEIGHT(1)
    }

    val defaultAspectRatio: Double get() = drawable?.let {drawable ->
        drawable.intrinsicWidth.toDouble() / drawable.intrinsicHeight.toDouble()
    } ?: 1.0

    /**
     * Option to determine which dimension to follow to scale the image.
     */
    private var mFixedScale: FixedScale? = null
    private var mAspectRatio: Double = 0.0
    private var mRelativeAspectRatio = true

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ScalableImageView,
            defStyleAttr, 0
        )
        try {
            setFixedScale(sFixedScaleArray[a.getInt(R.styleable.ScalableImageView_fixedScale, 0)])
            mRelativeAspectRatio = a.getBoolean(R.styleable.ScalableImageView_relativeAspectRatio, true)
            mAspectRatio = getAspectRatio(a.getString(R.styleable.ScalableImageView_aspectRatio), attrs?.positionDescription?:"")
        } finally {
            a.recycle()
        }
    }

    //Hack to inject default attrs before xml attributes are loaded
    override fun getImportantForAutofill(): Int {
        initializeDefaultAttrs()
        return super.getImportantForAutofill()
    }

    fun initializeDefaultAttrs() {
        scaleType = ScaleType.FIT_XY
    }

    fun setFixedScale(scalePriority: FixedScale?) {
        if (scalePriority == null) {
            throw NullPointerException()
        }
        if (mFixedScale != scalePriority) {
            mFixedScale = scalePriority
            requestLayout()
            invalidate()
        }
    }

    fun parseAspectRatio(string: String?, failDesc: String): Double {
        if (string == null || string.isEmpty()) return 1.0
        try {
            var matched: String? = null
            val m = Pattern.compile("(\\d+)([:xX])(\\d+)").matcher(string)
            if (m.matches()) matched = m.group(0)
            if (matched != null && matched == string) {
                val w = Integer.parseInt(m.group(1)).toDouble()
                val h = Integer.parseInt(m.group(3)).toDouble()
                return w / h
            }
        } catch (e: Exception) {}

        try {
            val aspectratio = java.lang.Double.parseDouble(string)
            if (aspectratio < 0) {
                Log.w(this.javaClass.simpleName, "$failDesc: Invalid aspect ratio value")
                return 1.0
            } else
                return aspectratio
        } catch (e: Exception) {}

        return 1.0
    }

    fun getAspectRatio(string: String?, failDesc: String): Double {
        val defaultAspectRatio = if (mRelativeAspectRatio) defaultAspectRatio else 1.0
        val aspectRatio = parseAspectRatio(string, failDesc)
        return defaultAspectRatio * aspectRatio
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        /*var height = 0
        var width = 0

        if (mFixedScale == FixedScale.HEIGHT) {
            height = measuredHeight.let {if (it!=0) it else this.height}
            width = (height * mAspectRatio).toInt()
        }
        else {
            width = measuredWidth.let {if (it!=0) it else this.width}
            height = (width / mAspectRatio).toInt()
        }*/

        val width = if (mFixedScale == FixedScale.HEIGHT) (measuredHeight * mAspectRatio).toInt() else measuredWidth
        val height = if (mFixedScale == FixedScale.HEIGHT) measuredHeight else (measuredWidth / mAspectRatio).toInt()
        setMeasuredDimension(width, height)
    }

    /*override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val originalWidth = View.MeasureSpec.getSize(widthMeasureSpec)

        val originalHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        val calculatedHeight = (originalWidth * mAspectRatio.pow(-1)).toInt()
        val finalWidth: Int
        val finalHeight: Int

        if (calculatedHeight > originalHeight) {
            finalWidth = (originalHeight * mAspectRatio).toInt()
            finalHeight = originalHeight
        } else {
            finalWidth = originalWidth
            finalHeight = calculatedHeight
        }

        super.onMeasure(
                View.MeasureSpec.makeMeasureSpec(finalWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(finalHeight, View.MeasureSpec.EXACTLY))
    }*/
}
