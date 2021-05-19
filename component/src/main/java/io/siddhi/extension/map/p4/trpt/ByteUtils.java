package io.siddhi.extension.map.p4.trpt;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Utilities for converting values within byte arrays into more useful data structures.
 */
public class ByteUtils {

    public static byte[] getBytesFrag(final byte[] theBytes, final int start, final int total) {
        final byte[] out = new byte[total];
        for (int i = 0; i < total; i++) {
            out[i] = theBytes[start + i];
        }
        return out;
    }

    public static InetAddress getInetAddress(final byte[] bytes, final int version, final int startIndex) {
        final int numBytes;
        if (version == 4) {
            numBytes = 4;
        } else {
            numBytes = 16;
        }
        final byte[] addrBytes = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            addrBytes[i] = bytes[startIndex + i];
        }
        try {
            return InetAddress.getByAddress(addrBytes);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMacStr(final byte[] bytes, final int startIndex) {
        final StringBuffer out = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            out.append(String.format("%02x", bytes[startIndex + i]));
            if (i < 5) {
                out.append(':');
            }
        }
        return out.toString();
    }

    public static String getBitString(final byte theByte) {
        final StringBuffer out = new StringBuffer();
        for (int i = 0; i < 8; i++) {
            out.append(theByte >> (8 - (i + 1)) & 0x0001);
        }
        return out.toString();
    }

    public static int getIntFromNibble(final byte theByte, final boolean firstHalf) {
        if (firstHalf) {
            return (theByte & 0xf0) >> 4;
        } else {
            return theByte & 0xf;
        }
    }

    public static short getBitVal(final byte theByte, final int position) {
        final String binStr = String.format("%08d", Integer.parseInt(Integer.toBinaryString(theByte)));
        final String bitStr = binStr.substring(position, position + 1);
        return Short.valueOf(bitStr, 2);
    }

    public static long getLongFromBytes(final byte[] theBytes, final int start, final int count) {
        final StringBuffer hexStr = new StringBuffer();
        for (int i = 0; i < count; i++) {
            final String byteHex = String.format("%02x", theBytes[start + i]);
            hexStr.append(byteHex);
        }

        final long out = Long.parseUnsignedLong(hexStr.toString(), 16);
        return out;
    }
}
