package tud.seemuh.nfcgate.gui.fragments;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.AboutWorkaroundActivity;
import tud.seemuh.nfcgate.gui.MainActivity;
import tud.seemuh.nfcgate.network.Callback;
import tud.seemuh.nfcgate.network.HighLevelNetworkHandler;
import tud.seemuh.nfcgate.network.HighLevelProtobufHandler;
import tud.seemuh.nfcgate.network.ProtobufCallback;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.nfc.reader.BCM20793Workaround;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterManager;
import tud.seemuh.nfcgate.util.sink.SinkInitException;
import tud.seemuh.nfcgate.util.sink.SinkManager;

public class RelayFragment extends Fragment
        implements OnClickListener, EnablenfcDialog.NFCNoticeDialogListener, TokenDialog.NoticeDialogListener{

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
    // should be able being set by activity
    public NfcManager mNfcManager;

    // Sink Manager
    private SinkManager mSinkManager;
    private BlockingQueue<NfcComm> mSinkManagerQueue = new LinkedBlockingQueue<NfcComm>();

    // Filter Manager
    private FilterManager mFilterManager;

    // IP:Port combination saved for enhanced user comfort
    private String ip;
    private int port;

    // declares main functionality
    private Button mReset, mConnecttoSession, mAbort, mJoinSession;
    private TextView mConnStatus, mInfo, mDebuginfo, mIP, mPort, mPartnerDevice, mtoken;

    // max. port possible
    private static int maxPort = 65535;
    int globalPort = 0;

    // regex for IP checking
    private static final String regexIPpattern ="^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static String joinSessionMessage = "Join Session";
    public static String createSessionMessage = "Create Session";
    public static String leaveSessionMessage = "Leave Session";
    public static String resetMessage = "Reset";
    public static String resetCardMessage = "Forget Card";

    private View v;

    private Callback mNetCallback = new ProtobufCallback();

    // private var set by settings dialog whether dev mode is enabled or not
    private boolean mDevModeEnabled = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_relay, container, false);
        Log.d(TAG, "onCreateView");

        mAdapter = NfcAdapter.getDefaultAdapter(v.getContext());

        mIntentFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

        if (!mAdapter.isEnabled()) {
            // NFC is not enabled -> "Tell the user to enable NFC"
            showEnableNFCDialog();
        }

        // Create a generic PendingIntent that will be delivered to this activity.
        // The NFC stack will fill in the intent with the details of the discovered tag before
        // delivering to this activity.
        mPendingIntent = PendingIntent.getActivity(getActivity(), 0, new Intent(getActivity(),
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
        mReset = (Button) v.findViewById(R.id.btnResetstatus);
        mReset.setOnClickListener(this);
        mConnecttoSession = (Button) v.findViewById(R.id.btnCreateSession);
        mConnecttoSession.setOnClickListener(this);
        mJoinSession = (Button) v.findViewById(R.id.btnJoinSession);
        mJoinSession.setOnClickListener(this);
        mAbort = (Button) v.findViewById(R.id.btnAbortbutton);
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

        final SharedPreferences preferences = getActivity().getSharedPreferences(PREF_FILE_NAME, v.getContext().MODE_PRIVATE);
        boolean neverShowAgain = preferences.getBoolean("mNeverWarnWorkaround", false);
        if (BCM20793Workaround.workaroundNeeded() && !neverShowAgain) {
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

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume(): intent: " + getActivity().getIntent().getAction());

        // Load values from the Shared Preferences Buffer
        SharedPreferences preferences = v.getContext().getSharedPreferences(PREF_FILE_NAME, v.getContext().MODE_PRIVATE);


        if (mAdapter != null && mAdapter.isEnabled()) {
            mAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, mFilters, mTechLists);

            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getActivity().getIntent().getAction())) {
                Log.i(TAG, "onResume(): starting onNewIntent()...");
                ((MainActivity)getActivity()).onNewIntent(getActivity().getIntent());
            }
        }

        ip = preferences.getString("ip", "192.168.178.31");
        port = preferences.getInt("port", 5566);
        globalPort = preferences.getInt("port", 5566);

        //on start set the text values
        if(mIP.getText().toString().trim().length() == 0) {
            mIP.setText(ip);
            mPort.setText(String.valueOf(port));
        }

        boolean chgsett;
        chgsett = preferences.getBoolean("changed_settings", false);

        if(chgsett) {
            mIP.setText(ip);
            mPort.setText(String.valueOf(port));

            // reset the 'settings changed' flag
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("changed_settings", false);
            editor.commit();
        }

        //ReaderMode
        boolean isReaderModeEnabled = preferences.getBoolean("mReaderModeEnabled", false);
        if(isReaderModeEnabled) {
            //This cast to ReaderCallback seems unavoidable, stupid Java...
            mAdapter.enableReaderMode(getActivity(), (NfcAdapter.ReaderCallback) getActivity(),
                    NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        } else {
            mAdapter.disableReaderMode(getActivity());
        }

        // De- or Enables Debug Window
        mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
        mDebuginfo = (TextView) v.findViewById(R.id.editTextDevModeEnabledDebugging);
        if (mDevModeEnabled) {
            mDebuginfo.setVisibility(View.VISIBLE);
            mDebuginfo.requestFocus();
        } else {
            mDebuginfo.setVisibility(View.GONE);  // View.invisible results in an error
        }

        mConnecttoSession.requestFocus();
    }


    public static RelayFragment newInstance() {

        RelayFragment f = new RelayFragment();

        //Bundle b = new Bundle();
        //b.putString("msg", text);

        //f.setArguments(b);

        return f;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnJoinSession:
                // Join an existing session
                if (!checkIpPort(mIP.getText().toString(), mPort.getText().toString())) {
                    Toast.makeText(v.getContext(), "Please enter a valid ip & port", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!mJoinSession.getText().equals(leaveSessionMessage)) {
                    // Display dialog to enter the token
                    showTokenDialog();   // all logic is implemented below in "onTokenDialogPositiveClick" method
                } else {
                    // the button was already clicked and we want to disconnect from the session
                    mJoinSession.setText(joinSessionMessage);
                    mConnecttoSession.setEnabled(true);
                    mConnectionClient.leaveSession();
                }
                break;
            case R.id.btnCreateSession:
                // Create a new Session
                if (!checkIpPort(mIP.getText().toString(), mPort.getText().toString())) {
                    Toast.makeText(v.getContext(), "Please enter a valid ip & port", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!mConnecttoSession.getText().equals(leaveSessionMessage)) {
                    mConnecttoSession.setText(leaveSessionMessage);
                    mJoinSession.setEnabled(false);
                    mAbort.setEnabled(true);

                    // Run common code for network connection establishment
                    networkConnectCommon();

                    // Create session
                    mConnectionClient.createSession();
                } else {
                    // the button was already clicked and we want to disconnect from the session
                    mConnecttoSession.setText(createSessionMessage);
                    mJoinSession.setEnabled(true);

                    mConnectionClient.leaveSession();
                }
                break;
            case R.id.btnResetstatus:
                if (mReset.getText().equals(resetMessage)) {
                    // mConnStatus.setText("Server status: Resetting");
                    // mPartnerDevice.setText("Partner status: no device");
                    mDebuginfo.setText("Debugging Output:\n");
                    // this.setTitle("You clicked reset");

                    if (mConnectionClient != null) mConnectionClient.disconnect();
                    mJoinSession.setText(joinSessionMessage);
                    mJoinSession.setEnabled(true);
                    mConnecttoSession.setText(createSessionMessage);
                    mConnecttoSession.setEnabled(true);

                    // Load values from the Shared Preferences Buffer
                    SharedPreferences preferences = getActivity().getSharedPreferences(PREF_FILE_NAME, v.getContext().MODE_PRIVATE);
                    mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
                    // De- or Enables Debug Window
                    if (mDevModeEnabled) {
                        mDebuginfo.setVisibility(View.VISIBLE);
                        mDebuginfo.requestFocus();
                    } else {
                        mDebuginfo.setVisibility(View.GONE);  // View.invisible results in an error
                    }

                    ip = preferences.getString("ip", "192.168.178.31");
                    port = preferences.getInt("port", 5566);
                    globalPort = preferences.getInt("port", 5566);
                    mIP.setText(ip);
                    mPort.setText(String.valueOf(port));
                } else if (mReset.getText().equals(resetCardMessage)) {
                    mConnectionClient.disconnectCardWorkaround();
                } else {
                    Log.e(TAG, "resetButtonClicked: Unknown message");
                }
                break;
        }
    }

    public boolean checkIpPort(String ip, String port) {
        boolean validPort = false;
        boolean gotException = false;
        boolean validIp = false;
        Pattern pattern = Pattern.compile(regexIPpattern);
        Matcher matcher = pattern.matcher(ip);
        int int_port = 0;
        try {
            int_port = Integer.parseInt(port.trim());
        } catch (NumberFormatException e) {
            gotException = true;
        }
        if (!gotException) {
            if ((int_port > 0) && (int_port <= maxPort)) validPort = true;
        }
        validIp = matcher.matches();
        if (validPort) globalPort = int_port;
        return validPort && validIp;
    }

    private void networkConnectCommon() {
        // Initialize SinkManager
        mSinkManager = new SinkManager(mSinkManagerQueue);

        // Initialize FilterManager
        mFilterManager = new FilterManager();

        // Pass references
        mNfcManager.setSinkManager(mSinkManager, mSinkManagerQueue);
        mNfcManager.setFilterManager(mFilterManager);
        mNfcManager.setNetworkHandler(mConnectionClient);

        // FIXME For debugging purposes, hardcoded selecting of sinks happens here
        // This should be selectable by the user

        // Initialize debug output sink
        // TODO This should most definitely be solved in a more elegant fashion
        try {
            mSinkManager.addSink(SinkManager.SinkType.DISPLAY_TEXTVIEW, mDebuginfo);
            mSinkManager.addSink(SinkManager.SinkType.FILE, "testFile.txt");
        } catch (SinkInitException e) {
            e.printStackTrace();
        }

        // TODO Initialize and add Filters

        // Do the actual network connection
        mConnectionClient.connect(mIP.getText().toString(), port);
    }


    public void showEnableNFCDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = EnablenfcDialog.getInstance(RelayFragment.this);
        dialog.show(getFragmentManager(), "Enable NFC: ");
    }

    @Override
    public void onNFCDialogPositiveClick() {
        // User touched the dialog's goto settings button
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
    }

    @Override
    public void onNFCDialogNegativeClick() {
        // User touched the dialog's cancel button
        Toast.makeText(v.getContext(), "Caution! The app can't do something useful without NFC enabled -> please enable NFC in your phone settings", Toast.LENGTH_LONG).show();
    }

    public void showTokenDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = TokenDialog.getInstance(RelayFragment.this);
        dialog.show(getFragmentManager(), "Enter token: ");
    }

    @Override
    public void onTokenDialogPositiveClick() {
        mJoinSession.setText(leaveSessionMessage);
        mConnecttoSession.setEnabled(false);
        mAbort.setEnabled(true);

        // Run common network connection est. code
        networkConnectCommon();

        // Load token from the Shared Preferences Buffer
        SharedPreferences preferences = v.getContext().getSharedPreferences(PREF_FILE_NAME, v.getContext().MODE_PRIVATE);
        String token = preferences.getString("token", "000000");

        mConnectionClient.joinSession(token);

    }

    @Override
    public void onTokenDialogNegativeClick() {
        // User touched the dialog's cancel button
        // Toast.makeText(this, "You clicked cancel, no connection was established...", Toast.LENGTH_LONG).show();
    }
}
