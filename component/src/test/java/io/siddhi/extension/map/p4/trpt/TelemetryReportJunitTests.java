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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.siddhi.extension.map.p4.TestTelemetryReports;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Testcase of P4TrptSourceMapper.
 */
public class TelemetryReportJunitTests {
    // To know about the related testcase,
    // refer https://github.com/siddhi-io/siddhi-map-xml/tree/master/component/src/test/

    @Test
    public void parseDropRpt() {
        byte[] origBytes = TestTelemetryReports.DROP_RPT;
        final TelemetryReport trpt = new TelemetryReport(origBytes);
        Assert.assertNull(trpt.intEthHdr);
        Assert.assertNull(trpt.ipHdr);
        Assert.assertNull(trpt.udpIntHdr);
        Assert.assertNull(trpt.intHdr);
        Assert.assertEquals(21587, trpt.trptHdr.getDomainId());
        Assert.assertEquals(0, trpt.trptHdr.getHardwareId());
        Assert.assertEquals(2, trpt.trptHdr.getInType());
        Assert.assertEquals(123, trpt.trptHdr.getNodeId());
        Assert.assertEquals(9, trpt.trptHdr.getReportLength());
        Assert.assertEquals(0, trpt.trptHdr.getSequenceId());
        Assert.assertEquals(2, trpt.trptHdr.getVersion());
        Assert.assertEquals(7, trpt.trptHdr.getMetadataLength());
        Assert.assertEquals(0, trpt.trptHdr.getReportType());
        Assert.assertArrayEquals(origBytes, trpt.getBytes());
        Assert.assertEquals(0, trpt.dropHdr.getDropCount());
//        Assert.assertEquals("7710404439049675602", trpt.dropHdr.getDropKey());
        Assert.assertEquals(1624470281L, trpt.dropHdr.getTimestamp());
        final String jsonStr = trpt.toJsonStr();
        Assert.assertNotNull(jsonStr);
        final JsonObject parsedJson = (JsonObject) new JsonParser().parse(jsonStr);

        Assert.assertTrue(trpt.toJson().equals(parsedJson));

        Assert.assertEquals(21587, trpt.trptHdr.toJson().get("domainId").getAsLong());
        Assert.assertEquals(0, trpt.trptHdr.toJson().get("hardwareId").getAsLong());
        Assert.assertEquals(2, trpt.trptHdr.toJson().get("inType").getAsLong());
        Assert.assertEquals(123, trpt.trptHdr.toJson().get("nodeId").getAsLong());
        Assert.assertEquals(9, trpt.trptHdr.toJson().get("rptLen").getAsLong());
        Assert.assertEquals(0, trpt.trptHdr.toJson().get("seqNo").getAsLong());
        Assert.assertEquals(2, trpt.trptHdr.toJson().get("version").getAsLong());
        Assert.assertEquals(7, trpt.trptHdr.toJson().get("metaLen").getAsLong());
        Assert.assertEquals(0, trpt.trptHdr.toJson().get("rptType").getAsLong());
        Assert.assertArrayEquals(origBytes, trpt.getBytes());
        final JsonObject dropHdr = (JsonObject) trpt.toJson().get("dropHdr");
        Assert.assertNotNull(dropHdr);
        Assert.assertEquals(0, dropHdr.get("dropCount").getAsLong());
        final String parsedDropKey = dropHdr.get("dropKey").getAsString();
        Assert.assertTrue(parsedDropKey.equals("6b00dbfc6026a3521bbe0f5d00170000"));
        Assert.assertEquals(1624470281L, dropHdr.get("timestamp").getAsLong());
    }

    @Test
    public void parse2HopIntUdp4Bytes() {
        byte[] origBytes = TestTelemetryReports.UDP4_2HOPS;
        final TelemetryReport trpt = new TelemetryReport(origBytes);
        byte[] convBytes = trpt.getBytes();
        Assert.assertArrayEquals(origBytes, convBytes);

        Assert.assertNull(trpt.dropHdr);

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
        Assert.assertEquals(6680, trpt.protoHdr.getSrcPort());
        Assert.assertEquals(5792, trpt.protoHdr.getDstPort());
        Assert.assertEquals(Hex.encodeHexString("hello transparent-security".getBytes(StandardCharsets.UTF_8)),
                trpt.getPayload());
    }

