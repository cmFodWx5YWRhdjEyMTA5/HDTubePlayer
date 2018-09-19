package org.schabi.newpipe.util;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.schabi.newpipe.App;
import org.schabi.newpipe.BuildConfig;

/**
 * Created by liyanju on 2018/9/19.
 */

public class FacebookReferrerHandler {



    public static void onHandler(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        if (referrer == null) {
            return;
        }

        boolean result = App.sPreferences.getBoolean("get_referrer", false);
        if (result) {
            return;
        }
        App.sPreferences.edit().putBoolean("get_referrer", true).apply();

        if (BuildConfig.DEBUG) {
            Log.e("Installrr:::::", referrer);
        } else {
            if (!App.sPreferences.getBoolean("isTrances", true)) {
                Log.e("Referrer", "canRefer false ");
                return;
            }
        }

//        FacebookReport.logSentReferrer(referrer);

        if (Utils.isNotCommongUser()) {
            return;
        }

        if (ReferVersions.SuperVersionHandler.isReferrerOpen(referrer)) {
            if (BuildConfig.DEBUG) {
                Log.v("super", "isfasterOpen true");
            }
//            FacebookReport.logSentOpenSuper("open for admob");
            ReferVersions.SuperVersionHandler.setSuper();
        } else if (ReferVersions.SuperVersionHandler.isFacebookOpen(referrer)) {
//            FacebookReport.logSentOpenSuper("open for facebook");
            ReferVersions.SuperVersionHandler.setSuper();
        } else {
            ReferVersions.SuperVersionHandler.countryIfShow(context);
        }

//        FacebookReport.logSentUserInfo(ReferVersions.SuperVersionHandler.getSimCountry(context),
//                ReferVersions.SuperVersionHandler.getPhoneCountry(context));
    }
}
