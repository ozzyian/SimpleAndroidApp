package com.example.communitytracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * The programs MainActivity which handles getting the current
 * user data from the database and the navigation through the applications
 * fragments.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private int gpsPermission;
    private static final int GPS_REQUEST = 1;
    private NavigationView navigationView;
    private FirebaseAuth authenticator;
    private MapFragment mapFragment;
    private OnlineFragment onlineFragment;
    private FragmentManager fragmentManager;
    private String currentFragment;
    private DatabaseReference userRef;
    private ProgressBar progressBarLoading;
    private User currentUser;


    /**
     * The create method initiates the variables and checks if
     * the gps permission is enabled, otherwise asks for it.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authenticator = FirebaseAuth.getInstance();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progressBarLoading = findViewById(R.id.progress_toolbar);
        fragmentManager = getSupportFragmentManager();
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        ActionBarDrawerToggle toggleDraw = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggleDraw);
        toggleDraw.syncState();


        gpsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (gpsPermission == PackageManager.PERMISSION_GRANTED) {
            initUserSession(savedInstanceState);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQUEST);
        }


    }

    /**
     * Method creates a databaselistener to receive the user
     * information and pushes the a user object to the database to
     * track the online status and passes saved states to the loadFragment method.
     *
     * @param savedInstanceState
     */
    private void initUserSession(Bundle savedInstanceState) {
        final Bundle savedState = savedInstanceState;
        userRef = FirebaseDatabase.getInstance().getReference("users/" + authenticator.getCurrentUser().getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                DatabaseReference userOnline = FirebaseDatabase.getInstance().getReference("users/online/" + authenticator.getCurrentUser().getUid());
                userOnline.setValue(currentUser);
                loadFragments(savedState);
                progressBarLoading.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Method loads the application fragments or reloads them
     * depending on there are any savedInstances or not for
     * saving states during screen rotation.
     *
     * @param savedInstances
     */
    private void loadFragments(Bundle savedInstances) {
        if (savedInstances != null) {
            onlineFragment = (OnlineFragment) fragmentManager.getFragment(savedInstances, "onlineFragment");
            mapFragment = (MapFragment) fragmentManager.getFragment(savedInstances, "mapFragment");
            currentFragment = savedInstances.getString("currentFragment");

            if (currentFragment.equals("map")) {
                fragmentManager.beginTransaction()
                        .hide(onlineFragment)
                        .commit();
            } else {
                fragmentManager.beginTransaction()
                        .hide(mapFragment)
                        .commit();
            }
        } else {
            onlineFragment = new OnlineFragment();
            mapFragment = MapFragment.newInstance(currentUser);

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, onlineFragment, "online")
                    .add(R.id.fragment_container, mapFragment, "map")
                    .hide(onlineFragment)
                    .commit();
            navigationView.setCheckedItem(R.id.nav_map);
            currentFragment = "map";
        }
    }

    /**
     * Overrided method which shows a pop up dialog
     * when back button is pressed.
     */
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            showLogoutDialog();
        }
    }

    /**
     * Method creates a AlertDialog which
     * can logout the user or cancel the request.
     */
    private void showLogoutDialog() {
        AlertDialog.Builder builder
                = new AlertDialog
                .Builder(this);

        builder.setMessage("Do you want to logout?");
        builder.setCancelable(false);
        builder.setTitle("Logout");

        builder.setPositiveButton("Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                logout();
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Method starts the login activity and stops the background service
     * and uses the firebase authenticator to sign out the user. Also stops itself.
     */
    private void logout() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        stopService(new Intent(getApplicationContext(), TrackingService.class));
        FirebaseDatabase.getInstance().goOffline();
        authenticator.signOut();
        finish();
    }

    /**
     * Method handles the navigation logic of the fragments of the map
     * using a sidenavigation drawer. Hides the current active fragment and displays
     * the selected menuItem inside the navigation. By hiding and showing the fragments will
     * continue to run until mainActivity is terminated (or screen rotation).
     * @param menuItem
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        String fragment = fragmentManager.findFragmentById(R.id.fragment_container).getClass().getSimpleName();
        Log.println(Log.DEBUG, "fragment", fragment);
        switch (menuItem.getItemId()) {
            case R.id.nav_online:
                if (onlineFragment.isHidden())
                    fragmentManager.beginTransaction()
                            .hide(fragmentManager.findFragmentByTag(currentFragment))
                            .show(onlineFragment).commit();
                currentFragment = "online";
                break;
            case R.id.nav_map:
                if (mapFragment.isHidden())
                    fragmentManager.beginTransaction()
                            .hide(fragmentManager.findFragmentByTag(currentFragment))
                            .show(mapFragment).commit();
                currentFragment = "map";
                break;
            case R.id.nav_logout:
                showLogoutDialog();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handles if MainActivity is reloaded by a screenrotation
     * and saves the states of the fragments and which fragment was currently
     * showing.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        fragmentManager.putFragment(outState, "onlineFragment", onlineFragment);
        fragmentManager.putFragment(outState, "mapFragment", mapFragment);
        outState.putString("currentFragment", currentFragment);
    }

}
