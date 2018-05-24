package tud.seemuh.nfcgate.gui;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.fragment.AboutFragment;
import tud.seemuh.nfcgate.gui.fragment.BaseFragment;
import tud.seemuh.nfcgate.gui.fragment.CloneFragment;
import tud.seemuh.nfcgate.gui.fragment.RelayFragment;
import tud.seemuh.nfcgate.gui.fragment.SettingsFragment;
import tud.seemuh.nfcgate.nfc.NfcManager;

public class MainActivity extends AppCompatActivity {
    // UI
    DrawerLayout mDrawerLayout;
    NavigationView mNavbar;
    Toolbar mToolbar;

    // NFC
    NfcManager mNfc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar setup
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // drawer icon in toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer_black_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // drawer setup
        mDrawerLayout = findViewById(R.id.main_drawer_layout);

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
            showWarning("The XPosed module is not enabled or XPosed is not installed.");
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
     * Helper for opening the drawer using the drawer button of the Toolbar
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // drawer button was pressed
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
