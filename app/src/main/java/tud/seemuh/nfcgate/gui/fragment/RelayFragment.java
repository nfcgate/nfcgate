package tud.seemuh.nfcgate.gui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.nfc.NfcManager;

public class RelayFragment extends Fragment implements BaseFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_relay, container, false);

        // button setup
        v.<Button>findViewById(R.id.btn_reader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNfc().enableRelayMode(true);
            }
        });
        v.<Button>findViewById(R.id.btn_tag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNfc().enableRelayMode(false);
            }
        });

        setHasOptionsMenu(true);
        return v;
    }

    @Override
    public String getTagName() {
        return "relay";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_relay, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // TODO:
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public NfcManager getNfc() {
        return ((MainActivity) getActivity()).getNfc();
    }
}