    @Test
    public void json2HopIntUdp4() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP4_2HOPS);

        final JsonObject trptJson = trpt.toJson();

        final JsonObject trptHdrJson = trptJson.getAsJsonObject(TelemetryReport.TRPT_HDR_KEY);
        Assert.assertNotNull(trptHdrJson);
        final JsonObject intEthHdrJson = trptJson.getAsJsonObject(TelemetryReport.INT_ETH_HDR_KEY);
        Assert.assertNotNull(intEthHdrJson);
        final JsonObject ipHdrJson = trptJson.getAsJsonObject(TelemetryReport.IP_HDR_KEY);
        Assert.assertNotNull(ipHdrJson);
        final JsonObject udpIntHdrJson = trptJson.getAsJsonObject(TelemetryReport.UDP_INT_HDR_KEY);
        Assert.assertNotNull(udpIntHdrJson);
        final JsonObject intHdrJson = trptJson.getAsJsonObject(TelemetryReport.INT_HDR_KEY);
        Assert.assertNotNull(intHdrJson);

        // TRPT Header Values
        Assert.assertEquals(2, trptHdrJson.get(TelemetryReportHeader.TRPT_VER_KEY).getAsInt());
        Assert.assertEquals(13, trptHdrJson.get(TelemetryReportHeader.TRPT_HW_ID_KEY).getAsInt());
        Assert.assertEquals(1089, trptHdrJson.get(TelemetryReportHeader.TRPT_SEQ_NO_KEY).getAsLong());
        Assert.assertEquals(234, trptHdrJson.get(TelemetryReportHeader.TRPT_NODE_ID_KEY).getAsLong());
        Assert.assertEquals(0, trptHdrJson.get(TelemetryReportHeader.TRPT_RPT_TYPE_KEY).getAsLong());
        Assert.assertEquals(4, trptHdrJson.get(TelemetryReportHeader.TRPT_IN_TYPE_KEY).getAsInt());
        Assert.assertEquals(10, trptHdrJson.get(TelemetryReportHeader.TRPT_RPT_LEN_KEY).getAsInt());
        Assert.assertEquals(8, trptHdrJson.get(TelemetryReportHeader.TRPT_META_LEN_KEY).getAsInt());
        Assert.assertEquals(0, trptHdrJson.get(TelemetryReportHeader.TRPT_D_KEY).getAsInt());
        Assert.assertEquals(1, trptHdrJson.get(TelemetryReportHeader.TRPT_Q_KEY).getAsInt());
        Assert.assertEquals(0, trptHdrJson.get(TelemetryReportHeader.TRPT_F_KEY).getAsInt());
        Assert.assertEquals(1, trptHdrJson.get(TelemetryReportHeader.TRPT_I_KEY).getAsInt());
        Assert.assertEquals("0101010110101010", trptHdrJson.get(
                TelemetryReportHeader.TRPT_REP_MD_BITS_KEY).getAsString());
        Assert.assertEquals(21587, trptHdrJson.get(TelemetryReportHeader.TRPT_DOMAIN_ID_KEY).getAsLong());
        Assert.assertEquals("0101010110101010", trptHdrJson.get(
                TelemetryReportHeader.TRPT_MDB_BITS_KEY).getAsString());
        Assert.assertEquals("1010101001010101", trptHdrJson.get(
                TelemetryReportHeader.TRPT_MDS_BITS_KEY).getAsString());
        Assert.assertEquals("00000000000000000000000000000000", trptHdrJson.get(
                TelemetryReportHeader.TRPT_VAR_OPT_MD_KEY).getAsString());

        // Original Ethernet Header Values
        Assert.assertEquals("00:00:00:00:05:01", intEthHdrJson.get(
                IntEthernetHeader.IETH_HDR_DST_MAC_KEY).getAsString());
        Assert.assertEquals("00:00:00:00:01:01", intEthHdrJson.get(
                IntEthernetHeader.IETH_HDR_SRC_MAC_KEY).getAsString());
        Assert.assertEquals(2048, intEthHdrJson.get(
                IntEthernetHeader.IETH_TYPE_KEY).getAsLong());

        // Original IP Header Values
        Assert.assertEquals(4, ipHdrJson.get(IpHeader.IP_HDR_VER_KEY).getAsInt());
        Assert.assertEquals(94, ipHdrJson.get(IpHeader.IP_HDR_LEN_KEY).getAsInt());
        Assert.assertEquals(17, ipHdrJson.get(IpHeader.IP_HDR_NEXT_PROTO_KEY).getAsInt()); // UDP
        Assert.assertEquals("192.168.1.2", ipHdrJson.get(IpHeader.IP_HDR_SRC_ADDR_KEY).getAsString()); // IP
        Assert.assertEquals("192.168.1.10", ipHdrJson.get(IpHeader.IP_HDR_DST_ADDR_KEY).getAsString()); // IP

        // UDP INT Header values
        Assert.assertEquals(0, udpIntHdrJson.get(UdpIntHeader.UDP_INT_HDR_SRC_PORT_KEY).getAsLong());
        Assert.assertEquals(555, udpIntHdrJson.get(UdpIntHeader.UDP_INT_HDR_DST_PORT_KEY).getAsLong());
        Assert.assertEquals(74, udpIntHdrJson.get(UdpIntHeader.UDP_INT_HDR_LEN_KEY).getAsInt());

        // INT Shim values
        final JsonObject shimHdrJson = intHdrJson.getAsJsonObject(IntHeader.INT_HDR_SHIM_HDR_KEY);
        Assert.assertNotNull(shimHdrJson);
        Assert.assertEquals(1, shimHdrJson.get(IntShimHeader.INT_SHIM_HDR_TYPE_KEY).getAsInt());
        Assert.assertEquals(2, shimHdrJson.get(IntShimHeader.INT_SHIM_HDR_NPT_KEY).getAsInt());
        Assert.assertEquals(17, shimHdrJson.get(IntShimHeader.INT_SHIM_HDR_NEXT_PROTO_KEY).getAsInt());

        // INT Metadata values
        final JsonObject mdHdrJson = intHdrJson.getAsJsonObject(IntHeader.INT_HDR_MD_HDR_KEY);
        Assert.assertNotNull(mdHdrJson);
        Assert.assertEquals(2, mdHdrJson.get(IntMetadataHeader.INT_MD_HDR_VER_KEY).getAsInt());
        Assert.assertEquals(0, mdHdrJson.get(IntMetadataHeader.INT_MD_HDR_D_KEY).getAsInt());
        Assert.assertEquals(0, mdHdrJson.get(IntMetadataHeader.INT_MD_HDR_E_KEY).getAsInt());
        Assert.assertEquals(0, mdHdrJson.get(IntMetadataHeader.INT_MD_HDR_M_KEY).getAsInt());
        Assert.assertEquals(1, mdHdrJson.get(IntMetadataHeader.INT_MD_HDR_MD_LEN_KEY).getAsInt());
        Assert.assertEquals(9, mdHdrJson.get(IntMetadataHeader.INT_MD_HDR_REMAIN_HOP_CNT_KEY).getAsInt());
        Assert.assertEquals("1000000000000000", mdHdrJson.get(
                IntMetadataHeader.INT_MD_HDR_INSTR_KEY).getAsString());
        Assert.assertEquals(21587, mdHdrJson.get(IntMetadataHeader.INT_MD_HDR_DOMAIN_ID_KEY).getAsInt());
        Assert.assertEquals("1000000000000000", mdHdrJson.get(
                IntMetadataHeader.INT_MD_HDR_DS_INSTR_KEY).getAsString());
        Assert.assertEquals("0100000000000000", mdHdrJson.get(
                IntMetadataHeader.INT_MD_HDR_DS_FLAGS_KEY).getAsString());

        // INT Metadata Stack values
        final JsonObject mdStackHdrJson = intHdrJson.getAsJsonObject(IntHeader.INT_HDR_MD_STACK_HDR_KEY);
        Assert.assertNotNull(mdStackHdrJson);
        Assert.assertEquals("00:00:00:00:01:01", mdStackHdrJson.get(
                IntMetadataStackHeader.INT_MD_STACK_ORIG_MAC_KEY).getAsString());

        final JsonArray hopsJson = mdStackHdrJson.getAsJsonArray(IntMetadataStackHeader.INT_MD_STACK_HOPS_KEY);
        Assert.assertNotNull(hopsJson);

        Assert.assertEquals(2, hopsJson.size());
        // TODO - Add validation of the JsonArray elements
