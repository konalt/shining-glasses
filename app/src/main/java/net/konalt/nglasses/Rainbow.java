package net.konalt.nglasses;

import android.graphics.Color;
import android.util.Log;

public class Rainbow {
    public static int getVerticalColor(int y, int minY, int maxY) {
        return Color.HSVToColor(new float[]{((float) y - minY) / ((float)((maxY + 1) - minY)) * 360f, 1f, 1f});
    }
    public static int getHorizontalColor(int x) {
        return Color.HSVToColor(new float[]{((float) x) / ((float)C.WIDTH)* 360f, 1f, 1f});
    }
}
