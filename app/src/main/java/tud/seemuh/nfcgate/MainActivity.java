package tud.seemuh.nfcgate;

import android.app.Activity;
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


public class MainActivity extends Activity {

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
    // private var if connect Button is enabled or not
    private boolean connectButtonEnabled = true;

    // IP:Port combination saved for enhanced user comfort
    private String ip;
    private int port;

    private CallbackImpl mNetCallback = new CallbackImpl();

    // declares main functionality
    private Button mReset, mConnect, mAbort;
    private TextView mOwnID, mInfo, mDebuginfo, mIP, mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mIntentFilter.addAction(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);

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
        mConnect = (Button) findViewById(R.id.connectbutton);
        mAbort = (Button) findViewById(R.id.abortbutton);
        mOwnID = (TextView) findViewById(R.id.editTextOwnID);
        mInfo = (TextView) findViewById(R.id.DisplayMsg);
        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
        mIP = (TextView) findViewById(R.id.editIP);
        mPort = (TextView) findViewById(R.id.editPort);

        mConnect.requestFocus();
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

        mConnect.requestFocus();
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

            mOwnID.setText("Your own ID is: " + tagId);
            Toast.makeText(this, "Found Tag: " + tagId, Toast.LENGTH_SHORT).show();
        }
    }

    /** Called when the user touches the button 'ButtonResetClicked application'  -- Code by Tom */
    public void ButtonResetClicked(View view) {
        // reset the entire application by pressing this button

        mOwnID.setText("Your own ID is:");
        mInfo.setText("Please hold your device next to an NFC tag / reader");
        mDebuginfo.setText("Debugging Infos: ");
        this.setTitle("You clicked reset");

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
        // -> please append code here to kill network connections etc.
        // boolean isHceSupported = getPackageManager().hasSystemFeature("android.hardware.nfc.hce");
        // Toast.makeText(this, "HCE: " + (isHceSupported ? "Yes" : "No"), Toast.LENGTH_SHORT).show();
        // TODO Aboard the connection e.g. properly close Server Connection etc.
        this.setTitle("You clicked abort");
    }

    /** Called when the user touches the button 'Connect'  -- Code by Tom */
    public void ButtonConnectClicked(View view) {
        // Connect to a given IP & port
        if (connectButtonEnabled)
        {
            // the buttons name is connect & we want to connect to the server:port
            connectButtonEnabled = false;
            mConnect.setText("Disconnect");
            String host = mIP.getText().toString();
            int port;
            try {
                port = Integer.parseInt(mPort.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid port", Toast.LENGTH_SHORT).show();
                return;
            }
            this.setTitle("You clicked connect");
            // -> please append code here to ButtonConnectClicked to IP:Port
            mConnectionClient = SimpleLowLevelNetworkConnectionClientImpl.getInstance().connect(host, port);
        }
        else
        {
            // the button connect was already clicked and we want to disconnect from the server:port
            connectButtonEnabled = true;
            mConnect.setText("Connect");
            // do some fancy stuff to disconnect from the server!
            // TODO
            // implement server disconnect
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
            case R.id.action_nfc:
                startActivityForResult(new Intent(Settings.ACTION_NFC_SETTINGS), 0);
                return true;
            case R.id.action_app:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                return true;
            case  R.id.action_settings:
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
