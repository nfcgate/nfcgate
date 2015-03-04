package tud.seemuh.nfcgate;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.ReaderCallback;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tud.seemuh.nfcgate.hce.DaemonConfiguration;
import tud.seemuh.nfcgate.network.CallbackImpl;
import tud.seemuh.nfcgate.network.HighLevelNetworkHandler;
import tud.seemuh.nfcgate.network.NetHandler;

public class MainActivity extends Activity implements token_dialog.NoticeDialogListener, enablenfc_dialog.NFCNoticeDialogListener, ReaderCallback{

    private NfcAdapter mAdapter;
    private IntentFilter mIntentFilter = new IntentFilter();
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    //Connection Client
    protected HighLevelNetworkHandler mConnectionClient;

    // Defined name of the Shared Preferences Buffer
    public static final String PREF_FILE_NAME = "SeeMoo.NFCGate.Prefs";

    // private var set by settings dialog whether dev mode is enabled or not
    private boolean mDevModeEnabled = false;

    // IP:Port combination saved for enhanced user comfort
    private String ip;
    private int port;

    private CallbackImpl mNetCallback = new CallbackImpl();

    // declares main functionality
    private Button mReset, mConnecttoSession, mAbort, mJoinSession;
    private TextView mConnStatus, mInfo, mDebuginfo, mIP, mPort, mPartnerDevice, mtoken;

    // regex for IP checking
    private static final String regexIPpattern ="^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static String joinSessionMessage = "Join Session";
    public static String createSessionMessage = "Create Session";
    public static String leaveSessionMessage = "Leave Session";
    public static String resetMessage = "Reset";
    public static String resetCardMessage = "Forget Card";

    // max. port possible
    private static int maxPort = 65535;
    int globalPort = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = NfcAdapter.getDefaultAdapter(this);

        mIntentFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

        if (!mAdapter.isEnabled())
        {
            // NFC is not enabled -> "Tell the user to enable NFC"
            showEnableNFCDialog();
        }

        // Create a generic PendingIntent that will be delivered to this activity.
        // The NFC stack will fill in the intent with the details of the discovered tag before
        // delivering to this activity.
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
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
        mReset = (Button) findViewById(R.id.resetstatus);
        mConnecttoSession = (Button) findViewById(R.id.btnCreateSession);
        mJoinSession = (Button) findViewById(R.id.btnJoinSession);
        mAbort = (Button) findViewById(R.id.abortbutton);
        mConnStatus = (TextView) findViewById(R.id.editConnectionStatus);
        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
        mIP = (TextView) findViewById(R.id.editIP);
        mPort = (TextView) findViewById(R.id.editPort);
        mPartnerDevice = (TextView) findViewById(R.id.editOtherDevice);
        mConnecttoSession.requestFocus();
        mtoken = (TextView) findViewById(R.id.token);

        mConnectionClient = NetHandler.getInstance();

        //Set the views to update the GUI from another thread
        mConnectionClient.setDebugView(mDebuginfo);
        mConnectionClient.setConnectionStatusView(mConnStatus);
        mConnectionClient.setPeerStatusView(mPartnerDevice);
        mConnectionClient.setButtons(mReset, mConnecttoSession, mAbort, mJoinSession);
        mConnectionClient.setCallback(mNetCallback);

