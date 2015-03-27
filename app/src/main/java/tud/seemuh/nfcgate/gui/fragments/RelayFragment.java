package tud.seemuh.nfcgate.gui.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.network.Callback;
import tud.seemuh.nfcgate.network.HighLevelNetworkHandler;
import tud.seemuh.nfcgate.network.HighLevelProtobufHandler;
import tud.seemuh.nfcgate.network.ProtobufCallback;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;
import tud.seemuh.nfcgate.util.filter.FilterManager;
import tud.seemuh.nfcgate.util.sink.SinkInitException;
import tud.seemuh.nfcgate.util.sink.SinkManager;

public class RelayFragment extends Fragment
        implements OnClickListener, TokenDialog.NoticeDialogListener{

    private final static String TAG = "RelayFragment";

    //single instance of this class
    private static RelayFragment mFragment;

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

    private View mRelayView;

    private Callback mNetCallback = new ProtobufCallback();

    // private var set by settings dialog whether dev mode is enabled or not
    private boolean mDevModeEnabled = false;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRelayView = inflater.inflate(R.layout.fragment_relay, container, false);
        Log.d(TAG, "onCreateView");

        // Create Buttons & TextViews
        mReset = (Button) mRelayView.findViewById(R.id.btnResetstatus);
        mReset.setOnClickListener(this);
        mConnecttoSession = (Button) mRelayView.findViewById(R.id.btnCreateSession);
        mConnecttoSession.setOnClickListener(this);
        mJoinSession = (Button) mRelayView.findViewById(R.id.btnJoinSession);
        mJoinSession.setOnClickListener(this);
        mAbort = (Button) mRelayView.findViewById(R.id.btnAbortbutton);
        mAbort.setOnClickListener(this);
        mConnStatus = (TextView) mRelayView.findViewById(R.id.editConnectionStatus);
        mDebuginfo = (TextView) mRelayView.findViewById(R.id.editTextDevModeEnabledDebugging);
        mIP = (TextView) mRelayView.findViewById(R.id.editIP);
        mPort = (TextView) mRelayView.findViewById(R.id.editPort);
        mPartnerDevice = (TextView) mRelayView.findViewById(R.id.editOtherDevice);
        mConnecttoSession.requestFocus();
        mtoken = (TextView) mRelayView.findViewById(R.id.token);

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

        return mRelayView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume(): intent: " + getActivity().getIntent().getAction());

        // Load values from the Shared Preferences Buffer
        SharedPreferences preferences = mRelayView.getContext().getSharedPreferences(PREF_FILE_NAME, mRelayView.getContext().MODE_PRIVATE);

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

        // De- or Enables Debug Window
        mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
        mDebuginfo = (TextView) mRelayView.findViewById(R.id.editTextDevModeEnabledDebugging);
        if (mDevModeEnabled) {
            mDebuginfo.setVisibility(View.VISIBLE);
            mDebuginfo.requestFocus();
        } else {
            mDebuginfo.setVisibility(View.GONE);  // View.invisible results in an error
        }

        mConnecttoSession.requestFocus();
    }


    public static RelayFragment getInstance() {
        if(mFragment == null) {
            mFragment = new RelayFragment();
        }
        return mFragment;
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
            case R.id.btnAbortbutton:
                // Abort the current connection attempt
                mJoinSession.setText(joinSessionMessage);
                mJoinSession.setEnabled(true);
                mConnecttoSession.setText(createSessionMessage);
                mConnecttoSession.setEnabled(true);
                mAbort.setEnabled(false);

                if (mConnectionClient != null) {
                    mConnectionClient.disconnect();
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
        SharedPreferences preferences = mRelayView.getContext().getSharedPreferences(PREF_FILE_NAME, mRelayView.getContext().MODE_PRIVATE);
        String token = preferences.getString("token", "000000");

        mConnectionClient.joinSession(token);

    }

    @Override
    public void onTokenDialogNegativeClick() {
        // User touched the dialog's cancel button
        // Toast.makeText(this, "You clicked cancel, no connection was established...", Toast.LENGTH_LONG).show();
    }


}
