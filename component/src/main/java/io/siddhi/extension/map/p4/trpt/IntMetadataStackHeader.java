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
import io.netty.util.internal.MacAddressUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for extracting the bytes that represent INT metadata header values into usable values.
 */
public class IntMetadataStackHeader {

    public static final String INT_MD_STACK_ORIG_MAC_KEY = "origMac";
    public static final String INT_MD_STACK_HOPS_KEY = "hops";

    private final int numHops;
    private final byte[] bytes;
    private final int lastHopIndex;
    private final List<Long> hops;

    /**
     * Default constructor without any bytes.
     */
    public IntMetadataStackHeader() {
        bytes = new byte[0];
        numHops = 0;
        lastHopIndex = 0;
        hops = new ArrayList<>();
    }

    /**
     * General use constructor.
     * @param bytes - the byte array representing the report
     */
    public IntMetadataStackHeader(final int numHops, final byte[] bytes) {
        this.numHops = numHops;
        this.bytes = bytes.clone();
        this.lastHopIndex = numHops * 4;
        this.hops = readHops();
    }

    public byte[] getBytes() {
        final List<Byte> outBytes = new ArrayList<>();
        for (int i = 0; i < getLastIndex(); i++) {
            outBytes.add(bytes[i]);
        }
        return ArrayUtils.toPrimitive(outBytes.toArray(new Byte[0]));
    }

    public int getLastIndex() {
        if (bytes.length == 0) {
            return 0;
        }
        return lastHopIndex + 6;
    }

    public String getOrigMac() {
        return ByteUtils.getMacStr(bytes, lastHopIndex);
    }

    public void setOrigMac(final String macAddress) {
        final byte[] macBytes = MacAddressUtil.parseMAC(macAddress);
        System.arraycopy(macBytes, 0, bytes, lastHopIndex, macBytes.length);
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

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty(INT_MD_STACK_ORIG_MAC_KEY, this.getOrigMac());

        final JsonArray hopsJsonArr = new JsonArray();

        for (final long hop : hops) {
            hopsJsonArr.add(hop);
        }
        outJson.add(INT_MD_STACK_HOPS_KEY, hopsJsonArr);

        return outJson;
    }
}