        File bcmdevice = new File("/dev/bcm2079x-i2c");
        // TODO Add an option to "never show this again"
        final SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        boolean neverShowAgain = preferences.getBoolean("mNeverWarnWorkarounds", false);
        if (bcmdevice.exists() && !neverShowAgain) {
            LayoutInflater checkboxInflater = this.getLayoutInflater();
            final View checkboxView = checkboxInflater.inflate(R.layout.workaroundwarning, null);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.BCMWarnHeader)
                    .setView(checkboxView)
                    //.setMessage(R.string.BCMWarn)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CheckBox dontShowAgain = (CheckBox) checkboxView.findViewById(R.id.neverAgain);
                            if (dontShowAgain.isChecked()) {
                                Log.i("MainActivity", "onCreate: Don't show this again is checked");
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putBoolean("mNeverWarnWorkaround", true);

                                editor.apply();
                            }
                        }
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            CheckBox dontShowAgain = (CheckBox) checkboxView.findViewById(R.id.neverAgain);
                            if (dontShowAgain.isChecked()) {
                                Log.i("MainActivity", "onCreate: Don't show this again is checked");
                                SharedPreferences.Editor editor = preferences.edit();

                                editor.putBoolean("mNeverWarnWorkaround", true);

                                editor.apply();
                            }
                        }
                    })
                    .show();
            CheckBox dontShowAgain = (CheckBox) checkboxView.findViewById(R.id.neverAgain);
            if (dontShowAgain.isChecked()) {

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("DEBUG", "onResume(): intent: " + getIntent().getAction());

        // Load values from the Shared Preferences Buffer
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);

        if (mAdapter != null && mAdapter.isEnabled()) {
            mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);

            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
                Log.i("NFCGATE_DEBUG", "onResume(): starting onNewIntent()...");
                onNewIntent(getIntent());
            }
        }

        ip = preferences.getString("ip", "192.168.178.31");
        port = preferences.getInt("port",5566);
        globalPort = preferences.getInt("port",5566);
        mIP.setText(ip);
        mPort.setText(String.valueOf(port));

        //ReaderMode
        boolean isReaderModeEnabled = preferences.getBoolean("mReaderModeEnabled", false);
        if(isReaderModeEnabled) {
            mAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
        } else {
            mAdapter.disableReaderMode(this);
        }

        // De- or Enables Debug Window
        mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
        if (mDevModeEnabled) {
            mDebuginfo.setVisibility(View.VISIBLE);
            mDebuginfo.requestFocus();
        } else {
            mDebuginfo.setVisibility(View.GONE);  // View.invisible results in an error
        }

        mConnecttoSession.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();

        mAdapter.disableForegroundDispatch(this);
    }

    /**
     * Function to get tag when readerMode is enabled
     * @param tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {

        Log.i("NFCGATE_DEBUG","Discovered tag in ReaderMode");
        mNetCallback.setTag(tag);

        //Toast here is not possible -> exception...
        // TODO This may lead to weird results if we are not already in a session
        if (mConnectionClient != null) mConnectionClient.notifyCardFound();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("DEBUG", "onNewIntent(): started");
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.i("NFCGATE_DEBUG","Discovered tag with intent: " + intent);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            mNetCallback.setTag(tag);

            // TODO This may lead to weird results if we are not already in a session
            if (mConnectionClient != null) mConnectionClient.notifyCardFound();

            Toast.makeText(this, "Found Tag", Toast.LENGTH_SHORT).show();
        }
    }

    public void ButtonResetClicked(View view) {
        // reset the entire application by pressing this button

        if (mReset.getText().equals(resetMessage)) {
            // mConnStatus.setText("Server status: Resetting");
            // mPartnerDevice.setText("Partner status: no device");
            mDebuginfo.setText("Debugging Output: ");
            // this.setTitle("You clicked reset");

            if (mConnectionClient != null) mConnectionClient.disconnect();
            mJoinSession.setText(joinSessionMessage);
            mJoinSession.setEnabled(true);
            mConnecttoSession.setText(createSessionMessage);
            mConnecttoSession.setEnabled(true);

            // Load values from the Shared Preferences Buffer
            SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
            mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
            // De- or Enables Debug Window
            mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
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
            Log.e("MainActivity", "resetButtonClicked: Unknown message");
        }
    }

    public void ButtonAbortClicked(View view) {
        // Abort the current connection attempt
        mJoinSession.setText(joinSessionMessage);
        mJoinSession.setEnabled(true);
        mConnecttoSession.setText(createSessionMessage);
        mConnecttoSession.setEnabled(true);
        mAbort.setEnabled(false);

        // mConnStatus.setText("Server status: Disconnecting");
        // mPartnerDevice.setText("Partner status: no device");
        if (mConnectionClient != null) mConnectionClient.disconnect();
        //this.setTitle("You clicked abort");
    }

    public void ButtonCreateSessionClicked(View view) {
        // Create a new Session
        if (!checkIpPort(mIP.getText().toString(), mPort.getText().toString()))
        {
            Toast.makeText(this, "Please enter a valid ip & port", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mConnecttoSession.getText().equals(leaveSessionMessage))
        {
            mConnecttoSession.setText(leaveSessionMessage);
            mJoinSession.setEnabled(false);
            mAbort.setEnabled(true);
//            this.setTitle("You clicked connect");
//            mConnStatus.setText("Server status: Connecting");
//            mPartnerDevice.setText("Partner status: waiting");

            mConnectionClient.connect(mIP.getText().toString(), port);
            mConnectionClient.createSession();
        }
        else
        {
            // the button was already clicked and we want to disconnect from the session
            mConnecttoSession.setText(createSessionMessage);
//            mConnStatus.setText("Server status: Disconnecting");
//            mPartnerDevice.setText("Partner status: no device");
            mJoinSession.setEnabled(true);
            // this.setTitle("You clicked disconnect");

            mConnectionClient.leaveSession();
        }
    }

    public void ButtonJoinSessionClicked(View view) {
        // Join an existing session
        if (!checkIpPort(mIP.getText().toString(), mPort.getText().toString()))
        {
            Toast.makeText(this, "Please enter a valid ip & port", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mJoinSession.getText().equals(leaveSessionMessage))
        {
            // Display dialog to enter the token
            showTokenDialog();   // all logic is implemented below in "onTokenDialogPositiveClick" method
        }
        else
        {
            // the button was already clicked and we want to disconnect from the session
            mJoinSession.setText(joinSessionMessage);
            //mConnStatus.setText("Server status: Disconnecting");
            //mPartnerDevice.setText("Partner status: no device");
            mConnecttoSession.setEnabled(true);
            //this.setTitle("You clicked disconnect");

            mConnectionClient.leaveSession();
        }
    }

    public boolean checkIpPort(String ip, String port)
    {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case  R.id.action_settings:
                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), 0);
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            case R.id.action_getpatchstate:
                Toast.makeText(this, "Patch state: " + (DaemonConfiguration.getInstance().isPatchEnabled() ? "Active" : "Inactive"), Toast.LENGTH_LONG).show();
                return true;
            case R.id.action_disablepatch:
                DaemonConfiguration.getInstance().disablePatch();
                Toast.makeText(this, "Patch disabled", Toast.LENGTH_LONG).show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showEnableNFCDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new enablenfc_dialog();
        dialog.show(this.getFragmentManager(), "Enable NFC: ");
    }

    public void showTokenDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new token_dialog();
        dialog.show(this.getFragmentManager(), "Enter token: ");
    }

    @Override
    public void onTokenDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's submit button
        // Toast.makeText(this, "You clicked submit, server is now processing your token...", Toast.LENGTH_LONG).show();

        mJoinSession.setText(leaveSessionMessage);
        mConnecttoSession.setEnabled(false);
        mAbort.setEnabled(true);
        //this.setTitle("You clicked connect");
        //mConnStatus.setText("Server status: Connecting");
        //mPartnerDevice.setText("Partner status: waiting");

        mConnectionClient.connect(mIP.getText().toString(), globalPort);

        // Load token from the Shared Preferences Buffer
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        String token = preferences.getString("token", "000000");

        mConnectionClient.joinSession(token);

    }

    @Override
    public void onTokenDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's cancel button
        // Toast.makeText(this, "You clicked cancel, no connection was established...", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNFCDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's goto settings button
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
}

    @Override
    public void onNFCDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's cancel button
        Toast.makeText(this, "Caution! The app can't do something useful without NFC enabled -> please enable NFC in your phone settings", Toast.LENGTH_LONG).show();
    }
}
