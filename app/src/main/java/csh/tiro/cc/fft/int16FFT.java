package csh.tiro.cc.fft;

/* loaded from: classes.dex */
public class int16FFT {
    public static short NUM_FFT;

    public static native void BitReverse(short[] sArr);

    public static native void IntFFT(short[] sArr, short[] sArr2);

    public static native void WindowCalc(short[] sArr, char c);

    static {
        System.loadLibrary("int16fft");
        NUM_FFT = (short) 128;
    }
}
