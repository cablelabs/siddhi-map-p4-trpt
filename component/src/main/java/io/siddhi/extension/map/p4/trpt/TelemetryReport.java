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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Responsible for extracting the bytes of a Telemetry Report UDP packet.
 */
public class TelemetryReport {

    public static final String TRPT_HDR_KEY = "telemRptHdr";
    public static final String INT_ETH_HDR_KEY = "intEthHdr";
    public static final String IP_HDR_KEY = "ipHdr";
    public static final String UDP_INT_HDR_KEY = "udpIntHdr";
    public static final String INT_HDR_KEY = "intHdr";
    public static final String SRC_PORT_KEY = "srcPort";
    public static final String DST_PORT_KEY = "dstPort";
    public static final String PAYLOAD = "payload";

    public final TelemetryReportHeader trptHdr;
    public final IntEthernetHeader intEthHdr;
    public final IpHeader ipHdr;
    public final UdpIntHeader udpIntHdr;
    public final IntHeader intHdr;
    private final byte[] bytes;
    private final int numBytes;
    private final int srcPortPos;
    private final int dstPortPos;
    private final int lastHdrBytePos;

    public TelemetryReport(final byte[] trptBytes) {
        bytes = trptBytes.clone();
        numBytes = bytes.length;
        int byteIndex = 0;
        trptHdr = new TelemetryReportHeader(ByteUtils.getBytesFrag(bytes, byteIndex, 24));
        byteIndex += 24;
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
    }

    public byte[] getBytes() {
        final List<Byte> outBytes = new ArrayList<>();
        for (final byte trptByte : trptHdr.getBytes()) {
            outBytes.add(trptByte);
        }
        for (final byte trptByte : intEthHdr.getBytes()) {
            outBytes.add(trptByte);
        }
        for (final byte trptByte : ipHdr.getBytes()) {
            outBytes.add(trptByte);
        }
        for (final byte trptByte : udpIntHdr.getBytes()) {
            outBytes.add(trptByte);
        }
        for (final byte trptByte : intHdr.getBytes()) {
            outBytes.add(trptByte);
        }

        for (int i = outBytes.size(); i < numBytes; i++) {
            outBytes.add(bytes[i]);
        }
        return ArrayUtils.toPrimitive(outBytes.toArray(new Byte[0]));
    }

    public long getSrcPort() {
        return ByteUtils.getLongFromBytes(bytes, srcPortPos, 2);
    }

    public void setSrcPort(final long port) {
        byte[] portBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(port).array();
        System.arraycopy(portBytes, 6, bytes, srcPortPos, 2);
    }

    public long getDstPort() {
        return ByteUtils.getLongFromBytes(bytes, dstPortPos, 2);
    }

    public void setDstPort(final long port) {
        byte[] portBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(port).array();
        System.arraycopy(portBytes, 6, bytes, dstPortPos, 2);
    }

    public String getPayload() {
        return Hex.encodeHexString(Arrays.copyOfRange(bytes, lastHdrBytePos, bytes.length));
    }

    public String toJsonStr() {
        return this.toJson().toString();
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.add(TRPT_HDR_KEY, trptHdr.toJson());
        outJson.add(INT_ETH_HDR_KEY, intEthHdr.toJson());
        outJson.add(IP_HDR_KEY, ipHdr.toJson());
        outJson.add(UDP_INT_HDR_KEY, udpIntHdr.toJson());
        outJson.add(INT_HDR_KEY, intHdr.toJson());
        outJson.addProperty(SRC_PORT_KEY, getSrcPort());
        outJson.addProperty(DST_PORT_KEY, getDstPort());
        outJson.addProperty(PAYLOAD, getPayload());

        return outJson;
    }
}
