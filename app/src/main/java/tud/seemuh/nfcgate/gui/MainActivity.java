package tud.seemuh.nfcgate.gui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import tud.seemuh.nfcgate.R;
import tud.seemuh.nfcgate.gui.fragment.BaseFragment;
import tud.seemuh.nfcgate.gui.fragment.CloneFragment;
import tud.seemuh.nfcgate.gui.fragment.RelayFragment;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";

    // save these for later
    DrawerLayout mDrawerLayout;
    NavigationView mNavbar;
    Toolbar mToolbar;

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
    }

    BaseFragment findOrCreateFragment(String tag, Class<? extends Fragment> clazz) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment == null) {
            try {
                fragment = clazz.newInstance();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return (BaseFragment)fragment;
    }

    BaseFragment getFragmentByAction(int id) {
        switch (id) {
            case R.id.nav_clone:
                return findOrCreateFragment("clone", CloneFragment.class);
            case R.id.nav_relay:
                return findOrCreateFragment("relay", RelayFragment.class);
            default:
                return findOrCreateFragment("clone", CloneFragment.class);
                //throw new IllegalArgumentException("Position out of range");
        }
    }

    void onNavbarAction(MenuItem item) {
        BaseFragment fragment = getFragmentByAction(item.getItemId());

        // no fancy animation for now
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment, fragment.getTagName())
                .commit();

        // for the looks
        getSupportActionBar().setTitle(item.getTitle());
        mDrawerLayout.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // drawer button was pressed
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
