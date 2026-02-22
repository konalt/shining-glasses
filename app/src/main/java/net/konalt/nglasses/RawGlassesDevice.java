package net.konalt.nglasses;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.alibaba.fastjson2.JSONB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class RawGlassesDevice {
    BluetoothDevice device;
    BluetoothGatt gatt;
    Context context;
    Runnable connectRunnable;
    private Runnable disconnectRunnable;

    private BluetoothGattCharacteristic mWriteCharacteristic1;
    private BluetoothGattCharacteristic mWriteCharacteristic2;
    private BluetoothGattCharacteristic mWriteCharacteristic3;
    private BluetoothGattCharacteristic mWriteCharacteristic4;
    private BluetoothGattDescriptor descriptor;

    private int notifyIndex = 0;

    private BluetoothGattCharacteristic ctrlChar;
    private BluetoothGattDescriptor ctrlDescriptor;
    private BluetoothGattCharacteristic dataChar;

    private Runnable wc1Runnable;
    private Runnable wc3Runnable;

    private Handler handler = new Handler(Looper.getMainLooper());

    public boolean connected = false;

    public RawGlassesDevice(Context context, BluetoothDevice bt) {
        this.context = context;
        this.device = bt;
        this.connectRunnable = () -> {};
        this.disconnectRunnable = () -> {};
        this.wc1Runnable = () -> {};
        this.wc3Runnable = () -> {};
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connect(Runnable onConnect) {
        this.connectRunnable = onConnect;
        gatt = device.connectGatt(this.context, false, gattCallback);
        gatt.requestConnectionPriority(1);
    }

    public void onDisconnect(Runnable onDisconnect) {
        this.disconnectRunnable = onDisconnect;
    }
    public void onWriteCharacteristic1(Runnable cb) {
        this.wc1Runnable = cb;
    }
    public void onWriteCharacteristic3(Runnable cb) {
        this.wc3Runnable = cb;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void writeRawData(byte[] bArr) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.mWriteCharacteristic1;
        if (bluetoothGattCharacteristic == null) {
            return;
        }
        bluetoothGattCharacteristic.setValue(bArr);
        boolean zWriteCharacteristic = this.gatt.writeCharacteristic(this.mWriteCharacteristic1);
        String str = C.TAG;
        Log.d(str, " -- write data: " + new String(Enigma.getDecodeData(bArr)));
        Log.d(str, " -- write result:" + zWriteCharacteristic);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void writeRawDataC2(byte[] bArr) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.mWriteCharacteristic2;
        if (bluetoothGattCharacteristic == null) {
            return;
        }
        bluetoothGattCharacteristic.setValue(bArr);
        this.gatt.writeCharacteristic(this.mWriteCharacteristic2);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void writeRawDataC3(byte[] bArr) {
        BluetoothGattCharacteristic bluetoothGattCharacteristic = this.mWriteCharacteristic3;
        if (bluetoothGattCharacteristic == null) {
            return;
        }
        bluetoothGattCharacteristic.setValue(bArr);
        this.gatt.writeCharacteristic(this.mWriteCharacteristic3);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void sendSendDataCommand(int length) {
        byte[] bArrInt2Bytes = Enigma.int2Bytes(length);
        byte[] bArrInt2Bytes2 = Enigma.int2Bytes(length); // formerly i2
        writeRawData(Enigma.getEncryptData(new byte[]{9, JSONB.Constants.BC_INT32_SHORT_ZERO, 65, 84, 83, bArrInt2Bytes[0], bArrInt2Bytes[1], bArrInt2Bytes2[0], bArrInt2Bytes2[1], 1, 0, 0, 0, 0, 0, 0}));
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "Connected to GATT server");
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "Disconnected from GATT server");
                connected = false;
                disconnectRunnable.run();
                disconnectRunnable = () -> {};
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                notifyIndex = 0;
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.d(C.TAG, "-----------");
                    if (UUIDS.UUID_SERVICE.equals(service.getUuid())) {
                        Log.i(C.TAG, "Main service found: " + service.getUuid());
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            UUID uuid = characteristic.getUuid();
                            Log.i(C.TAG, "Characteristic UUID: " + uuid);
                            if (UUIDS.UUID_CHARACTERISTIC_WRITE1.equals(uuid)) {
                                mWriteCharacteristic1 = characteristic;
                                descriptor = characteristic.getDescriptors().get(0);
                                Log.d(C.TAG, "========" + descriptor);
                            }
                            if (UUIDS.UUID_CHARACTERISTIC_WRITE2.equals(uuid)) {
                                mWriteCharacteristic2 = characteristic;
                            }
                            if (UUIDS.UUID_CHARACTERISTIC_WRITE3.equals(uuid)) {
                                mWriteCharacteristic3 = characteristic;
                            }
                            if (UUIDS.UUID_CHARACTERISTIC_WRITE4.equals(uuid)) {
                                mWriteCharacteristic4 = characteristic;
                            }
                        }
                    } else {
                        if (UUIDS.UUID_OTA_SERVICE.equals(service.getUuid())) {
                            Log.i(C.TAG, "OTA service found: " + service.getUuid());
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                UUID uuid = characteristic.getUuid();
                                Log.d(C.TAG, "OTA Characteristic UUID: " + uuid);
                                if (UUIDS.UUID_OTA_CHARACTERISTIC_CTRL.equals(uuid)) {
                                    ctrlChar = characteristic;
                                    ctrlDescriptor = characteristic.getDescriptor(UUIDS.UUID_OTA_DESCRIPTOR);
                                    Log.d(C.TAG, "Found OTA ctrl char and ctrl descriptor");
                                } else if (UUIDS.UUID_OTA_CHARACTERISTIC_DATA.equals(uuid)) {
                                    dataChar = characteristic;
                                    Log.d(C.TAG, "Found OTA data characteristic");
                                }
                            }
                        } else {
                            Log.d(C.TAG, "Other service found: " + service.getUuid());
                            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                                UUID uuid = characteristic.getUuid();
                                Log.d(C.TAG, "Characteristic UUID: " + uuid);
                            }
                        }
                    }
                }
                Log.d(C.TAG, "-----------");
                handler.postDelayed(() -> {
                    gatt.setCharacteristicNotification(mWriteCharacteristic4, true);
                    BluetoothGattDescriptor desc = mWriteCharacteristic4.getDescriptor(UUIDS.UUID_DESCRIPTOR);
                    if (descriptor != null) {
                        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(desc);
                        setMtuSize(100);
                    }
                }, 500L);
            }
        }

        @Override
        public void onCharacteristicChanged(
                BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                byte[] value
        ) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d("BLE", "Received: " + Arrays.toString(value));
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d("BLE", "MTU changed to " + mtu);

            gatt.setCharacteristicNotification(ctrlChar, true);
            if (ctrlDescriptor != null) {
                ctrlDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(ctrlDescriptor);
            }
        }

        @Override
        public void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value, int status) {
            super.onCharacteristicRead(gatt, characteristic, value, status);
            Log.e("BLE", "Read characteristic " + characteristic + ", " + Arrays.toString(value) + ", " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (mWriteCharacteristic1.getUuid().equals(characteristic.getUuid())) {
                wc1Runnable.run();
                wc1Runnable = () -> {};
            }
            if (mWriteCharacteristic3.getUuid().equals(characteristic.getUuid())) {
                wc3Runnable.run();
                wc3Runnable = () -> {};
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(C.TAG, "Read RSSI: " + rssi);
        }

        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            Log.d(C.TAG, "Phy Update: " + txPhy + " " + rxPhy);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i(C.TAG, "onDescriptorWrite " + status);
            super.onDescriptorWrite(gatt, descriptor, status);
            if (status == 0) {
                if (ctrlDescriptor.equals(descriptor)) {
                    Log.d(C.TAG, "ctrlDescriptor written");
                } else {
                    Log.d(C.TAG, "ARE YOU READY TO RUMBLE?");
                    // READY!!!!
                    connected = true;
                    connectRunnable.run();
                    connectRunnable = () -> {};
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int i, byte[] value) {
            String str = new String(value);
            Log.i(C.TAG, "Read descriptor: " + descriptor.getUuid().toString() + ", val: " + str);
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        private void setMtuSize(int i) {
            if (!gatt.requestMtu(i + 3)) {
                Log.d("BLE", "MTU Changed");
            } else {
                Log.d("BLE", "Unable to change MTU");
            }
        }
    };
}
