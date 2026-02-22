package net.konalt.nglasses;

import android.Manifest;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.util.Arrays;

public class GlassesDevice {
    public RawGlassesDevice raw;
    String name;

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public GlassesDevice(RawGlassesDevice raw) {
        this.raw = raw;
        this.name = raw.device.getName();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void clear() {
        byte[] clear = Enigma.getClearCommand();
        Log.d(C.TAG, "Clearing");
        raw.writeRawData(Enigma.getEncryptData(clear));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setBrightness(int i) {
        if (i == 0) {
            i = 1;
        }
        byte[] light = Enigma.getLight(i);
        Log.d(C.TAG, "Setting brightness to " + i);
        raw.writeRawData(Enigma.getEncryptData(light));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setAnimation(int i) {
        byte[] anim = Enigma.getAnimCommand(i);
        Log.d(C.TAG, "Setting animation to " + i);
        raw.writeRawData(Enigma.getEncryptData(anim));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setImage(int i) {
        byte[] img = Enigma.getImageCommand(i);
        Log.d(C.TAG, "Setting image to " + i);
        raw.writeRawData(Enigma.getEncryptData(img));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setMatrixPixel(int x, int y, int r, int g, int b) {
        Log.d(C.TAG, "Setting pixel " + x + ", " + y + " to " + r + " " + g + " " + b);
        byte[] bArr = new byte[20];
        bArr[0] = (byte) 5;
        bArr[1] = (byte) r;
        bArr[2] = (byte) g;
        bArr[3] = (byte) b;
        bArr[4] = (byte) x;
        bArr[5] = (byte) y;
        Log.d(C.TAG, "Setting pixel data: " + Arrays.toString(bArr));
        raw.writeRawDataC3(bArr);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setMatrix(Matrix matrix) {
        Log.d(C.TAG, "Setting matrix");
        int index = 0;
        for (byte[] px : matrix.getData()) {
            //Log.d("BLE", px.toString());
            int x = index % C.WIDTH;
            int y = index / C.WIDTH;
            setMatrixPixel(x, y, 255, 0, 255);
            index++;
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setTextSpeed(int i) {
        if (i == 0) {
            i = 1;
        }
        byte[] speed = Enigma.getSpeed(i);
        Log.d(C.TAG, "Setting text speed to " + i);
        raw.writeRawData(Enigma.getEncryptData(speed));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setTextColor(int r, int g, int b) {
        Log.d(C.TAG, "Setting text color to " + r + " " + g + " " + b);
        raw.writeRawData(Enigma.getEncryptData(Enigma.getTextColor((byte) r, (byte) g, (byte) g, false)));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void setTextBackgroundColor(int r, int g, int b) {
        Log.d(C.TAG, "Setting text background color to " + r + " " + g + " " + b);
        raw.writeRawData(Enigma.getEncryptData(Enigma.getTextBgColor((byte) r, (byte) g, (byte) g, false)));
    }
}