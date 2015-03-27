package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tud.seemuh.nfcgate.R;

public class CloneFragment extends Fragment {

    private static CloneFragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clone, container, false);
        return v;
    }

    public static CloneFragment getInstance() {

        if(mFragment == null) {
            mFragment = new CloneFragment();
        }

        return mFragment;
    }
}
