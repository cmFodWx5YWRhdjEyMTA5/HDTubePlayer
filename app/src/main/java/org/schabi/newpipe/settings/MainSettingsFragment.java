package org.schabi.newpipe.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;

import org.schabi.newpipe.App;
import org.schabi.newpipe.BuildConfig;
import org.schabi.newpipe.R;

public class MainSettingsFragment extends BasePreferenceFragment {
    public static final boolean DEBUG = !BuildConfig.BUILD_TYPE.equals("release");

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (App.isGodMode()) {
            addPreferencesFromResource(R.xml.main_settings);
        } else {
            addPreferencesFromResource(R.xml.main_settings2);
        }

//        if (!DEBUG) {
//            final Preference debug = findPreference(getString(R.string.debug_pref_screen_key));
//            getPreferenceScreen().removePreference(debug);
//        }
    }
}
