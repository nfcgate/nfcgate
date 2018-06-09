package tud.seemuh.nfcgate.gui;

import android.content.Intent;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.db.AppDatabase;
import tud.seemuh.nfcgate.db.NfcCommEntry;
import tud.seemuh.nfcgate.db.SessionLog;
import tud.seemuh.nfcgate.db.SessionLogJoin;
import tud.seemuh.nfcgate.gui.fragment.AboutFragment;
import tud.seemuh.nfcgate.gui.fragment.BaseFragment;
import tud.seemuh.nfcgate.gui.fragment.CloneFragment;
import tud.seemuh.nfcgate.gui.fragment.LoggingFragment;
import tud.seemuh.nfcgate.gui.fragment.RelayFragment;
import tud.seemuh.nfcgate.gui.fragment.SettingsFragment;
import tud.seemuh.nfcgate.nfc.NfcManager;
import tud.seemuh.nfcgate.util.NfcComm;

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
        else if (!NfcManager.isHookLoaded())
            showWarning("The Xposed module is not enabled or Xposed is not installed.");

        new Thread(new Runnable() {
            @Override
            public void run() {
                final AppDatabase database = AppDatabase.getDatabase(MainActivity.this);
                final SessionLog sessionLog = new SessionLog(new Date());
                List<NfcCommEntry> entries = new ArrayList<>();

                long sessionId = database.sessionLogDao().insert(sessionLog);

                final NfcCommEntry entry = new NfcCommEntry(new NfcComm(true, false,
                        new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
                                0, 1, 2, 3, 4, 5, 6, 7, 8, 9,}), sessionId);
                entries.add(entry);
                database.nfcCommEntryDao().insert(entry);

                final List<SessionLogJoin> all = database.sessionLogJoinDao().getAll();
                Log.d("NFCGATE", ""+all.size());
                for (SessionLogJoin log : all) {
                    Log.d("NFCGATE", log.getSessionLog().getId() + " " + log.getSessionLog().getDate());
                    for (NfcCommEntry nfcCommEntry : log.getNfcCommEntries()) {
                        Log.d("NFCGATE", nfcCommEntry.getId() + " " + nfcCommEntry.getSessionId());
                    }
                }

            }
        }).start();

        //database.sessionLogJoinDao().insert(new SessionLogJoin(sessionLog, entries));

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
        else
            super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        mNfc.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mNfc.onPause();
        super.onPause();
    }

    /**
     * Try to find a Fragment on the stack or create a new one
     */
    private Fragment findOrCreateFragment(String tag, Class<? extends Fragment> clazz) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment == null) {
            try {
                fragment = clazz.newInstance();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fragment;
    }

    /**
     * Returns a Fragment for every navbar action
     */
    private Fragment getFragmentByAction(int id) {
        switch (id) {
            case R.id.nav_clone:
                return findOrCreateFragment("clone", CloneFragment.class);
            case R.id.nav_relay:
                return findOrCreateFragment("relay", RelayFragment.class);
            case R.id.nav_settings:
                return findOrCreateFragment("settings", SettingsFragment.class);
            case R.id.nav_about:
                return findOrCreateFragment("about", AboutFragment.class);
            case R.id.nav_logging:
                return findOrCreateFragment("logging", LoggingFragment.class);
            default:
                return findOrCreateFragment("clone", CloneFragment.class);
                //throw new IllegalArgumentException("Position out of range");
        }
    }

    /**
     * Handles all navbar actions by switching to an existing fragment or creating a new one
     */
    private void onNavbarAction(MenuItem item) {
        // every fragment must implement BaseFragment
        Fragment fragment = getFragmentByAction(item.getItemId());
        BaseFragment baseFragment = (BaseFragment) fragment;

        // no fancy animation for now
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment, baseFragment.getTagName())
                .commit();

        // for the looks
        getSupportActionBar().setTitle(item.getTitle());
        mDrawerLayout.closeDrawers();
    }

    /**
     * Displays a warning dialog with the specified message
     */
    void showWarning(String warning) {
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
