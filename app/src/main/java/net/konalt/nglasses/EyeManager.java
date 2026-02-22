package net.konalt.nglasses;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressLint("MissingPermission")
public class EyeManager {
    public static final Map<String, String> EYES = new HashMap<>();
    public static final Map<String, Bitmap> EYES_BITMAP = new HashMap<>();
    public static final Map<String, Bitmap> EYES_BITMAP_RAINBOW = new HashMap<>();
    public static final Map<String, Integer> COLORS = new HashMap<>();

    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static Runnable periodicBlink;

    public static String currentEyeImage = "0,0";
    public static int currentEyeColor = 0xffffffff;
    public static boolean currentBlushState = false;
    public static boolean currentBlinkState = false;

    public static boolean isUpdating = false;

    static {
        EYES.put("dead", "7,1 14,1 21,1 28,1 6,2 7,2 8,2 13,2 14,2 15,2 20,2 21,2 22,2 27,2 28,2 29,2 7,3 8,3 9,3 12,3 13,3 14,3 21,3 22,3 23,3 26,3 27,3 28,3 8,4 9,4 10,4 11,4 12,4 13,4 22,4 23,4 24,4 25,4 26,4 27,4 9,5 10,5 11,5 12,5 23,5 24,5 25,5 26,5 9,6 10,6 11,6 12,6 23,6 24,6 25,6 26,6 8,7 9,7 10,7 11,7 12,7 13,7 22,7 23,7 24,7 25,7 26,7 27,7 7,8 8,8 9,8 12,8 13,8 14,8 21,8 22,8 23,8 26,8 27,8 28,8 6,9 7,9 8,9 13,9 14,9 15,9 20,9 21,9 22,9 27,9 28,9 29,9 7,10 14,10 21,10 28,10");
        EYES.put("excited", "10,0 11,0 24,0 25,0 9,1 10,1 11,1 12,1 23,1 24,1 25,1 26,1 9,2 10,2 11,2 12,2 23,2 24,2 25,2 26,2 8,3 9,3 10,3 11,3 12,3 13,3 22,3 23,3 24,3 25,3 26,3 27,3 8,4 9,4 10,4 11,4 12,4 13,4 22,4 23,4 24,4 25,4 26,4 27,4 8,5 9,5 10,5 11,5 12,5 13,5 22,5 23,5 24,5 25,5 26,5 27,5 8,6 9,6 10,6 11,6 12,6 13,6 22,6 23,6 24,6 25,6 26,6 27,6 8,7 9,7 12,7 13,7 22,7 23,7 26,7 27,7 8,8 13,8 22,8 27,8");
        EYES.put("expressionless", "7,6 8,6 9,6 10,6 11,6 12,6 13,6 22,6 23,6 24,6 25,6 26,6 27,6 28,6");
        EYES.put("glasses", "1,2 2,2 3,2 4,2 5,2 6,2 7,2 8,2 9,2 10,2 11,2 12,2 13,2 14,2 15,2 16,2 17,2 18,2 19,2 20,2 21,2 22,2 23,2 24,2 25,2 26,2 27,2 28,2 29,2 30,2 31,2 32,2 33,2 34,2 1,3 2,3 3,3 4,3 5,3 6,3 7,3 8,3 9,3 10,3 11,3 12,3 13,3 14,3 15,3 16,3 17,3 18,3 19,3 20,3 21,3 22,3 23,3 24,3 25,3 26,3 27,3 28,3 29,3 30,3 31,3 32,3 33,3 34,3 2,4 3,4 4,4 5,4 6,4 7,4 8,4 9,4 10,4 11,4 12,4 13,4 14,4 15,4 16,4 17,4 18,4 19,4 20,4 21,4 22,4 23,4 24,4 25,4 26,4 27,4 28,4 29,4 30,4 31,4 32,4 3,5 4,5 5,5 6,5 7,5 8,5 9,5 10,5 11,5 12,5 13,5 14,5 15,5 16,5 17,5 18,5 19,5 20,5 21,5 22,5 23,5 24,5 25,5 26,5 27,5 28,5 29,5 30,5 31,5 32,5 4,6 5,6 6,6 7,6 8,6 9,6 10,6 11,6 12,6 13,6 14,6 15,6 16,6 17,6 18,6 19,6 20,6 21,6 22,6 23,6 24,6 25,6 26,6 27,6 28,6 29,6 30,6 5,7 6,7 7,7 8,7 9,7 10,7 11,7 12,7 13,7 14,7 15,7 16,7 19,7 20,7 21,7 22,7 23,7 24,7 25,7 26,7 27,7 28,7 29,7 30,7 6,8 7,8 8,8 9,8 10,8 11,8 12,8 13,8 14,8 15,8 20,8 21,8 22,8 23,8 24,8 25,8 26,8 27,8 28,8 7,9 8,9 9,9 10,9 11,9 12,9 13,9 14,9 21,9 22,9 23,9 24,9 25,9 26,9 27,9 28,9");
        EYES.put("happy", "10,3 11,3 24,3 25,3 9,4 12,4 23,4 26,4 8,5 13,5 22,5 27,5 8,6 13,6 22,6 27,6 7,7 14,7 21,7 28,7");
        EYES.put("heart", "7,1 8,1 12,1 13,1 22,1 23,1 27,1 28,1 6,2 7,2 8,2 9,2 11,2 12,2 13,2 14,2 21,2 22,2 23,2 24,2 26,2 27,2 28,2 29,2 5,3 6,3 7,3 8,3 9,3 10,3 11,3 12,3 13,3 14,3 15,3 20,3 21,3 22,3 23,3 24,3 25,3 26,3 27,3 28,3 29,3 30,3 5,4 6,4 7,4 8,4 9,4 10,4 11,4 12,4 13,4 14,4 15,4 20,4 21,4 22,4 23,4 24,4 25,4 26,4 27,4 28,4 29,4 30,4 5,5 6,5 7,5 8,5 9,5 10,5 11,5 12,5 13,5 14,5 15,5 20,5 21,5 22,5 23,5 24,5 25,5 26,5 27,5 28,5 29,5 30,5 6,6 7,6 8,6 9,6 10,6 11,6 12,6 13,6 14,6 21,6 22,6 23,6 24,6 25,6 26,6 27,6 28,6 29,6 7,7 8,7 9,7 10,7 11,7 12,7 13,7 22,7 23,7 24,7 25,7 26,7 27,7 28,7 8,8 9,8 10,8 11,8 12,8 23,8 24,8 25,8 26,8 27,8 9,9 10,9 11,9 24,9 25,9 26,9 10,10 25,10");
        EYES.put("murder", "0,0 1,0 2,0 3,0 32,0 33,0 34,0 35,0 0,1 1,1 2,1 3,1 4,1 5,1 6,1 29,1 30,1 31,1 32,1 33,1 34,1 35,1 3,2 4,2 5,2 6,2 7,2 8,2 9,2 26,2 27,2 28,2 29,2 30,2 31,2 32,2 7,3 8,3 9,3 10,3 11,3 12,3 23,3 24,3 25,3 26,3 27,3 28,3 10,4 11,4 12,4 13,4 14,4 15,4 20,4 21,4 22,4 23,4 24,4 25,4 13,5 14,5 15,5 16,5 17,5 18,5 19,5 20,5 21,5 22,5 13,6 14,6 15,6 16,6 17,6 18,6 19,6 20,6 21,6 22,6 10,7 11,7 12,7 13,7 14,7 15,7 20,7 21,7 22,7 23,7 24,7 25,7 7,8 8,8 9,8 10,8 11,8 12,8 23,8 24,8 25,8 26,8 27,8 28,8 3,9 4,9 5,9 6,9 7,9 8,9 9,9 26,9 27,9 28,9 29,9 30,9 31,9 32,9 0,10 1,10 2,10 3,10 4,10 5,10 6,10 29,10 30,10 31,10 32,10 33,10 34,10 35,10 0,11 1,11 2,11 3,11 32,11 33,11 34,11 35,11");
        EYES.put("narrow", "7,3 8,3 9,3 10,3 11,3 12,3 13,3 14,3 21,3 22,3 23,3 24,3 25,3 26,3 27,3 28,3 8,4 9,4 10,4 11,4 12,4 13,4 22,4 23,4 24,4 25,4 26,4 27,4 8,5 9,5 10,5 11,5 12,5 13,5 22,5 23,5 24,5 25,5 26,5 27,5 8,6 9,6 10,6 11,6 12,6 13,6 22,6 23,6 24,6 25,6 26,6 27,6 8,7 9,7 10,7 11,7 12,7 13,7 22,7 23,7 24,7 25,7 26,7 27,7 7,8 8,8 9,8 10,8 11,8 12,8 13,8 14,8 21,8 22,8 23,8 24,8 25,8 26,8 27,8 28,8");
        EYES.put("normal", "10,0 11,0 24,0 25,0 9,1 10,1 11,1 12,1 23,1 24,1 25,1 26,1 9,2 10,2 11,2 12,2 23,2 24,2 25,2 26,2 8,3 9,3 10,3 11,3 12,3 13,3 22,3 23,3 24,3 25,3 26,3 27,3 8,4 9,4 10,4 11,4 12,4 13,4 22,4 23,4 24,4 25,4 26,4 27,4 8,5 9,5 10,5 11,5 12,5 13,5 22,5 23,5 24,5 25,5 26,5 27,5 8,6 9,6 10,6 11,6 12,6 13,6 22,6 23,6 24,6 25,6 26,6 27,6 8,7 9,7 10,7 11,7 12,7 13,7 22,7 23,7 24,7 25,7 26,7 27,7 8,8 9,8 10,8 11,8 12,8 13,8 22,8 23,8 24,8 25,8 26,8 27,8 9,9 10,9 11,9 12,9 23,9 24,9 25,9 26,9 9,10 10,10 11,10 12,10 23,10 24,10 25,10 26,10 10,11 11,11 24,11 25,11");
        EYES.put("scared", "10,0 11,0 24,0 25,0 9,1 12,1 23,1 26,1 9,2 12,2 23,2 26,2 8,3 13,3 22,3 27,3 8,4 13,4 22,4 27,4 8,5 13,5 22,5 27,5 8,6 13,6 22,6 27,6 8,7 13,7 22,7 27,7 6,8 8,8 13,8 22,8 27,8 29,8 6,9 9,9 12,9 23,9 26,9 29,9 6,10 9,10 12,10 23,10 26,10 29,10 7,11 10,11 11,11 24,11 25,11 28,11");
        EYES.put("squirm", "9,1 26,1 10,2 25,2 11,3 24,3 12,4 23,4 13,5 22,5 12,6 23,6 11,7 24,7 10,8 25,8 9,9 26,9");
        EYES.put("tired", "7,3 8,3 9,3 10,3 11,3 12,3 13,3 22,3 23,3 24,3 25,3 26,3 27,3 28,3 8,4 9,4 10,4 11,4 12,4 13,4 22,4 23,4 24,4 25,4 26,4 27,4 8,5 9,5 10,5 11,5 12,5 13,5 22,5 23,5 24,5 25,5 26,5 27,5 8,6 9,6 10,6 11,6 12,6 13,6 22,6 23,6 24,6 25,6 26,6 27,6 8,7 9,7 10,7 11,7 12,7 13,7 22,7 23,7 24,7 25,7 26,7 27,7 8,8 9,8 10,8 11,8 12,8 13,8 22,8 23,8 24,8 25,8 26,8 27,8 9,9 10,9 11,9 12,9 23,9 24,9 25,9 26,9 9,10 10,10 11,10 12,10 23,10 24,10 25,10 26,10 10,11 11,11 24,11 25,11");
        EYES.put("woozy", "21,3 22,3 23,3 24,3 25,3 26,3 27,3 28,3 22,4 23,4 24,4 25,4 26,4 27,4 7,5 8,5 9,5 10,5 11,5 12,5 13,5 14,5 22,5 23,5 24,5 25,5 26,5 27,5 8,6 9,6 10,6 11,6 12,6 13,6 22,6 23,6 24,6 25,6 26,6 27,6 8,7 9,7 10,7 11,7 12,7 13,7 22,7 23,7 24,7 25,7 26,7 27,7 8,8 9,8 10,8 11,8 12,8 13,8 22,8 23,8 24,8 25,8 26,8 27,8 6,9 9,9 10,9 11,9 12,9 23,9 24,9 25,9 26,9 29,9 6,10 9,10 10,10 11,10 12,10 23,10 24,10 25,10 26,10 29,10 7,11 10,11 11,11 24,11 25,11 28,11");
        EYES.put("_blush", "2,10 4,10 32,10 34,10 1,11 3,11 31,11 33,11");

        COLORS.put("nvj", 0xFFFFF200);
        COLORS.put("uzi", 0xFFB65BFF);
        COLORS.put("doll", 0xFFFF4924);
        COLORS.put("lizzy", 0xFFFF65E5);
        COLORS.put("thad", 0xFF55E441);
        COLORS.put("worker", 0xFF2BF4FF);
        COLORS.put("white", 0xFFFFFFFF);
        COLORS.put("rainbow", 1);

        for (Map.Entry<String, String> e : EYES.entrySet()) {
            Log.d(C.TAG, e.getKey());
            EYES_BITMAP.put(e.getKey(), stringToBitmap(e.getValue(), false));
            EYES_BITMAP_RAINBOW.put(e.getKey(), stringToBitmap(e.getValue(), true));
        }

        periodicBlink = () -> {
            if (!currentBlinkState) return;

            blink();

            handler.postDelayed(periodicBlink, C.BLINK_INTERVAL);
        };
    }

