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

import java.net.InetAddress;

/**
 * Responsible for extracting the bytes that represent IP header values into usable values.
 */
public class IpHeader {

    private final byte[] bytes;

    public IpHeader(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public short getVer() {
        return (short) ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public long getLen() {
        if (getVer() == 4) {
            return ByteUtils.getLongFromBytes(bytes, 2, 2);
        } else {
            return ByteUtils.getLongFromBytes(bytes, 4, 2);
        }
    }

    public int getNextProto() {
        if (getVer() == 4) {
            return bytes[9];
        } else {
            return bytes[6];
        }
    }

    public InetAddress getSrcAddr() {
        final int byteIndex;
        if (getVer() == 4) {
           byteIndex = 12;
        } else {
            byteIndex = 8;
        }
        return ByteUtils.getInetAddress(bytes, getVer(), byteIndex);
    }

    public InetAddress getDstAddr() {
        final int byteIndex;
        if (getVer() == 4) {
            byteIndex = 16;
        } else {
            byteIndex = 24;
        }
        return ByteUtils.getInetAddress(bytes, getVer(), byteIndex);
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty("len", this.getLen());
        outJson.addProperty("nextProto", this.getNextProto());
        outJson.addProperty("version", this.getVer());
        outJson.addProperty("dstAddr", this.getDstAddr().getHostAddress());
        outJson.addProperty("srcAddr", this.getSrcAddr().getHostAddress());

        return outJson;
    }
}
