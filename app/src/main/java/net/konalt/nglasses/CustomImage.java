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
    public static final long IF_WAIT_TIME = 20;
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
    private static void sendPixelPacket(List<int[]> pixels, int startIndex, int color, Runnable onFinishAll) {
        int remaining = pixels.size() - startIndex;
        if (remaining == 0) {
            onFinishAll.run();
            finishSend();
            return;
        }
        if (remaining > 8) remaining = 8;
        List<int[]> packet = pixels.subList(startIndex, startIndex + remaining);
        if (remaining == 8) {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                sendPixelPacket(pixels, startIndex + 8, color, onFinishAll);
            }, IF_WAIT_TIME));
        } else {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                onFinishAll.run();
                finishSend();
            }, IF_WAIT_TIME));
        }
        setPixels(packet, color);
    }
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendBlinkPacket(List<Integer> pixels, int startIndex, int color, List<List<Integer>> lines, int lineIndex, boolean isClosing, int minY, int maxY, Runnable onFinish) {
        int remaining = pixels.size() - startIndex;
        if (remaining <= 0) {
            // Packet is empty, send the next line
            Log.d(C.TAG, "Packet empty, sending next line");
            int changeValue = isClosing ? 1 : -1;
            sendBlinkLine(lines, lineIndex + changeValue, color, isClosing, minY, maxY, onFinish);
            return;
        }
        if (remaining >= 9) remaining = 9;

        Runnable onPixelsSet;
        if (remaining == 9) {
            // More pixels to set for this line, send another packet
            onPixelsSet = () -> {
                Log.d(C.BLINK_TAG, "Sending another packet");
                sendBlinkPacket(pixels, startIndex + 8, color, lines, lineIndex, isClosing, minY, maxY, onFinish);
            };
        } else {
            // After sending this, line will be done sending. We can send the next line
            boolean isLastLine = isClosing ? (lineIndex == lines.size() - 1) : lineIndex == 0;
            if (isLastLine) {
                if (isClosing) {
                    // Start opening again
                    onPixelsSet = () -> {
                        Log.d(C.BLINK_TAG, "Finished closing, now opening");
                        sendBlinkLine(lines, lineIndex, color, false, minY, maxY, onFinish);
                    };
                } else {
                    // Last line sent while opening, we can finish
                    onPixelsSet = () -> {
                        Log.d(C.BLINK_TAG, "Finished");
                        onFinish.run();
                    };
                }
            } else {
                // There are lines remaining, send the next
                // If we are closing, index must increment, otherwise decrement
                int changeValue = isClosing ? 1 : -1;
                onPixelsSet = () -> {
                    Log.d(C.BLINK_TAG, "Sending next line");
                    sendBlinkLine(lines, lineIndex + changeValue, color, isClosing, minY, maxY, onFinish);
                };
            }
        }
        App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(onPixelsSet, IF_WAIT_TIME));

        if (remaining == 9) remaining = 8;
        List<Integer> packet = pixels.subList(startIndex, startIndex + remaining);
        List<int[]> packet2 = new ArrayList<>();
        for (int px : packet) {
            packet2.add(new int[]{px, lineIndex});
        }

        if (color == 1) {
            setPixels(packet2, isClosing ? 0x0 : Rainbow.getVerticalColor(lineIndex,  minY, maxY));
        } else {
            setPixels(packet2, isClosing ? 0x0 : color);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendBlinkLine(List<List<Integer>> lines, int index, int color, boolean isClosing, int minY, int maxY, Runnable onFinish) {
        if (lines.size() <= index || index == -1) {
            onFinish.run();
            finishSend();
            return;
        }
        sendBlinkPacket(lines.get(index), 0, color, lines, index, isClosing, minY, maxY, onFinish);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendRainbowPacket(List<Integer> pixels, int startIndex, List<List<Integer>> lines, int lineIndex, Runnable onFinish, int minY, int maxY) {
        int remaining = pixels.size() - startIndex;
        if (remaining == 0) {
            sendRainbowLine(lines, lineIndex + 1, onFinish, minY, maxY);
            return;
        }
        if (remaining > 8) remaining = 8;
        List<Integer> packet = pixels.subList(startIndex, startIndex + remaining);
        if (remaining == 8) {
            App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                sendRainbowPacket(pixels, startIndex + 8, lines, lineIndex, onFinish, minY, maxY);
            }, IF_WAIT_TIME));
        } else {
            if (lineIndex == lines.size() - 1) {
                App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                    onFinish.run();
                    finishSend();
                }, IF_WAIT_TIME));
            } else {
                App.currentDevice.raw.onWriteCharacteristic3(() -> handler.postDelayed(() -> {
                    sendRainbowLine(lines, lineIndex + 1, onFinish, minY, maxY);
                }, BLINK_WAIT_TIME));
            }
        }

        List<int[]> packet2 = new ArrayList<>();
        for (int px : packet) {
            packet2.add(new int[]{px, lineIndex});
        }

        setPixels(packet2, Rainbow.getVerticalColor(lineIndex, minY, maxY));
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendRainbowLine(List<List<Integer>> lines, int index, Runnable onFinish, int minY, int maxY) {
        if (lines.size() <= index) {
            onFinish.run();
            return;
        }
        sendRainbowPacket(lines.get(index), 0, lines, index, onFinish, minY, maxY);
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void finishSend() {
        Log.d(C.TAG, "Finished initializing");
        //App.currentDevice.raw.writeRawData(Enigma.getEncryptData(Enigma.getExitDiySave()));
    }
    public static List<int[]> parsePixelList(String data) {
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
    public static void init(String customImageData, int customImageColor, boolean clear, Runnable onFinish) {
        Log.d(C.TAG, "Loading custom image...");
        if (customImageColor != 0x0) currentColor = customImageColor;
        List<int[]> pixels = parsePixelList(customImageData);
        if (clear) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendPixelPacket(pixels, 0, customImageColor, onFinish);
                }, IF_WAIT_TIME);
            });
            App.currentDevice.clear();
        } else {
            sendPixelPacket(pixels, 0, customImageColor, onFinish);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private static void sendPaletteGroup(HashMap<Integer, List<int[]>> img, List<Integer> order, int index, Runnable onFinish) {
        int color = order.get(index);
        List<int[]> pixels = img.get(color);
        assert pixels != null;
        if (index < order.size() - 1) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendPaletteGroup(img, order, index + 1, onFinish);
                }, IF_WAIT_TIME);
            });
        }
        sendPixelPacket(pixels, 0, color, onFinish);
    }

    public static int[] getMinMaxY(List<int[]> pixels) {
        int min = C.HEIGHT;
        int max = 0;

        for (int[] px : pixels) {
            if (px[1] < min) min = px[1];
            if (px[1] > max) max = px[1];
        }

        return new int[]{min, max};
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void initPaletteImage(String customImageData, boolean clear, Runnable onFinish) {
        Log.d(C.TAG, "Loading custom image (palette)...");
        HashMap<Integer, List<int[]>> img = parsePaletteImage(customImageData);
        List<Integer> order = new ArrayList<>(img.keySet());
        if (clear) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendPaletteGroup(img, order, 0, onFinish);
                }, IF_WAIT_TIME);
            });
            App.currentDevice.clear();
        } else {
            sendPaletteGroup(img, order, 0, onFinish);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void initRainbow(String customImageData, boolean clear, Runnable onFinish) {
        Log.d(C.TAG, "Loading custom image (rainbow)...");
        currentColor = -1;
        List<int[]> pixels = parsePixelList(customImageData);
        List<List<Integer>> lines = pixelListToLines(pixels);

        int[] minmax = getMinMaxY(pixels);
        int min = minmax[0];
        int max = minmax[1];

        if (clear) {
            App.currentDevice.raw.onWriteCharacteristic1(() -> {
                handler.postDelayed(() -> {
                    sendRainbowLine(lines, 0, onFinish, min, max);
                }, IF_WAIT_TIME);
            });
            App.currentDevice.clear();
        } else {
            sendRainbowLine(lines, 0, onFinish, min, max);
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public static void blink(String customImageData, int customImageColor, Runnable onFinish) {
        Log.d(C.TAG, "Blinking custom image...");
        List<int[]> pixels = parsePixelList(customImageData);
        List<List<Integer>> lines = pixelListToLines(pixels);

        int[] minmax = getMinMaxY(pixels);
        int min = minmax[0];
        int max = minmax[1];

        sendBlinkLine(lines, 0, customImageColor, true, min, max, onFinish);
    }
}