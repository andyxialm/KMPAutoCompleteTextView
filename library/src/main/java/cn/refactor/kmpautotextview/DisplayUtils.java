package cn.refactor.kmpautotextview;

import android.content.Context;

/**
 * 作者 : andy
 * 日期 : 15/12/29 10:05
 * 邮箱 : andyxialm@gmail.com
 * 描述 :
 */
public class DisplayUtils {
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
}
