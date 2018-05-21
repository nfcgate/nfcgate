package tud.seemuh.nfcgate.newgui;

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
import tud.seemuh.nfcgate.gui.fragments.CloneFragment;
import tud.seemuh.nfcgate.gui.fragments.RelayFragment;

public class MainActivity extends AppCompatActivity {
    // save these for later
    DrawerLayout mDrawerLayout;
    NavigationView mNavbar;
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_main);

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

    Fragment getFragmentByAction(int id) {
        // TODO: add the actual fragments here later on
        switch (id) {
            case R.id.nav_clone:
                return new CloneFragment();
            case R.id.nav_relay:
                return new RelayFragment();
            default:
                throw new IllegalArgumentException("Position out of range");
        }
    }

    void onNavbarAction(MenuItem item) {
        Fragment fragment = getFragmentByAction(item.getItemId());

        // no fancy animation for now
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_content, fragment)
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
