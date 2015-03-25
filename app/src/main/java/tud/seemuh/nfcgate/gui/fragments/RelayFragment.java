package tud.seemuh.nfcgate.gui.fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.AboutWorkaroundActivity;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.network.Callback;
import tud.seemuh.nfcgate.network.HighLevelNetworkHandler;
import tud.seemuh.nfcgate.network.HighLevelProtobufHandler;
import tud.seemuh.nfcgate.network.ProtobufCallback;
import tud.seemuh.nfcgate.nfc.NfcManager;

public class RelayFragment extends Fragment {

    private final static String TAG = "RelayFragment";


    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    private NfcAdapter mAdapter;
    private IntentFilter mIntentFilter = new IntentFilter();

    // Defined name of the Shared Preferences Buffer
    public static final String PREF_FILE_NAME = "SeeMoo.NFCGate.Prefs";

    //Connection Client
    protected HighLevelNetworkHandler mConnectionClient;

    // NFC Manager
    private NfcManager mNfcManager;

    // declares main functionality
    private Button mReset, mConnecttoSession, mAbort, mJoinSession;
    private TextView mConnStatus, mInfo, mDebuginfo, mIP, mPort, mPartnerDevice, mtoken;

    private View v;

    private Callback mNetCallback = new ProtobufCallback();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_relay, container, false);

        //button1 = (Button) v.findViewById(R.id.btn1);


        mAdapter = NfcAdapter.getDefaultAdapter(v.getContext());

        mIntentFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

        if (!mAdapter.isEnabled())
        {
            // NFC is not enabled -> "Tell the user to enable NFC"
            //FIXME
            //showEnableNFCDialog();
        }

        // Create a generic PendingIntent that will be delivered to this activity.
        // The NFC stack will fill in the intent with the details of the discovered tag before
        // delivering to this activity.
        //FIXME
        mPendingIntent = PendingIntent.getActivity(v.getContext(), 0, new Intent(v.getContext(),
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Setup an foreground intent filter for NFC
        IntentFilter tech = new IntentFilter();
        tech.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        mFilters = new IntentFilter[] { tech, };
        // this thing must have the same structure as in the tech.xml
        mTechLists = new String[][] {
                new String[] {NfcA.class.getName()},
                new String[] {Ndef.class.getName()},
                new String[] {IsoDep.class.getName()}
                //we could add all of the Types from the tech.xml here
        };

        // Create Buttons & TextViews
        mReset = (Button) v.findViewById(R.id.resetstatus);
        mConnecttoSession = (Button) v.findViewById(R.id.btnCreateSession);
        mJoinSession = (Button) v.findViewById(R.id.btnJoinSession);
        mAbort = (Button) v.findViewById(R.id.abortbutton);
        mConnStatus = (TextView) v.findViewById(R.id.editConnectionStatus);
        mDebuginfo = (TextView) v.findViewById(R.id.editTextDevModeEnabledDebugging);
        mIP = (TextView) v.findViewById(R.id.editIP);
        mPort = (TextView) v.findViewById(R.id.editPort);
        mPartnerDevice = (TextView) v.findViewById(R.id.editOtherDevice);
        mConnecttoSession.requestFocus();
        mtoken = (TextView) v.findViewById(R.id.token);

        // Create connection client
        mConnectionClient = HighLevelProtobufHandler.getInstance();
        mNfcManager = NfcManager.getInstance();

        // Pass necessary references to ConnectionClient
        mConnectionClient.setDebugView(mDebuginfo);
        mConnectionClient.setConnectionStatusView(mConnStatus);
        mConnectionClient.setPeerStatusView(mPartnerDevice);
        mConnectionClient.setButtons(mReset, mConnecttoSession, mAbort, mJoinSession);
        mConnectionClient.setNfcManager(mNfcManager);
        mConnectionClient.setCallback(mNetCallback);

        File bcmdevice = new File("/dev/bcm2079x-i2c");
        final SharedPreferences preferences = getActivity().getSharedPreferences(PREF_FILE_NAME, v.getContext().MODE_PRIVATE);
        boolean neverShowAgain = preferences.getBoolean("mNeverWarnWorkaround", false);
        if (bcmdevice.exists() && !neverShowAgain) {
            LayoutInflater checkboxInflater = getActivity().getLayoutInflater();
            final View checkboxView = checkboxInflater.inflate(R.layout.workaroundwarning, null);
            new AlertDialog.Builder(v.getContext())
                    .setTitle(R.string.BCMWarnHeader)
                    .setView(checkboxView)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CheckBox dontShowAgain = (CheckBox) checkboxView.findViewById(R.id.neverAgain);
                            if (dontShowAgain.isChecked()) {
                                Log.i(TAG, "onCreate: Don't show this again is checked");
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putBoolean("mNeverWarnWorkaround", true);

                                editor.apply();
                            }
                            startActivity(new Intent(v.getContext(), AboutWorkaroundActivity.class));
                        }
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CheckBox dontShowAgain = (CheckBox) checkboxView.findViewById(R.id.neverAgain);
                            if (dontShowAgain.isChecked()) {
                                Log.i(TAG, "onCreate: Don't show this again is checked");
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putBoolean("mNeverWarnWorkaround", true);

                                editor.apply();
                            }
                        }
                    })
                    .show();
        }

        return v;
    }

    public static RelayFragment newInstance() {

        RelayFragment f = new RelayFragment();

        //Bundle b = new Bundle();
        //b.putString("msg", text);

        //f.setArguments(b);

        return f;
    }

//    @Override
//    public void onTokenDialogPositiveClick(DialogFragment dialog) {
//        // User touched the dialog's submit button
//        // Toast.makeText(this, "You clicked submit, server is now processing your token...", Toast.LENGTH_LONG).show();
//
//        mJoinSession.setText(leaveSessionMessage);
//        mConnecttoSession.setEnabled(false);
//        mAbort.setEnabled(true);
//        //this.setTitle("You clicked connect");
//        //mConnStatus.setText("Server status: Connecting");
//        //mPartnerDevice.setText("Partner status: waiting");
//
//        // Run common network connection est. code
//        networkConnectCommon();
//
//        // Load token from the Shared Preferences Buffer
//        SharedPreferences preferences = super.getActivity().getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
//        String token = preferences.getString("token", "000000");
//
//        mConnectionClient.joinSession(token);
//
//    }

    public void onTokenDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's cancel button
        // Toast.makeText(this, "You clicked cancel, no connection was established...", Toast.LENGTH_LONG).show();
    }

    public void onNFCDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's goto settings button
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
    }

    public void onNFCDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's cancel button
        Toast.makeText(v.getContext(), "Caution! The app can't do something useful without NFC enabled -> please enable NFC in your phone settings", Toast.LENGTH_LONG).show();
    }


}
