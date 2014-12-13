package tud.seemuh.nfcgate;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Tom on 21.11.2014.
 *
 * This activity creates the view for our settings section using the settings.xml file
 */
public class SettingsActivity extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
    }
    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
