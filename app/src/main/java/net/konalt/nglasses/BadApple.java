package net.konalt.nglasses;

import android.Manifest;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BadApple {
    private static final int FPS = 4;
    private static final long FRAMETIME = 1000 / FPS;
    private static final long SETPX_WAIT = 15;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static boolean play = false;
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void setPixels(List<int[]> pixels, int color) {
        if (pixels.isEmpty()) {
            Log.w(C.TAG, "hi :3");
        }
        if (pixels.size() > 8) {
            Log.e(C.TAG, "Unable to set more than 8 pixels");
            return;
        }
        byte[] bArr = new byte[20];
        bArr[0] = (byte) (3 + pixels.size() * 2);
        bArr[1] = (byte) Color.red(color);
        bArr[2] = (byte) Color.green(color);
        bArr[3] = (byte) Color.blue(color);
        int i = 4;
        for (int[] p : pixels) {
            bArr[i] = (byte) p[0];
            bArr[i + 1] = (byte) p[1];
            i += 2;
        }
        Log.d(C.TAG, "Setting pixel data: " + Arrays.toString(bArr));
        App.currentDevice.raw.writeRawDataC3(bArr);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendPixelPacket(List<int[]> pixels, int startIndex, int color, Runnable onFinish) {
        int remaining = pixels.size() - startIndex;
        if (remaining == 0) {
            onFinish.run();
            return;
        }
        if (remaining > 8) remaining = 8;
        List<int[]> packet = pixels.subList(startIndex, startIndex + remaining);
        if (remaining == 8) {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                sendPixelPacket(pixels, startIndex + 8, color, onFinish);
            }, SETPX_WAIT));
        } else {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(onFinish, SETPX_WAIT));
        }
        setPixels(packet, color);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendFrame(List<int[]> on, List<int[]> off) {
        Log.d(C.TAG, "Sending frame");
        if (on.isEmpty()) {
            if (!off.isEmpty()) {
                sendPixelPacket(off, 0, 0x0, () -> {
                    Log.d(C.TAG, "Finished drawing frame, off only");
                });
            } else {
                Log.d(C.TAG, "empty frame :3");
            }
        } else {
            if (!off.isEmpty()) {
                sendPixelPacket(on, 0, 0xffffff, () -> {
                    handler.postDelayed(() -> {
                        sendPixelPacket(off, 0, 0x0, () -> {
                            Log.d(C.TAG, "Finished drawing frame, on and off");
                        });
                    }, SETPX_WAIT);
                });
            } else {
                sendPixelPacket(on, 0, 0xffffff, () -> {
                    Log.d(C.TAG, "Finished drawing frame, on only");
                });
            }
        }

    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void animate(List<List<List<int[]>>> animation, int index, boolean loop) {
        if (!play) return;
        sendFrame(animation.get(index).get(0), animation.get(index).get(1));
        if (index < animation.size() - 1) {
            handler.postDelayed(() -> {
                animate(animation, index + 1, loop);
            }, FRAMETIME);
        } else {
            if (loop) {
                handler.postDelayed(() -> {
                    animate(animation, 0, true);
                }, FRAMETIME);
            } else {
                play = false;
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void playAnimation(List<List<List<int[]>>> animation, boolean loop) {
        play = true;
        App.currentDevice.raw.onWriteCharacteristic1(() -> {
            handler.postDelayed(() -> {
                animate(animation, 0, loop);
            }, SETPX_WAIT);
        });
        App.currentDevice.clear();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void stopAnimation() {
        play = false;
    }

    // REMINDER:
    // List 1: List of animation frames
    // List 2: Pair of on/off
    // List 3: List of pixels
    // Pixel: [x,y]
    public static List<List<List<int[]>>> parseAnimationData(String[] frames) {
        List<List<List<int[]>>> animation = new ArrayList<>();
        for (String frame : frames) {
            List<List<int[]>> parsedFrame = new ArrayList<>();

            if (!Objects.equals(frame, "|")) {
                List<String> onPixelStrings = Arrays.asList(frame.split("\\|", -1)[0].split(" "));
                List<int[]> onPixels = new ArrayList<>();
                if (!onPixelStrings.isEmpty()) {
                    for (String px : onPixelStrings) {
                        if (px.isEmpty()) continue;
                        String[] coords = px.split(",");
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        onPixels.add(new int[]{x, y});
                    }
                }
                parsedFrame.add(onPixels);

                List<String> offPixelStrings = Arrays.asList(frame.split("\\|", -1)[1].split(" "));
                List<int[]> offPixels = new ArrayList<>();
                if (!offPixelStrings.isEmpty()) {
                    for (String px : offPixelStrings) {
                        if (px.isEmpty()) continue;
                        String[] coords = px.split(",");
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        offPixels.add(new int[]{x, y});
                    }
                }
                parsedFrame.add(offPixels);
            } else {
                List<int[]> empty = new ArrayList<>();
                parsedFrame.add(empty);
                parsedFrame.add(empty);
            }

            animation.add(parsedFrame);
        }
        return animation;
    }
}
