package com.moneytracker.aimoney.moneymanager.finance.service;


import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.moneytracker.aimoney.moneymanager.finance.R;

public class FirebaseRemoteManager {
    private static final String TAG = "FirebaseRemoteConfig";
    private static final String API_KEY = "keyMoney";
    private static FirebaseRemoteManager instance;
    private final FirebaseRemoteConfig mFirebaseRemoteConfig;

    private FirebaseRemoteManager() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

        fetchRemoteConfig();
    }

    public static synchronized FirebaseRemoteManager getInstance() {
        if (instance == null) {
            instance = new FirebaseRemoteManager();
        }
        return instance;
    }

    private void fetchRemoteConfig() {
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Fetch successful");
                    } else {
                        Log.e(TAG, "Fetch failed");
                    }
                });
    }

    public String getApiKey() {
        return mFirebaseRemoteConfig.getString(API_KEY);
    }
}
