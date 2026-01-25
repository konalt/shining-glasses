package net.konalt.nglasses;

import com.alibaba.fastjson2.JSONB;

import java.util.Random;

import csh.tiro.cc.aes;
import kotlin.jvm.internal.ByteCompanionObject;

public class Enigma {
    public static byte[] getClearCommand() {
        return new byte[]{6, 83, JSONB.Constants.BC_STR_ASCII_FIX_4, 86, 69, 87, 1, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getLight(int i) {
        byte b = (byte) i;
        return new byte[]{6, 76, 73, JSONB.Constants.BC_INT32_SHORT_MAX, JSONB.Constants.BC_INT32, 84, b, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getRandomThing1() {
        return new byte[]{6, 83, JSONB.Constants.BC_STR_ASCII_FIX_4, 86, 69, 87, 0, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getExitDiySave() {
        return new byte[]{6, 83, JSONB.Constants.BC_STR_ASCII_FIX_4, 86, 69, 87, 2, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getTextColor(byte b, byte b2, byte b3, boolean z) {
        return new byte[]{6, 70, 67, z ? (byte) 1 : (byte) 0, b, b2, b3, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getTextBgColor(byte b, byte b2, byte b3, boolean z) {
        return new byte[]{6, 66, 67, z ? (byte) 1 : (byte) 0, b, b2, b3, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getDefaultMode(int i, boolean z) {
        return new byte[]{3, JSONB.Constants.BC_STR_ASCII_FIX_4, z ? (byte) 1 : (byte) 0, (byte) i, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getSpeed(int i) {
        byte b = (byte) i;
        return new byte[]{6, 83, 80, 69, 69, JSONB.Constants.BC_INT32_SHORT_ZERO, b, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getAnimCommand(int i) {
        byte b = (byte) i;
        return new byte[]{5, 65, JSONB.Constants.BC_STR_ASCII_FIX_5, 73, JSONB.Constants.BC_STR_ASCII_FIX_4, b, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getImageCommand(int i) {
        byte b = (byte) i;
        return new byte[]{5, 73, JSONB.Constants.BC_STR_ASCII_FIX_4, 65, JSONB.Constants.BC_INT32_SHORT_MAX, b, getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom(), getRandom()};
    }
    public static byte[] getMatrixBytes1236(byte[] bArr) {
        int i;
        int i2;
        if (bArr == null) {
            return null;
        }
        byte[] bArr2 = new byte[72];
        byte b = 0;
        byte b2 = 0;
        for (int i3 = 0; i3 < bArr.length; i3++) {
            int i4 = i3 / 12;
            int i5 = i3 % 12;
            if (bArr[i3] == 1) {
                switch (i5) {
                    case 0:
                        i = b | ByteCompanionObject.MIN_VALUE;
                        b = (byte) i;
                        break;
                    case 1:
                        i = b | JSONB.Constants.BC_INT32_SHORT_MIN;
                        b = (byte) i;
                        break;
                    case 2:
                        i = b | 32;
                        b = (byte) i;
                        break;
                    case 3:
                        i = b | JSONB.Constants.BC_INT32_NUM_16;
                        b = (byte) i;
                        break;
                    case 4:
                        i = b | 8;
                        b = (byte) i;
                        break;
                    case 5:
                        i = b | 4;
                        b = (byte) i;
                        break;
                    case 6:
                        i = b | 2;
                        b = (byte) i;
                        break;
                    case 7:
                        i = b | 1;
                        b = (byte) i;
                        break;
                    case 8:
                        i2 = b2 | ByteCompanionObject.MIN_VALUE;
                        b2 = (byte) i2;
                        break;
                    case 9:
                        i2 = b2 | JSONB.Constants.BC_INT32_SHORT_MIN;
                        b2 = (byte) i2;
                        break;
                    case 10:
                        i2 = b2 | 32;
                        b2 = (byte) i2;
                        break;
                    case 11:
                        i2 = b2 | JSONB.Constants.BC_INT32_NUM_16;
                        b2 = (byte) i2;
                        break;
                }
            }
            if (i5 == 11) {
                int i6 = i4 * 2;
                bArr2[i6] = b;
                bArr2[i6 + 1] = b2;
                b = 0;
                b2 = 0;
            }
        }
        return bArr2;
    }
    public static byte[] int2Bytes(int i) {
        return new byte[]{(byte) (i / 256), (byte) (i % 256)};
    }
    public static byte[] getEncryptData(byte[] bArr) {
        aes.cipher(bArr, bArr);
        return bArr;
    }

    public static byte[] getDecodeData(byte[] bArr) {
        aes.invCipher(bArr, bArr);
        return bArr;
    }

    public static byte getRandom() {
        return (byte) (new Random().nextInt(256) & 255);
    }
}
