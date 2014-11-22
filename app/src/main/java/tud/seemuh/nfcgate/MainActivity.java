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
import android.widget.TextView;

import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClientImpl;
import tud.seemuh.nfcgate.network.WiFiDirectBroadcastReceiver;
import tud.seemuh.nfcgate.reader.IsoDepReaderImpl;
import tud.seemuh.nfcgate.reader.NFCTagReader;
import tud.seemuh.nfcgate.reader.NfcAReaderImpl;
import tud.seemuh.nfcgate.util.Utils;


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
    private Worker workerRunnable = null;
    private Thread workerThread;

    /**
     * called at FIRST, next: onStart()
     * @param savedInstanceState
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
        // The NFC stack
        // will fill in the intent with the details of the discovered tag before
        // delivering to
        // this activity.
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
        mConnectionClient = SimpleNetworkConnectionClientImpl.getInstance().connect("192.168.178.31", 5566);
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
     * @param intent
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
                    tagId = "IsoDep: " + tagId;
                    found_supported_tag = true;

                    //Start worker for NW communication
                    startWorker(tag);
                    break;
                } else if("android.nfc.tech.NfcA".equals(type)) {
                    tagId = Utils.bytesToHex(NfcA.get(tag).getTag().getId());
                    tagId = "NfcA: " + tagId;
                    found_supported_tag = true;

                    //Start worker for NW communication
                    startWorker(tag);
                    break;
                }
            }

            if(!found_supported_tag) {
                tagId = "Not supported";
            }

            TextView view = (TextView) findViewById(R.id.hello);
            view.setText("Found Tag: " + tagId);

        }
    }

    private void startWorker(Tag tag) {
        if(workerRunnable == null) {
            workerRunnable = new Worker(tag);
            workerThread = new Thread(workerRunnable);
            workerThread.start();
        }
    }

    private class Worker implements Runnable {
        private Tag tag;

        public Worker(Tag t) {
            tag = t;
        }

        public void run() {
            Log.d(Worker.class.getName(), "started new worker thread for networking");
            NFCTagReader reader = null;
            for(String type: tag.getTechList()) {
                Log.i("NFCGATE_DEBUG", "Checking technology: " + type);
                if ("android.nfc.tech.IsoDep".equals(type)) {
                    reader = new IsoDepReaderImpl(tag);
                    Log.d("NFCGATE_DEBUG", "Chose IsoDep technology.");
                    break;
                } else if ("android.nfc.tech.NfcA".equals(type)) {
                    reader = new NfcAReaderImpl(tag);
                    Log.d("NFCGATE_DEBUG", "Chose IsoDep technology.");
                    break;
                } else {
                    Log.d("NFCGATE_DEBUG", "Technology not (yet) supported: " + type);
                }
            }

            // Check if the reader was instantiated (if it was not, no supported tech was found)
            if (reader == null) {
                Log.e("NFCGATE_DEBUG", "No supported tech found for this tag. Aborting :(");
                return;
            }

            byte[] bytesFromCard;
            byte[] nwBytes = mConnectionClient.getBytes();

            while(true) {
                if (nwBytes != null && nwBytes.length != 0) {
                    Log.d(Worker.class.getName(), "got following bytes from nw: " + Utils.bytesToHex(nwBytes));

                    bytesFromCard = reader.sendCmd(nwBytes);
                    if(bytesFromCard != null) {
                        Log.d(Worker.class.getName(), "got the following bytes from tag: " + Utils.bytesToHex(bytesFromCard));
                        mConnectionClient.sendBytes(bytesFromCard);
                    } else {
                        Log.e(Worker.class.getName(), "Did not receive a reply from tag... :(");
                        // TODO: This means that there probably was an exception in the Reader instance that we should handle
                        //       (or at least notify the other party about)
                    }
                }
                nwBytes = mConnectionClient.getBytes();
            }
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
