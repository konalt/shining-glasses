package net.konalt.nglasses;

import android.graphics.Color;

public class Rainbow {
    public static int getVerticalColor(int y) {
        return Color.HSVToColor(new float[]{((float) y) / ((float)C.HEIGHT) * 360f, 1f, 1f});
    }
    public static int getHorizontalColor(int x) {
        return Color.HSVToColor(new float[]{((float) x) / ((float)C.WIDTH)* 360f, 1f, 1f});
    }
}
