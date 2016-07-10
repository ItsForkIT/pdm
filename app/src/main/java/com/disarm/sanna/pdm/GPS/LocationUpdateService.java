package com.disarm.sanna.pdm.GPS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by sanna on 10/6/16.
 */
public class LocationUpdateService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    Double firstTimeCheckLat,firstTimeCheckLong ,secondTimeCheckLat,secondTimeCheckLong,calculatedDistance,garbage,calculatedBearing = 0.0;
    Handler handler = new Handler();
    boolean a = false;
    boolean b = false;
    protected static final String TAG = "LocationUpdateService";
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = //15000;
                                                                 UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public long LOCATION_LISTENER_TOGGLER = 30000;
    public static Boolean mRequestingLocationUpdates;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    protected Location mCurrentLocation;
    public static boolean isEnded = false;

    @Override
    public void onCreate() {
        super.onCreate();
        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LOC", "Service init...");
        isEnded = false;
        mRequestingLocationUpdates = false;
        buildGoogleApiClient();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        return Service.START_REDELIVER_INTENT;
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.v("onConnected","connected");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended==");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
       // updateUI();
        if (a){
            garbage = mCurrentLocation.getLatitude();
            garbage = mCurrentLocation.getLongitude();
        }
        if (firstTimeCheckLat == null && firstTimeCheckLong == null){
            firstTimeCheckLat = mCurrentLocation.getLatitude();
            firstTimeCheckLong = mCurrentLocation.getLongitude();
            Logger.addRecordToLog(String.valueOf(firstTimeCheckLat) + "," + String.valueOf(firstTimeCheckLong) + "," + String.valueOf(calculatedBearing));
            Log.v("First time ",String.valueOf(firstTimeCheckLat)+" "
                    +String.valueOf(firstTimeCheckLong));
           // Toast.makeText(LocationUpdateService.this,"first value" +String.valueOf(firstTimeCheckLat)+String.valueOf(firstTimeCheckLong), Toast.LENGTH_SHORT).show();
        }
        else if (firstTimeCheckLat != null && firstTimeCheckLong != null ){
            secondTimeCheckLat= mCurrentLocation.getLatitude();
            secondTimeCheckLong = mCurrentLocation.getLongitude();
            //Toast.makeText(LocationUpdateService.this,"second value" +String.valueOf(secondTimeCheckLat)+String.valueOf(secondTimeCheckLong), Toast.LENGTH_SHORT).show();
            Log.v("First time ",String.valueOf(firstTimeCheckLat)+" "
                    +String.valueOf(firstTimeCheckLong));
            Log.v("Second time ",String.valueOf(secondTimeCheckLat)+" "
                    +String.valueOf(secondTimeCheckLong));
        }

        if (firstTimeCheckLat != null && firstTimeCheckLong != null &&
                secondTimeCheckLat != null && secondTimeCheckLong!=null){
            calculatedDistance = CalculateDistance.distance(firstTimeCheckLat,firstTimeCheckLong,
                                                        secondTimeCheckLat,secondTimeCheckLong);
            calculatedBearing = CalculateBearing.bearing(firstTimeCheckLat,firstTimeCheckLong,
                                                        secondTimeCheckLat,secondTimeCheckLong);
            //calculatedDistance = calculatedDistance*1000;
            //Log.v("Distance Between 2 points",String.valueOf(calculatedDistance*1000)+ "  "+DISTANCE_THRESOLD);
           Log.v("Distance Between 2 points",String.valueOf(calculatedDistance)+" "+0.025);
            firstTimeCheckLat = secondTimeCheckLat;
            firstTimeCheckLong = secondTimeCheckLong;
            secondTimeCheckLat = null;
            secondTimeCheckLong = null;
        }

        if(calculatedDistance != null && calculatedDistance < 0.025){
            if (mRequestingLocationUpdates) {
                mRequestingLocationUpdates = false;
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, LocationUpdateService.this);
                Log.d(TAG, "stopLocationUpdates by Runnable");
                LOCATION_LISTENER_TOGGLER += 20000;
            }
        }else if (calculatedDistance != null && calculatedDistance > 0.025) {
            Log.v(TAG, "witting to file");
            Logger.addRecordToLog(String.valueOf(firstTimeCheckLat) + "," + String.valueOf(firstTimeCheckLong) + "," + String.valueOf(calculatedBearing));
            Log.v(TAG, "Written to file");
        }

        //Toast.makeText(LocationUpdateService.this, String.valueOf(calculatedDistance), Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, getResources().getString(R.string.location_updated_message),
            //    Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    private Runnable LocationListenerToggler = new Runnable() {
        @Override
        public void run() {
            if (!mRequestingLocationUpdates) {
                mRequestingLocationUpdates = true;

                // The final argument to {@code requestLocationUpdates()} is a LocationListener
                // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
                if (ActivityCompat.checkSelfPermission(LocationUpdateService.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationUpdateService.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, LocationUpdateService.this);
                Log.i(TAG, " start Location Updates by RUnnable");
                isEnded = true;
                a = true;
            }


           /* if (!b){
                b = true;
            }else if (b) {*/
            if (LOCATION_LISTENER_TOGGLER < 120000){
                handler.postDelayed(LocationListenerToggler, LOCATION_LISTENER_TOGGLER);
            }
               else if (LOCATION_LISTENER_TOGGLER > 120000) {
                    LOCATION_LISTENER_TOGGLER = 30000;
                    handler.postDelayed(LocationListenerToggler, LOCATION_LISTENER_TOGGLER);
                }
            }

    };

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient===");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        createLocationRequest();
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        // setLocationData();
        Logger.addRecordToLog(mCurrentLocation.getLatitude() + " " + mCurrentLocation.getLongitude());
        Toast.makeText(this, "Latitude: =" + mCurrentLocation.getLatitude() + " Longitude:=" + mCurrentLocation
                .getLongitude(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Latitude:==" + mCurrentLocation.getLatitude()
                + "\nLongitude:==" + mCurrentLocation.getLongitude());

        //LocationDBHelper.getInstance(this).insertLocationDetails(mLocationData);
    }


    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    protected void createLocationRequest() {
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
       // mLocationRequest.setSmallestDisplacement(10);
    }

    /**
     * Requests location updates from the FusedLocationApi.
     */
    protected void startLocationUpdates() {
        /*if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;

            // The final argument to {@code requestLocationUpdates()} is a LocationListener
            // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
            Log.i(TAG, " startLocationUpdates===");
            isEnded = true;
        }*/
        handler.post(LocationListenerToggler);
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            // It is a good practice to remove location requests when the activity is in a paused or
            // stopped state. Doing so helps battery performance and is especially
            // recommended in applications that request frequent location updates.

            Log.d(TAG, "stopLocationUpdates();==");
            // The final argument to {@code requestLocationUpdates()} is a LocationListener
            // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            handler.removeCallbacks(LocationListenerToggler);
           // a = false;
            b = false;
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();

    }

}
