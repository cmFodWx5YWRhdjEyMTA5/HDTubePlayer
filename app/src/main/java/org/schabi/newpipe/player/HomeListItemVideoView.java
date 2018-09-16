package org.schabi.newpipe.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.dueeeke.videoplayer.player.IjkVideoView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by liyanju on 2018/9/16.
 */

public class HomeListItemVideoView extends IjkVideoView{
    public HomeListItemVideoView(@NonNull Context context) {
        super(context);
    }

    public HomeListItemVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private PlayLogicEventCallBack mCallBack;

    public void setPlayLogicEventCallBack(PlayLogicEventCallBack callBack) {
        mCallBack = callBack;
    }

    private static final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void startPrepare(boolean needReset) {
        if (TextUtils.isEmpty(mCurrentUrl)) {
            if (mCallBack != null) {
                singleThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                setPlayState(STATE_PREPARING);
                            }
                        });

                        mCurrentUrl = mCallBack.handleGetPlayUrl();

                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                HomeListItemVideoView.super.startPrepare(needReset);
                            }
                        });
                    }
                });
            }
        } else {
            super.startPrepare(needReset);
        }

    }
}
