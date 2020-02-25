package com.example.communitytracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


/**
 * Service that runs in the background which collects the users
 * location periodically and sends it to the connected Database (Firebase).
 */

public class TrackingService extends Service {

    public static final String SERVICE_STOPPED = "com.example.communitytracker.service.TrackingService";

    private LocationCallback locationCallback;
    private FusedLocationProviderClient client;
    private BroadcastReceiver stopReceiver;
    private User currentUser;
    private String userID;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentUser = (User) intent.getSerializableExtra("user");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /**
     * The create method for the service
     */
    @Override
    public void onCreate() {
        super.onCreate();
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        createForegroundNotification();
        requestLocationInfo();

    }


    /**
     * Method creates a foreground notification and binds
     * a receiver to it that stops the service if notification is pressed.
     */
    private void createForegroundNotification() {

        stopReceiver = new BroadcastReceiver() {
            @Override
            /**
             * Receiver method for the notification to stop the service
             * and remove the user from the sharing the location in the database
             * when clicked.
             */
            public void onReceive(Context context, Intent intent) {
                DatabaseReference sharingNode = FirebaseDatabase.getInstance().getReference("users/sharing/" + userID);
                sharingNode.removeValue();
                sendTrackingStatus();
                client.removeLocationUpdates(locationCallback);
                stopSelf();
            }
        };

        registerReceiver(stopReceiver, new IntentFilter("Stop"));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent("Stop"), PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel one",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
            builder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_gps_location)
                    .setContentTitle("GPS status")
                    .setContentText("App is running in background, press to stop")
                    .setContentIntent(broadcastIntent);

            Notification notification = builder.build();


            startForeground(1, notification);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_gps_location)
                    .setContentTitle("Gps status")
                    .setContentText("App is running in background, press to stop")
                    .setContentIntent(broadcastIntent);

            Notification notification = builder.build();
            startForeground(1, notification);
        }
    }

    /**
     * Method creates a locationCallback which pushes the gps data
     * of the user to the database every other second if data could be retrieved.
     * Uses the google FusedLocationProviderClient to execute the request after the specification.
     */
    private void requestLocationInfo() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users/sharing");
                Location location = locationResult.getLastLocation();
                if (location != null && currentUser != null) {
                    Log.println(Log.DEBUG, "loc", "getting location");
                    LocationObject locObj = new LocationObject(location.getLongitude(), location.getLatitude());
                    currentUser.setLocation(locObj);
                    ref.child(userID).setValue(currentUser);
                }
            }
        };

        LocationRequest request = new LocationRequest();
        request.setInterval(2000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, locationCallback, null);
        }

    }

    /**
     * Method which executes when service is killed
     * and updates the UI of its status and removes listeners.
     */
    @Override
    public void onDestroy() {
        sendTrackingStatus();
        unregisterReceiver(stopReceiver);
        DatabaseReference sharingNode = FirebaseDatabase.getInstance().getReference("users/sharing/" + userID);
        sharingNode.removeValue();
        client.removeLocationUpdates(locationCallback);
        stopSelf();
        super.onDestroy();
    }

    private void sendTrackingStatus() {
        Intent intent = new Intent(SERVICE_STOPPED);
        intent.putExtra("status", false);
        sendBroadcast(intent);
    }
}
