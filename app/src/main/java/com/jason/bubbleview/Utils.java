package com.jason.bubbleview;

import android.content.Context;
import android.util.TypedValue;

public class Utils {

    public static int getStatusBarHeight(Context context) {
        int resourcesId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourcesId > 0) {
            return context.getResources().getDimensionPixelSize(resourcesId);
        }
        return dp2px(25, context);
    }

    private static int dp2px(int dpValue, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources
                ().getDisplayMetrics());
    }
}
