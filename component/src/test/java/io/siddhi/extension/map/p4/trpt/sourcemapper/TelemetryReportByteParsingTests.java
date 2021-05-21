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

package io.siddhi.extension.map.p4.trpt.sourcemapper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.siddhi.extension.map.p4.trpt.TelemetryReport;
import org.junit.Assert;
import org.junit.Test;

/**
 * Testcase of P4TrptSourceMapper.
 */
public class TelemetryReportByteParsingTests {
    // To know about the related testcase,
    // refer https://github.com/siddhi-io/siddhi-map-xml/tree/master/component/src/test/

    @Test
    public void parse2HopIntUdp4Bytes() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP4_2HOPS);

        // TRPT Header Values
        Assert.assertEquals(2, trpt.trptHdr.getVersion());
        Assert.assertEquals(13, trpt.trptHdr.getHardwareId());
        Assert.assertEquals(1089, trpt.trptHdr.getSequenceId());
        Assert.assertEquals(234, trpt.trptHdr.getNodeId());
        Assert.assertEquals(0, trpt.trptHdr.getReportType());
        Assert.assertEquals(4, trpt.trptHdr.getInType());
        Assert.assertEquals(10, trpt.trptHdr.getReportLength());
        Assert.assertEquals(8, trpt.trptHdr.getMetadataLength());
        Assert.assertEquals(0, trpt.trptHdr.getD());
        Assert.assertEquals(1, trpt.trptHdr.getQ());
        Assert.assertEquals(0, trpt.trptHdr.getF());
        Assert.assertEquals(1, trpt.trptHdr.getI());
        Assert.assertEquals("0101010110101010", trpt.trptHdr.getRepMdBitStr());
        Assert.assertEquals(21587, trpt.trptHdr.getDomainId());
        Assert.assertEquals("0101010110101010", trpt.trptHdr.getDsMdbBitStr());
        Assert.assertEquals("1010101001010101", trpt.trptHdr.getDsMdsBitStr());
        Assert.assertEquals("00000000000000000000000000000000", trpt.trptHdr.getVarOptMd());

        // Original Ethernet Header Values
        Assert.assertEquals("00:00:00:00:05:01", trpt.intEthHdr.getDstMac());
        Assert.assertEquals("00:00:00:00:01:01", trpt.intEthHdr.getSrcMac());
        Assert.assertEquals(2048, trpt.intEthHdr.getType());

        // Original IP Header Values
        Assert.assertEquals(4, trpt.ipHdr.getVer());
        Assert.assertEquals(94, trpt.ipHdr.getLen());
        Assert.assertEquals(17, trpt.ipHdr.getNextProto()); // UDP
        Assert.assertEquals("192.168.1.2", trpt.ipHdr.getSrcAddr().getHostAddress()); // IP
        Assert.assertEquals("192.168.1.10", trpt.ipHdr.getDstAddr().getHostAddress()); // IP

        // UDP INT Header values
        Assert.assertEquals(0, trpt.udpIntHdr.getUdpIntSrcPort());
        Assert.assertEquals(555, trpt.udpIntHdr.getUdpIntDstPort());
        Assert.assertEquals(74, trpt.udpIntHdr.getUdpIntLen());

        // INT Shim values
        Assert.assertEquals(1, trpt.intHdr.shimHdr.getType());
        Assert.assertEquals(2, trpt.intHdr.shimHdr.getNpt());
        Assert.assertEquals(17, trpt.intHdr.shimHdr.getNextProto());

        // INT Metadata values
        Assert.assertEquals(2, trpt.intHdr.mdHdr.getVersion());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getD());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getE());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getM());
        Assert.assertEquals(1, trpt.intHdr.mdHdr.getPerHopMdLen());
        Assert.assertEquals(9, trpt.intHdr.mdHdr.getRemainingHopCount());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getInstructions());
        Assert.assertEquals(21587, trpt.intHdr.mdHdr.getDomainId());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getDsInstructions());
        Assert.assertEquals("0100000000000000", trpt.intHdr.mdHdr.getDsFlags());

        // INT Metadata Stack values
        Assert.assertEquals("00:00:00:00:01:01", trpt.intHdr.mdStackHdr.getOrigMac());
        Assert.assertEquals(2, trpt.intHdr.mdStackHdr.getHops().size());
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 123));
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 234));

        // The originating port values
        Assert.assertEquals(6680, trpt.srcPort);
        Assert.assertEquals(5792, trpt.dstPort);
    }

    @Test
    public void json2HopIntUdp4() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP4_2HOPS);

        final JsonObject trptJson = trpt.toJson();

        final JsonObject trptHdrJson = trptJson.getAsJsonObject("telemRptHdr");
        Assert.assertNotNull(trptHdrJson);
        final JsonObject intEthHdrJson = trptJson.getAsJsonObject("intEthHdr");
        Assert.assertNotNull(intEthHdrJson);
        final JsonObject ipHdrJson = trptJson.getAsJsonObject("ipHdr");
        Assert.assertNotNull(ipHdrJson);
        final JsonObject udpIntHdrJson = trptJson.getAsJsonObject("udpIntHdr");
        Assert.assertNotNull(udpIntHdrJson);
        final JsonObject intHdrJson = trptJson.getAsJsonObject("intHdr");
        Assert.assertNotNull(intHdrJson);

        // TRPT Header Values
        Assert.assertEquals(2, trptHdrJson.get("version").getAsInt());
        Assert.assertEquals(13, trptHdrJson.get("hardwareId").getAsInt());
        Assert.assertEquals(1089, trptHdrJson.get("seqNo").getAsLong());
        Assert.assertEquals(234, trptHdrJson.get("nodeId").getAsLong());
        Assert.assertEquals(0, trptHdrJson.get("rptType").getAsLong());
        Assert.assertEquals(4, trptHdrJson.get("inType").getAsInt());
        Assert.assertEquals(10, trptHdrJson.get("rptLen").getAsInt());
        Assert.assertEquals(8, trptHdrJson.get("metaLen").getAsInt());
        Assert.assertEquals(0, trptHdrJson.get("d").getAsInt());
        Assert.assertEquals(1, trptHdrJson.get("q").getAsInt());
        Assert.assertEquals(0, trptHdrJson.get("f").getAsInt());
        Assert.assertEquals(1, trptHdrJson.get("i").getAsInt());
        Assert.assertEquals("0101010110101010", trptHdrJson.get("repMdBits").getAsString());
        Assert.assertEquals(21587, trptHdrJson.get("domainId").getAsLong());
        Assert.assertEquals("0101010110101010", trptHdrJson.get("mdbBits").getAsString());
        Assert.assertEquals("1010101001010101", trptHdrJson.get("mdsBits").getAsString());
        Assert.assertEquals("00000000000000000000000000000000", trptHdrJson.get("varOptMd").getAsString());

        // Original Ethernet Header Values
        Assert.assertEquals("00:00:00:00:05:01", intEthHdrJson.get("dstMac").getAsString());
        Assert.assertEquals("00:00:00:00:01:01", intEthHdrJson.get("srcMac").getAsString());
        Assert.assertEquals(2048, intEthHdrJson.get("type").getAsLong());

        // Original IP Header Values
        Assert.assertEquals(4, ipHdrJson.get("version").getAsInt());
        Assert.assertEquals(94, ipHdrJson.get("len").getAsInt());
        Assert.assertEquals(17, ipHdrJson.get("nextProto").getAsInt()); // UDP
        Assert.assertEquals("192.168.1.2", ipHdrJson.get("srcAddr").getAsString()); // IP
        Assert.assertEquals("192.168.1.10", ipHdrJson.get("dstAddr").getAsString()); // IP

        // UDP INT Header values
        Assert.assertEquals(0, udpIntHdrJson.get("srcPort").getAsLong());
        Assert.assertEquals(555, udpIntHdrJson.get("dstPort").getAsLong());
        Assert.assertEquals(74, udpIntHdrJson.get("len").getAsInt());

        // INT Shim values
        final JsonObject shimHdrJson = intHdrJson.getAsJsonObject("shimHdr");
        Assert.assertNotNull(shimHdrJson);
        Assert.assertEquals(1, shimHdrJson.get("type").getAsInt());
        Assert.assertEquals(2, shimHdrJson.get("npt").getAsInt());
        Assert.assertEquals(17, shimHdrJson.get("nextProto").getAsInt());

        // INT Metadata values
        final JsonObject mdHdrJson = intHdrJson.getAsJsonObject("mdHdr");
        Assert.assertNotNull(mdHdrJson);
        Assert.assertEquals(2, mdHdrJson.get("version").getAsInt());
        Assert.assertEquals(0, mdHdrJson.get("d").getAsInt());
        Assert.assertEquals(0, mdHdrJson.get("e").getAsInt());
        Assert.assertEquals(0, mdHdrJson.get("m").getAsInt());
        Assert.assertEquals(1, mdHdrJson.get("mdLen").getAsInt());
        Assert.assertEquals(9, mdHdrJson.get("remainingHopCount").getAsInt());
        Assert.assertEquals("1000000000000000", mdHdrJson.get("instructions").getAsString());
        Assert.assertEquals(21587, mdHdrJson.get("domainId").getAsInt());
        Assert.assertEquals("1000000000000000", mdHdrJson.get("dsInstructions").getAsString());
        Assert.assertEquals("0100000000000000", mdHdrJson.get("dsFlags").getAsString());

        // INT Metadata Stack values
        final JsonObject mdStackHdrJson = intHdrJson.getAsJsonObject("mdStackHdr");
        Assert.assertNotNull(mdStackHdrJson);
        Assert.assertEquals("00:00:00:00:01:01", mdStackHdrJson.get("origMac").getAsString());

        final JsonArray hopsJson = mdStackHdrJson.getAsJsonArray("hops");
        Assert.assertNotNull(hopsJson);

        Assert.assertEquals(2, hopsJson.size());
        // TODO - Add validation of the JsonArray elements
