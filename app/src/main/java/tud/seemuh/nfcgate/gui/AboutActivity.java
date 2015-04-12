package tud.seemuh.nfcgate.gui;

import android.app.Activity;
import android.os.Bundle;

import tud.seemuh.nfcgate.R;

/**
 *
 *  This activity creates the view for our about section using the about.xml file
 */
public class AboutActivity extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }
    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
