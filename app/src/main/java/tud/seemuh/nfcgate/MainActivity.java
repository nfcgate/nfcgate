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
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;

import tud.seemuh.nfcgate.network.SimpleNetworkConnectionClient;
import tud.seemuh.nfcgate.network.SimpleNetworkConnectionServer;
import tud.seemuh.nfcgate.network.WiFiDirectBroadcastReceiver;
import tud.seemuh.nfcgate.util.reader.IsoDepReader;
import tud.seemuh.nfcgate.util.reader.NfcAReader;
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

    //ConnectionServer
    private SimpleNetworkConnectionServer mConnectionServer;
    private SimpleNetworkConnectionClient mConnectionClient;

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
        //mIntentFilter = new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED");
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
        mConnectionClient = new SimpleNetworkConnectionClient("192.168.178.31", 5566);
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
     * Ugly button to stop server
     * @param v
     */
    public void clickStartServer(View v) {
        mConnectionServer = new SimpleNetworkConnectionServer(15000);
    }

    /**
     * Ugly button to start server
     * @param v
     */
    public void clickStopServer(View v) {
        mConnectionServer.tearDown();
    }

    /**
     * Ugly send text button
     * @param v
     */
    public void clickSendText(View v) {
        mConnectionClient.sendBytes(new byte[] {});
    }

    /**
     * Called when activity is paused
     */
    @Override
    public void onPause() {
        super.onPause();
        //WiFi Direct
        unregisterReceiver(mReceiver);
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
            byte[] bytesFromCard;

            for(String type: tag.getTechList()) {
                Log.i("NFCGATE_DEBUG", "Tag TechList: " + type);
                if("android.nfc.tech.IsoDep".equals(type)) {

                    //tagId = Utils.bytesToHex(IsoDep.get(tag).getTag().getId());
                    tagId = "IsoDep: " + tagId;
                    found_supported_tag = true;
                    //Log.i("NFCGATE_DEBUG", "Found 'IsoDep' Tag with ID: " + tagId);

                    /*
                    //byte[] nwBytes = mConnectionClient.getBytes();
                    IsoDepReader reader2 = new IsoDepReader(tag);
                    byte[] nwBytes =  {0x00, (byte) 0xa4, 0x04};
                    Log.d("NFCGATE_DEBUG", "got following bytes from nw: "+Utils.bytesToHex(nwBytes));
                    bytesFromCard = reader2.sendCmd(nwBytes);
                    Log.d("NFCGATE_DEBUG", "got the following bytes from tag 1: "+ Utils.bytesToHex(bytesFromCard));
                    bytesFromCard = reader2.sendCmd(nwBytes);
                    Log.d("NFCGATE_DEBUG", "got the following bytes from tag 2: "+ Utils.bytesToHex(bytesFromCard));
                    */


                    if(workerRunnable == null) {
                        workerRunnable = new Worker(tag);
                        workerThread = new Thread(workerRunnable);
                        workerThread.start();
                    }
                    break;
                }

                    //break;
                /*
                } else if("android.nfc.tech.NfcA".equals(type)) {
                    tagId = Utils.bytesToHex(NfcA.get(tag).getTag().getId());
                    tagId = "NfcA: "+tagId;
                    found_supported_tag = true;
                    Log.i("NFCGATE_DEBUG", "Found 'NfcA' Tag with ID: " + tagId);


                    NfcAReader reader2 = new NfcAReader(tag);
                    byte[] nwBytes = mConnectionClient.getBytes();
                    //bytesFromCard = reader.sendCmd(new byte[] {0x00, (byte) 0xa4, 0x04});
                    Log.d("NFCGATE_DEBUG", "got following bytes from nw: "+Utils.bytesToHex(nwBytes));
                    bytesFromCard = reader2.sendCmd(nwBytes);
                    Log.d("NFCGATE_DEBUG", "got the following bytes: "+ Utils.bytesToHex(bytesFromCard));
                    break;
                } else if("android.nfc.tech.Ndef".equals(type)) {
                    tagId = Utils.bytesToHex(Ndef.get(tag).getTag().getId());
                    tagId = "Ndef: "+tagId;
                    found_supported_tag = true;
                    Log.i("NFCGATE_DEBUG", "Found 'Ndef' Tag with ID: " + tagId);
                    break;
                }
                */
            }

            if(!found_supported_tag) {
                tagId = "Not supported";
            }

            TextView view = (TextView) findViewById(R.id.hello);
            view.setText("Found Tag: " + tagId);

        }
    }

    private class Worker implements Runnable {
        private Tag tag;

        public Worker(Tag t) {
            tag = t;
        }

        public void run() {
            IsoDepReader reader = new IsoDepReader(tag);
            byte[] bytesFromCard;
            byte[] nwBytes = mConnectionClient.getBytes();
            int sum = 0;

            while(true) {
                for (int i = 0; i < nwBytes.length; ++i) {
                    sum |= nwBytes[i];
                }

                if (sum != 0) {
                    Log.d("NFCGATE_DEBUG", "got following bytes from nw: " + Utils.bytesToHex(nwBytes));
                    bytesFromCard = reader.sendCmd(nwBytes);
                    Log.d("NFCGATE_DEBUG", "got the following bytes from tag: " + Utils.bytesToHex(bytesFromCard));
                    mConnectionClient.sendBytes(bytesFromCard);
                    nwBytes = mConnectionClient.getBytes();
                }
                try {
                    Thread.sleep(100);
                } catch( InterruptedException e ) { }
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
