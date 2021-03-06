package com.wanandroid.java.ui.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.wanandroid.java.R;

import androidx.annotation.Nullable;

/**
 * @author jere
 */
public class CustomCollectView extends View {
    private boolean mIsCollect;

    public CustomCollectView(Context context) {
        this(context, null);
    }

    public CustomCollectView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCollectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomCollectView);
        mIsCollect = typedArray.getBoolean(R.styleable.CustomCollectView_isCollect, false);

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth() / 2;
        int height = getHeight() / 2;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (mIsCollect) {
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
        }
        Path path = new Path();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            path.addArc(width - 25, height - 25, width, height, -225, 225);
            path.arcTo(width, height - 25, width + 25, height, -180, 225, false);
            path.lineTo(width, width + 20);
            path.lineTo(width - 22, height - 5);
            canvas.drawPath(path, paint);
        }
    }

    public boolean isCollect() {
        return mIsCollect;
    }

    public void setIsCollect(boolean mIsCollect) {
        this.mIsCollect = mIsCollect;
        invalidate();
    }
}