    private static Bitmap stringToBitmap(String data, boolean rainbow) {
        List<int[]> pixels = CustomImage.parsePixelList(data);

        int[] minmax = CustomImage.getMinMaxY(pixels);
        int min = minmax[0];
        int max = minmax[1];

        Bitmap bitmap = Bitmap.createBitmap(C.WIDTH, C.HEIGHT, Bitmap.Config.ARGB_8888);

        bitmap.eraseColor(Color.BLACK);

        for (int[] px : pixels) {
            int color = Color.WHITE;
            if (rainbow) {
                color = Rainbow.getVerticalColor(px[1], min, max);
            }
            bitmap.setPixel(px[0], px[1], color);
        }

        return Bitmap.createScaledBitmap(bitmap, C.WIDTH * 10, C.HEIGHT * 10, false);
    }

    public static int colorTransform(int color) {
        return Color.argb(255, Color.red(color), (int) (Color.green(color) * 0.65), Color.blue(color));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void setEyes(String id, int color, boolean forceClear) {
        if (isUpdating) return;
        isUpdating = true;

        String newEyeImage = EYES.getOrDefault(id, "0,0");

        if (color == 1) {
            currentEyeColor = 1;
            CustomImage.initRainbow(newEyeImage, forceClear || !Objects.equals(currentEyeImage, newEyeImage), () -> {
                if (currentBlushState) {
                    CustomImage.init(EYES.getOrDefault("_blush", ""), 0xffffff, false, () -> {
                        isUpdating = false;
                    });
                } else {
                    isUpdating = false;
                }
            });
        } else {
            currentEyeColor = colorTransform(color);
            CustomImage.init(newEyeImage, currentEyeColor, forceClear || !Objects.equals(currentEyeImage, newEyeImage), () -> {
                if (currentBlushState) {
                    CustomImage.init(EYES.getOrDefault("_blush", ""), currentEyeColor, false, () -> {
                        Log.d(C.TAG, "DONE");
                        isUpdating = false;
                    });
                } else {
                    isUpdating = false;
                }
            });
        }

        currentEyeImage = newEyeImage;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void setBlush(boolean set) {
        if (isUpdating) return;
        isUpdating = true;

        currentBlushState = set;

        CustomImage.init(EYES.getOrDefault("_blush", ""), currentBlushState ? (currentEyeColor == 1 ? 0xffffff : currentEyeColor) : 0x0, false, () -> {
            isUpdating = false;
        });
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void setBlink(boolean enable) {
        currentBlinkState = enable;

        if (currentBlinkState) {
            periodicBlink.run();
        } else {
            handler.removeCallbacks(EyeManager.periodicBlink);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void blink() {
        if (isUpdating) return;
        isUpdating = true;

        CustomImage.blink(currentEyeImage, currentEyeColor, () -> {
            isUpdating = false;
        });
    }

    public static void endBlink() {
        currentBlinkState = false;
        handler.removeCallbacks(EyeManager.periodicBlink);
    }

    public static int getColor(String id) {
        return COLORS.get(id);
    }
}
