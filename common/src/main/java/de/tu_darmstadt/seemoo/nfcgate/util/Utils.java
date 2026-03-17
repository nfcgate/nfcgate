package de.tu_darmstadt.seemoo.nfcgate.util;

public class Utils {

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /**
     * Convert a byte-array to a hexadecimal String with bytes separated by ':'.
     * @param bytes
     * @return Colon-separated hex-string from byte-array.
     */
    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, ':');
    }

    /**
     * Convert a byte-array to a hexadecimal String.
     *
     * from https://stackoverflow.com/a/9855338/207861
     *
     * @param bytes Byte[] to convert to String
     * @param separator Separator between bytes
     * @return Byte[] as hex-string
     */
    public static String bytesToHex(byte[] bytes, char separator) {
        char[] hexChars = new char[bytes.length * 3];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = separator;
        }

        return new String(hexChars, 0, hexChars.length - 1);
    }
    public static String bytesToHex(byte b) {
        return bytesToHex(new byte[]{ b });
    }

    /**
     * Convert a byte-array to a multiline hexdump String.
     *
     * Every line is prefixed with the hex offset, e.g.:
     * 000 01 02 03 ...
     * 010 10 20 30 ...
     *
     * @param bytes Byte[] to convert to String
     * @return Byte[] as hexdump string
     */
    public static String bytesToHexDump(byte[] bytes) {
        int lines = bytes.length / 16 + 1;
        int linePreamble = 5;
        char[] hexChars = new char[bytes.length * 3 + lines * linePreamble];
        for ( int j = 0, l = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            int baseIx = l * linePreamble + j * 3;

            // begin of new line
            if ((j % 16) == 0) {
                // if not first line
                if (j != 0) {
                    l++;
                    baseIx = l * linePreamble + j * 3;
                    hexChars[baseIx] = '\n';
                }

                // hex offset
                hexChars[baseIx + 1] = hexArray[(j >>> 8) & 0x0F];
                hexChars[baseIx + 2] = hexArray[(j >>> 4) & 0x0F];
                hexChars[baseIx + 3] = hexArray[j & 0x0F];
                hexChars[baseIx + 4] = ' ';
            }

            hexChars[baseIx + linePreamble] = ' ';
            hexChars[baseIx + linePreamble + 1] = hexArray[v >>> 4];
            hexChars[baseIx + linePreamble + 2] = hexArray[v & 0x0F];
        }
        return new String(hexChars, 1, hexChars.length - 1);
    }
}
