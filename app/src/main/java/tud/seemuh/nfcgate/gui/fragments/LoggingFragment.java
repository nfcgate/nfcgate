package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tud.seemuh.nfcgate.R;

/**
 * Created by Tom on 28.03.2015.
 */
public class LoggingFragment extends Fragment {

    private static LoggingFragment mFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_logging, container, false);
        return v;
    }

    public static LoggingFragment getInstance() {

        if(mFragment == null) {
            mFragment = new LoggingFragment();
        }

        return mFragment;
    }
}
