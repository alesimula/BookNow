package com.pdd.booknow.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;

import com.pdd.booknow.R;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ScalableImageView extends androidx.appcompat.widget.AppCompatImageView {

    public ScalableImageView(Context context) {
        this(context, (AttributeSet)null);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScalableImageView,
                defStyleAttr, 0);
        try {
            setFixedScale(sFixedScaleArray[a.getInt(R.styleable.ScalableImageView_fixedScale, 0)]);
            mRelativeAspectRatio = a.getBoolean(R.styleable.ScalableImageView_relativeAspectRatio, true);
            mAspectRatio = getAspectRatio(a.getString(R.styleable.ScalableImageView_aspectRatio), attrs.getPositionDescription());
        } finally {
            a.recycle();
        }
    }

    //Hack to inject default attrs before xml attributes are loaded
    @Override
    public int getImportantForAutofill() {
        initializeDefaultAttrs();
        return super.getImportantForAutofill();
    }

    public void initializeDefaultAttrs() {
        setScaleType(ScaleType.FIT_XY);
    }

    public void setFixedScale(FixedScale scalePriority) {
        if (scalePriority == null) {
            throw new NullPointerException();
        }
        if (mFixedScale != scalePriority) {
            mFixedScale = scalePriority;
            requestLayout();
            invalidate();
        }
    }

    public double parseAspectRatio(String string, String failDesc) {
        if (string==null || string.isEmpty()) return 1;
        try {
            String matched=null;
            Matcher m = Pattern.compile("(\\d+)([:xX])(\\d+)").matcher(string);
            if (m.matches()) matched = m.group(0);
            if (matched!=null && matched.equals(string)) {
                double w = Integer.parseInt(m.group(1));
                double h = Integer.parseInt(m.group(3));
                return w/h;
            }
        } catch (Exception e) {}
        try {
            double aspectratio = Double.parseDouble(string);
            if (aspectratio<0) {
                Log.w(this.getClass().getSimpleName(), failDesc+": Invalid aspect ratio value");
                return 1;
            }
            else return aspectratio;
        } catch (Exception e) {}
        return 1;
    }

    public double getAspectRatio(String string, String failDesc) {
        double defaultAspectRatio = mRelativeAspectRatio ? getDefaultAspectRatio() : 1;
        double aspectRatio = parseAspectRatio(string, failDesc);
        return defaultAspectRatio*aspectRatio;
    }

    public double getDefaultAspectRatio() {
        Drawable drawable = getDrawable();
        double aspectRatio = 1;
        if (drawable!=null) aspectRatio = ((double)drawable.getIntrinsicWidth())/((double)drawable.getIntrinsicHeight());
        return aspectRatio;
    }

    /**
     * Option to determine which dimension to follow to scale the image.
     */
    private FixedScale mFixedScale;
    private double mAspectRatio;
    private boolean mRelativeAspectRatio = true;
    public enum FixedScale {
        WIDTH      (0),
        HEIGHT     (1);

        FixedScale(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }
    private static final FixedScale[] sFixedScaleArray = {
            FixedScale.WIDTH,
            FixedScale.HEIGHT,
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = (mFixedScale.equals(FixedScale.HEIGHT))?(int)(getMeasuredHeight()*mAspectRatio):getMeasuredWidth();
        int height = (mFixedScale.equals(FixedScale.HEIGHT))?getMeasuredHeight():(int)(getMeasuredWidth()/mAspectRatio);
        setMeasuredDimension(width, height);
    }
}