//        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 123));
//        Assert.assertTrue(trpt.intHdr.mdStackHdr.getHops().contains((long) 234));

        // The originating port values
        final JsonObject protoHdrJson = trptJson.getAsJsonObject(TelemetryReport.PROTO_HDR_KEY);
        Assert.assertNotNull(protoHdrJson);
        Assert.assertEquals(6680, protoHdrJson.get(ProtoHeader.PROTO_HDR_SRC_PORT_KEY).getAsLong());
        Assert.assertEquals(5792, protoHdrJson.get(ProtoHeader.PROTO_HDR_DST_PORT_KEY).getAsLong());
        Assert.assertEquals(Hex.encodeHexString("hello transparent-security".getBytes(StandardCharsets.UTF_8)),
                trptJson.get(TelemetryReport.PAYLOAD).getAsString());

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
        Assert.assertEquals(6680, trpt.protoHdr.getSrcPort());
        Assert.assertEquals(5792, trpt.protoHdr.getDstPort());
        Assert.assertEquals(Hex.encodeHexString("hello transparent-security".getBytes(StandardCharsets.UTF_8)),
                trpt.getPayload());
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
        Assert.assertEquals(6680, trpt.protoHdr.getSrcPort());
        Assert.assertEquals(5792, trpt.protoHdr.getDstPort());
        Assert.assertEquals(Hex.encodeHexString("hello transparent-security".getBytes(StandardCharsets.UTF_8)),
                trpt.getPayload());
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
        Assert.assertEquals(6680, trpt.protoHdr.getSrcPort());
        Assert.assertEquals(5792, trpt.protoHdr.getDstPort());
        Assert.assertEquals(Hex.encodeHexString("hello transparent-security".getBytes(StandardCharsets.UTF_8)),
                trpt.getPayload());
    }

    @Test
    public void convertUdp4Bytes() {
        byte[] origBytes = TestTelemetryReports.UDP4_2HOPS;
        final TelemetryReport trpt = new TelemetryReport(origBytes);
        byte[] convBytes = trpt.getBytes();
        Assert.assertArrayEquals(origBytes, convBytes);
    }

    @Test
    public void convertUdp6Bytes() {
        byte[] origBytes = TestTelemetryReports.UDP6_2HOPS;
        final TelemetryReport trpt = new TelemetryReport(origBytes);
        byte[] convBytes = trpt.getBytes();
        Assert.assertArrayEquals(origBytes, convBytes);
    }

    @Test
    public void convertTcp4Bytes() {
        byte[] origBytes = TestTelemetryReports.TCP4_2HOPS;
        final TelemetryReport trpt = new TelemetryReport(origBytes);
        byte[] convBytes = trpt.getBytes();
        Assert.assertArrayEquals(origBytes, convBytes);
    }

    @Test
    public void convertTcp6Bytes() {
        byte[] origBytes = TestTelemetryReports.TCP6_2HOPS;
        final TelemetryReport trpt = new TelemetryReport(origBytes);
        byte[] convBytes = trpt.getBytes();
        Assert.assertArrayEquals(origBytes, convBytes);
    }

    @Test(expected = RuntimeException.class)
    public void updateUdp4SrcAddrWith6Addr() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP4_2HOPS);
        trpt.ipHdr.setSrcAddr("::1");
    }

    @Test(expected = RuntimeException.class)
    public void updateTcp4SrcAddrWith6Addr() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.TCP4_2HOPS);
        trpt.ipHdr.setSrcAddr("::1");
    }

    @Test(expected = RuntimeException.class)
    public void updateUdp6SrcAddrWith6Addr() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP6_2HOPS);
        trpt.ipHdr.setSrcAddr("10.10.1.2");
    }

    @Test(expected = RuntimeException.class)
    public void updateTcp6SrcAddrWith6Addr() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.TCP6_2HOPS);
        trpt.ipHdr.setSrcAddr("10.10.1.2");
    }

    @Test
    public void updateUdp4() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP4_2HOPS);

        // Check original
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "00:00:00:00:01:01");
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "192.168.1.2"); // IP
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "192.168.1.10"); // IP
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 6680);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 5792);
        Assert.assertEquals(trpt.ipHdr.getVer(), 4);
        final byte[] origBytes = trpt.getBytes();

        // Update values
        trpt.protoHdr.setSrcPort(2345);
        byte[] updatedBytes1 = trpt.getBytes();
        trpt.protoHdr.setDstPort(6789);
        byte[] updatedBytes2 = trpt.getBytes();
        trpt.ipHdr.setSrcAddr("10.10.1.2");
        byte[] updatedBytes3 = trpt.getBytes();
        trpt.ipHdr.setDstAddr("10.10.1.10");
        byte[] updatedBytes4 = trpt.getBytes();
        trpt.intHdr.mdStackHdr.setOrigMac("11:11:11:11:00:00");
        byte[] updatedBytes5 = trpt.getBytes();
