package tud.seemuh.nfcgate.gui;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Toast;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.fragments.LoggingDetailFragment;

public class LoggingDetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoggingDetailFragment ldf = new LoggingDetailFragment();
        // Pass arguments to fragment
        ldf.setArguments(getIntent().getExtras());

        setContentView(R.layout.activity_logging_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, ldf)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
