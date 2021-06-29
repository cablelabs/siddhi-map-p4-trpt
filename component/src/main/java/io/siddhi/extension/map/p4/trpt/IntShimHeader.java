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
 * Responsible for extracting the bytes that represent INT Shim header values into usable values.
 */
public class IntShimHeader {

    public static final String INT_SHIM_HDR_TYPE_KEY = "type";
    public static final String INT_SHIM_HDR_NPT_KEY = "npt";
    public static final String INT_SHIM_HDR_LEN_KEY = "len";
    public static final String INT_SHIM_HDR_NEXT_PROTO_KEY = "nextProto";

    private final byte[] bytes;

    /**
     * Default constructor without any bytes.
     */
    public IntShimHeader() {
        this.bytes = new byte[0];
    }

    /**
     * General use constructor.
     * @param bytes - the byte array representing the report
     */
    public IntShimHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return this.bytes.clone();
    }

    public int getType() {
        if (bytes.length < 1) {
            return 0;
        }
        return ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public int getNpt() {
        if (bytes.length < 1) {
            return 0;
        }
        final String byteBitStr = ByteUtils.getBitString(bytes[0]);
        final String theBitsStr = byteBitStr.substring(4, 6);
        return Integer.parseInt(theBitsStr, 2);
    }

    public int getLength() {
        if (bytes.length < 1) {
            return 0;
        }
        return bytes[1];
    }

    public int getNextProto() {
        if (bytes.length < 3) {
            return 0;
        }
        return bytes[3];
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty(INT_SHIM_HDR_TYPE_KEY, this.getType());
        outJson.addProperty(INT_SHIM_HDR_NPT_KEY, this.getNpt());
        outJson.addProperty(INT_SHIM_HDR_LEN_KEY, this.getLength());
        outJson.addProperty(INT_SHIM_HDR_NEXT_PROTO_KEY, this.getNextProto());

        return outJson;
    }
}
