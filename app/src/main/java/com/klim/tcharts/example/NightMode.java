package com.klim.tcharts.example;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import java.lang.ref.WeakReference;

public class NightMode {
    private static final String PREF_KEY = "nightModeState";

    private static int sUiNightMode = Configuration.UI_MODE_NIGHT_UNDEFINED;

    private WeakReference<Activity> mActivity;
    private SharedPreferences mPrefs;

    public NightMode(Activity activity, int theme) {
        int currentMode = (activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

        //init
        mActivity = new WeakReference<Activity>(activity);
        if (sUiNightMode == Configuration.UI_MODE_NIGHT_UNDEFINED) {
            sUiNightMode = mPrefs.getInt(PREF_KEY, currentMode);
        }
        updateConfig(sUiNightMode);
        activity.setTheme(theme);
    }

    public void toggle() {
        if (sUiNightMode == Configuration.UI_MODE_NIGHT_YES) {
            updateConfig(Configuration.UI_MODE_NIGHT_NO);
        } else {
            updateConfig(Configuration.UI_MODE_NIGHT_YES);
        }
        mActivity.get().recreate();
    }

    private void updateConfig(int uiNightMode) {
        Activity activity = mActivity.get();
        if (activity != null) {
            Configuration newConfig = new Configuration(activity.getResources().getConfiguration());
            newConfig.uiMode &= ~Configuration.UI_MODE_NIGHT_MASK;
            newConfig.uiMode |= uiNightMode;
            activity.getResources().updateConfiguration(newConfig, null);
            sUiNightMode = uiNightMode;
            if (mPrefs != null) {
                mPrefs.edit().putInt(PREF_KEY, sUiNightMode).apply();
            }
        }
    }
}