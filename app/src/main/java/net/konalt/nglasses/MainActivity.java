package net.konalt.nglasses;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    Button requestPermsButton;
    Button startScanButton;
    Button stopScanButton;

    LinearLayout listContainer;

    HashMap<String, GlassesDevice> availableDevices;

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("AES", "AES: " + Arrays.toString(Enigma.getDecodeData(new byte[]{0, 1, 2, 3})));

        availableDevices = new HashMap<>();

        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        requestPermsButton = findViewById(R.id.requestPermsButton);
        startScanButton = findViewById(R.id.startScanButton);
        stopScanButton = findViewById(R.id.stopScanButton);

        listContainer = findViewById(R.id.listContainer);

        updateDevicesList();

        requestPermsButton.setOnClickListener(view -> {
            requestPermsButton.setEnabled(false);
            startScanButton.setEnabled(true);
            Perms.requestBlePerms(this);
        });

        startScanButton.setOnClickListener(view -> {
            startScan();
        });

        stopScanButton.setOnClickListener(view -> {
            stopScan();
        });
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onResume() {
        super.onResume();
        if (App.currentDevice != null) {
            BadApple.stopAnimation();
            availableDevices = new HashMap<>();
            updateDevicesList();
            startScanButton.setEnabled(true);
            App.currentDevice = null;
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void startScan() {
        startScanButton.setEnabled(false);
        stopScanButton.setEnabled(true);
        Log.d("BLE", "Start scan button pressed");
        btScanner.startScan(leScanCallback);
        availableDevices = new HashMap<>();
        updateDevicesList();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void stopScan() {
        startScanButton.setEnabled(true);
        stopScanButton.setEnabled(false);
        Log.d("MainActivity", "Stop scan button pressed");
        btScanner.stopScan(leScanCallback);
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN})
    private void updateDevicesList() {
        listContainer.removeAllViews();

        availableDevices.forEach((a, device) -> {
            Button button = new Button(this);
            button.setText(device.raw.device.getName());
            button.setOnClickListener(v -> {
                Log.d("BLE", "Connect to " + device.raw.device.getName());
                stopScan();
                device.raw.connect(() -> {
                    App.currentDevice = device;
                    Log.d("BLE", "Connected, launching activity");
                    Intent i = new Intent(this, ControlActivity.class);
                    startActivity(i);
                });
                button.setEnabled(false);
                startScanButton.setEnabled(false);
            });
            listContainer.addView(button);
        });
    }

    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanFailed(int errorCode) {
            Log.e("BLE", "FAILED");
            super.onScanFailed(errorCode);
        }

        @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            List<ParcelUuid> uuids = Objects.requireNonNull(result.getScanRecord()).getServiceUuids();
            if (uuids == null) return;
            UUID uuid = uuids.get(0).getUuid();
            if (!UUIDS.UUID_SERVICE.equals(uuid)) return;

            BluetoothDevice d = result.getDevice();
            Log.d("BLE", "Found " + d.getName() + " (RSSI " + result.getRssi() + ")");
            RawGlassesDevice rgd = new RawGlassesDevice(getApplicationContext(), d);
            GlassesDevice gd = new GlassesDevice(rgd);

            String addr = d.getAddress();
            if (!availableDevices.containsKey(addr)) {
                availableDevices.put(addr, gd);
            }
            updateDevicesList();
            super.onScanResult(callbackType, result);
        }
    };
}