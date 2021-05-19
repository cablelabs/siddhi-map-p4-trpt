package io.siddhi.extension.map.p4.trpt.sourcemapper;

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
