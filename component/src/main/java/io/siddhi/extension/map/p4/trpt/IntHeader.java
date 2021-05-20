package io.siddhi.extension.map.p4.trpt;

import com.google.gson.JsonObject;

/**
 * Responsible for extracting the bytes that represent INT header values into usable values.
 */
public class IntHeader {

    static final int INT_SHIM_SIZE = 6;
    private final byte[] bytes;

    public final IntShimHeader shimHdr;
    public final IntMetadataHeader mdHdr;
    public final IntMetataStackHeader mdStackHdr;
    public final int lastIndex;

    public IntHeader(byte[] bytes) {
        this.bytes = bytes.clone();
        int byteIndex = 0;
        shimHdr = new IntShimHeader(ByteUtils.getBytesFrag(this.bytes, byteIndex, 4));
        byteIndex += 4;
        mdHdr = new IntMetadataHeader(ByteUtils.getBytesFrag(this.bytes, byteIndex, 12));
        byteIndex += 12;
        mdStackHdr = new IntMetataStackHeader(shimHdr.getLength() - INT_SHIM_SIZE,
                ByteUtils.getBytesFrag(this.bytes, byteIndex, bytes.length - byteIndex));

        lastIndex = byteIndex + mdStackHdr.getLastIndex();
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.add("shimHdr", shimHdr.toJson());
        outJson.add("mdHdr", mdHdr.toJson());
        outJson.add("mdStackHdr", mdStackHdr.toJson());

        return outJson;
    }
}
