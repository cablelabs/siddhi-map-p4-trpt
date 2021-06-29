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

import com.google.common.net.InetAddresses;
import com.google.gson.JsonObject;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

/**
 * Responsible for extracting the bytes that represent IP header values into usable values.
 */
public class IpHeader {

    public static final String IP_HDR_LEN_KEY = "len";
    public static final String IP_HDR_NEXT_PROTO_KEY = "nextProto";
    public static final String IP_HDR_VER_KEY = "version";
    public static final String IP_HDR_DST_ADDR_KEY = "dstAddr";
    public static final String IP_HDR_SRC_ADDR_KEY = "srcAddr";

    private final byte[] bytes;

    /**
     * Default constructor without any bytes.
     */
    public IpHeader() {
        this.bytes = new byte[0];
    }

    /**
     * General use constructor.
     * @param bytes - the byte array representing the report
     */
    public IpHeader(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return this.bytes.clone();
    }

    public short getVer() {
        if (bytes.length < 1) {
            return 0;
        }
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
        } else if (getVer() == 6) {
            return bytes[6];
        } else {
            return 0;
        }
    }

    public InetAddress getSrcAddr() {
        final int byteIndex;
        if (getVer() == 4) {
           byteIndex = 12;
        } else if (getVer() == 6) {
            byteIndex = 8;
        } else {
            return null;
        }
        return ByteUtils.getInetAddress(bytes, getVer(), byteIndex);
    }

    public void setSrcAddr(final String ipAddr) {
        final InetAddress inetAddress = InetAddresses.forString(ipAddr);
        final byte[] ipAddrBytes = inetAddress.getAddress();
        if (inetAddress instanceof Inet4Address && getVer() == 4) {
            System.arraycopy(ipAddrBytes, 0, bytes, 12, ipAddrBytes.length);
        } else if (inetAddress instanceof Inet6Address && getVer() == 6) {
            System.arraycopy(ipAddrBytes, 0, bytes, 8, ipAddrBytes.length);
        } else {
            throw new RuntimeException("Invalid IP address");
        }
    }

    public InetAddress getDstAddr() {
        final int byteIndex;
        if (getVer() == 4) {
            byteIndex = 16;
        } else if (getVer() == 6) {
            byteIndex = 24;
        } else {
            return null;
        }
        return ByteUtils.getInetAddress(bytes, getVer(), byteIndex);
    }

    public void setDstAddr(final String ipAddr) {
        final InetAddress inetAddress = InetAddresses.forString(ipAddr);
        final byte[] ipAddrBytes = inetAddress.getAddress();
        if (inetAddress instanceof Inet4Address && getVer() == 4) {
            System.arraycopy(ipAddrBytes, 0, bytes, 16, ipAddrBytes.length);
        } else if (inetAddress instanceof Inet6Address && getVer() == 6) {
            System.arraycopy(ipAddrBytes, 0, bytes, 24, ipAddrBytes.length);
        } else {
            throw new RuntimeException("Invalid IP address");
        }
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty(IP_HDR_LEN_KEY, this.getLen());
        outJson.addProperty(IP_HDR_NEXT_PROTO_KEY, this.getNextProto());
        outJson.addProperty(IP_HDR_VER_KEY, this.getVer());
        if (this.getDstAddr() == null) {
            outJson.addProperty(IP_HDR_DST_ADDR_KEY, "");
        } else {
            outJson.addProperty(IP_HDR_DST_ADDR_KEY, this.getDstAddr().getHostAddress());
        }
        if (this.getSrcAddr() == null) {
            outJson.addProperty(IP_HDR_SRC_ADDR_KEY, "");
        } else {
            outJson.addProperty(IP_HDR_SRC_ADDR_KEY, this.getSrcAddr().getHostAddress());
        }

        return outJson;
    }
}
