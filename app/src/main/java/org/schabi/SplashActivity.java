package org.schabi;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.schabi.newpipe.MainActivity;
import org.schabi.newpipe.R;

import java.lang.reflect.Method;

/**
 * Created by liyanju on 2018/9/17.
 */

public class SplashActivity extends AppCompatActivity{

    public static final int WRITE_REQUEST_CODE = 102;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_activity);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
        } else {
            startMain();
        }

        FrameLayout frameLayout = findViewById(R.id.splash_container);
        try {
            int height = getVirtualBarHeigh();
            if (height > 0) {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(frameLayout.getLayoutParams());
                lp.setMargins(0, height, 0, 0);
                frameLayout.setLayoutParams(lp);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private int getVirtualBarHeigh() {
        int vh = 0;
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return vh;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startMain();
                } else {
                    finish();
                }
            }
        }

    }

    private void startMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.switch_service_in, 0);
        finish();
    }
}
