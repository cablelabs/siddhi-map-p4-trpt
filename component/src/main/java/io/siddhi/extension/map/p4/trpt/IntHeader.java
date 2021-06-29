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
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for extracting the bytes that represent INT header values into usable values.
 */
public class IntHeader {

    static final int INT_SHIM_SIZE = 6;
    public static final String INT_HDR_SHIM_HDR_KEY = "shimHdr";
    public static final String INT_HDR_MD_HDR_KEY = "mdHdr";
    public static final String INT_HDR_MD_STACK_HDR_KEY = "mdStackHdr";

    public final IntShimHeader shimHdr;
    public final IntMetadataHeader mdHdr;
    public final IntMetadataStackHeader mdStackHdr;
    public final int lastIndex;

    /**
     * Default constructor without any bytes.
     */
    public IntHeader() {
        shimHdr = new IntShimHeader();
        mdHdr = new IntMetadataHeader();
        mdStackHdr = new IntMetadataStackHeader();
        lastIndex = 0;
    }

    /**
     * General use constructor.
     * @param bytes - the byte array representing the report
     */
    public IntHeader(byte[] bytes) {
        final byte[] bytes1 = bytes.clone();
        int byteIndex = 0;
        shimHdr = new IntShimHeader(ByteUtils.getBytesFrag(bytes1, byteIndex, 4));
        byteIndex += 4;
        mdHdr = new IntMetadataHeader(ByteUtils.getBytesFrag(bytes1, byteIndex, 12));
        byteIndex += 12;
        mdStackHdr = new IntMetadataStackHeader(shimHdr.getLength() - INT_SHIM_SIZE,
                ByteUtils.getBytesFrag(bytes1, byteIndex, bytes.length - byteIndex));

        lastIndex = byteIndex + mdStackHdr.getLastIndex();
    }

    public byte[] getBytes() {
        final List<Byte> outBytes = new ArrayList<>();
        for (final byte trptByte : shimHdr.getBytes()) {
            outBytes.add(trptByte);
        }
        for (final byte trptByte : mdHdr.getBytes()) {
            outBytes.add(trptByte);
        }
        for (final byte trptByte : mdStackHdr.getBytes()) {
            outBytes.add(trptByte);
        }
        return ArrayUtils.toPrimitive(outBytes.toArray(new Byte[outBytes.size()]));
    }

    public JsonObject toJson() {
        final JsonObject outJson = new JsonObject();

        outJson.add(INT_HDR_SHIM_HDR_KEY, shimHdr.toJson());
        outJson.add(INT_HDR_MD_HDR_KEY, mdHdr.toJson());
        outJson.add(INT_HDR_MD_STACK_HDR_KEY, mdStackHdr.toJson());

        return outJson;
    }
}
