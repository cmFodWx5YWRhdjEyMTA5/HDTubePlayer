package org.schabi.newpipe.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;

import org.schabi.newpipe.App;
import org.schabi.newpipe.BuildConfig;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liyanju on 2018/9/10.
 */

public class Utils {

    private static final int INVALID_VAL = -1;

    private static Handler sHandler = new Handler(Looper.getMainLooper());

    public static ExecutorService sSingleExecutor = Executors.newSingleThreadExecutor();

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

    public static int getScreenWidth() {
        return getScreenSize().x;
    }

    public static int dip2px(float dipValue) {
        final float scale = App.sContext.getResources()
                .getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int getScreenHeight() {
        return getScreenSize().y;
    }

    private static Point screenSize;
    public static synchronized Point getScreenSize() {
        if (null == screenSize) {
            Context context = App.sContext;
            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            screenSize = new Point();
            Display display = wm.getDefaultDisplay();
            if (Build.VERSION.SDK_INT > 12) {
                display.getSize(screenSize);
            } else {
                screenSize.x = display.getWidth();
                screenSize.y = display.getHeight();
            }
        }

        return screenSize;
    }

    public static boolean isNetWorkConnected() {
        Context context = App.sContext;
        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static void runUIThead(Runnable runnable) {
        sHandler.post(runnable);
    }

    public static void runUIThreadDelay(Runnable runnable, long delay) {
        sHandler.postDelayed(runnable, delay);
    }

    private static boolean isEmulator(Context context) {
        try {
            return Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.toLowerCase().contains("vbox")
                    || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK built for x86")
                    || Build.MANUFACTURER.contains("Genymotion")
                    || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                    || "google_sdk".equals(Build.PRODUCT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return true 开启调试，false 未开启调试
     * @author James
     * @Description 是否是usb调试模式
     */
    @TargetApi(3)
    public static boolean isAdbDebugEnable(Context mContext) {
        boolean enableAdb = (Settings.Secure.getInt(
                mContext.getContentResolver(), android.provider.Settings.Global.ADB_ENABLED, 0) > 0);
        return enableAdb;
    }

    /**
     * 不是普通用户
     * 设备开启Debug模式，模拟器，以及ROOT的手机都认为不是普通玩家
     *
     * @return
     */
    public static boolean isNotCommongUser() {
        if (BuildConfig.DEBUG) {
            return false;
        }
        Context context = App.sContext;
        return isAdbDebugEnable(context) || isEmulator(context) || isRoot();
    }

    public static boolean isRoot(){
        boolean bool = false;

        try{
            if ((!new File("/system/bin/su").exists())
                    && (!new File("/system/xbin/su").exists())){
                bool = false;
            } else {
                bool = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bool;
    }
}
