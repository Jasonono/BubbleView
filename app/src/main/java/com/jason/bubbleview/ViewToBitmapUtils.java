package com.jason.bubbleview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;
import android.view.View.MeasureSpec;

public class ViewToBitmapUtils {
    private static final String TAG = "ViewToBitmapUtil";

    public static Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    public static Bitmap convertViewToBitmap(View view, int bitmapWidth, int bitmapHeight) {
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        view.draw(new Canvas(bitmap));
        return bitmap;
    }

    public static void getScreenRectOfView(View view, Rect outRect) {
        int pos[] = new int[2];
        view.getLocationOnScreen(pos);
        outRect.set(pos[0], pos[1], pos[0] + view.getWidth(), pos[1] + view.getHeight());
    }

}
