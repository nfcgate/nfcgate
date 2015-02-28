package tud.seemuh.nfcgate;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static String joinSessionMessage = "Join Session";
    private static String createSessionMessage = "Create Session";

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
        mInfo = (TextView) findViewById(R.id.DisplayMsg);
        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
        mIP = (TextView) findViewById(R.id.editIP);
        mPort = (TextView) findViewById(R.id.editPort);
        mPartnerDevice = (TextView) findViewById(R.id.editOtherDevice);
        mConnecttoSession.requestFocus();
        mtoken = (TextView) findViewById(R.id.token);
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

        // check if settings were changed -> if no reload default values
        boolean chgsett;
        if (preferences.getBoolean("changed_settings", false))
        {
            SharedPreferences.Editor editor = preferences.edit();
            ip = preferences.getString("ip", "192.168.178.31");
            port = preferences.getInt("port",5566);
            globalPort = preferences.getInt("port",5566);
            mIP.setText(ip);
            mPort.setText(String.valueOf(port));

            //ReaderMode
            boolean lReaderMode = preferences.getBoolean("mReaderModeEnabled", false);
            if(lReaderMode) {
                mAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null);
            } else {
                mAdapter.disableReaderMode(this);
            }

            chgsett = false;
            editor.putBoolean("changed_settings", chgsett);
            editor.commit();
        }

        mConnecttoSession.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();

        mAdapter.disableForegroundDispatch(this);
        //TODO -> kill our threads here?
    }

    /**
     * Function to get tag when readerMode is enabled
     * @param tag
     */
    @Override
    public void onTagDiscovered(Tag tag) {

        Log.i("NFCGATE_DEBUG","Discovered tag in ReaderMode");
        mNetCallback.setTag(tag);
        //Set the view to update the GUI from another thread
        mNetCallback.setUpdateView(mDebuginfo);

        //Toast here is not possible -> exception...
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("DEBUG", "onNewIntent(): started");
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.i("NFCGATE_DEBUG","Discovered tag with intent: " + intent);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            mNetCallback.setTag(tag);
            //Set the view to update the GUI from another thread
            mNetCallback.setUpdateView(mDebuginfo);

            Toast.makeText(this, "Found Tag", Toast.LENGTH_SHORT).show();
        }
    }

    public void ButtonResetClicked(View view) {
        // reset the entire application by pressing this button

        mConnStatus.setText("Server status: Resetting");
        // ToDo -> really reset network connection by calling the required method
        mPartnerDevice.setText("Partner status: no device");
        // Todo -> notify partner on reset method called by calling the required method
        mInfo.setText("Please hold your device next to an NFC tag / reader");
        mDebuginfo.setText("Debugging Output: ");
        this.setTitle("You clicked reset");

        mJoinSession.setText(joinSessionMessage);
        mJoinSession.setEnabled(true);
        mConnecttoSession.setText(createSessionMessage);
        mConnecttoSession.setEnabled(true);

        // Load values from the Shared Preferences Buffer
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
        // De- or Enables Debug Window
        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
        if (mDevModeEnabled)
        {
            mDebuginfo.setVisibility(View.VISIBLE);
            mDebuginfo.requestFocus();
        }
        else
        {
            mDebuginfo.setVisibility(View.GONE);  // View.invisible results in an error
        }

        ip = preferences.getString("ip", "192.168.178.31");
        port = preferences.getInt("port",5566);
        globalPort = preferences.getInt("port",5566);
        mIP.setText(ip);
        mPort.setText(String.valueOf(port));
    }

    public void ButtonAbortClicked(View view) {
        // Abort the current connection attempt
        // TODO Abort the connection -> properly close network connection by calling the required method
        mJoinSession.setText(joinSessionMessage);
        mJoinSession.setEnabled(true);
        mConnecttoSession.setText(createSessionMessage);
        mConnecttoSession.setEnabled(true);

        mConnStatus.setText("Server status: Disconnecting");
        mPartnerDevice.setText("Partner status: no device");
        // Todo -> notify Partner about abort by calling the required method
        this.setTitle("You clicked abort");
    }

    public void ButtonCreateSessionClicked(View view) {
        // Create a new Session
        if (!checkIpPort(mIP.getText().toString(), mPort.getText().toString()))
        {
            Toast.makeText(this, "Please enter a valid ip & port", Toast.LENGTH_LONG).show();
            return;
        }

        if (!mConnecttoSession.getText().equals("Leave Session")) // TODO Maybe refactor this to use constants?
        {
            mConnecttoSession.setText("Leave Session"); // TODO Maybe refactor this to use constants?
            mJoinSession.setEnabled(false);
            this.setTitle("You clicked connect");
            mConnStatus.setText("Server status: Connecting - (token: )");
            mPartnerDevice.setText("Partner status: waiting");
            mConnectionClient = NetHandler.getInstance().connect(mIP.getText().toString(), port);
            mConnectionClient.createSession();
            // Todo notify user about the token the server assigned him -> will be displayed at mConnStatus
        }
        else
        {
            // the button was already clicked and we want to disconnect from the session
            mConnecttoSession.setText(createSessionMessage);
            mConnStatus.setText("Server status: Disconnecting");
            mPartnerDevice.setText("Partner status: no device");
            mJoinSession.setEnabled(true);
            this.setTitle("You clicked disconnect");

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

        if (!mJoinSession.getText().equals("Leave Session"))
        {
            // Display dialog to enter the token
            showTokenDialog();
            // all logic is implemented below in "onTokenDialogPositiveClick" method
        }
        else
        {
            // the button was already clicked and we want to disconnect from the session
            mJoinSession.setText(joinSessionMessage);
            mConnStatus.setText("Server status: Disconnecting");
            mPartnerDevice.setText("Partner status: no device");
            mConnecttoSession.setEnabled(true);
            this.setTitle("You clicked disconnect");

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
        Toast.makeText(this, "You clicked submit, server is now processing your token...", Toast.LENGTH_LONG).show();

        mJoinSession.setText("Leave Session");
        mConnecttoSession.setEnabled(false);
        this.setTitle("You clicked connect");
        mConnStatus.setText("Server status: Connecting");
        mPartnerDevice.setText("Partner status: waiting");
        mConnectionClient = NetHandler.getInstance().connect(mIP.getText().toString(), globalPort);

        // Load token from the Shared Preferences Buffer
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        String token = preferences.getString("token", "000000");

        mConnectionClient.joinSession(token);

    }

    @Override
    public void onTokenDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's cancel button
        Toast.makeText(this, "You clicked cancel, no connection was established...", Toast.LENGTH_LONG).show();
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
