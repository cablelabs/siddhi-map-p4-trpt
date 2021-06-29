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
 * Responsible for extracting the bytes that represent UDP INT header values into usable values.
 */
public class UdpIntHeader {

    public static final String UDP_INT_HDR_SRC_PORT_KEY = "srcPort";
    public static final String UDP_INT_HDR_DST_PORT_KEY = "dstPort";
    public static final String UDP_INT_HDR_LEN_KEY = "len";

    private final byte[] bytes;

    /**
     * Default constructor without any bytes.
     */
    public UdpIntHeader() {
        this.bytes = new byte[0];
    }

    /**
     * General use constructor.
     * @param bytes - the byte array representing the report
     */
    public UdpIntHeader(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return this.bytes.clone();
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

        outJson.addProperty(UDP_INT_HDR_SRC_PORT_KEY, this.getUdpIntSrcPort());
        outJson.addProperty(UDP_INT_HDR_DST_PORT_KEY, this.getUdpIntDstPort());
        outJson.addProperty(UDP_INT_HDR_LEN_KEY, this.getUdpIntLen());

        return outJson;
    }
}
