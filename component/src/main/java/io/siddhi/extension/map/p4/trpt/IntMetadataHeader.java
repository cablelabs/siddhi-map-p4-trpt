package io.siddhi.extension.map.p4.trpt;

import com.google.gson.JsonObject;

/**
 * Responsible for extracting the bytes that represent INT Metadata header values into usable values.
 */
public class IntMetadataHeader {

    private final byte[] bytes;

    public IntMetadataHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public int getVersion() {
        return ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public int getD() {
        final String byteStr = ByteUtils.getBitString(bytes[0]);
        return Integer.parseInt(Character.toString(byteStr.charAt(6)), 2);
    }

    public int getE() {
        final String byteStr = ByteUtils.getBitString(bytes[0]);
        return Integer.parseInt(Character.toString(byteStr.charAt(7)), 2);
    }

    public int getM() {
        final String byteStr = ByteUtils.getBitString(bytes[1]);
        return Integer.parseInt(Character.toString(byteStr.charAt(0)), 2);
    }

    public int getPerHopMdLen() {
        int theByte = bytes[2];
        final String byteStr = String.format("%05d", Integer.parseInt(Integer.toBinaryString(theByte)));
        return Integer.parseInt(byteStr, 2);
    }

    public int getRemainingHopCount() {
        return bytes[3];
    }

    public String getInstructions() {
        return ByteUtils.getBitString(bytes[4]) + ByteUtils.getBitString(bytes[5]);
    }

    public long getDomainId() {
        return ByteUtils.getLongFromBytes(bytes, 6, 2);
    }

    public String getDsInstructions() {
        return ByteUtils.getBitString(bytes[8]) + ByteUtils.getBitString(bytes[9]);
    }

    public String getDsFlags() {
        return ByteUtils.getBitString(bytes[10]) + ByteUtils.getBitString(bytes[11]);
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty("version", this.getVersion());
        outJson.addProperty("d", this.getD());
        outJson.addProperty("e", this.getE());
        outJson.addProperty("m", this.getM());
        outJson.addProperty("mdLen", this.getPerHopMdLen());
        outJson.addProperty("remainingHopCount", this.getRemainingHopCount());
        outJson.addProperty("instructions", this.getInstructions());
        outJson.addProperty("domainId", this.getDomainId());
        outJson.addProperty("dsInstructions", this.getDsInstructions());
        outJson.addProperty("dsFlags", this.getDsFlags());

        return outJson;
    }
}
