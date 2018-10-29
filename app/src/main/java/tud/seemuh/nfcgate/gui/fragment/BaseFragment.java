package tud.seemuh.nfcgate.gui.fragment;

import android.support.v4.app.Fragment;

import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.nfc.NfcManager;

public abstract class BaseFragment extends Fragment {
    protected NfcManager getNfc() {
        return getMainActivity().getNfc();
    }

    protected MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }
}
