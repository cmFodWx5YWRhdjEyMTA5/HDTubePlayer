package org.schabi.newpipe.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by liyanju on 2018/9/19.
 */

public class FBInstallReferrerReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            FacebookReferrerHandler.onHandler(context, intent);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
