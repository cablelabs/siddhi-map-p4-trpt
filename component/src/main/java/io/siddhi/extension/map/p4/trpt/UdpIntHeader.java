package io.siddhi.extension.map.p4.trpt;

import com.google.gson.JsonObject;

/**
 * Responsible for extracting the bytes that represent UDP INT header values into usable values.
 */
public class UdpIntHeader {

    private final byte[] bytes;

    public UdpIntHeader(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public long getUdpIntSrcPort() {
        return ByteUtils.getLongFromBytes(bytes, 0, 2);
    }

    public long getUdpIntDstPort() {
        return ByteUtils.getLongFromBytes(bytes, 2, 2);
    }

    public long getUdpIntLen() {
        return ByteUtils.getLongFromBytes(bytes, 4, 2);
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty("srcPort", this.getUdpIntSrcPort());
        outJson.addProperty("dstPort", this.getUdpIntDstPort());
        outJson.addProperty("len", this.getUdpIntLen());

        return outJson;
    }
}
