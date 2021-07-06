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
 * Responsible for extracting the bytes that represent INT ethernet header values into usable values.
 */
public class IntEthernetHeader {

    public static final String IETH_HDR_DST_MAC_KEY = "dstMac";
    public static final String IETH_HDR_SRC_MAC_KEY = "srcMac";
    public static final String IETH_TYPE_KEY = "type";

    private final byte[] bytes;

    public IntEthernetHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return this.bytes.clone();
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

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty(IETH_HDR_DST_MAC_KEY, this.getDstMac());
        outJson.addProperty(IETH_HDR_SRC_MAC_KEY, this.getSrcMac());
        outJson.addProperty(IETH_TYPE_KEY, this.getType());

        return outJson;
    }
}
