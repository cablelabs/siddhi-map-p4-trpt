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

import java.util.Arrays;

/**
 * Responsible for extracting the bytes that represent INT Drop header into usable values.
 */
public class DropHeader {

    public static final String DROP_HDR_TIMESTAMP_HDR_KEY = "timestamp";
    public static final String DROP_HDR_DROP_KEY_KEY = "dropKey";
    public static final String DROP_HDR_DROP_COUNT_KEY = "dropCount";

    private final byte[] bytes;

    /**
     * General use constructor.
     * @param bytes - the byte array representing the report
     */
    public DropHeader(final byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return this.bytes.clone();
    }

    public int getType() {
        if (bytes.length < 1) {
            return 0;
        }
        return ByteUtils.getIntFromNibble(bytes[0], true);
    }

    public long getTimestamp() {
        return ByteUtils.getLongFromBytes(this.bytes, 0, 4);
    }

    public long getDropCount() {
        return ByteUtils.getLongFromBytes(this.bytes, 4, 4);
    }

    public String getDropKey() {
        return Hex.encodeHexString(Arrays.copyOfRange(bytes, 16, 32));
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.addProperty(DROP_HDR_TIMESTAMP_HDR_KEY, this.getTimestamp());
        outJson.addProperty(DROP_HDR_DROP_KEY_KEY, this.getDropKey());
        outJson.addProperty(DROP_HDR_DROP_COUNT_KEY, this.getDropCount());

        return outJson;
    }
}
