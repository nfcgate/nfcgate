package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tud.seemuh.nfcgate.R;

public class FragmentClone extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clone, container, false);

        //button1 = (Button) v.findViewById(R.id.btn1);

        return v;
    }

    public static RelayFragment newInstance(String text) {

        RelayFragment f = new RelayFragment();

        //Bundle b = new Bundle();
        //b.putString("msg", text);

        //f.setArguments(b);

        return f;
    }
}
