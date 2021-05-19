package io.siddhi.extension.map.p4.trpt;

/**
 * Responsible for extracting the bytes that represent INT ethernet header values into usable values.
 */
public class IntEthernetHeader {

    private final byte[] bytes;

    public IntEthernetHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public String getDstMac() {
        return ByteUtils.getMacStr(bytes, 0);
    }

    public String getSrcMac() {
        return ByteUtils.getMacStr(bytes, 6);
    }

    public long getType() {
        return ByteUtils.getLongFromBytes(bytes, 12, 2);
    }
}
