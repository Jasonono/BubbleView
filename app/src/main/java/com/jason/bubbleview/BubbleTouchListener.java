package com.jason.bubbleview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jason.bubbleview.BubbleView.BubbleViewDismissListener;

import static android.view.View.GONE;

public class BubbleTouchListener implements View.OnTouchListener {

    private Context mContext;
    private WindowManager mWindowManager;
    private final BubbleView mBubbleView;
    private final WindowManager.LayoutParams mParams;
    private final FrameLayout bombLayout;
    private final ImageView imageView;
    private BubbleViewDismissListener mBubbleViewDismissListener;

    public BubbleTouchListener(Context context, BubbleViewDismissListener bubbleViewDismissListener) {
        this.mContext = context;
        this.mBubbleViewDismissListener = mBubbleViewDismissListener;
        this.mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.mBubbleView = new BubbleView(context);
        this.mParams = new WindowManager.LayoutParams();
        mParams.format = PixelFormat.TRANSPARENT;

        bombLayout = new FrameLayout(mContext);
        imageView = new ImageView(mContext);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams
                .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        bombLayout.addView(imageView);

        mBubbleView.setBubbleViewListener(new BubbleView.BubbleViewListener() {
            @Override
            public void onDismiss(final View view, PointF dragPoint) {
                mWindowManager.removeView(mBubbleView);

                mWindowManager.addView(bombLayout, mParams);
                imageView.setBackgroundResource(R.drawable.anim_bubble_pop);
                AnimationDrawable drawable = (AnimationDrawable) imageView.getBackground();

                imageView.setX(dragPoint.x - drawable.getIntrinsicWidth()/2);
                imageView.setY(dragPoint.y - drawable.getIntrinsicHeight()/2);

                drawable.start();

                imageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mWindowManager.removeView(bombLayout);

                        if (mBubbleViewDismissListener!=null){
                            mBubbleViewDismissListener.onDismiss(view);
                        }
                    }
                }, getDrawableTime(drawable));
            }

            @Override
            public void onRestore(View view) {
                view.setVisibility(View.VISIBLE);
                mWindowManager.removeView(mBubbleView);
            }
        });
    }

    private long getDrawableTime(AnimationDrawable drawable) {
        int numberOfFrames = drawable.getNumberOfFrames();
        long time = 0;
        for (int i = 0; i < numberOfFrames; i++) {
            time += drawable.getDuration(i);
        }
        return time;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setVisibility(GONE);
                mWindowManager.addView(mBubbleView, mParams);
                int[] locations = new int[2];
                view.getLocationOnScreen(locations);
                Bitmap bitmap = getBitmapView(view);
                mBubbleView.initPoint(locations[0] + bitmap.getWidth() / 2,
                        locations[1] + bitmap.getHeight() / 2 - Utils.getStatusBarHeight(mContext));
                mBubbleView.initDragBitmap(bitmap);
                break;
            case MotionEvent.ACTION_MOVE:
                mBubbleView.updatePoint(event.getRawX(), event.getRawY() - Utils.getStatusBarHeight(mContext));
                break;
            case MotionEvent.ACTION_UP:
                mBubbleView.handleActionUp();
                break;
        }
        return true;
    }

    private Bitmap getBitmapView(View view) {
        return ViewToBitmapUtils.convertViewToBitmap(view);
    }
}
