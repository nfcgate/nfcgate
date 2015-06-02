package tud.seemuh.nfcgate.gui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.sink.SinkInitException;
import tud.seemuh.nfcgate.util.sink.SinkManager;

public class CloneFragment extends Fragment implements OnClickListener {

    private final static String TAG = "CloneFragment";

    private static CloneFragment mFragment;
    public NfcManager mNfcManager = NfcManager.getInstance();
    private SinkManager mSinkManager;
    private BlockingQueue<NfcComm> mSinkManagerQueue = new LinkedBlockingQueue<NfcComm>();

    private TextView mCurrUID;
    private Switch mToggleCloneMode;
    //tell other classes about the status of the clone mode
    private boolean mCloneModeEnabled = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_clone, container, false);
        mCurrUID = (TextView) v.findViewById(R.id.cloned_uid);
        mToggleCloneMode = (Switch) v.findViewById(R.id.btnSwitchCloneMode);
        mToggleCloneMode.setOnClickListener(this);

        try{
            //singManager could already be set by relayFragment, if not to it here
            if(mNfcManager.getSinkManager() == null) {
                mSinkManager = new SinkManager(mSinkManagerQueue);
                mNfcManager.setSinkManager(mSinkManager, mSinkManagerQueue);
            }
            mNfcManager.getSinkManager().addSink(SinkManager.SinkType.DISPLAY_TEXTVIEW, mCurrUID, true);

            //the start method knows when there is already a thread running
            mNfcManager.start();

        } catch (SinkInitException e) {
            e.printStackTrace();
        }


        return v;
    }

    public boolean isCloneModeEnabled() {
        return mCloneModeEnabled;
    }

    public static CloneFragment getInstance() {

        if(mFragment == null) {
            mFragment = new CloneFragment();
        }

        return mFragment;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSwitchCloneMode:
                boolean on = ((Switch) v).isChecked();

                if (on) {
                    mCloneModeEnabled = true;
                } else {
                    mCloneModeEnabled = false;
                }

                break;
        }
    }

    /*
    @Override
    public void onResume() {

        super.onResume();
        getActivity().getSupportFragmentManager().popBackStack();
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK){

                    // handle back button

                    return true;

                }

                return false;
            }
        });
    } */

}
