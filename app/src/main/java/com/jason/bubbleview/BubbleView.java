package com.jason.bubbleview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public class BubbleView extends View {

    private Paint mPaint;
    private PointF mFixedPoint, mDragedPoint;
    private float mFixedRadiusMax = dp2px(8);
    private float mFixedRadiusMin = dp2px(2);
    private float mDragedRadius = dp2px(10);
    private float downX;
    private float downY;
    private double distance;
    private float dx;
    private float dy;
    private float fixedRadius;
    private Bitmap bitmap;
    private Context mContext;
    private static View attachView;

    private float dp2px(int dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    public BubbleView(Context context) {
        this(context, null);
    }

    public BubbleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFixedPoint == null || mDragedPoint == null) return;

        //画贝赛尔曲线
        Path path = getPath();
        if (fixedRadius > mFixedRadiusMin) {
            canvas.drawCircle(mDragedPoint.x, mDragedPoint.y, mDragedRadius, mPaint);
            canvas.drawCircle(mFixedPoint.x, mFixedPoint.y, fixedRadius, mPaint);
            canvas.drawPath(path, mPaint);
        }

        if (bitmap != null) {
            canvas.drawBitmap(bitmap, mDragedPoint.x - bitmap.getWidth() / 2,
                    mDragedPoint.y - bitmap.getHeight() / 2, null);
        }


    }

    private Path getPath() {
        distance = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

        fixedRadius = (float) (mFixedRadiusMax - distance / dp2px(8));

        double atan = Math.atan(dy / dx);

        float x0 = (float) (downX + fixedRadius * Math.sin(atan));
        float y0 = (float) (downY - fixedRadius * Math.cos(atan));

        float x1 = (float) (downX - fixedRadius * Math.sin(atan));
        float y1 = (float) (downY + fixedRadius * Math.cos(atan));

        float x2 = (float) (mDragedPoint.x + mDragedRadius * Math.sin(atan));
        float y2 = (float) (mDragedPoint.y - mDragedRadius * Math.cos(atan));

        float x3 = (float) (mDragedPoint.x - mDragedRadius * Math.sin(atan));
        float y3 = (float) (mDragedPoint.y + mDragedRadius * Math.cos(atan));

        //控制点
        float x = downX + dx * 0.4f;
        float y = downY + dy * 0.4f;

        Path path = new Path();
        path.moveTo(x0, y0);
        path.quadTo(x, y, x2, y2);
        path.lineTo(x3, y3);
        path.quadTo(x, y, x1, y1);
        path.close();

        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                initPoint(downX, downY);
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                updatePoint(moveX, moveY);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    public void handleActionUp() {
        if (fixedRadius > mFixedRadiusMin) {
            //回弹
            ValueAnimator valueAnimator = ObjectAnimator.ofFloat(0, 1);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float animatedValue = (float) animation.getAnimatedValue();
                    mDragedPoint.x = downX + (1 - animatedValue) * (mDragedPoint.x - downX);
                    mDragedPoint.y = downY + (1 - animatedValue) * (mDragedPoint.y - downY);
                    updatePoint(mDragedPoint.x, mDragedPoint.y);
                }
            });
            valueAnimator.setInterpolator(new OvershootInterpolator(5f));

            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    //显示原来的View
                    if (bubbleViewListener!=null){
                        bubbleViewListener.onRestore(attachView);
                    }
                    //销毁Bubble bitmap
                    initDragBitmap(null);
                    mFixedPoint = null;
                    mDragedPoint = null;
                    invalidate();
                }
            });
            valueAnimator.start();
        } else {
            //显示原来的View
            if (bubbleViewListener!=null){
                bubbleViewListener.onDismiss(attachView,mDragedPoint);
            }
            //爆炸消失
            initDragBitmap(null);
            mFixedPoint = null;
            mDragedPoint = null;
            invalidate();
        }
    }

    public void updatePoint(float moveX, float moveY) {
        mDragedPoint.x = moveX;
        mDragedPoint.y = moveY;
        dx = moveX - downX;
        dy = moveY - downY;
        invalidate();
    }

    public void initPoint(float downX, float downY) {
        this.downX = downX;
        this.downY = downY;
        mDragedPoint = new PointF(downX, downY);
        mFixedPoint = new PointF(downX, downY);
    }


    private BubbleViewDismissListener bubbleViewDismissListener;
    public static void attach(Context context, View view, BubbleViewDismissListener bubbleViewDismissListener) {
        attachView = view;
        view.setOnTouchListener(new BubbleTouchListener(context,bubbleViewDismissListener));
    }

    public void initDragBitmap(Bitmap bitmapView) {
        this.bitmap = bitmapView;
    }


    private BubbleViewListener bubbleViewListener;
    public void setBubbleViewListener(BubbleViewListener bubbleViewListener){
        this.bubbleViewListener = bubbleViewListener;
    }

    public interface BubbleViewListener {
        void onDismiss(View view, PointF pointF);
        void onRestore(View view);
    }

    public interface BubbleViewDismissListener {
        void onDismiss(View view);
    }
}
