package tud.seemuh.nfcgate.util;

public class Utils {

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Convert a byte-array to a hexadecimal String.
     *
     * @param bytes Byte[] to convert to String
     * @return Byte[] as hex-string
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public static String bytesToHex(byte b) {
        return bytesToHex(new byte[]{ b });
    }
}
