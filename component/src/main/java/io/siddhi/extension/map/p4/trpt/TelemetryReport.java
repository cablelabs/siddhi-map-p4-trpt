package io.siddhi.extension.map.p4.trpt;

/**
 * Responsible for extracting the bytes of a Telemetry Report UDP packet.
 */
public class TelemetryReport {

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
}

