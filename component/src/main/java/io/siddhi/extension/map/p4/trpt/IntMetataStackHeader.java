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

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty("origMac", this.getOrigMac());

        final JsonArray hopsJsonArr = new JsonArray();

        for (final long hop : hops) {
            hopsJsonArr.add(hop);
        }
        outJson.add("hops", hopsJsonArr);

        return outJson;
    }
}
