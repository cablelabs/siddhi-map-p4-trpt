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
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to represent a P4 Telemetry Report as described in the 2.1 specification.
 * An object can be instantiated with the payload of a Telemetry Report UDP packet as a byte array.
 */
public class TelemetryReport {

    // JSON keys
    public static final String TRPT_HDR_KEY = "telemRptHdr";
    public static final String INT_ETH_HDR_KEY = "intEthHdr";
    public static final String IP_HDR_KEY = "ipHdr";
    public static final String UDP_INT_HDR_KEY = "udpIntHdr";
    public static final String INT_HDR_KEY = "intHdr";
    public static final String SRC_PORT_KEY = "srcPort";
    public static final String DST_PORT_KEY = "dstPort";
    public static final String PAYLOAD = "payload";
    public static final String TIMESTAMP = "timestamp";
    public static final String DROP_KEY = "dropKey";
    public static final String DROP_COUNT = "dropCount";

    // The Telemetry report bytes
    private final byte[] bytes;

    // The position of the last byte containing Telemetry Report header data. Any bytes after this point is the payload
    private final int lastHdrBytePos;

    // Member variables
    public final TelemetryReportHeader trptHdr;

    // Headers and other values specific to packet telemetry reports
    public final IntEthernetHeader intEthHdr;
    public final IpHeader ipHdr;
    public final UdpIntHeader udpIntHdr;
    public final IntHeader intHdr;
    private final int srcPortPos;
    private final int dstPortPos;

    // Fields specific to drop reports
    private final long timestamp;
    private final long dropCount;
    private final String dropKey;

    /**
     * Constructor.
     * @param trptBytes - this byte array to be cloned
     */
    public TelemetryReport(final byte[] trptBytes) {
        bytes = trptBytes.clone();
        int byteIndex = 0;
        trptHdr = new TelemetryReportHeader(ByteUtils.getBytesFrag(bytes, byteIndex, 24));
        byteIndex += 24;
        if (trptHdr.getInType() != 2) {
            intEthHdr = new IntEthernetHeader(ByteUtils.getBytesFrag(bytes, byteIndex, 14));
            byteIndex += 14;
            if (intEthHdr.getType() == 0x800) {
                ipHdr = new IpHeader(ByteUtils.getBytesFrag(bytes, byteIndex, 20));
                byteIndex += 20;
            } else {
                ipHdr = new IpHeader(ByteUtils.getBytesFrag(bytes, byteIndex, 40));
                byteIndex += 40;
            }
            udpIntHdr = new UdpIntHeader(ByteUtils.getBytesFrag(bytes, byteIndex, 8));
            byteIndex += 8;

            intHdr = new IntHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, bytes.length - byteIndex));
            srcPortPos = byteIndex + intHdr.lastIndex + 2;
            dstPortPos = byteIndex + intHdr.lastIndex + 4;

