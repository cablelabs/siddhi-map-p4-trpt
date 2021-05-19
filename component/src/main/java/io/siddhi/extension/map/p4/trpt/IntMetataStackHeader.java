package io.siddhi.extension.map.p4.trpt;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for extracting the bytes that represent INT metadata header values into usable values.
 */
public class IntMetataStackHeader {

    private final int numHops;
    private final byte[] bytes;
    private final String origMac;
    private final int lastHopIndex;
    private final List<Long> hops;

    public IntMetataStackHeader(final int numHops, final byte[] bytes) {
        this.numHops = numHops;
        this.bytes = bytes.clone();
        this.lastHopIndex = numHops * 4;
        this.origMac = ByteUtils.getMacStr(bytes, lastHopIndex);
        this.hops = readHops();
    }

    public int getLastIndex() {
        return lastHopIndex + 6;
    }

    public String getOrigMac() {
        return origMac;
    }

    public List<Long> getHops() {
        return new ArrayList<>(hops);
    }

    private List<Long> readHops() {
        List<Long> out = new ArrayList<>(numHops);
        for (int i = 0; i < numHops; i++) {
            out.add(ByteUtils.getLongFromBytes(bytes, lastHopIndex - (i * 4) - 4, 4));
        }
        return out;
    }

}
