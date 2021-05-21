/*
 * Copyright (c) 2021 Cable Television Laboratories, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.siddhi.extension.map.p4.trpt;

import com.google.gson.JsonObject;

/**
 * Responsible for extracting the bytes that represent the Telemetry Report header values into usable values.
 */
public class TelemetryReportHeader {

    private final byte[] bytes;

    public TelemetryReportHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    // The getters to parses through the byte array to extract expected values
    public int getVersion() {
        return ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public int getHardwareId() {
        // Create 6 bit binary string
        int first = bytes[0] & 0xf;
        final String firstByteStr = String.format("%04d", Integer.parseInt(Integer.toBinaryString(first)));

        int second = (bytes[1] & 0xf0) >>> 6;
        final String secondByteStr = String.format("%02d", Integer.parseInt(Integer.toBinaryString(second)));
        final String outBytesStr = firstByteStr + secondByteStr;

        return Integer.parseInt(outBytesStr, 2);
    }

    public long getSequenceId() {
        // Create 16 bit binary string
        final String fullFirstByteStr = String.format("%08d", Integer.parseInt(Integer.toBinaryString(bytes[1])));
        final String firstByteStr = fullFirstByteStr.substring(2);
        final String secondByteStr = String.format("%08d", Integer.parseInt(Integer.toBinaryString(bytes[2])));

        int third = (bytes[3] & 0xf0) >>> 6;
        final String thirdByteStr = String.format("%02d", Integer.parseInt(Integer.toBinaryString(third)));
        final String outBytesStr = firstByteStr + secondByteStr + thirdByteStr;

        return Long.parseLong(outBytesStr, 2);
    }

    public long getNodeId() {
        return ByteUtils.getLongFromBytes(bytes, 4, 4);
    }

    public long getReportType() {
        return ByteUtils.getIntFromNibble(bytes[8], true);
    }

    public long getInType() {
        return ByteUtils.getIntFromNibble(bytes[8], false);
    }

    public long getReportLength() {
        return bytes[9];
    }

    public long getMetadataLength() {
        return bytes[10];
    }

    public short getD() {
        return ByteUtils.getBitVal(bytes[11], 0);
    }

    public short getQ() {
        return ByteUtils.getBitVal(bytes[11], 1);
    }

    public short getF() {
        return ByteUtils.getBitVal(bytes[11], 2);
    }

    public short getI() {
        return ByteUtils.getBitVal(bytes[11], 3);
    }

    public String getRepMdBitStr() {
        return ByteUtils.getBitString(bytes[12]) + ByteUtils.getBitString(bytes[13]);
    }

    public long getDomainId() {
        return ByteUtils.getLongFromBytes(bytes, 14, 2);
    }

    public String getDsMdbBitStr() {
        return ByteUtils.getBitString(bytes[16]) + ByteUtils.getBitString(bytes[17]);
    }

    public String getDsMdsBitStr() {
        return ByteUtils.getBitString(bytes[18]) + ByteUtils.getBitString(bytes[19]);
    }

    public String getVarOptMd() {
        return ByteUtils.getBitString(bytes[20]) + ByteUtils.getBitString(bytes[21])
                + ByteUtils.getBitString(bytes[22]) + ByteUtils.getBitString(bytes[23]);
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty("domainId", this.getDomainId());
        outJson.addProperty("hardwareId", this.getHardwareId());
        outJson.addProperty("inType", this.getInType());
        outJson.addProperty("nodeId", this.getNodeId());
        outJson.addProperty("rptLen", this.getReportLength());
        outJson.addProperty("seqNo", this.getSequenceId());
        outJson.addProperty("version", this.getVersion());
        outJson.addProperty("metaLen", this.getMetadataLength());
        outJson.addProperty("rptType", this.getReportType());
        outJson.addProperty("d", this.getD());
        outJson.addProperty("q", this.getQ());
        outJson.addProperty("f", this.getF());
        outJson.addProperty("i", this.getI());
        outJson.addProperty("repMdBits", this.getRepMdBitStr());
        outJson.addProperty("mdbBits", this.getDsMdbBitStr());
        outJson.addProperty("mdsBits", this.getDsMdsBitStr());
        outJson.addProperty("mdsBits", this.getDsMdsBitStr());
        outJson.addProperty("varOptMd", this.getVarOptMd());

        return outJson;
    }

}
