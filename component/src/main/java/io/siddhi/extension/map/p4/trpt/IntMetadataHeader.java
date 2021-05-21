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
 * Responsible for extracting the bytes that represent INT Metadata header values into usable values.
 */
public class IntMetadataHeader {

    public final static String INT_MD_HDR_VER_KEY = "version";
    public final static String INT_MD_HDR_D_KEY = "d";
    public final static String INT_MD_HDR_E_KEY = "e";
    public final static String INT_MD_HDR_M_KEY = "m";
    public final static String INT_MD_HDR_MD_LEN_KEY = "mdLen";
    public final static String INT_MD_HDR_REMAIN_HOP_CNT_KEY = "remainingHopCount";
    public final static String INT_MD_HDR_INSTR_KEY = "instructions";
    public final static String INT_MD_HDR_DOMAIN_ID_KEY = "domainId";
    public final static String INT_MD_HDR_DS_INSTR_KEY = "dsInstructions";
    public final static String INT_MD_HDR_DS_FLAGS_KEY = "dsFlags";

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

        outJson.addProperty(INT_MD_HDR_VER_KEY, this.getVersion());
        outJson.addProperty(INT_MD_HDR_D_KEY, this.getD());
        outJson.addProperty(INT_MD_HDR_E_KEY, this.getE());
        outJson.addProperty(INT_MD_HDR_M_KEY, this.getM());
        outJson.addProperty(INT_MD_HDR_MD_LEN_KEY, this.getPerHopMdLen());
        outJson.addProperty(INT_MD_HDR_REMAIN_HOP_CNT_KEY, this.getRemainingHopCount());
        outJson.addProperty(INT_MD_HDR_INSTR_KEY, this.getInstructions());
        outJson.addProperty(INT_MD_HDR_DOMAIN_ID_KEY, this.getDomainId());
        outJson.addProperty(INT_MD_HDR_DS_INSTR_KEY, this.getDsInstructions());
        outJson.addProperty(INT_MD_HDR_DS_FLAGS_KEY, this.getDsFlags());

        return outJson;
    }
}
