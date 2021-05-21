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
 * Responsible for extracting the bytes of a Telemetry Report UDP packet.
 */
public class TelemetryReport {

    public final static String TRPT_HDR_KEY = "telemRptHdr";
    public final static String INT_ETH_HDR_KEY = "intEthHdr";
    public final static String IP_HDR_KEY = "ipHdr";
    public final static String UDP_INT_HDR_KEY = "udpIntHdr";
    public final static String INT_HDR_KEY = "intHdr";
    public final static String SRC_PORT_KEY = "srcPort";
    public final static String DST_PORT_KEY = "dstPort";

    public final TelemetryReportHeader trptHdr;
    public final IntEthernetHeader intEthHdr;
    public final IpHeader ipHdr;
    public final UdpIntHeader udpIntHdr;
    public final IntHeader intHdr;
    public final long srcPort;
    public final long dstPort;

    public TelemetryReport(final byte[] trptBytes) {
        int byteIndex = 0;
        trptHdr = new TelemetryReportHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, 24));
        byteIndex += 24;
        intEthHdr = new IntEthernetHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, 14));
        byteIndex += 14;
        if (intEthHdr.getType() == 0x800) {
            ipHdr = new IpHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, 20));
            byteIndex += 20;
        } else {
            ipHdr = new IpHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, 40));
            byteIndex += 40;
        }
        udpIntHdr = new UdpIntHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, 8));
        byteIndex += 8;

        intHdr = new IntHeader(ByteUtils.getBytesFrag(trptBytes, byteIndex, trptBytes.length - byteIndex));
        srcPort = ByteUtils.getLongFromBytes(trptBytes, byteIndex + intHdr.lastIndex + 2, 2);
        dstPort = ByteUtils.getLongFromBytes(trptBytes, byteIndex + intHdr.lastIndex + 4, 2);
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
        outJson.addProperty(SRC_PORT_KEY, srcPort);
        outJson.addProperty(DST_PORT_KEY, dstPort);

        return outJson;
    }

}

