package io.siddhi.extension.map.p4.trpt;

import java.net.InetAddress;

/**
 * Responsible for extracting the bytes that represent IP header values into usable values.
 */
public class IpHeader {

    private final byte[] bytes;

    public IpHeader(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public short getVer() {
        return (short) ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public long getLen() {
        if (getVer() == 4) {
            return ByteUtils.getLongFromBytes(bytes, 2, 2);
        } else {
            return ByteUtils.getLongFromBytes(bytes, 4, 2);
        }
    }

    public int getNextProto() {
        if (getVer() == 4) {
            return bytes[9];
        } else {
            return bytes[6];
        }
    }

    public InetAddress getSrcAddr() {
        final int byteIndex;
        if (getVer() == 4) {
           byteIndex = 12;
        } else {
            byteIndex = 8;
        }
        return ByteUtils.getInetAddress(bytes, getVer(), byteIndex);
    }

    public InetAddress getDstAddr() {
        final int byteIndex;
        if (getVer() == 4) {
            byteIndex = 16;
        } else {
            byteIndex = 24;
        }
        return ByteUtils.getInetAddress(bytes, getVer(), byteIndex);
    }
}
