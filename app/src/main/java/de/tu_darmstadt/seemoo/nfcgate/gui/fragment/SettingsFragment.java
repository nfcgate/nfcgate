package de.tu_darmstadt.seemoo.nfcgate.gui.fragment;

import android.os.Bundle;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import de.tu_darmstadt.seemoo.nfcgate.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
