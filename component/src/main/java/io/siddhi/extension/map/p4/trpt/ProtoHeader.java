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

import java.nio.ByteBuffer;

/**
 * Responsible for extracting the bytes that represent the originating packet's prototype (TCP|UDP) header port values.
 */
public class ProtoHeader {

    public static final String PROTO_HDR_SRC_PORT_KEY = "srcPort";
    public static final String PROTO_HDR_DST_PORT_KEY = "dstPort";

    private final byte[] bytes;

    /**
     * General use constructor.
     * @param bytes - the byte array representing the report
     */
    public ProtoHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return this.bytes.clone();
    }

    /**
     * Returns the INT packet's source port value.
     * @return - the port value (zero will be returned if this is a drop report)
     */
    public long getSrcPort() {
        return ByteUtils.getLongFromBytes(bytes, 0, 2);
    }

    /**
     * Sets the INT packet's source port value.
     */
    public void setSrcPort(final long port) {
        byte[] portBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(port).array();
        System.arraycopy(portBytes, 6, bytes, 0, 2);
    }

    /**
     * Returns the INT packet's destination port value.
     * @return - the port value (zero will be returned if this is a drop report)
     */
    public long getDstPort() {
        return ByteUtils.getLongFromBytes(bytes, 2, 2);
    }

    /**
     * Sets the INT packet's destination port value.
     */
    public void setDstPort(final long port) {
        byte[] portBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(port).array();
        System.arraycopy(portBytes, 6, bytes, 2, 2);
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();
        outJson.addProperty(PROTO_HDR_SRC_PORT_KEY, this.getSrcPort());
        outJson.addProperty(PROTO_HDR_DST_PORT_KEY, this.getDstPort());
        return outJson;
    }
}