//        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 123));
//        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 234));

        // The originating port values
        Assert.assertEquals(6680, trptJson.get("srcPort").getAsLong());
        Assert.assertEquals(5792, trptJson.get("dstPort").getAsLong());

        // TODO - Add validation to the JSON string value
        final String telemRptJsonStr = trpt.toJsonStr();
        Assert.assertNotNull(telemRptJsonStr);
    }

    @Test
    public void parse2HopIntUdp6Bytes() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP6_2HOPS);

        // TRPT Header Values
        Assert.assertEquals(2, trpt.trptHdr.getVersion());
        Assert.assertEquals(0, trpt.trptHdr.getHardwareId());
        Assert.assertEquals(1, trpt.trptHdr.getSequenceId());
        Assert.assertEquals(234, trpt.trptHdr.getNodeId());
        Assert.assertEquals(0, trpt.trptHdr.getReportType());
        Assert.assertEquals(5, trpt.trptHdr.getInType());
        Assert.assertEquals(15, trpt.trptHdr.getReportLength());
        Assert.assertEquals(8, trpt.trptHdr.getMetadataLength());
        Assert.assertEquals(0, trpt.trptHdr.getD());
        Assert.assertEquals(0, trpt.trptHdr.getQ());
        Assert.assertEquals(0, trpt.trptHdr.getF());
        Assert.assertEquals(0, trpt.trptHdr.getI());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getRepMdBitStr());
        Assert.assertEquals(21587, trpt.trptHdr.getDomainId());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getDsMdbBitStr());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getDsMdsBitStr());
        Assert.assertEquals("00000000000000000000000000000000", trpt.trptHdr.getVarOptMd());

        // Original Ethernet Header Values
        Assert.assertEquals("00:00:00:00:05:02", trpt.intEthHdr.getDstMac());
        Assert.assertEquals("00:00:00:00:01:01", trpt.intEthHdr.getSrcMac());
        Assert.assertEquals(34525, trpt.intEthHdr.getType());

        // Original IP Header Values
        Assert.assertEquals(6, trpt.ipHdr.getVer());
        Assert.assertEquals(74, trpt.ipHdr.getLen());
        Assert.assertEquals(17, trpt.ipHdr.getNextProto()); // UDP
        Assert.assertEquals("0:0:0:0:0:1:1:2", trpt.ipHdr.getSrcAddr().getHostAddress()); // IP
        Assert.assertEquals("0:0:0:0:0:1:1:1d", trpt.ipHdr.getDstAddr().getHostAddress()); // IP

        // UDP INT Header values
        Assert.assertEquals(0, trpt.udpIntHdr.getUdpIntSrcPort());
        Assert.assertEquals(555, trpt.udpIntHdr.getUdpIntDstPort());
        Assert.assertEquals(74, trpt.udpIntHdr.getUdpIntLen());

        // INT Shim values
        Assert.assertEquals(1, trpt.intHdr.shimHdr.getType());
        Assert.assertEquals(2, trpt.intHdr.shimHdr.getNpt());
        Assert.assertEquals(17, trpt.intHdr.shimHdr.getNextProto());

        // INT Metadata values
        Assert.assertEquals(2, trpt.intHdr.mdHdr.getVersion());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getD());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getE());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getM());
        Assert.assertEquals(1, trpt.intHdr.mdHdr.getPerHopMdLen());
        Assert.assertEquals(9, trpt.intHdr.mdHdr.getRemainingHopCount());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getInstructions());
        Assert.assertEquals(21587, trpt.intHdr.mdHdr.getDomainId());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getDsInstructions());
        Assert.assertEquals("0100000000000000", trpt.intHdr.mdHdr.getDsFlags());

        // INT Metadata Stack values
        Assert.assertEquals("00:00:00:00:01:01", trpt.intHdr.mdStackHdr.getOrigMac());
        Assert.assertEquals(2, trpt.intHdr.mdStackHdr.getHops().size());
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 123));
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 234));

        // The originating port values
        Assert.assertEquals(6680, trpt.srcPort);
        Assert.assertEquals(5792, trpt.dstPort);
    }

    @Test
    public void parse2HopIntTcp4Bytes() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.TCP4_2HOPS);

        // TRPT Header Values
        Assert.assertEquals(2, trpt.trptHdr.getVersion());
        Assert.assertEquals(0, trpt.trptHdr.getHardwareId());
        Assert.assertEquals(1, trpt.trptHdr.getSequenceId());
        Assert.assertEquals(234, trpt.trptHdr.getNodeId());
        Assert.assertEquals(0, trpt.trptHdr.getReportType());
        Assert.assertEquals(4, trpt.trptHdr.getInType());
        Assert.assertEquals(10, trpt.trptHdr.getReportLength());
        Assert.assertEquals(8, trpt.trptHdr.getMetadataLength());
        Assert.assertEquals(0, trpt.trptHdr.getD());
        Assert.assertEquals(0, trpt.trptHdr.getQ());
        Assert.assertEquals(0, trpt.trptHdr.getF());
        Assert.assertEquals(0, trpt.trptHdr.getI());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getRepMdBitStr());
        Assert.assertEquals(21587, trpt.trptHdr.getDomainId());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getDsMdbBitStr());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getDsMdsBitStr());
        Assert.assertEquals("00000000000000000000000000000000", trpt.trptHdr.getVarOptMd());

        // Original Ethernet Header Values
        Assert.assertEquals("00:00:00:00:05:01", trpt.intEthHdr.getDstMac());
        Assert.assertEquals("00:00:00:00:01:01", trpt.intEthHdr.getSrcMac());
        Assert.assertEquals(2048, trpt.intEthHdr.getType());

        // Original IP Header Values
        Assert.assertEquals(4, trpt.ipHdr.getVer());
        Assert.assertEquals(106, trpt.ipHdr.getLen());
        Assert.assertEquals(17, trpt.ipHdr.getNextProto()); // UDP
        Assert.assertEquals("192.168.1.2", trpt.ipHdr.getSrcAddr().getHostAddress()); // IP
        Assert.assertEquals("192.168.1.10", trpt.ipHdr.getDstAddr().getHostAddress()); // IP

        // UDP INT Header values
        Assert.assertEquals(0, trpt.udpIntHdr.getUdpIntSrcPort());
        Assert.assertEquals(555, trpt.udpIntHdr.getUdpIntDstPort());
        Assert.assertEquals(86, trpt.udpIntHdr.getUdpIntLen());

        // INT Shim values
        Assert.assertEquals(1, trpt.intHdr.shimHdr.getType());
        Assert.assertEquals(2, trpt.intHdr.shimHdr.getNpt());
        Assert.assertEquals(6, trpt.intHdr.shimHdr.getNextProto());

        // INT Metadata values
        Assert.assertEquals(2, trpt.intHdr.mdHdr.getVersion());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getD());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getE());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getM());
        Assert.assertEquals(1, trpt.intHdr.mdHdr.getPerHopMdLen());
        Assert.assertEquals(9, trpt.intHdr.mdHdr.getRemainingHopCount());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getInstructions());
        Assert.assertEquals(21587, trpt.intHdr.mdHdr.getDomainId());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getDsInstructions());
        Assert.assertEquals("0100000000000000", trpt.intHdr.mdHdr.getDsFlags());

        // INT Metadata Stack values
        Assert.assertEquals("00:00:00:00:01:01", trpt.intHdr.mdStackHdr.getOrigMac());
        Assert.assertEquals(2, trpt.intHdr.mdStackHdr.getHops().size());
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 123));
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 234));

        // The originating port values
        Assert.assertEquals(6680, trpt.srcPort);
        Assert.assertEquals(5792, trpt.dstPort);
    }

    @Test
    public void parse2HopIntTcp6Bytes() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.TCP6_2HOPS);

        // TRPT Header Values
        Assert.assertEquals(2, trpt.trptHdr.getVersion());
        Assert.assertEquals(0, trpt.trptHdr.getHardwareId());
        Assert.assertEquals(1, trpt.trptHdr.getSequenceId());
        Assert.assertEquals(234, trpt.trptHdr.getNodeId());
        Assert.assertEquals(0, trpt.trptHdr.getReportType());
        Assert.assertEquals(5, trpt.trptHdr.getInType());
        Assert.assertEquals(15, trpt.trptHdr.getReportLength());
        Assert.assertEquals(8, trpt.trptHdr.getMetadataLength());
        Assert.assertEquals(0, trpt.trptHdr.getD());
        Assert.assertEquals(0, trpt.trptHdr.getQ());
        Assert.assertEquals(0, trpt.trptHdr.getF());
        Assert.assertEquals(0, trpt.trptHdr.getI());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getRepMdBitStr());
        Assert.assertEquals(21587, trpt.trptHdr.getDomainId());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getDsMdbBitStr());
        Assert.assertEquals("0000000000000000", trpt.trptHdr.getDsMdsBitStr());
        Assert.assertEquals("00000000000000000000000000000000", trpt.trptHdr.getVarOptMd());

        // Original Ethernet Header Values
        Assert.assertEquals("00:00:00:00:05:02", trpt.intEthHdr.getDstMac());
        Assert.assertEquals("00:00:00:00:01:01", trpt.intEthHdr.getSrcMac());
        Assert.assertEquals(34525, trpt.intEthHdr.getType());

        // Original IP Header Values
        Assert.assertEquals(6, trpt.ipHdr.getVer());
        Assert.assertEquals(86, trpt.ipHdr.getLen());
        Assert.assertEquals(6, trpt.ipHdr.getNextProto()); // UDP
        Assert.assertEquals("0:0:0:0:0:1:1:2", trpt.ipHdr.getSrcAddr().getHostAddress()); // IP
        Assert.assertEquals("0:0:0:0:0:1:1:1d", trpt.ipHdr.getDstAddr().getHostAddress()); // IP

        // UDP INT Header values
        Assert.assertEquals(0, trpt.udpIntHdr.getUdpIntSrcPort());
        Assert.assertEquals(555, trpt.udpIntHdr.getUdpIntDstPort());
        Assert.assertEquals(86, trpt.udpIntHdr.getUdpIntLen());

        // INT Shim values
        Assert.assertEquals(1, trpt.intHdr.shimHdr.getType());
        Assert.assertEquals(2, trpt.intHdr.shimHdr.getNpt());
        Assert.assertEquals(6, trpt.intHdr.shimHdr.getNextProto());

        // INT Metadata values
        Assert.assertEquals(2, trpt.intHdr.mdHdr.getVersion());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getD());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getE());
        Assert.assertEquals(0, trpt.intHdr.mdHdr.getM());
        Assert.assertEquals(1, trpt.intHdr.mdHdr.getPerHopMdLen());
        Assert.assertEquals(9, trpt.intHdr.mdHdr.getRemainingHopCount());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getInstructions());
        Assert.assertEquals(21587, trpt.intHdr.mdHdr.getDomainId());
        Assert.assertEquals("1000000000000000", trpt.intHdr.mdHdr.getDsInstructions());
        Assert.assertEquals("0100000000000000", trpt.intHdr.mdHdr.getDsFlags());

        // INT Metadata Stack values
        Assert.assertEquals("00:00:00:00:01:01", trpt.intHdr.mdStackHdr.getOrigMac());
        Assert.assertEquals(2, trpt.intHdr.mdStackHdr.getHops().size());
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 123));
        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 234));

        // The originating port values
        Assert.assertEquals(6680, trpt.srcPort);
        Assert.assertEquals(5792, trpt.dstPort);
    }
}
