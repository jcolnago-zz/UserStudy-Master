package com.example.jessica.masterproject;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.jessica.masterproject.helpers.FileSaver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MotherActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final int FILE_PERM = 0x46;
    private static final int LOCATION_PERM = 0x52;
    private static final int REQUEST_PERMISSIONS = 0x50;

    public static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private String mFilename;
    private String[] mData;
    private boolean mAppend;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    protected Location mLocation;
    protected SupportMapFragment mapFragment;
    protected GoogleMap map;
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;

    protected final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case FILE_PERM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSave();
                } else {
                    Toast.makeText(getApplicationContext(), "Você deve dar permissão para o estudo funcionar.", Toast.LENGTH_SHORT).show();
                    requestSave(mFilename, mData, mAppend);
                }
                break;
            case LOCATION_PERM:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Você deve dar permissão para o estudo funcionar.", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERM);
                }
                else {
                    requestLocationUpdates();
                }
                break;
            case REQUEST_PERMISSIONS:
                break;
        }
    }

    protected boolean requestFilePermission() {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasPermission = ActivityCompat.checkSelfPermission(this, permission);

        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, FILE_PERM);
            return false;
        }
        return true;
    }

    protected boolean requestLocationPermission() {
        String permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        int hasPermission = ActivityCompat.checkSelfPermission(this, permission);

        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, LOCATION_PERM);
            return false;
        }
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, mLocationListener);
        return true;
    }

    protected boolean requestLocationUpdates() {
        return requestLocationPermission();
    }

    protected void setLocationListener() {
        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mLocation = location;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    public boolean requestSave(String filename, String[] data, boolean append){
        mFilename = filename;
        mData = data;
        mAppend = append;

        if (requestFilePermission()) {
            return doSave();
        }
        return false;
    }

    protected boolean doSave() {
        return FileSaver.save(mFilename, mData, mAppend, this);
    }

    protected void requestMultiplePermissions() {
        String locationPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int hasLocPermission = ActivityCompat.checkSelfPermission(this, locationPermission);
        int hasStorePermission = ActivityCompat.checkSelfPermission(this, storagePermission);
        List<String> permissions = new ArrayList<String>();
        if (hasLocPermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(locationPermission);
        }
        if (hasStorePermission != PackageManager.PERMISSION_GRANTED) {
            permissions.add(storagePermission);
        }
        if (!permissions.isEmpty()) {
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, REQUEST_PERMISSIONS);
        } else {
            // We already have permission, so handle as normal
        }
    }

    public static void scheduleAlarm(Context context, long time, Intent intentAlarm) {
        // create the object
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //set the alarm for particular time
        alarmManager.set(AlarmManager.RTC_WAKEUP, time, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    public static void reScheduleAlarm(Context context, long time, Intent intentAlarm) {
        scheduleAlarm(context, time, intentAlarm);
    }

    public int readRadio(View view, int viewId, String name) {
        RadioGroup group = (RadioGroup) view.findViewById(viewId);
        int id = group.getCheckedRadioButtonId();
        if(id < 0) {
            Toast.makeText(this, getString(R.string.missing_option) + name, Toast.LENGTH_SHORT).show();
            return -1;
        }

        return group.indexOfChild(view.findViewById(id));
    }

    public String readTextField(View view, int viewId) {
        EditText editText = (EditText) view.findViewById(viewId);
        String text = editText.getText().toString();
        if (text.equalsIgnoreCase(""))
            return "N/A";

        return text;
    }

    public String readCheckBox(View view, int viewId) {
        CheckBox box = (CheckBox) view.findViewById(viewId);
        return Boolean.toString(box.isChecked());
    }

    public String readSeekBar(View view, int viewId) {
        SeekBar bar = (SeekBar) view.findViewById(viewId);
        return Integer.toString(bar.getProgress());
    }


    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            map.setMyLocationEnabled(true);

            // Now that map has loaded, let's get our location!
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

            connectClient();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    protected void connectClient() {
        // Connect the client.
        if (isGooglePlayServicesAvailable() && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {

            }

            return false;
        }
    }

    // Override methods
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }
}
