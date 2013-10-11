package com.telepresenceRobot.android.util;

public class ByteUtil {
    final protected static char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String byteToHex(byte b) {
        char[] hexChars = new char[2];
        hexChars[0] = hexArray[(b >> 4) & 0x0f];
        hexChars[1] = hexArray[(b >> 0) & 0x0f];
        return new String(hexChars);
    }
}
