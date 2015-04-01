package tud.seemuh.nfcgate.gui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.util.db.DbInitTask;

/**
 *
 * This activity creates the view for our splash screen which is the first activity started when launching the application
 * Here we use our splash.xml file
 */
public class Splash extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        // Start an AsyncTask to create / update the database scheme, if needed.
        new DbInitTask().execute(this);

        Thread wait = new Thread()
        {
            public void run()
            {
                try
                {
                    sleep(2000);
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    Intent Main = new Intent("tud.seemuh.nfcgate.main");
                    startActivity(Main);
                }
            }
        };
        wait.start();
    }
    protected void onPause()
    {
        super.onPause();
        finish();
    }
}
