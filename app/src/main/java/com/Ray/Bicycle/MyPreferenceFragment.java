package com.Ray.Bicycle;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.Ray.Bicycle.R;

public class MyPreferenceFragment extends PreferenceFragmentCompat {

    public static final String FRAGMENT_TAG = "my_preference_fragment";

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

}