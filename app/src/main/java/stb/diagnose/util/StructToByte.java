package stb.diagnose.util;

/**
 * Created by Lenovo on 2017/8/15.
 */

public class StructToByte {

    private byte[] buf = null;

    /**
     * int转为低字节在前，高字节在后的byte数组 VC
     * @param n
     * @return byte[]
     */
    private byte[] toLH(int n){
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * 将float转为低字节在前，高字节在后的byte数组
     * @param f
     * @return byte[]
     */
    private byte[] toLH(float f) {
        return toLH(Float.floatToRawIntBits(f));
    }

    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        //由高位到低位
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);

        return result;
    }





    public static int bytes2Integer(byte[] byteVal) {
        int result = 0;
        for (int i = 0; i < byteVal.length; i++) {
            int tmpVal = (byteVal[i] << (8 * (3 - i)));
            switch (i) {
                case 0:
                    tmpVal = tmpVal & 0xFF000000;
                    break;
                case 1:
                    tmpVal = tmpVal & 0x00FF0000;
                    break;
                case 2:
                    tmpVal = tmpVal & 0x0000FF00;
                    break;
                case 3:
                    tmpVal = tmpVal & 0x000000FF;
                    break;
            }
            result = result | tmpVal;
        }
        return result;
    }

    public static byte[] toLHint(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }




    public StructToByte(String packetID, int packetLen, int ea, int eb, int ec, int markbit ) {
        byte[] temp = null;
        buf = new byte[24];
        temp = (packetID).getBytes();
        System.arraycopy(temp, 0, buf, 0, temp.length);
        temp = toLH(packetLen);
        System.arraycopy(temp, 0, buf, 4, temp.length);
        temp = toLH(ea);
        System.arraycopy(temp, 0, buf, 8, temp.length);
        temp = toLH(eb);
        System.arraycopy(temp, 0, buf, 12, temp.length);
        temp = toLH(ec);
        System.arraycopy(temp, 0, buf, 16, temp.length);
        temp = toLH(markbit);
        System.arraycopy(temp, 0, buf, 20, temp.length);

    }
    /**
     * 返回要发送的数组
     */
    public byte[] getBuf() {
        return buf;
    }

}