//        final byte[] updatedBytes = trpt.getBytes();

        // Check updated values
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 2345);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 6789);
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "10.10.1.2");
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "10.10.1.10");
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "11:11:11:11:00:00");
        validateBytes(trpt);
    }

    @Test
    public void updateUdp6() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.UDP6_2HOPS);

        // Check original
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "00:00:00:00:01:01");
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "0:0:0:0:0:1:1:2"); // IP
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "0:0:0:0:0:1:1:1d"); // IP
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 6680);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 5792);
        Assert.assertEquals(trpt.ipHdr.getVer(), 6);

        // Update values
        trpt.protoHdr.setSrcPort(2345);
        trpt.protoHdr.setDstPort(6789);
        trpt.ipHdr.setSrcAddr("::1");
        trpt.ipHdr.setDstAddr("::2");
        trpt.intHdr.mdStackHdr.setOrigMac("11:11:11:11:00:00");

        // Check updated values
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 2345);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 6789);
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "0:0:0:0:0:0:0:1");
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "0:0:0:0:0:0:0:2");
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "11:11:11:11:00:00");
        validateBytes(trpt);
    }

    @Test
    public void updateTcp4() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.TCP4_2HOPS);

        // Check original
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "00:00:00:00:01:01");
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "192.168.1.2"); // IP
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "192.168.1.10"); // IP
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 6680);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 5792);
        Assert.assertEquals(trpt.ipHdr.getVer(), 4);

        // Update values
        trpt.protoHdr.setSrcPort(2345);
        trpt.protoHdr.setDstPort(6789);
        trpt.ipHdr.setSrcAddr("10.10.1.2");
        trpt.ipHdr.setDstAddr("10.10.1.10");
        trpt.intHdr.mdStackHdr.setOrigMac("11:11:11:11:00:00");

        // Check updated values
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 2345);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 6789);
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "10.10.1.2");
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "10.10.1.10");
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "11:11:11:11:00:00");
        validateBytes(trpt);
    }

    @Test
    public void updateTcp6() {
        final TelemetryReport trpt = new TelemetryReport(TestTelemetryReports.TCP6_2HOPS);

        // Check original
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "00:00:00:00:01:01");
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "0:0:0:0:0:1:1:2"); // IP
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "0:0:0:0:0:1:1:1d"); // IP
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 6680);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 5792);
        Assert.assertEquals(trpt.ipHdr.getVer(), 6);

        // Update values
        trpt.protoHdr.setSrcPort(2345);
        trpt.protoHdr.setDstPort(6789);
        trpt.ipHdr.setSrcAddr("::1");
        trpt.ipHdr.setDstAddr("::2");
        trpt.intHdr.mdStackHdr.setOrigMac("11:11:11:11:00:00");

        // Check updated values
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), 2345);
        Assert.assertEquals(trpt.protoHdr.getDstPort(), 6789);
        Assert.assertEquals(trpt.ipHdr.getSrcAddr().getHostAddress(), "0:0:0:0:0:0:0:1");
        Assert.assertEquals(trpt.ipHdr.getDstAddr().getHostAddress(), "0:0:0:0:0:0:0:2");
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), "11:11:11:11:00:00");
        validateBytes(trpt);
    }

    private void validateBytes(final TelemetryReport trpt) {
        final byte[] trptBytes = trpt.getBytes();
        final TelemetryReport newTrpt = new TelemetryReport(trptBytes);
        final byte[] newTrptBytes = newTrpt.getBytes();


        // Validate new object created from the bytes generated from the older
        Assert.assertEquals(trpt.protoHdr.getSrcPort(), newTrpt.protoHdr.getSrcPort());
        Assert.assertEquals(trpt.protoHdr.getDstPort(), newTrpt.protoHdr.getDstPort());
        Assert.assertEquals(trpt.ipHdr.getSrcAddr(), newTrpt.ipHdr.getSrcAddr());
        Assert.assertEquals(trpt.ipHdr.getDstAddr(), newTrpt.ipHdr.getDstAddr());
        Assert.assertEquals(trpt.intHdr.mdStackHdr.getOrigMac(), newTrpt.intHdr.mdStackHdr.getOrigMac());
    }
}
