package csh.tiro.cc;

/* loaded from: classes.dex */
public class aes {
    public static native void cipher(byte[] bArr, byte[] bArr2);

    public static native void invCipher(byte[] bArr, byte[] bArr2);

    public static native void keyExpansion(byte[] bArr);

    public static native void keyExpansionDefault();

    static {
        System.loadLibrary("AES");
    }
}
