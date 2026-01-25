package net.konalt.nglasses;

import android.Manifest;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CustomImage {
    public static final long IF_WAIT_TIME = 12;
    public static final long BLINK_WAIT_TIME = 15;
    private static final Handler handler = new Handler(Looper.getMainLooper());
    public static int currentColor = 0x0;
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void setPixels(List<int[]> pixels, int color) {
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
    private static void sendPixelPacket(List<int[]> pixels, int startIndex, int color) {
        int remaining = pixels.size() - startIndex;
        if (remaining == 0) {
            finishSend();
            return;
        }
        if (remaining > 8) remaining = 8;
        List<int[]> packet = pixels.subList(startIndex, startIndex + remaining);
        if (remaining == 8) {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                sendPixelPacket(pixels, startIndex + 8, color);
            }, IF_WAIT_TIME));
        } else {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(CustomImage::finishSend, IF_WAIT_TIME));
        }
        setPixels(packet, color);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendBlinkPacket(List<Integer> pixels, int startIndex, int color, List<List<Integer>> lines, int lineIndex, boolean isClosing) {
        int remaining = pixels.size() - startIndex;
        if (remaining == 0) {
            if (isClosing) {
                sendBlinkLine(lines, lineIndex + 1, color, true);
            } else {
                if (lineIndex > 0) {
                    sendBlinkLine(lines, lineIndex - 1, color, false);
                }
            }
            return;
        }
        if (remaining > 8) remaining = 8;
        List<Integer> packet = pixels.subList(startIndex, startIndex + remaining);
        if (remaining == 8) {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                sendBlinkPacket(pixels, startIndex + 8, color, lines, lineIndex, isClosing);
            }, IF_WAIT_TIME));
        } else {
            if ((isClosing && lineIndex == lines.size() - 1) || (!isClosing && lineIndex == 0)) {
                if (isClosing && color >= 0) {
                    App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                        sendBlinkLine(lines, lines.size() - 1, color, false);
                    }, IF_WAIT_TIME));
                } else {
                    App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(CustomImage::finishSend, IF_WAIT_TIME));
                }
            } else {
                App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                    if (isClosing) {
                        sendBlinkLine(lines, lineIndex + 1, color, true);
                    } else {
                        sendBlinkLine(lines, lineIndex - 1, color, false);
                    }
                }, BLINK_WAIT_TIME));
            }
        }
        List<int[]> packet2 = new ArrayList<>();
        for (int px : packet) {
            packet2.add(new int[]{px, lineIndex});
        }
        if (color == -1) {
            Log.d(C.TAG, "i: " + lineIndex);
            setPixels(packet2, Rainbow.getVerticalColor(lineIndex));
        } else {
            setPixels(packet2, isClosing ? 0x0 : color);
        }
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendBlinkLine(List<List<Integer>> lines, int index, int color, boolean isClosing) {
        if (lines.size() <= index) return;
        sendBlinkPacket(lines.get(index), 0, color, lines, index, isClosing);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void finishSend() {
        Log.d(C.TAG, "Finished initializing");
        App.currentDevice.raw.writeRawData(Enigma.getEncryptData(Enigma.getExitDiySave()));
    }
    private static List<int[]> parsePixelList(String data) {
        List<int[]> outPixels = new ArrayList<>();
        String[] onPixels = data.split(" ");
        for (String px : onPixels) {
            String[] coords = px.split(",");
            int x = Integer.parseInt(coords[0]);
            int y = Integer.parseInt(coords[1]);
            outPixels.add(new int[]{x,y});
        }
        return outPixels;
    }
    private static List<List<Integer>> pixelListToLines(List<int[]> pixels) {
        List<List<Integer>> outLines = new ArrayList<>();
        for (int[] px : pixels) {
            while (outLines.size() < px[1] + 1) {
                outLines.add(new ArrayList<>());
            }
            outLines.get(px[1]).add(px[0]);
        }
        return outLines;
    }
    private static HashMap<Integer, List<int[]>> parsePaletteImage(String data) {
        HashMap<Integer, List<int[]>> out = new HashMap<>();
        String[] colorStrings = data.split("\\|")[0].split(" ");
        List<Integer> colors = new ArrayList<>();
        for (String cs : colorStrings) {
            colors.add(Color.parseColor(cs));
        }
        Log.d(C.TAG, colors.toString());

        final int[] iter = {0};
        Arrays.stream(data.split("\\|")).skip(1).forEach((pg) -> {
            out.put(colors.get(iter[0]), new ArrayList<>());
            String[] pixels = pg.split(" ");
            for (String px : pixels) {
                String[] coords = px.split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                Objects.requireNonNull(out.get(colors.get(iter[0]))).add(new int[]{x,y});
            }
            iter[0]++;
        });

        Log.d(C.TAG, String.valueOf(out));

        return out;
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void init(String customImageData, int customImageColor, boolean clear) {
        Log.d(C.TAG, "Loading custom image...");
        if (customImageColor != 0x0) currentColor = customImageColor;
        List<int[]> pixels = parsePixelList(customImageData);
        if (clear) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendPixelPacket(pixels, 0, customImageColor);
                }, IF_WAIT_TIME);
            });
            App.currentDevice.clear();
        } else {
            sendPixelPacket(pixels, 0, customImageColor);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendPaletteGroup(HashMap<Integer, List<int[]>> img, List<Integer> order, int index) {
        int color = order.get(index);
        List<int[]> pixels = img.get(color);
        assert pixels != null;
        if (index < order.size() - 1) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendPaletteGroup(img, order, index + 1);
                }, IF_WAIT_TIME);
            });
        }
        sendPixelPacket(pixels, 0, color);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void initPaletteImage(String customImageData, boolean clear) {
        Log.d(C.TAG, "Loading custom image (palette)...");
        HashMap<Integer, List<int[]>> img = parsePaletteImage(customImageData);
        List<Integer> order = new ArrayList<>(img.keySet());
        if (clear) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendPaletteGroup(img, order, 0);
                }, IF_WAIT_TIME);
            });
            App.currentDevice.clear();
        } else {
            sendPaletteGroup(img, order, 0);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void initRainbow(String customImageData, boolean clear) {
        Log.d(C.TAG, "Loading custom image (rainbow)...");
        currentColor = -1;
        List<int[]> pixels = parsePixelList(customImageData);
        List<List<Integer>> lines = pixelListToLines(pixels);
        if (clear) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendBlinkLine(lines, 0, -1, true);
                }, IF_WAIT_TIME);
            });
            App.currentDevice.clear();
        } else {
            sendBlinkLine(lines, 0, -1, true);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void blink(String customImageData, int customImageColor) {
        Log.d(C.TAG, "Blinking custom image...");
        List<int[]> pixels = parsePixelList(customImageData);
        List<List<Integer>> lines = pixelListToLines(pixels);

        Log.d(C.TAG, lines.toString());

        sendBlinkLine(lines, 0, customImageColor, true);
    }
}