package tud.seemuh.nfcgate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * This activity creates the view for our settings section using the settings.xml file
 */
public class SettingsActivity extends Activity{

    // Define whether Debugging Mode is enabled or not
    private CheckBox mDevMode;
    // Define ReaderMode (disables HCE)
    private CheckBox mReaderMode;
    private TextView supportedFeatures;
    private boolean mDevModeEnabled;
    private boolean mReaderModeEnabled;
    private Button mbtnSaveSettings;

    // Define IP:Port Settings
    private TextView mIP,mPort;
    private String ip;
    private int port;

    // Hardware features of the current smartphone
    private NfcAdapter mAdapter;
    private boolean nfcisActive;
    private boolean hce;

    // Defined name of the Shared Preferences Buffer
    public static final String PREF_FILE_NAME = "SeeMoo.NFCGate.Prefs";

    // regex for IP checking
    private static final String regexIPpattern ="^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    // max. port possible
    private static int maxPort = 65535;
    int globalPort = 0;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mDevMode = (CheckBox) findViewById(R.id.checkBoxDevMode);
        mReaderMode = (CheckBox) findViewById(R.id.checkReaderMode);
        mIP = (TextView) findViewById(R.id.editIP);
        mPort = (TextView) findViewById(R.id.editPort);
        supportedFeatures = (TextView) findViewById(R.id.textViewSupportedFeatures);
        mbtnSaveSettings = (Button) findViewById(R.id.btnSaveSettings);

        // create Shared Preferences Buffer in private mode
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);

        // retrieve mDevModeEnabled from the preferences buffer, if not found set to false
        mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
        // retrieve mReaderModeEnabled
        mReaderModeEnabled = preferences.getBoolean("mReaderModeEnabled", false);
        // reload saved values & if not found set to default IP:Port (192.168.178.31:5566)
        ip = preferences.getString("ip", "192.168.178.31");
        port = preferences.getInt("port",5566);
        globalPort = port;
        // give the loaded (or default) values to the views
        mIP.setText(ip);
        mPort.setText(String.valueOf(port));
        mDevMode.setChecked(mDevModeEnabled);
        mReaderMode.setChecked(mReaderModeEnabled);

        nfcisActive = false;
        hce = getPackageManager().hasSystemFeature("android.hardware.nfc.hce");

        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter != null && mAdapter.isEnabled()) {nfcisActive = true; }

        String values = " NFC: ";
        if (nfcisActive)
        {
            values = values + "is enabled";
        }
        else
        {
            values = values + "is not enabled";
        }
        values = values + "\n HCE: ";
        if (hce)
        {
            values = values + "is available";
        }
        else
        {
            values = values + "is not available";
        }
        supportedFeatures.setText("\n Supported features by your smartphone: \n" + values);

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

    public void btnSaveSettingsClicked(View view)
    {
        if (!checkIpPort(mIP.getText().toString(), mPort.getText().toString()))
        {
            Toast.makeText(this, "Please enter a valid ip & port", Toast.LENGTH_LONG).show();
            return;
        }

        mDevModeEnabled = (((CheckBox) findViewById(R.id.checkBoxDevMode)).isChecked());
        mReaderModeEnabled = (((CheckBox) findViewById(R.id.checkReaderMode)).isChecked());

        // create Shared Preferences Buffer in private mode
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);

        // Store some of the application settings in the preferences buffer
        SharedPreferences.Editor editor = preferences.edit();
        // save mDevModeEnabled into the to the preferences buffer
        editor.putBoolean("mDevModeEnabled", mDevModeEnabled);
        // save mReaderModeEnabled...
        editor.putBoolean("mReaderModeEnabled", mReaderModeEnabled);
        // save ip into the to the preferences buffer
        editor.putString("ip", mIP.getText().toString());
        // save port into the to the preferences buffer
        editor.putInt("port", globalPort);
        editor.commit();

        // sent the user back to the main activity
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
        finish();
    }

    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
