package tud.seemuh.nfcgate.gui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * CircleView displays a circle / ring that can be animated.
 * Based on http://stackoverflow.com/a/29381788, with modifications
 */
public class CircleView extends View {

    private static final int START_ANGLE_POINT = 90;
    private static final int DIMENSIONS = 600;

    private final Paint mAnimPaint;
    private final Paint mBackgroundPaint;
    private final RectF mDrawRect;

    private float mAnimAngle;

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final int strokeWidth = 50;

        mAnimPaint = new Paint();
        mAnimPaint.setAntiAlias(true);
        mAnimPaint.setStyle(Paint.Style.STROKE);
        mAnimPaint.setStrokeWidth(strokeWidth);
        //Circle color
        mAnimPaint.setColor(Color.GREEN);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setAntiAlias(true);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(strokeWidth);
        mBackgroundPaint.setColor(Color.WHITE);

        //size 500x500 example
        mDrawRect = new RectF(strokeWidth, strokeWidth, DIMENSIONS + strokeWidth, DIMENSIONS + strokeWidth);

        //Initial Angle (optional, it can be zero)
        mAnimAngle = 0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mDrawRect, 0, 360, false, mBackgroundPaint);
        canvas.drawArc(mDrawRect, START_ANGLE_POINT, mAnimAngle, false, mAnimPaint);
    }

    public float getAngle() {
        return mAnimAngle;
    }

    public void setAngle(float angle) {
        this.mAnimAngle = angle;
    }

    public void setCircleAnimColor(int color) {
        mAnimPaint.setColor(color);
    }

    public void setCircleBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
    }

    // All measurement logic based on
    // https://kahdev.wordpress.com/2008/09/13/making-a-custom-android-button-using-a-custom-view/
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                             measureHeight(heightMeasureSpec));
    }

    private int measureWidth(int measureSpec) {
        int preferred = (int)(mDrawRect.width() + 2* mAnimPaint.getStrokeWidth());
        return getMeasurement(measureSpec, preferred);
    }

    private int measureHeight(int measureSpec) {
        int preferred = (int)(mDrawRect.height() + 2* mAnimPaint.getStrokeWidth());
        return getMeasurement(measureSpec, preferred);
    }

    private int getMeasurement(int measureSpec, int preferred) {
        int specSize = MeasureSpec.getSize(measureSpec);
        int measurement = 0;

        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.EXACTLY:
                // This means the width of this view has been given.
                measurement = specSize;
                break;
            case MeasureSpec.AT_MOST:
                // Take the minimum of the preferred size and what
                // we were told to be.
                measurement = Math.min(preferred, specSize);
                break;
            default:
                measurement = preferred;
                break;
        }
        return measurement;
    }

    // End adapted measurement logic
}
