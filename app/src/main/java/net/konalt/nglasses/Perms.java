package net.konalt.nglasses;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Perms {
    private static final int BLE_PERMISSION_REQUEST_CODE = 1001;

    private static final List<String> requiredPerms = Arrays.asList(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    );

    public static void requestBlePerms(Activity activity) {
        List<String> permissions = new ArrayList<>();

        for (String perm : requiredPerms) {
            if (activity.checkSelfPermission(perm)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d("BLEPerms", "No permission " + perm + ", requesting");
                permissions.add(perm);
            } else {
                Log.d("BLEPerms", "Permission " + perm + " granted");
            }
        }

        if (!permissions.isEmpty()) {
            Log.d("BLEPerms", "Requesting permissions");
            activity.requestPermissions(
                    permissions.toArray(new String[0]),
                    BLE_PERMISSION_REQUEST_CODE
            );
        }
    }
}
