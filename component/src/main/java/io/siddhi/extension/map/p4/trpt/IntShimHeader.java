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
 * Responsible for extracting the bytes that represent INT Shim header values into usable values.
 */
public class IntShimHeader {

    private final byte[] bytes;

    public IntShimHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public int getType() {
        return ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public int getNpt() {
        final String byteBitStr = ByteUtils.getBitString(bytes[0]);
        final String theBitsStr = byteBitStr.substring(4, 6);
        return Integer.parseInt(theBitsStr, 2);
    }

    public int getLength() {
        return bytes[1];
    }

    public int getNextProto() {
        return bytes[3];
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty("type", this.getType());
        outJson.addProperty("npt", this.getNpt());
        outJson.addProperty("len", this.getLength());
        outJson.addProperty("nextProto", this.getNextProto());

        return outJson;
    }
}
