package tud.seemuh.nfcgate;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
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

import tud.seemuh.nfcgate.network.CallbackImpl;
import tud.seemuh.nfcgate.network.SimpleLowLevelNetworkConnectionClientImpl;
import tud.seemuh.nfcgate.network.WiFiDirectBroadcastReceiver;


public class MainActivity extends Activity implements token_dialog.NoticeDialogListener, enablenfc_dialog.NFCNoticeDialogListener{

    private NfcAdapter mAdapter;
    private IntentFilter mIntentFilter = new IntentFilter();
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    //WiFi Direct
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private BroadcastReceiver mReceiver = null;

    //Connection Client
    protected SimpleLowLevelNetworkConnectionClientImpl mConnectionClient;

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
    private TextView mConnStatus, mInfo, mDebuginfo, mIP, mPort, mPartnerDevice;

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

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

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

        //WiFi Direct
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

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
            mIP.setText(ip);
            mPort.setText(String.valueOf(port));
            chgsett = false;
            editor.putBoolean("changed_settings", chgsett);
            editor.commit();
        }

        //WiFi Direct
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);

        mConnecttoSession.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        //WiFi Direct
        unregisterReceiver(mReceiver);

        //TODO
        //kill our threads here?
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("DEBUG", "onNewIntent(): started");
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.i("NFCGATE_DEBUG","Discovered tag with intent: " + intent);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            String tagId = "";

            mNetCallback.setTag(tag);
            mNetCallback.setUpdateButton(mDebuginfo);

            mDebuginfo.setText(mDebuginfo + "\n Identified a new Tag: " + tagId);
            Toast.makeText(this, "Found Tag: " + tagId, Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user touches the button 'ButtonResetClicked application'  -- Code by Tom */
    public void ButtonResetClicked(View view) {
        // reset the entire application by pressing this button

        mConnStatus.setText("Server status: Resetting");
        // ToDo -> really reset network connection
        mPartnerDevice.setText("Partner status: no device");
        // Todo -> notify partner on reset method called
        mInfo.setText("Please hold your device next to an NFC tag / reader");
        mDebuginfo.setText("Debugging Output: ");
        this.setTitle("You clicked reset");

        mJoinSession.setText("Join Session");
        mJoinSession.setEnabled(true);
        mConnecttoSession.setText("Create Session");
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
        mIP.setText(ip);
        mPort.setText(String.valueOf(port));
    }

    /** Called when the user touches the button 'Abort'  -- Code by Tom */
    public void ButtonAbortClicked(View view) {
        // Abort the current connection attempt
        // TODO Aboard the connection -> properly close network connection
        mJoinSession.setText("Join Session");
        mJoinSession.setEnabled(true);
        mConnecttoSession.setText("Create Session");
        mConnecttoSession.setEnabled(true);

        mConnStatus.setText("Server status: Disconnecting");
        mPartnerDevice.setText("Partner status: no device");
        // Todo -> notify Partner about abort
        this.setTitle("You clicked abort");
    }

    /** Called when the user touches the button 'Create Session'  -- Code by Tom */
    public void ButtonCreateSessionClicked(View view) {
        // Create a new Session
        // Todo -> nach cancel gedrückt -> setzt er trotzdem die button namen falsch
        if (!mConnecttoSession.getText().equals("Leave Session"))
        {
            mConnecttoSession.setText("Leave Session");
            mJoinSession.setEnabled(false);

            // Todo -> check op valid IP

            String host = mIP.getText().toString();
            int port;
            try {
                port = Integer.parseInt(mPort.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid port", Toast.LENGTH_SHORT).show();
                // Todo -> bei falschen port setzt er trotzdem die button namen falsch
                return;
            }
            this.setTitle("You clicked connect");
            mConnStatus.setText("Server status: Connecting - (token: )");
            mPartnerDevice.setText("Partner status: waiting");
            mConnectionClient = SimpleLowLevelNetworkConnectionClientImpl.getInstance().connect(host, port);
            // Todo notify user about the token the server assigned him -> will be displayed at mConnStatus
        }
        else
        {
            // the button was already clicked and we want to disconnect from the session
            mConnecttoSession.setText("Create Session");
            mConnStatus.setText("Server status: Disconnecting");
            mPartnerDevice.setText("Partner status: no device");
            mJoinSession.setEnabled(true);
            this.setTitle("You clicked disconnect");
            // TODO -> implement server disconnect
        }
    }

    /** Called when the user touches the button 'Join Session'  -- Code by Tom */
    public void ButtonJoinSessionClicked(View view) {
        // Join an existing session
        // Todo -> nach cancel gedrückt -> setzt er trotzdem die button namen falsch
        if (!mJoinSession.getText().equals("Leave Session"))
        {
            mJoinSession.setText("Leave Session");
            mConnecttoSession.setEnabled(false);

            String host = mIP.getText().toString();
            int port;
            try {
                port = Integer.parseInt(mPort.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid port", Toast.LENGTH_SHORT).show();
                return;
            }
            this.setTitle("You clicked connect");

            // Display dialog to enter the token
            showTokenDialog();

            mConnStatus.setText("Server status: Connecting");
            mPartnerDevice.setText("Partner status: waiting");
            mConnectionClient = SimpleLowLevelNetworkConnectionClientImpl.getInstance().connect(host, port);
        }
        else
        {
            // the button was already clicked and we want to disconnect from the session
            mJoinSession.setText("Join Session");
            mConnStatus.setText("Server status: Disconnecting");
            mPartnerDevice.setText("Partner status: no device");
            mConnecttoSession.setEnabled(true);
            this.setTitle("You clicked disconnect");
            // TODO -> implement server disconnect
        }
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
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
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

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the token_dialog.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        // todo user typed token into field & pressed submit
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        Toast.makeText(this, "To abort the connection press the Abort button above", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNFCDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
        startActivity(intent);
}

    @Override
    public void onNFCDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
        // Tell the user he is stupid -> the app wont work without NFC enabled...
        Toast.makeText(this, "Caution! The app won't work without NFC enabled -> please enable NFC in your phone settings", Toast.LENGTH_LONG).show();
    }

    /*
    TODO
    manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

        @Override
        public void onSuccess() {
            Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int reasonCode) {
            Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                    Toast.LENGTH_SHORT).show();
        }
    });
     */

    //TODO
    //onStop()
    //destory all threads
}
