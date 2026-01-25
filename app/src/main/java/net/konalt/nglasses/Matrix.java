package net.konalt.nglasses;

import android.graphics.Color;

import java.util.Arrays;

public class Matrix {
    private final int[] data;

    public Matrix() {
        this.data = new int[C.WIDTH * C.HEIGHT];
    }

    public void fill(int r, int g, int b) {
        Arrays.fill(data, Color.rgb(r,g,b));
    }

    public void set(int x, int y, int r, int g, int b) {
        data[y * C.WIDTH + x] = Color.rgb(r,g,b);
    }

    public void clear() {
        Arrays.fill(data, Color.rgb(0,0,0));
    }

    public byte[][] getData() {
        byte[][] out = new byte[data.length][3];
        int i = 0;
        for (int c : data) {
            out[i] = new byte[]{(byte) Color.red(c), (byte) Color.green(c), (byte) Color.blue(c)};
            i++;
        }
        return out;
    }
}
