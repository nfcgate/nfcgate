package de.tu_darmstadt.seemoo.nfcgate.gui;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import de.tu_darmstadt.seemoo.nfcgate.R;
import de.tu_darmstadt.seemoo.nfcgate.db.SessionLog;
import de.tu_darmstadt.seemoo.nfcgate.db.pcapng.ISO14443Stream;
import de.tu_darmstadt.seemoo.nfcgate.db.worker.LogInserter;
import de.tu_darmstadt.seemoo.nfcgate.gui.fragment.AboutFragment;
import de.tu_darmstadt.seemoo.nfcgate.gui.fragment.CaptureFragment;
import de.tu_darmstadt.seemoo.nfcgate.gui.fragment.CloneFragment;
import de.tu_darmstadt.seemoo.nfcgate.gui.fragment.StatusFragment;
import de.tu_darmstadt.seemoo.nfcgate.gui.log.LoggingFragment;
import de.tu_darmstadt.seemoo.nfcgate.gui.fragment.RelayFragment;
import de.tu_darmstadt.seemoo.nfcgate.gui.fragment.ReplayFragment;
import de.tu_darmstadt.seemoo.nfcgate.gui.fragment.SettingsFragment;
import de.tu_darmstadt.seemoo.nfcgate.nfc.NfcManager;
import de.tu_darmstadt.seemoo.nfcgate.nfc.reader.NFCTagReader;
import de.tu_darmstadt.seemoo.nfcgate.util.NfcComm;

public class MainActivity extends AppCompatActivity {
    // UI
    DrawerLayout mDrawerLayout;
    NavigationView mNavbar;
    Toolbar mToolbar;
    ActionBarDrawerToggle mToggle;

    // NFC
    NfcManager mNfc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar setup
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // drawer setup
        mDrawerLayout = findViewById(R.id.main_drawer_layout);

        // drawer toggle in toolbar
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.empty, R.string.empty);
        mToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when drawer icon is NOT visible (due to fragment on backstack), issue back action
                onBackPressed();
            }
        });
        mDrawerLayout.addDrawerListener(mToggle);

        // display "up-arrow" when non-empty backstack, display navigation drawer otherwise
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final ActionBar actionBar = getSupportActionBar();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    // https://stackoverflow.com/a/29594947
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    mToggle.setDrawerIndicatorEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                } else {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    mToggle.setDrawerIndicatorEnabled(true);
                    mToggle.syncState();
                }
            }
        });

        // navbar setup actions
        mNavbar = findViewById(R.id.main_navigation);
        mNavbar.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                onNavbarAction(item);
                return true;
            }
        });

        // initially select clone mode
        mNavbar.setCheckedItem(R.id.nav_clone);
        mNavbar.getMenu().performIdentifierAction(R.id.nav_clone, 0);

        // NFC setup
        mNfc = new NfcManager(this);
        if (!mNfc.hasNfc())
            showWarning("This device seems to be missing the NFC capability.");
    }

    @Override
    protected void onStart() {
        super.onStart();

        // pass initial intent to current mode in case it carries a tag
        if (getIntent() != null)
            onNewIntent(getIntent());
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        mToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // tech discovered is triggered by XML, tag discovered by foreground dispatch
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
            mNfc.onTagDiscovered(intent.<Tag>getParcelableExtra(NfcAdapter.EXTRA_TAG));
        else if (Intent.ACTION_SEND.equals(intent.getAction()))
            importPcap(intent.<Uri>getParcelableExtra(Intent.EXTRA_STREAM));
        else if (Intent.ACTION_VIEW.equals(intent.getAction()))
            importPcap(intent.getData());
        else if ("de.tu_darmstadt.seemoo.nfcgate.capture".equals(intent.getAction()))
            importCapture(intent.<Bundle>getParcelableArrayListExtra("capture"));
        else
            super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfc.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfc.onPause();
    }

    /**
     * Returns a Fragment for every navbar action
     */
    private Fragment getFragmentByAction(int id) {
        switch (id) {
            case R.id.nav_clone:
                return new CloneFragment();
            case R.id.nav_relay:
                return new RelayFragment();
            case R.id.nav_replay:
                return new ReplayFragment();
            case R.id.nav_capture:
                return new CaptureFragment();
            case R.id.nav_settings:
                return new SettingsFragment();
            case R.id.nav_status:
                return new StatusFragment();
            case R.id.nav_about:
                return new AboutFragment();
            case R.id.nav_logging:
                return new LoggingFragment();
            default:
                throw new IllegalArgumentException("Position out of range");
        }
    }

    /**
     * Handles all navbar actions by creating a new fragment
     */
    private void onNavbarAction(MenuItem item) {
        // every fragment must implement BaseFragment
        Fragment fragment = getFragmentByAction(item.getItemId());

        // remove all currently opened on-top fragments (e.g. log entry)
        getSupportFragmentManager().popBackStack();
        // no fancy animation for now
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment)
                .commit();

        // for the looks
        getSupportActionBar().setTitle(item.getTitle());
        // reset the subtitle because a fragment might have changed it
        getSupportActionBar().setSubtitle(null);
        // hide status bar
        findViewById(R.id.tag_semaphore).setVisibility(View.GONE);

        // avoid carrying over actions from previous fragment
        supportInvalidateOptionsMenu();

        mDrawerLayout.closeDrawers();
    }

    private void importPcap(Uri uri) {
        try {
            LogInserter inserter = new LogInserter(this, SessionLog.SessionType.RELAY, null);

            for (NfcComm e : new ISO14443Stream().readAll(getContentResolver().openInputStream(uri)))
                inserter.log(e);

            Toast.makeText(this, "Pcap import success", Toast.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Pcap import error", Toast.LENGTH_SHORT).show();
        }
    }

    private void importCapture(List<Bundle> capture) {
        LogInserter inserter = new LogInserter(this, SessionLog.SessionType.CAPTURE, null);

        for (Bundle b : capture)
            inserter.log(CaptureFragment.fromBundle(b));

        Toast.makeText(this, "Capture data added to log", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        // reset the subtitle because a fragment might have changed it
        getSupportActionBar().setSubtitle(null);

        super.onBackPressed();
    }

    /**
     * Displays a warning dialog with the specified message
     */
    public void showWarning(String warning) {
        new AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage(warning)
                .setNegativeButton("OK", null)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .show();
    }

    public NfcManager getNfc() {
        return mNfc;
    }
}
