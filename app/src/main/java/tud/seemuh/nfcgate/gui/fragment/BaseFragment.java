package tud.seemuh.nfcgate.gui.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.nfc.NfcManager;

public abstract class BaseFragment extends Fragment {
    public abstract String getTagName();

    public NfcManager getNfc() {
        return ((MainActivity) getActivity()).getNfc();
    }
}