            if (intHdr.shimHdr.getNextProto() == 6) {
                lastHdrBytePos = dstPortPos + 18; // TCP
            } else {
                lastHdrBytePos = dstPortPos + 6; // UDP
            }
            timestamp = -1;
            dropCount = -1;
            dropKey = null;
        } else {
            intEthHdr = new IntEthernetHeader();
            ipHdr = new IpHeader();
            udpIntHdr = new UdpIntHeader();
            intHdr = new IntHeader();
            srcPortPos = 0;
            dstPortPos = 0;
            timestamp = ByteUtils.getLongFromBytes(this.bytes, byteIndex, 4);
            byteIndex += 4;
            dropCount = ByteUtils.getLongFromBytes(this.bytes, byteIndex, 4);
            byteIndex += 12;
            final byte[] dropKeyBytes = Arrays.copyOfRange(bytes, byteIndex, byteIndex + 16);
            dropKey = String.valueOf(ByteBuffer.wrap(dropKeyBytes).getLong());
            byteIndex += 16;
            lastHdrBytePos = byteIndex;
        }
    }

    /**
     * Returns a new byte array containing all of the updated information.
     * @return - the new bytes
     */
    public byte[] getBytes() {
        final List<Byte> outBytes = new ArrayList<>();
        for (final byte trptByte : trptHdr.getBytes()) {
            outBytes.add(trptByte);
        }

        if (intEthHdr != null) {
            for (final byte trptByte : intEthHdr.getBytes()) {
                outBytes.add(trptByte);
            }
        }

        if (ipHdr != null) {
            for (final byte trptByte : ipHdr.getBytes()) {
                outBytes.add(trptByte);
            }
        }

        if (udpIntHdr != null) {
            for (final byte trptByte : udpIntHdr.getBytes()) {
                outBytes.add(trptByte);
            }
        }

        if (intHdr != null) {
            for (final byte trptByte : intHdr.getBytes()) {
                outBytes.add(trptByte);
            }
        }

        for (int i = outBytes.size(); i < bytes.length; i++) {
            outBytes.add(bytes[i]);
        }
        return ArrayUtils.toPrimitive(outBytes.toArray(new Byte[0]));
    }

    /**
     * Returns the INT packet's source port value.
     * @return - the port value (zero will be returned if this is a drop report)
     */
    public long getSrcPort() {
        if (srcPortPos == 0) {
            return 0;
        }
        return ByteUtils.getLongFromBytes(bytes, srcPortPos, 2);
    }

    /**
     * Sets the INT packet's source port value.
     */
    public void setSrcPort(final long port) {
        if (this.trptHdr.getInType() != 2) {
            byte[] portBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(port).array();
            System.arraycopy(portBytes, 6, bytes, srcPortPos, 2);
        } else {
            throw new IllegalStateException("Source port cannot be set on drop reports");
        }
    }

    /**
     * Returns the INT packet's destination port value.
     * @return - the port value (zero will be returned if this is a drop report)
     */
    public long getDstPort() {
        if (dstPortPos == 0) {
            return 0;
        }
        return ByteUtils.getLongFromBytes(bytes, dstPortPos, 2);
    }

    /**
     * Sets the INT packet's destination port value.
     */
    public void setDstPort(final long port) {
        if (this.trptHdr.getInType() != 2) {
            byte[] portBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(port).array();
            System.arraycopy(portBytes, 6, bytes, dstPortPos, 2);
        } else {
            throw new IllegalStateException("Source port cannot be set on drop reports");
        }
    }

    /**
     * Sets the INT packet's destination port value.
     * @return - packet's payload as a hex string
     */
    public String getPayload() {
        return Hex.encodeHexString(Arrays.copyOfRange(bytes, lastHdrBytePos, bytes.length));
    }

    /**
     * Returns the drop report's timestamp.
     * @return - the time of the event (zero will be returned if this is a drop report)
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the number of dropped packets.
     * @return - the count of dropped packets (zero will be returned if this is a drop report)
     */
    public long getDropCount() {
        return dropCount;
    }

    /**
     * Returns the dimensional hash value derived something like below in python.
     *
     * For Telemetry Packet Reports, this value will be derived as below:
     * hash_str = "{}|{}|{}|{}".format(mac, port, ipv4, ipv6)
     * hash_hex = hashlib.sha256(hash_str.encode()).hexdigest()
     * hash_int = int(hash_hex[:16], 16)
     *
     * For Telemetry Drop Reports, this value will be sent in
     * @return - the count of dropped packets (zero will be returned if this is a drop report)
     */
    public String getDropKey() {
        if (dropKey != null) {
            return dropKey;
        }
        return calcHash();
    }

    /**
     * Calculates the drop hash value.
     * @return - a long derived from the source mac & destination port & IP
     */
    private String calcHash() {
        if (this.ipHdr != null) {
            String ip4Addr = "";
            String ip6Addr = "";
            switch (this.ipHdr.getVer()) {
                case 4:
                    ip4Addr = this.ipHdr.getDstAddr().getHostAddress();
                    ip6Addr = "::";
                    break;
                case 6:
                    ip4Addr = "0.0.0.0";
                    ip6Addr = this.ipHdr.getDstAddr().getHostAddress();
                    break;
                default:
                    return "";
            }
            final String hashString = String.format("%s|%s|%s|%s",
                    this.intHdr.mdStackHdr.getOrigMac(), this.getDstPort(), ip4Addr, ip6Addr);
            try {
                final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                final byte[] encodedHash = messageDigest.digest(hashString.getBytes(StandardCharsets.UTF_8));
                final long out = ByteUtils.getLongFromBytes(encodedHash, 0, 8);
                return Long.toUnsignedString(out);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        } else {
            return dropKey;
        }
    }

    /**
     * Returns a JSON string representation of this object.
     * @return - JSON encoded string value
     */
    public String toJsonStr() {
        return this.toJson().toString();
    }

    /**
     * Returns a JsonObject representation of this object.
     * @return - a JsonObject
     */
    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.add(TRPT_HDR_KEY, trptHdr.toJson());
        outJson.add(INT_ETH_HDR_KEY, intEthHdr.toJson());
        outJson.add(IP_HDR_KEY, ipHdr.toJson());
        outJson.add(UDP_INT_HDR_KEY, udpIntHdr.toJson());
        outJson.add(INT_HDR_KEY, intHdr.toJson());
        outJson.addProperty(SRC_PORT_KEY, getSrcPort());
        outJson.addProperty(DST_PORT_KEY, getDstPort());
        outJson.addProperty(TIMESTAMP, timestamp);
        outJson.addProperty(DROP_COUNT, dropCount);
        outJson.addProperty(DROP_KEY, getDropKey());
        outJson.addProperty(PAYLOAD, getPayload());

        return outJson;
    }
}
