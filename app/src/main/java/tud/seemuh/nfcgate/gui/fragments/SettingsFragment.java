package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import tud.seemuh.nfcgate.R;

/**
 * Settings Fragment. Controls the display and use of the Settings
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
