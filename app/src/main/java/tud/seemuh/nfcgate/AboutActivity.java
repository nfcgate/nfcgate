package tud.seemuh.nfcgate;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Tom on 21.11.2014.
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
