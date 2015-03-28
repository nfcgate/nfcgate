package tud.seemuh.nfcgate.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.fragments.SettingsFragment;

/**
 *
 * Activity containing the SettingsFragment.
 */
public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String regexIPpattern ="^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static int maxPort = 65535;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_key_ip))) {

            String ip = sharedPreferences.getString(getString(R.string.pref_key_ip), "");
            if (!isValidIP(ip)) {
                // Reset IP to an empty String
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getString(R.string.pref_key_ip), "");
                editor.apply();

                // Warn the user about the incorrect IP
                AlertDialog notice = getNoticeDialog(R.string.pref_error_ip_title, R.string.pref_error_port_text);
                notice.show();
            }
        } else if (key.equals(getString(R.string.pref_key_port))) {
            int port = sharedPreferences.getInt(getString(R.string.pref_key_port), 0);
            if (!isValidPort(port)) {
                // Reset Port to default value
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(getString(R.string.pref_key_port), 5566);
                editor.apply();

                // Warn the user about the incorrect IP
                AlertDialog notice = getNoticeDialog(R.string.pref_error_port_title, R.string.pref_error_port_text);
                notice.show();
            }
        }
    }

    private boolean isValidIP(String ip) {
        if (ip.equals("")) return true;
        // Compile RegEx pattern
        Pattern pattern = Pattern.compile(regexIPpattern);

        // Get matcher
        Matcher matcher = pattern.matcher(ip);

        // Check if IP matches pattern and return
        return matcher.matches();
    }

    private boolean isValidPort(int port) {
        return port <= maxPort && port > 0;
    }

    private AlertDialog getNoticeDialog(int titleID, int textID) {
        String title = getString(titleID);
        String text = getString(textID);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setTitle(title);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
