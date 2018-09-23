package org.schabi.newpipe.player;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.dueeeke.videoplayer.player.IjkVideoView;
import com.facebook.stetho.common.LogUtil;
import com.squareup.haha.trove.THash;

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

    private AsyncTask loadAsyncTask;

    private void stopPlayLogicEvent() {
        Log.v("HomeListItemVideoView", "stopPlayLogicEvent " + loadAsyncTask);
        if (loadAsyncTask != null) {
            loadAsyncTask.cancel(true);
            loadAsyncTask = null;
        } else {
            setPlayState(STATE_IDLE);
        }
    }

    @Override
    public void release() {
        stopPlayLogicEvent();
        super.release();
    }

    @Override
    public void stopPlayback() {
        stopPlayLogicEvent();
        super.stopPlayback();
    }

    @Override
    protected void startPrepare(boolean needReset) {
        Log.v("HomeListItemVideoView", "startPrepare>> " + needReset + " mCallBack "+ mCallBack);
        if (mCallBack != null) {
            if (loadAsyncTask != null) {
                loadAsyncTask.cancel(true);
                loadAsyncTask = null;
            }

            loadAsyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    setPlayState(STATE_PREPARING);
                }

                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        return mCallBack.handleGetPlayUrl(false);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (!isCancelled()) {
                            cancel(true);
                        }
                    }
                    return null;
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    setPlayState(STATE_IDLE);
                }

                @Override
                protected void onPostExecute(String url) {
                    super.onPostExecute(url);
                    if (TextUtils.isEmpty(url)) {
                        setPlayState(STATE_ERROR);
                        return;
                    }

                    mCurrentUrl = url;
                    Log.v("HomeListItemVideoView", "super startPrepare " + needReset);
                    HomeListItemVideoView.super.startPrepare(needReset);
                }
            }.executeOnExecutor(singleThreadExecutor);
        } else {
            super.startPrepare(needReset);
        }

    }
}
