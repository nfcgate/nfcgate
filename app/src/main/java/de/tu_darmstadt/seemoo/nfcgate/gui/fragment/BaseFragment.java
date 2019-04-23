package de.tu_darmstadt.seemoo.nfcgate.gui.fragment;

import android.support.v4.app.Fragment;

import de.tu_darmstadt.seemoo.nfcgate.gui.MainActivity;
import de.tu_darmstadt.seemoo.nfcgate.nfc.NfcManager;

public abstract class BaseFragment extends Fragment {
    protected NfcManager getNfc() {
        return getMainActivity().getNfc();
    }

    protected MainActivity getMainActivity() {
        return ((MainActivity) getActivity());
    }
}
