package com.sakaimobile.development.sakaiclient20.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.sakaimobile.development.sakaiclient20.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UserPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

}
