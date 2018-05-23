package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import tud.seemuh.nfcgate.R;

public class SettingsFragment extends PreferenceFragmentCompat implements BaseFragment {
    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public String getTagName() {
        return "settings";
    }
}
