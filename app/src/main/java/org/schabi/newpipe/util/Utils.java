package org.schabi.newpipe.util;

import android.app.Activity;
import android.os.Build;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liyanju on 2018/9/10.
 */

public class Utils {

    private static final int INVALID_VAL = -1;

    public static void compat(Activity activity, int statusColor) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (statusColor != INVALID_VAL) {
                    activity.getWindow().setStatusBarColor(statusColor);
                }
                return;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
