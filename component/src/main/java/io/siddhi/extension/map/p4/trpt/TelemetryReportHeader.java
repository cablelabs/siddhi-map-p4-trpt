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

    public static final String TRPT_DOMAIN_ID_KEY = "domainId";
    public static final String TRPT_HW_ID_KEY = "hardwareId";
    public static final String TRPT_IN_TYPE_KEY = "inType";
    public static final String TRPT_NODE_ID_KEY = "nodeId";
    public static final String TRPT_RPT_LEN_KEY = "rptLen";
    public static final String TRPT_SEQ_NO_KEY = "seqNo";
    public static final String TRPT_VER_KEY = "version";
    public static final String TRPT_META_LEN_KEY = "metaLen";
    public static final String TRPT_RPT_TYPE_KEY = "rptType";
    public static final String TRPT_D_KEY = "d";
    public static final String TRPT_Q_KEY = "q";
    public static final String TRPT_F_KEY = "f";
    public static final String TRPT_I_KEY = "i";
    public static final String TRPT_REP_MD_BITS_KEY = "repMdBits";
    public static final String TRPT_MDB_BITS_KEY = "mdbBits";
    public static final String TRPT_MDS_BITS_KEY = "mdsBits";
    public static final String TRPT_VAR_OPT_MD_KEY = "varOptMd";

    private final byte[] bytes;

    public TelemetryReportHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return this.bytes.clone();
    }

    // The getters to parses through the byte array to extract expected values
    public int getVersion() {
        if (bytes.length < 1) {
            return 0;
        }
        return ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public int getHardwareId() {
        // Create 6 bit binary string
        if (bytes.length < 1) {
            return 0;
        }
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

    public int getInType() {
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

        outJson.addProperty(TRPT_DOMAIN_ID_KEY, this.getDomainId());
        outJson.addProperty(TRPT_HW_ID_KEY, this.getHardwareId());
        outJson.addProperty(TRPT_IN_TYPE_KEY, this.getInType());
        outJson.addProperty(TRPT_NODE_ID_KEY, this.getNodeId());
        outJson.addProperty(TRPT_RPT_LEN_KEY, this.getReportLength());
        outJson.addProperty(TRPT_SEQ_NO_KEY, this.getSequenceId());
        outJson.addProperty(TRPT_VER_KEY, this.getVersion());
        outJson.addProperty(TRPT_META_LEN_KEY, this.getMetadataLength());
        outJson.addProperty(TRPT_RPT_TYPE_KEY, this.getReportType());
        outJson.addProperty(TRPT_D_KEY, this.getD());
        outJson.addProperty(TRPT_Q_KEY, this.getQ());
        outJson.addProperty(TRPT_F_KEY, this.getF());
        outJson.addProperty(TRPT_I_KEY, this.getI());
        outJson.addProperty(TRPT_REP_MD_BITS_KEY, this.getRepMdBitStr());
        outJson.addProperty(TRPT_MDB_BITS_KEY, this.getDsMdbBitStr());
        outJson.addProperty(TRPT_MDS_BITS_KEY, this.getDsMdsBitStr());
        outJson.addProperty(TRPT_VAR_OPT_MD_KEY, this.getVarOptMd());

        return outJson;
    }

}
