package stb.diagnose;

/**
 * Created by liu on 2017-10-11.
 */

public class TestData {

    static {
        System.loadLibrary("stb_diagnose_test");
    }

//
//    public static native int nativeFlashTest();
//
//    public static native int nativeGpioTest();

    private static native int nativeTFTest();
}
