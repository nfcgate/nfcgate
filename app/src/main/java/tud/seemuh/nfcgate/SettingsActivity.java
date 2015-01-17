package tud.seemuh.nfcgate;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * This activity creates the view for our settings section using the settings.xml file
 */
public class SettingsActivity extends Activity{

    // Define whether Debugging Mode is enabled or not
    private CheckBox mDevMode;
    private boolean mDevModeEnabled;
    private Button mbtnSaveSettings;

    // Define IP:Port Settings
    private TextView mIP,mPort;
    private String ip;
    private int port;

    // Defined name of the Shared Preferences Buffer
    public static final String PREF_FILE_NAME = "SeeMoo.NFCGate.Prefs";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mDevMode = (CheckBox) findViewById(R.id.checkBoxDevMode);
        mIP = (TextView) findViewById(R.id.editIP);
        mPort = (TextView) findViewById(R.id.editPort);
        mbtnSaveSettings = (Button) findViewById(R.id.btnSaveSettings);

        // create Shared Preferences Buffer in private mode
        // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);

        // retrieve mDevModeEnabled from the preferences buffer, if not found set to false
        mDevModeEnabled = preferences.getBoolean("mDevModeEnabled", false);
        // reload saved values & if not found set to default IP:Port (192.168.178.31:5566)
        ip = preferences.getString("ip", "192.168.178.31");
        port = preferences.getInt("port",5566);
        // give the loaded (or default) values to the textviews
        mIP.setText(ip);
        mPort.setText(String.valueOf(port));
        mDevMode.setChecked(mDevModeEnabled);

       /* mIP.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v,boolean hasFocus){
                if (!hasFocus) {
                    ip = mIP.getText().toString();
                    // Toast.makeText(this, "had focus and then lost focus", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mPort.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    try {
                        port = Integer.parseInt(mPort.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        // Toast.makeText(this, "Please enter a valid port", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        // Store some of the application settings in the preferences buffer
        SharedPreferences.Editor editor = preferences.edit();
        // save ip into the to the preferences buffer
        editor.putString("ip", ip);
        // save port into the to the preferences buffer
        editor.putInt("port", port);
        editor.commit();*/
    }

    /** Called when the user touches the button 'btnSaveSettingsClicked'  -- Code by Tom */
    public void btnSaveSettingsClicked(View view)
    {
        ip = mIP.getText().toString();
        try {
            port = Integer.parseInt(mPort.getText().toString().trim());
        } catch (NumberFormatException e) {
            // Toast.makeText(this, "Please enter a valid port", Toast.LENGTH_SHORT).show();
        }

        boolean checked = (((CheckBox) findViewById(R.id.checkBoxDevMode)).isChecked());
        if (checked) {
            this.mDevModeEnabled = true;
        } else {
            this.mDevModeEnabled = false;
        }

        // save all these values
        // create Shared Preferences Buffer in private mode
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
       // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Store some of the application settings in the preferences buffer
        SharedPreferences.Editor editor = preferences.edit();
        // save mDevModeEnabled into the to the preferences buffer
        editor.putBoolean("mDevModeEnabled", mDevModeEnabled);
        // save ip into the to the preferences buffer
        editor.putString("ip", ip);
        // save port into the to the preferences buffer
        editor.putInt("port", port);
        editor.commit();
    }

/*
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (!hasFocus)
        {
            ip = mIP.getText().toString();
            try {
                port = Integer.parseInt(mPort.getText().toString().trim());
            } catch (NumberFormatException e) {
                // Toast.makeText(this, "Please enter a valid port", Toast.LENGTH_SHORT).show();
            }
        }
    }
*/

    public void DevCheckboxClicked(View view) {
        boolean checked = (((CheckBox) findViewById(R.id.checkBoxDevMode)).isChecked());

        if (checked) {
            this.mDevModeEnabled = true;
        } else {
            this.mDevModeEnabled = false;
        }

        // store some of the application settings in the preferences buffer
        // SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // save mDevModeEnabled into the to the preferences buffer
        editor.putBoolean("mDevModeEnabled", mDevModeEnabled);
        editor.commit();
    }

    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
