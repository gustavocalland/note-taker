package ca.finalproject.notetaker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by gustavo on 2017-04-28.
 */

public class PermissionManager {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_GPS = 2;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static String[] ALL_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

    /**
     * Checks if the app has permission to access the GPS location
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyGPSPermissions(Activity activity){
        int permissionCoarse = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFine = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCoarse != PackageManager.PERMISSION_GRANTED || permissionFine != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_GPS);
        }
    }

    public static boolean isGogleServicesAvailable(Activity activity){
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvaliable = api.isGooglePlayServicesAvailable(activity);
        if(isAvaliable == ConnectionResult.SUCCESS){
            return true;
        }else if(api.isUserResolvableError(isAvaliable)){//The user doesn't have google services, but can install them
            Dialog dialog = api.getErrorDialog(activity,isAvaliable,0);
            dialog.show();
        }else{//The user doesn't have google services and CAN'T install it
            Toast.makeText(activity, "This device can't connect to google play services. Get a better phone.",Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static void verifyAllPermissions(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCoarse = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFine = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED ||
                permissionCoarse != PackageManager.PERMISSION_GRANTED ||
                permissionFine != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, ALL_PERMISSIONS, REQUEST_EXTERNAL_STORAGE);
        }
    }

    /*
    TODO: put something like this on the MainActivity:
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case ACCESS_LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locateThisDevice();
                } else {
                    Toast.makeText(this, "Your geolocation won't be calculated because you don't have the required permissions", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    */
}
