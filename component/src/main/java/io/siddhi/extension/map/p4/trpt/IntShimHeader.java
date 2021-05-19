package io.siddhi.extension.map.p4.trpt;

/**
 * Responsible for extracting the bytes that represent INT Shim header values into usable values.
 */
public class IntShimHeader {

    private final byte[] bytes;

    public IntShimHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public int getType() {
        return ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public int getNpt() {
        final String byteBitStr = ByteUtils.getBitString(bytes[0]);
        final String theBitsStr = byteBitStr.substring(4, 6);
        return Integer.parseInt(theBitsStr, 2);
    }

    public int getLength() {
        return bytes[1];
    }

    public int getNextProto() {
        return bytes[3];
    }
}
