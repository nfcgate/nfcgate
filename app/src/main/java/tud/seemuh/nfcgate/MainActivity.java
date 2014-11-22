package tud.seemuh.nfcgate;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.provider.Settings;
import android.widget.Toast;


import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClientImpl;
import tud.seemuh.nfcgate.network.WiFiDirectBroadcastReceiver;
import tud.seemuh.nfcgate.reader.IsoDepReaderImpl;
import tud.seemuh.nfcgate.reader.NFCTagReader;
import tud.seemuh.nfcgate.reader.NfcAReaderImpl;
import tud.seemuh.nfcgate.util.Utils;
import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClientImpl.Callback;


public class MainActivity extends Activity {

    private NfcAdapter mAdapter;
    private IntentFilter mIntentFilter = new IntentFilter();
    //private IntentFilter mIntentFilter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;
    private String[][] mTechLists;

    //WiFi Direct
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private BroadcastReceiver mReceiver = null;

    //Connection Client
    private SimpleNetworkConnectionClientImpl mConnectionClient;

    //Worker
    //private Worker workerRunnable = null;
    //private Thread workerThread;
    NFCTagReader mReader = null;

    // private var if dev mode is enabled or not
    private boolean mDevModeEnabled = false;
    private boolean connectButtonEnabled = true;

    // declares main functionality
    Button mReset, mConnect, mAbort;
    CheckBox mDevMode;
    TextView mOwnID, mInfo, mDebuginfo, mIP, mPort;


    /**
     * called at FIRST, next: onStart()
     * @param savedInstanceState saved instance state
     */
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

        // Create a generic PendingIntent that will be deliver to this activity.
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

        //TCP Client

        // Create Buttons & TextViews
        mReset = (Button) findViewById(R.id.resetstatus);
        mConnect = (Button) findViewById(R.id.connectbutton);
        mAbort = (Button) findViewById(R.id.abortbutton);
        mDevMode = (CheckBox) findViewById(R.id.checkBoxDevMode);
        mOwnID = (TextView) findViewById(R.id.editTextOwnID);
        mInfo = (TextView) findViewById(R.id.DisplayMsg);
        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
        mIP = (TextView) findViewById(R.id.editIP);
        mPort = (TextView) findViewById(R.id.editPort);
    }

    /**
     * called at SECOND, next onResume()
     * onStart(), currently not implemented
     */

    /**
     * called at THIRD
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.i("DEBUG", "onResume(): intent: " + getIntent().getAction());

        /* TODO
        Ist NFC Aktiviert checken...
        Utils.checkNfcEnabled(this,mAdapter);
         */

        mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(getIntent().getAction())) {
            Log.i("NFCGATE_DEBUG", "onResume(): starting onNewIntent()...");
            onNewIntent(getIntent());
        }

        //WiFi Direct
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);
    }

    /**
     * Called when activity is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        //WiFi Direct
        unregisterReceiver(mReceiver);

        //TODO
        //kill our threads here?
    }

    /**
     * called when app is already open and intent is fired
     * @param intent intent
     */
    @Override
    public void onNewIntent(Intent intent) {
        Log.i("DEBUG", "onNewIntent(): started");
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Log.i("NFCGATE_DEBUG","Discovered tag with intent: " + intent);
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            //Ab hier koennte man schon mit dem Tag arbeiten!!!
            boolean found_supported_tag = false;
            String tagId = "";

            for(String type: tag.getTechList()) {
                // TODO: Refactor this into something much nicer to avoid redundant work betw.
                //       this code and the worker thread, which also does this check.
                Log.i("NFCGATE_DEBUG", "Tag TechList: " + type);
                if("android.nfc.tech.IsoDep".equals(type)) {
                    tagId = Utils.bytesToHex(NfcA.get(tag).getTag().getId());
                    found_supported_tag = true;

                    mReader = new IsoDepReaderImpl(tag);
                    Log.d("NFCGATE_DEBUG", "Chose IsoDep technology.");
                    break;
                } else if("android.nfc.tech.NfcA".equals(type)) {
                    tagId = Utils.bytesToHex(NfcA.get(tag).getTag().getId());
                    tagId = "NfcA: " + tagId;
                    found_supported_tag = true;

                    mReader = new NfcAReaderImpl(tag);
                    Log.d("NFCGATE_DEBUG", "Chose NfcA technology.");
                    break;
                }
            }

            if(!found_supported_tag) {
                tagId = "Not supported";
            } else {
                SimpleNetworkConnectionClientImpl.getInstance().setCallback(mNetCallback);
            }

            mOwnID.setText("Your own ID is: " + tagId);
            Toast.makeText(this, "Found Tag: " + tagId, Toast.LENGTH_SHORT).show();
        }
    }

    private Callback mNetCallback = new Callback() {
        @Override
        public void onDataReceived(byte[] data) {
            if(mReader.isConnected()) {
                byte[] bytesFromCard = mReader.sendCmd(data);
                mConnectionClient.sendBytes(bytesFromCard);
                mDebuginfo.append(Utils.bytesToHex(bytesFromCard) + "\n");
            }
        }
    };
/*
    /** Called when the user touches the button 'ButtonResetClicked application'  -- Code by Tom */
    public void ButtonResetClicked(View view) {
        // do an entire ButtonResetClicked of the application
        mDevModeEnabled = false;
        mDevMode.setChecked(false);
        mDebuginfo.setVisibility(View.INVISIBLE);
        mOwnID.setText("Your own ID is:");
        mInfo.setText("Please hold your device next to an NFC tag / reader");
        mDebuginfo.setText("");
        this.setTitle("You clicked reset");
    }

    /** Called when the user touches the button 'Abort'  -- Code by Tom */
    public void ButtonAbortClicked(View view) {
        // Abort the current connection attempt
        // -> please append code here to kill network connections etc.
        boolean isHceSupported = getPackageManager().hasSystemFeature("android.hardware.nfc.hce");
        Toast.makeText(this, "HCE: " + (isHceSupported ? "Yes" : "No"), Toast.LENGTH_SHORT).show();
        this.setTitle("You clicked abort");
    }

    /** Called when the user touches the button 'Connect'  -- Code by Tom */
    public void ButtonConnectClicked(View view) {
        // Connect to a given IP & port
        if (connectButtonEnabled == true)
        {
            // the buttons name is connect & we want to connect to the server:port
            connectButtonEnabled = false;
            mConnect.setText("Disconnect");
        }
        else
        {
            // the button connect was already clicked and we want to disconnect from the server:port
            connectButtonEnabled = true;
            mConnect.setText("Connect");
        }
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
        mConnectionClient = SimpleNetworkConnectionClientImpl.getInstance().connect(host, port);
    }

    /** Called when the user checkes the checkbox 'enable dev mode'  -- Code by Tom */
    public void DevCheckboxClicked(View view) {
        boolean checked = (((CheckBox) findViewById(R.id.checkBoxDevMode)).isChecked());
        mDebuginfo = (TextView) findViewById(R.id.editTextDevModeEnabledDebugging);
        if (checked) {
            this.mDevModeEnabled = true;
            mDebuginfo.setVisibility(View.VISIBLE); }
        else {
            this.mDevModeEnabled = false;
            mDebuginfo.setVisibility(View.INVISIBLE); }
        mDebuginfo.setText("Value of DevMode = " + this.mDevModeEnabled);
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
