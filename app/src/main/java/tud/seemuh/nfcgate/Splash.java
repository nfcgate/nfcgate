package tud.seemuh.nfcgate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Tom on 21.11.2014.
 */
public class Splash extends Activity{

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
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
