package com.example.communitytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * Application fragments which retrieves
 * users location and puts markers on a google map. And turning
 * of the background tracking service by a switch.
 */
public class MapFragment extends Fragment {

    private MapView mMapView;
    private GoogleMap googleMap;
    private FirebaseDatabase database;
    private Switch trackingOption;
    private FloatingActionButton zoomToUser;

    private DatabaseReference infoConnected;
    private DatabaseReference sharing;
    private DatabaseReference online;
    private FirebaseAuth authenticator;
    private User currentUser;
    private String userID;
    private BroadcastReceiver receiver;


    /**
     * Instance method used to send user data to
     * a new FragmentMap object.
     *
     * @param user
     * @return
     */
    public static MapFragment newInstance(User user) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putSerializable("user", user);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Method helps pass the tracking state and currentUser Data
     * to the MainActivity if fragment is recreated due to
     * screen rotation.
     *
     * @param savedInstanceState fragments saved states if there are any.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            trackingOption.setChecked(savedInstanceState.getBoolean("switchStatus"));
            currentUser = (User) savedInstanceState.getSerializable("user");
        } else {
            trackingOption.setChecked(false);
        }
        super.onActivityCreated(savedInstanceState);
    }


    /**
     * @param inflater           inflates a layout with the fragment
     * @param container          the fragments container
     * @param savedInstanceState saved states if there are any
     * @return returns the view object of the fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_map, container, false);
        Bundle bundle = getArguments();
        if (bundle != null)
            currentUser = (User) bundle.getSerializable("user");
        initiateSwitch(view);
        initiateZoomButton(view);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    trackingOption.setChecked(bundle.getBoolean("status"));
                }
            }
        };
        getContext().registerReceiver(receiver, new IntentFilter(TrackingService.SERVICE_STOPPED));

        initiateMap(view, savedInstanceState);

        return view;
    }

    /**
     * Method creates and initates a google map.
     *
     * @param view               the view of which the map is ot be displayed.
     * @param savedInstanceState saved states of the map if any are passed (screen rotation).
     */
    private void initiateMap(View view, final Bundle savedInstanceState) {
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                if (savedInstanceState != null)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(savedInstanceState.getDouble("lat"), savedInstanceState.getDouble("lon")), savedInstanceState.getFloat("zoom")));
                createDatabaseReferences();
                initiateDatabaseListeners();
            }
        });

    }

    /**
     * Called when MapFragment is terminated, removes the
     * broadcast receiver.
     */
    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(receiver);
        super.onDestroy();
    }

    /**
     * Initiates the floating button and adds a onClickListener that
     * zooms to the user location when pressed.
     *
     * @param view the view where the floating button will be displayed.
     */
    private void initiateZoomButton(View view) {
        zoomToUser = view.findViewById(R.id.floatingButton);
        zoomToUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.println(Log.DEBUG, "floating", "pressed");
                if (currentUser.getLocation() != null && googleMap != null && trackingOption.isChecked()) {
                    LocationObject userLocation = currentUser.getLocation();
                    Log.println(Log.DEBUG, "floating", "pressed and " + userLocation.getLatitude());
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 12.0f));
                }
            }
        });
    }

    /**
     * Initates the switch with an onClickListener which
     * turns the tracking service on or off.
     *
     * @param view the viewObject where the switch will be displayed.
     */
    private void initiateSwitch(View view) {

        trackingOption = view.findViewById(R.id.gps_option);
        trackingOption.setChecked(false);

        trackingOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (trackingOption.isChecked()) {
                    Log.println(Log.DEBUG, "service", "service start bool=" + trackingOption.isChecked());
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        getContext().startService(new Intent(getContext(), TrackingService.class));
                    } else {
                        Intent serviceIntent = new Intent(getContext(), TrackingService.class);
                        serviceIntent.putExtra("user", currentUser);
                        getContext().startForegroundService(serviceIntent);
                    }
                } else {
                    getContext().stopService(new Intent(getContext(), TrackingService.class));
                }
            }
        });
    }

    /**
     * Method initiates the needed listeners to retrieve
     * database data and modify database data.
     * One to remove the user from the database if program is terminated.
     * The other listens for users locations to change and update them accordingly
     * on the map.
     */
    private void initiateDatabaseListeners() {
        infoConnected.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if (connected) {
                    online.child(userID).onDisconnect().removeValue();
                    sharing.child(userID).onDisconnect().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sharing.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (googleMap != null) {
                    googleMap.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        LocationObject locationObject = user.getLocation();
                        if (locationObject != null) {
                            if (user.getId().equals(currentUser.getId()))
                                currentUser.setLocation(locationObject);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(locationObject.getLatitude(), locationObject.getLongitude())).title(ds.child("name").getValue(String.class)));
                        }
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Method creates the needed references to
     * handle the database.
     */
    private void createDatabaseReferences() {
        database = FirebaseDatabase.getInstance();
        authenticator = FirebaseAuth.getInstance();
        userID = authenticator.getCurrentUser().getUid();
        infoConnected = database.getReference(".info/connected");
        sharing = database.getReference("users/sharing");
        online = database.getReference("/users/online");
    }

    /**
     * Called when fragment is recreated due to screen
     * rotation and sends the state of the switch,
     * currentUser and map status.
     *
     * @param state bundle to save the states to.
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putBoolean("switchStatus", trackingOption.isChecked());
        state.putSerializable("user", currentUser);
        state.putDouble("lat", googleMap.getCameraPosition().target.latitude);
        state.putDouble("lon", googleMap.getCameraPosition().target.longitude);
        state.putFloat("zoom", googleMap.getCameraPosition().zoom);
        super.onSaveInstanceState(state);
    }
}
