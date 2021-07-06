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
    public static final String PROTO_HDR_KEY = "protoHdr";
    public static final String DROP_HDR_KEY = "dropHdr";
    public static final String PAYLOAD = "payload";
    public static final String DROP_KEY = "dropKey";

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
    public final DropHeader dropHdr;
    public final ProtoHeader protoHdr;

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
            byteIndex += intHdr.lastIndex;

            final int protoHdrBytes;
            if (intHdr.shimHdr.getNextProto() == 0x11) {
                protoHdrBytes = 8;
            } else {
                protoHdrBytes = 20;
            }
            protoHdr = new ProtoHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, protoHdrBytes));
            byteIndex += protoHdrBytes;

            dropHdr = null;
        } else {
            intEthHdr = null;
            ipHdr = null;
            udpIntHdr = null;
            intHdr = null;
            protoHdr = null;

            dropHdr = new DropHeader(ByteUtils.getBytesFrag(bytes, byteIndex, 32));
            byteIndex += 32;
        }
        lastHdrBytePos = byteIndex;
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

        if (protoHdr != null) {
            for (final byte trptByte : protoHdr.getBytes()) {
                outBytes.add(trptByte);
            }
        }

        if (dropHdr != null) {
            for (final byte trptByte : dropHdr.getBytes()) {
                outBytes.add(trptByte);
            }
        }

        for (int i = outBytes.size(); i < bytes.length; i++) {
            outBytes.add(bytes[i]);
        }
        return ArrayUtils.toPrimitive(outBytes.toArray(new Byte[0]));
    }

    /**
     * Sets the INT packet's destination port value.
     * @return - packet's payload as a hex string
     */
    public String getPayload() {
        return Hex.encodeHexString(Arrays.copyOfRange(bytes, lastHdrBytePos, bytes.length));
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
        if (intEthHdr != null) {
            outJson.add(INT_ETH_HDR_KEY, intEthHdr.toJson());
        }
        if (ipHdr != null) {
            outJson.add(IP_HDR_KEY, ipHdr.toJson());
        }
        if (udpIntHdr != null) {
            outJson.add(UDP_INT_HDR_KEY, udpIntHdr.toJson());
        }
        if (intHdr != null) {
            outJson.add(INT_HDR_KEY, intHdr.toJson());
        }
        if (protoHdr != null) {
            outJson.add(PROTO_HDR_KEY, protoHdr.toJson());
        }
        if (dropHdr != null) {
            outJson.add(DROP_HDR_KEY, dropHdr.toJson());
        }

        outJson.addProperty(PAYLOAD, getPayload());

        return outJson;
    }
}
